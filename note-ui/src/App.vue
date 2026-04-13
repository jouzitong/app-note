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
#app {
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
</style>
