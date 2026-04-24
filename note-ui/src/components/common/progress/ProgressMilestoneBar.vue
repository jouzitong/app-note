<template>
  <div class="progress-milestone-bar" :style="cssVars">
    <div class="header-row">
      <span class="title">{{ viewModel.title }}</span>
      <span class="mode-text">{{ viewModel.modeText }}</span>
    </div>

    <div class="track">
      <div class="fill" :style="{ width: `${percent}%` }" />

      <span v-if="showFractionInBar" class="bar-fraction">
        {{ safeCurrent }}/{{ safeTotal }}
      </span>

      <span
        v-if="shouldShowMovingPercent"
        class="moving-percent"
        :style="{ left: `${percent}%` }"
      >
        {{ percent }}%
      </span>

      <div class="milestones" aria-hidden="true">
        <span
          v-for="index in milestoneCountSafe"
          :key="`milestone-${index}`"
          class="dot"
          :class="{ active: index <= activeMilestoneCount }"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { normalizeMilestoneProgressModel } from "@/model/common/progress/milestone-progress.model";

function toSafeNumber(value, fallback = 0) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function clamp(value, min, max) {
  if (value < min) return min;
  if (value > max) return max;
  return value;
}

export default {
  name: "ProgressMilestoneBar",
  props: {
    model: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    viewModel() {
      return normalizeMilestoneProgressModel(this.model || {});
    },
    safeTotal() {
      const total = Math.floor(toSafeNumber(this.viewModel.total, 1));
      return total > 0 ? total : 1;
    },
    safeCurrent() {
      const current = Math.floor(toSafeNumber(this.viewModel.current, 0));
      return clamp(current, 0, this.safeTotal);
    },
    percent() {
      return Math.round((this.safeCurrent / this.safeTotal) * 100);
    },
    milestoneCountSafe() {
      const count = Math.floor(toSafeNumber(this.viewModel.milestoneCount, 5));
      return count > 1 ? count : 1;
    },
    activeMilestoneCount() {
      return Math.round((this.percent / 100) * this.milestoneCountSafe);
    },
    showFractionInBar() {
      return this.viewModel.showFractionInBar !== false;
    },
    shouldShowMovingPercent() {
      if (this.viewModel.showMovingPercent === false) {
        return false;
      }
      const lowProgress = this.viewModel.lowProgress || {};
      const enabled = lowProgress.enabled !== false;
      if (!enabled) {
        return true;
      }
      const threshold = toSafeNumber(lowProgress.threshold, 12);
      const hideMovingPercent = lowProgress.hideMovingPercent !== false;
      if (hideMovingPercent && this.percent <= threshold) {
        return false;
      }
      return true;
    },
    cssVars() {
      const style = this.viewModel.style || {};
      return {
        "--bar-height": `${toSafeNumber(style.barHeight, 14)}px`,
        "--bar-radius": `${toSafeNumber(style.radius, 999)}px`,
        "--bar-track": style.trackColor || "#e8eef6",
        "--bar-fill": style.fillColor || "#2563eb",
      };
    },
  },
};
</script>

<style scoped>
.progress-milestone-bar {
  width: 100%;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  white-space: nowrap;
}

.title {
  color: #0f172a;
  font-weight: 600;
}

.mode-text {
  color: #64748b;
  font-size: 11px;
}

.track {
  position: relative;
  width: 100%;
  height: var(--bar-height);
  border-radius: var(--bar-radius);
  background: var(--bar-track);
  overflow: hidden;
}

.fill {
  position: relative;
  z-index: 1;
  height: 100%;
  border-radius: inherit;
  background: var(--bar-fill);
  transition: width 0.24s ease;
}

.bar-fraction {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.35);
  pointer-events: none;
  white-space: nowrap;
}

.moving-percent {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translate(-50%, -50%);
  z-index: 4;
  font-size: 10px;
  font-weight: 700;
  color: #1e3a8a;
  background: #ffffff;
  border: 1px solid #bfd2fb;
  border-radius: 999px;
  padding: 0 5px;
  height: var(--bar-height);
  line-height: calc(var(--bar-height) - 2px);
  pointer-events: none;
  white-space: nowrap;
  transition: left 0.24s ease;
}

.milestones {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 2px;
  pointer-events: none;
}

.dot {
  width: 4px;
  height: 4px;
  margin-top: calc((var(--bar-height) - 4px) / 2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
}

.dot.active {
  background: rgba(255, 255, 255, 0.95);
}
</style>
