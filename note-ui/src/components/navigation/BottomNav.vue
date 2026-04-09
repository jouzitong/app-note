<template>
  <nav
    class="bottom-nav"
    :class="{ 'bottom-nav-bounded': bounded }"
    :aria-label="ariaLabel"
  >
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      class="nav-item"
      :class="{ active: activeKey === tab.key }"
      @click="$emit('select', tab)"
    >
      <span class="nav-icon">{{ tab.icon }}</span>
      <span class="nav-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<script>
export default {
  name: "BottomNav",
  props: {
    tabs: {
      type: Array,
      required: true,
    },
    activeKey: {
      type: String,
      default: "",
    },
    ariaLabel: {
      type: String,
      default: "主导航",
    },
    bounded: {
      type: Boolean,
      default: false,
    },
  },
};
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
  min-height: 56px;
  padding-top: 4px;
  padding-bottom: calc(4px + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -4px 16px rgba(15, 23, 42, 0.06);
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: stretch;
  z-index: 1000;
  transform: translateZ(0);
  -webkit-transform: translateZ(0);
}

.nav-item {
  border: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 3px;
  min-height: 48px;
  padding-top: 0;
  margin: 0;
  color: #6b7280;
  cursor: pointer;
}

.nav-item.active {
  color: #1d4ed8;
}

.nav-icon {
  font-size: 15px;
  line-height: 1;
}

.nav-label {
  font-size: 10px;
  line-height: 1;
}

@media (min-width: 768px) {
  .bottom-nav.bottom-nav-bounded {
    max-width: 430px;
    margin: 0 auto;
    left: 50%;
    transform: translateX(-50%) translateZ(0);
    -webkit-transform: translateX(-50%) translateZ(0);
  }
}
</style>
