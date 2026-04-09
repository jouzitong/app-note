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
  bottom: 0;
  height: calc(8px + env(safe-area-inset-bottom));
  padding-bottom: calc(4px + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: start;
  z-index: 10;
  transform: translateZ(0);
  -webkit-transform: translateZ(0);
}

.nav-item {
  border: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  gap: 3px;
  padding-top: 4px;
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
