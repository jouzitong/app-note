<template>
  <div id="app">
    <router-view />
    <transition name="toast-fade">
      <div
        v-if="$store.state.toast.visible && $store.state.toast.message"
        class="app-toast"
        :class="`app-toast--${$store.state.toast.type || 'error'}`"
        role="status"
        aria-live="polite"
      >
        {{ $store.state.toast.message }}
      </div>
    </transition>
  </div>
</template>

<style>
html,
body {
  width: 100%;
  min-height: 100%;
  margin: 0;
  padding: 0;
}

#app {
  min-height: 100vh;
  min-height: 100dvh;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: left;
  color: #2c3e50;
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
}

nav a.router-link-exact-active {
  color: #42b983;
}

.app-toast {
  position: fixed;
  top: 16px;
  right: 16px;
  max-width: min(520px, calc(100vw - 32px));
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.4;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.18);
  border: 1px solid transparent;
  z-index: 9999;
  word-break: break-word;
}

.app-toast--error {
  background: #fef2f2;
  color: #991b1b;
  border-color: #fecaca;
}

.app-toast--info {
  background: #eff6ff;
  color: #1e3a8a;
  border-color: #bfdbfe;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.toast-fade-enter,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.app-tag {
  --app-tag-color: #41464b;
  --app-tag-bg: #e2e3e5;
  --app-tag-border: #c4c8cb;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 22px;
  padding: 2px 10px;
  border-radius: 10px;
  border: 1px solid var(--app-tag-border);
  background: var(--app-tag-bg);
  color: var(--app-tag-color);
  font-size: 12px;
  line-height: 1.2;
  font-weight: 600;
  white-space: nowrap;
}

.app-tag--primary {
  --app-tag-color: #084298;
  --app-tag-bg: #cfe2ff;
  --app-tag-border: #9ec5fe;
}

.app-tag--secondary {
  --app-tag-color: #41464b;
  --app-tag-bg: #e2e3e5;
  --app-tag-border: #c4c8cb;
}

.app-tag--success {
  --app-tag-color: #0f5132;
  --app-tag-bg: #d1e7dd;
  --app-tag-border: #a3cfbb;
}

.app-tag--danger {
  --app-tag-color: #842029;
  --app-tag-bg: #f8d7da;
  --app-tag-border: #f1aeb5;
}

.app-tag--warning {
  --app-tag-color: #664d03;
  --app-tag-bg: #fff3cd;
  --app-tag-border: #ffe69c;
}

.app-tag--info {
  --app-tag-color: #055160;
  --app-tag-bg: #cff4fc;
  --app-tag-border: #9eeaf9;
}

.app-tag--light {
  --app-tag-color: #495057;
  --app-tag-bg: #fcfcfd;
  --app-tag-border: #f1f3f5;
}

.app-tag--dark {
  --app-tag-color: #e9ecef;
  --app-tag-bg: #343a40;
  --app-tag-border: #1f2327;
}
</style>
