const DEFAULT_PLAYBACK_RATE = 1;

export function loadPlaybackRate(
  storageKey,
  options = [DEFAULT_PLAYBACK_RATE]
) {
  const fallbackRate = options[0] || DEFAULT_PLAYBACK_RATE;
  if (!storageKey || typeof window === "undefined" || !window.localStorage) {
    return fallbackRate;
  }
  const rawValue = window.localStorage.getItem(storageKey);
  const parsedRate = Number(rawValue);
  return options.includes(parsedRate) ? parsedRate : fallbackRate;
}

export function savePlaybackRate(storageKey, rate) {
  if (!storageKey || typeof window === "undefined" || !window.localStorage) {
    return;
  }
  window.localStorage.setItem(storageKey, String(rate));
}

export class AudioPlaybackManager {
  constructor({ lang = "ja-JP" } = {}) {
    this.lang = lang;
    this.audio = null;
    this.seq = 0;
  }

  warmupVoices() {
    if (typeof window === "undefined" || !window.speechSynthesis) {
      return;
    }
    window.speechSynthesis.getVoices();
  }

  stop() {
    this.seq += 1;
    if (typeof window !== "undefined" && window.speechSynthesis) {
      window.speechSynthesis.cancel();
    }
    if (this.audio) {
      this.audio.pause();
      this.audio.currentTime = 0;
    }
  }

  destroy() {
    if (!this.audio) {
      return;
    }
    this.audio.pause();
    this.audio.currentTime = 0;
    this.audio.removeAttribute("src");
    this.audio.load();
    this.audio = null;
  }

  async playSequence(sources = []) {
    if (!Array.isArray(sources) || !sources.length) {
      return;
    }
    this.stop();
    const currentSeq = this.seq;
    for (const source of sources) {
      if (currentSeq !== this.seq) {
        return;
      }
      await this.playSingleSource(source, currentSeq);
    }
  }

  async playSingleSource(source = {}, currentSeq) {
    const url = source.url || "";
    const text = source.text || "";
    const rate =
      typeof source.rate === "number" ? source.rate : DEFAULT_PLAYBACK_RATE;

    if (url) {
      try {
        await this.playUrlAudio(url, rate, currentSeq);
        return;
      } catch (error) {
        if (!text) {
          throw error;
        }
      }
    }

    if (!text) {
      throw new Error("缺少可播放文本");
    }
    await this.playSpeech(text, rate, currentSeq);
  }

  playUrlAudio(url, rate, currentSeq) {
    return new Promise((resolve, reject) => {
      if (currentSeq !== this.seq) {
        resolve();
        return;
      }

      const audio = this.ensureAudio();
      audio.pause();
      audio.currentTime = 0;
      audio.src = url;
      audio.defaultPlaybackRate = rate;
      audio.playbackRate = rate;
      if (typeof audio.preservesPitch === "boolean") {
        audio.preservesPitch = false;
      }
      if (typeof audio.webkitPreservesPitch === "boolean") {
        audio.webkitPreservesPitch = false;
      }
      if (typeof audio.mozPreservesPitch === "boolean") {
        audio.mozPreservesPitch = false;
      }

      const cleanup = () => {
        audio.removeEventListener("ended", handleEnded);
        audio.removeEventListener("error", handleError);
      };
      const handleEnded = () => {
        cleanup();
        resolve();
      };
      const handleError = () => {
        cleanup();
        reject(new Error("音频资源加载失败"));
      };

      audio.addEventListener("ended", handleEnded);
      audio.addEventListener("error", handleError);

      const playPromise = audio.play();
      if (playPromise && typeof playPromise.catch === "function") {
        playPromise.catch((error) => {
          cleanup();
          reject(error);
        });
      }
    });
  }

  playSpeech(text, rate, currentSeq) {
    return new Promise((resolve, reject) => {
      if (
        typeof window === "undefined" ||
        !window.speechSynthesis ||
        typeof window.SpeechSynthesisUtterance !== "function"
      ) {
        reject(new Error("当前环境不支持语音合成"));
        return;
      }
      if (currentSeq !== this.seq) {
        resolve();
        return;
      }

      const utterance = this.createUtterance(text, rate);
      utterance.onend = () => resolve();
      utterance.onerror = (event) =>
        reject(new Error(event?.error || "语音合成失败"));
      window.speechSynthesis.speak(utterance);
    });
  }

  createUtterance(text, rate) {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = this.lang;
    utterance.rate = rate;
    utterance.pitch = 1;
    utterance.volume = 1;

    if (typeof window !== "undefined" && window.speechSynthesis) {
      const voices = window.speechSynthesis.getVoices();
      const preferred =
        voices.find((voice) => voice.lang === this.lang) ||
        voices.find(
          (voice) =>
            voice.lang &&
            this.lang &&
            voice.lang.startsWith(this.lang.split("-")[0])
        );
      if (preferred) {
        utterance.voice = preferred;
      }
    }
    return utterance;
  }

  ensureAudio() {
    if (!this.audio) {
      this.audio = new Audio();
      this.audio.preload = "auto";
    }
    return this.audio;
  }
}
