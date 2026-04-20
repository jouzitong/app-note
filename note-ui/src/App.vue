<template>
  <div id="app">
    <router-view />
    <transition name="toast-fade">
      <div
        v-if="$store.state.app.toast.visible && $store.state.app.toast.message"
        class="app-toast"
        :class="`app-toast--${$store.state.app.toast.type || 'error'}`"
        role="status"
        aria-live="polite"
      >
        {{ $store.state.app.toast.message }}
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: "AppRoot",
  data() {
    return {
      onGestureStart: null,
      onGestureChange: null,
      onGestureEnd: null,
      onTouchMove: null,
      onWheel: null,
    };
  },
  mounted() {
    this.installGlobalInteractionGuards();
  },
  beforeDestroy() {
    this.uninstallGlobalInteractionGuards();
  },
  methods: {
    installGlobalInteractionGuards() {
      this.onGestureStart = (event) => event.preventDefault();
      this.onGestureChange = (event) => event.preventDefault();
      this.onGestureEnd = (event) => event.preventDefault();
      this.onTouchMove = (event) => {
        if (event.touches && event.touches.length > 1) {
          event.preventDefault();
        }
      };
      this.onWheel = (event) => {
        if (event.ctrlKey || event.metaKey) {
          event.preventDefault();
        }
      };

      document.addEventListener("gesturestart", this.onGestureStart, {
        passive: false,
      });
      document.addEventListener("gesturechange", this.onGestureChange, {
        passive: false,
      });
      document.addEventListener("gestureend", this.onGestureEnd, {
        passive: false,
      });
      document.addEventListener("touchmove", this.onTouchMove, {
        passive: false,
      });
      document.addEventListener("wheel", this.onWheel, { passive: false });
    },
    uninstallGlobalInteractionGuards() {
      if (this.onGestureStart) {
        document.removeEventListener("gesturestart", this.onGestureStart);
      }
      if (this.onGestureChange) {
        document.removeEventListener("gesturechange", this.onGestureChange);
      }
      if (this.onGestureEnd) {
        document.removeEventListener("gestureend", this.onGestureEnd);
      }
      if (this.onTouchMove) {
        document.removeEventListener("touchmove", this.onTouchMove);
      }
      if (this.onWheel) {
        document.removeEventListener("wheel", this.onWheel);
      }
    },
  },
};
</script>

<style>
html,
body {
  width: 100%;
  min-height: 100%;
  margin: 0;
  padding: 0;
  user-select: none;
  -webkit-user-select: none;
  -webkit-touch-callout: none;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  overscroll-behavior: none;
}

#app {
  min-height: 100vh;
  min-height: 100dvh;
  font-family: var(--font-family-base);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: left;
  color: var(--color-text-primary);
}

input,
textarea,
[contenteditable="true"] {
  user-select: text;
  -webkit-user-select: text;
  -webkit-touch-callout: default;
}

* {
  scrollbar-width: thin;
  scrollbar-color: rgba(100, 116, 139, 0.22) transparent;
}

*::-webkit-scrollbar {
  width: 1px;
  height: 1px;
}

*::-webkit-scrollbar-track {
  background: transparent;
}

*::-webkit-scrollbar-thumb {
  background: rgba(100, 116, 139, 0.2);
  border-radius: 999px;
}

*::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 116, 139, 0.35);
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: var(--color-text-primary);
}

nav a.router-link-exact-active {
  color: var(--color-brand-accent);
}

.app-toast {
  position: fixed;
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  max-width: min(520px, calc(100vw - var(--space-8)));
  padding: 10px var(--space-3);
  border-radius: 10px;
  font-size: var(--font-size-sm);
  line-height: 1.4;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.18);
  border: 1px solid transparent;
  z-index: 9999;
  word-break: break-word;
}

.app-toast--error {
  background: var(--color-error-bg);
  color: var(--color-error-text);
  border-color: var(--color-error-border);
}

.app-toast--info {
  background: var(--color-info-bg);
  color: var(--color-info-text);
  border-color: var(--color-info-border);
}

.app-toast--success {
  background: var(--color-tag-success-bg);
  color: var(--color-tag-success-text);
  border-color: var(--color-tag-success-border);
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.toast-fade-enter,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}

.app-tag {
  --app-tag-color: var(--color-tag-secondary-text);
  --app-tag-bg: var(--color-tag-secondary-bg);
  --app-tag-border: var(--color-tag-secondary-border);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 22px;
  padding: 2px 10px;
  border-radius: 10px;
  border: 1px solid var(--app-tag-border);
  background: var(--app-tag-bg);
  color: var(--app-tag-color);
  font-size: var(--font-size-xs);
  line-height: 1.2;
  font-weight: 600;
  white-space: nowrap;
}

.app-tag--primary {
  --app-tag-color: var(--color-tag-primary-text);
  --app-tag-bg: var(--color-tag-primary-bg);
  --app-tag-border: var(--color-tag-primary-border);
}

.app-tag--secondary {
  --app-tag-color: var(--color-tag-secondary-text);
  --app-tag-bg: var(--color-tag-secondary-bg);
  --app-tag-border: var(--color-tag-secondary-border);
}

.app-tag--success {
  --app-tag-color: var(--color-tag-success-text);
  --app-tag-bg: var(--color-tag-success-bg);
  --app-tag-border: var(--color-tag-success-border);
}

.app-tag--danger {
  --app-tag-color: var(--color-tag-danger-text);
  --app-tag-bg: var(--color-tag-danger-bg);
  --app-tag-border: var(--color-tag-danger-border);
}

.app-tag--warning {
  --app-tag-color: var(--color-tag-warning-text);
  --app-tag-bg: var(--color-tag-warning-bg);
  --app-tag-border: var(--color-tag-warning-border);
}

.app-tag--info {
  --app-tag-color: var(--color-tag-info-text);
  --app-tag-bg: var(--color-tag-info-bg);
  --app-tag-border: var(--color-tag-info-border);
}

.app-tag--light {
  --app-tag-color: var(--color-tag-light-text);
  --app-tag-bg: var(--color-tag-light-bg);
  --app-tag-border: var(--color-tag-light-border);
}

.app-tag--dark {
  --app-tag-color: var(--color-tag-dark-text);
  --app-tag-bg: var(--color-tag-dark-bg);
  --app-tag-border: var(--color-tag-dark-border);
}
</style>
