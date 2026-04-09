<template>
  <div
    class="speech-input"
    :class="{ listening: isListening, unsupported: !isSupported }"
  >
    <button
      type="button"
      class="speech-btn"
      :disabled="!isSupported"
      :title="buttonTitle"
      :aria-label="buttonTitle"
      @pointerdown="handlePressStart"
      @pointerup="handlePressEnd"
      @pointerleave="handlePressEnd"
      @pointercancel="handlePressEnd"
      @click.prevent
      @contextmenu.prevent
    >
      {{ isListening ? "■" : "🎤" }}
    </button>
    <input
      :value="displayText"
      class="speech-field"
      :class="fieldStateClass"
      type="text"
      :placeholder="resolvedPlaceholder"
      :readonly="readonly"
      @input="handleManualInput"
    />
  </div>
</template>

<script>
export default {
  name: "SpeechTranscribeInput",
  props: {
    value: {
      type: String,
      default: "",
    },
    lang: {
      type: String,
      default: "ja-JP",
    },
    placeholder: {
      type: String,
      default: "点击麦克风开始语音转写",
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    releaseStopDelayMs: {
      type: Number,
      default: 450,
    },
  },
  data() {
    return {
      recognition: null,
      isSupported: false,
      isListening: false,
      isPressing: false,
      pendingStart: false,
      stopTimer: null,
      recognitionState: "idle",
      finalText: "",
      interimText: "",
    };
  },
  computed: {
    displayText() {
      return `${this.finalText}${this.interimText}`.trim();
    },
    buttonTitle() {
      if (!this.isSupported) {
        return "当前浏览器不支持语音识别";
      }
      return this.isListening ? "松开结束语音转写" : "按住开始语音转写";
    },
    fieldStateClass() {
      if (this.recognitionState === "listening") {
        return "is-listening";
      }
      if (this.recognitionState === "completed") {
        return "is-completed";
      }
      return "";
    },
    resolvedPlaceholder() {
      if (this.isSupported) {
        return this.placeholder;
      }
      return "当前浏览器不支持语音识别";
    },
  },
  watch: {
    value(nextValue) {
      if (nextValue === this.displayText) {
        return;
      }
      this.finalText = nextValue || "";
      this.interimText = "";
    },
    lang(nextLang) {
      if (this.recognition) {
        this.recognition.lang = nextLang || "ja-JP";
      }
    },
  },
  created() {
    this.finalText = this.value || "";
    this.initRecognition();
  },
  beforeDestroy() {
    this.clearStopTimer();
    this.stopListening(true);
    this.recognition = null;
  },
  methods: {
    clearStopTimer() {
      if (this.stopTimer) {
        window.clearTimeout(this.stopTimer);
        this.stopTimer = null;
      }
    },
    appendText(base = "", extra = "") {
      const left = String(base || "").trim();
      const right = String(extra || "").trim();
      if (!right) {
        return left;
      }
      if (!left) {
        return right;
      }
      return `${left} ${right}`;
    },
    initRecognition() {
      const SpeechRecognition =
        window.SpeechRecognition || window.webkitSpeechRecognition;
      if (!SpeechRecognition) {
        this.isSupported = false;
        this.$emit("unsupported");
        return;
      }

      this.isSupported = true;
      this.recognition = new SpeechRecognition();
      this.recognition.lang = this.lang || "ja-JP";
      this.recognition.continuous = true;
      this.recognition.interimResults = true;
      this.recognition.maxAlternatives = 1;

      this.recognition.onstart = () => {
        this.pendingStart = false;
        this.isListening = true;
        this.recognitionState = "listening";
      };

      this.recognition.onresult = (event) => {
        let interim = "";
        for (let i = event.resultIndex; i < event.results.length; i += 1) {
          const text = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            this.finalText = this.appendText(this.finalText, text);
          } else {
            interim += text;
          }
        }
        this.interimText = interim;
        this.$emit("input", this.displayText);
      };

      this.recognition.onerror = (event) => {
        const errorCode = event?.error || "unknown";
        this.pendingStart = false;
        this.isPressing = false;
        this.$emit("error", errorCode);
      };

      this.recognition.onend = () => {
        this.pendingStart = false;
        this.isListening = false;
        this.isPressing = false;
        this.clearStopTimer();
        if (this.interimText) {
          this.finalText = this.appendText(this.finalText, this.interimText);
        }
        this.interimText = "";
        this.recognitionState = this.displayText ? "completed" : "idle";
        this.$emit("input", this.displayText);
      };
    },
    handlePressStart(event) {
      if (!this.isSupported) {
        return;
      }
      if (event && event.isPrimary === false) {
        return;
      }
      if (this.isPressing) {
        return;
      }
      this.clearStopTimer();
      this.isPressing = true;
      this.startListening();
    },
    handlePressEnd(event) {
      if (!this.isSupported) {
        return;
      }
      if (event && event.isPrimary === false) {
        return;
      }
      if (!this.isPressing) {
        return;
      }
      this.isPressing = false;
      this.clearStopTimer();
      this.stopTimer = window.setTimeout(() => {
        this.stopTimer = null;
        this.stopListening();
      }, Math.max(0, Number(this.releaseStopDelayMs) || 0));
    },
    startListening() {
      if (!this.recognition || this.isListening) {
        return;
      }
      try {
        this.recognition.lang = this.lang || "ja-JP";
        this.pendingStart = true;
        this.recognitionState = "listening";
        this.recognition.start();
      } catch (error) {
        this.pendingStart = false;
        this.recognitionState = "idle";
        this.$emit("error", error?.message || "start-failed");
      }
    },
    stopListening(silent = false) {
      if (!this.recognition) {
        return;
      }
      if (!this.isListening) {
        if (this.pendingStart) {
          try {
            this.pendingStart = false;
            this.recognition.abort();
          } catch (error) {
            if (!silent) {
              this.$emit("error", error?.message || "abort-failed");
            }
          }
        }
        return;
      }
      try {
        this.recognition.stop();
      } catch (error) {
        if (!silent) {
          this.$emit("error", error?.message || "stop-failed");
        }
      }
    },
    handleManualInput(event) {
      if (this.readonly) {
        return;
      }
      this.finalText = event?.target?.value || "";
      this.interimText = "";
      this.recognitionState = "idle";
      this.$emit("input", this.finalText);
    },
  },
};
</script>

<style scoped>
.speech-input {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  max-width: 100%;
}

.speech-btn {
  width: 24px;
  height: 24px;
  border: 1px solid #c7d2fe;
  border-radius: 999px;
  background: #eef2ff;
  color: #1d4ed8;
  cursor: pointer;
  font-size: 11px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  flex: 0 0 auto;
  touch-action: none;
}

.speech-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.speech-input.listening .speech-btn {
  background: #fee2e2;
  color: #b91c1c;
  border-color: #fecaca;
}

.speech-field {
  width: min(260px, 70vw);
  height: 26px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  padding: 0 8px;
  font-size: 12px;
  color: #1f2937;
  background: #f8fbff;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.speech-field.is-listening {
  background: #fef3c7;
  border-color: #f59e0b;
}

.speech-field.is-completed {
  background: #dcfce7;
  border-color: #22c55e;
}

.speech-field:focus {
  border-color: #60a5fa;
  outline: none;
}

.speech-input.unsupported .speech-field {
  background: #f9fafb;
  color: #9ca3af;
}
</style>
