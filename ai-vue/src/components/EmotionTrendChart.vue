<template>
  <div class="emotion-trend-card">
    <div class="card-header">
      <span class="card-title">情绪趋势</span>
      <div class="range-tabs">
        <button
          class="range-tab"
          :class="{ active: currentDays === 7 }"
          @click="switchRange(7)"
        >7天</button>
        <button
          class="range-tab"
          :class="{ active: currentDays === 30 }"
          @click="switchRange(30)"
        >30天</button>
      </div>
    </div>
    <div v-if="loading" class="chart-loading">
      <span class="loading-text">加载中...</span>
    </div>
    <div v-else-if="isEmpty" class="chart-empty">
      <div class="empty-icon">📈</div>
      <div class="empty-text">暂无情绪数据</div>
      <div class="empty-hint">记录情绪日记后可查看趋势</div>
    </div>
    <div v-else ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getEmotionTrend } from '@/api/frontend'

const props = defineProps({
  days: {
    type: Number,
    default: 7
  }
})

const chartRef = ref(null)
const currentDays = ref(props.days)
const loading = ref(true)
const isEmpty = ref(false)
let chart = null

const loadData = () => {
  loading.value = true
  isEmpty.value = false
  getEmotionTrend({ days: currentDays.value }).then(res => {
    const trendData = res.trend || []
    loading.value = false

    if (trendData.length === 0 || trendData.every(d => d.moodScore === null)) {
      isEmpty.value = true
      if (chart) {
        chart.dispose()
        chart = null
      }
      return
    }

    nextTick(() => {
      renderChart(trendData)
    })
  }).catch(() => {
    loading.value = false
    isEmpty.value = true
  })
}

const renderChart = (trendData) => {
  if (!chartRef.value) return

  if (chart) {
    chart.dispose()
  }

  chart = echarts.init(chartRef.value)

  const dates = trendData.map(d => {
    if (d.date) {
      const parts = d.date.split('-')
      return `${parts[1]}-${parts[2]}`
    }
    return ''
  })

  const scores = trendData.map(d => d.moodScore)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: '#fb923c',
      borderWidth: 1,
      borderRadius: 8,
      padding: [10, 14],
      textStyle: {
        color: '#334155',
        fontSize: 13
      },
      formatter: (params) => {
        const p = params[0]
        const idx = p.dataIndex
        const item = trendData[idx]
        let html = `<div style="font-weight:600;margin-bottom:4px">${item.date}</div>`
        if (item.moodScore !== null) {
          html += `<div>情绪评分：<span style="color:#fb923c;font-weight:700">${item.moodScore}</span></div>`
        } else {
          html += '<div style="color:#94a3b8">暂无记录</div>'
        }
        if (item.dominantEmotion) {
          html += `<div>主要情绪：${item.dominantEmotion}</div>`
        }
        return html
      }
    },
    grid: {
      left: 12,
      right: 16,
      top: 12,
      bottom: 8,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: {
        lineStyle: { color: '#e2e8f0' }
      },
      axisTick: { show: false },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 10,
      splitNumber: 5,
      splitLine: {
        lineStyle: { color: '#f1f5f9', type: 'dashed' }
      },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 11
      }
    },
    series: [
      {
        type: 'line',
        data: scores,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: true,
        connectNulls: false,
        lineStyle: {
          width: 2.5,
          color: '#fb923c'
        },
        itemStyle: {
          color: '#fb923c',
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            borderWidth: 3,
            shadowBlur: 8,
            shadowColor: 'rgba(251, 146, 60, 0.4)'
          }
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(251, 146, 60, 0.25)' },
            { offset: 1, color: 'rgba(251, 146, 60, 0.02)' }
          ])
        }
      }
    ]
  }

  chart.setOption(option)
}

const switchRange = (days) => {
  currentDays.value = days
  loadData()
}

const handleResize = () => {
  if (chart) {
    chart.resize()
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped lang="scss">
.emotion-trend-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 252, 248, 0.95) 100%);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(251, 146, 60, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(251, 146, 60, 0.08);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.range-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 2px;
}

.range-tab {
  border: none;
  background: transparent;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
  line-height: 1.4;

  &.active {
    background: #fb923c;
    color: #fff;
    box-shadow: 0 2px 8px rgba(251, 146, 60, 0.3);
  }

  &:hover:not(.active) {
    color: #fb923c;
  }
}

.chart-container {
  width: 100%;
  height: 200px;
}

.chart-loading {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;

  .loading-text {
    color: #94a3b8;
    font-size: 13px;
  }
}

.chart-empty {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;

  .empty-icon {
    font-size: 28px;
    margin-bottom: 2px;
  }

  .empty-text {
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
  }

  .empty-hint {
    font-size: 12px;
    color: #94a3b8;
  }
}
</style>
