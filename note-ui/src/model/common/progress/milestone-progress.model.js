/**
 * MilestoneProgressModel
 *
 * 仅 current / total 为核心业务参数，其他字段均为可选展示配置。
 * 组件应只接收一个 `model` prop，避免 props 膨胀。
 *
 * @typedef {Object} MilestoneProgressModel
 * @property {number} current 当前进度值（核心）
 * @property {number} total 总进度值（核心）
 * @property {string} [title="学习进度"] 标题文案
 * @property {string} [modeText="里程碑模式"] 右上角模式文案
 * @property {number} [milestoneCount=5] 里程碑点数量
 * @property {boolean} [showMovingPercent=true] 是否显示跟随进度移动的百分比标签
 * @property {boolean} [showFractionInBar=true] 是否在进度条内显示 current/total
 * @property {{enabled?: boolean, threshold?: number, hideMovingPercent?: boolean}} [lowProgress]
 * 小进度降噪策略：当 percent <= threshold 时可隐藏移动百分比标签
 * @property {{barHeight?: number, radius?: number, trackColor?: string, fillColor?: string}} [style]
 * 轻量样式覆盖
 */

/**
 * 创建 MilestoneProgressModel 默认值。
 * @returns {MilestoneProgressModel}
 */
export function createDefaultMilestoneProgressModel() {
  return {
    current: 0,
    total: 1,
    title: "学习进度",
    modeText: "里程碑模式",
    milestoneCount: 5,
    showMovingPercent: true,
    showFractionInBar: true,
    lowProgress: {
      enabled: true,
      threshold: 12,
      hideMovingPercent: true,
    },
    style: {
      barHeight: 14,
      radius: 999,
      trackColor: "#e8eef6",
      fillColor: "#2563eb",
    },
  };
}

/**
 * 归一化输入对象，确保结构完整。
 * @param {Partial<MilestoneProgressModel>} source
 * @returns {MilestoneProgressModel}
 */
export function normalizeMilestoneProgressModel(source = {}) {
  const defaults = createDefaultMilestoneProgressModel();
  return {
    ...defaults,
    ...source,
    lowProgress: {
      ...defaults.lowProgress,
      ...(source.lowProgress || {}),
    },
    style: {
      ...defaults.style,
      ...(source.style || {}),
    },
  };
}
