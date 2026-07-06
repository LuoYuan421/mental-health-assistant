<template>
  <div class="result-container">
    <div class="header-section">
      <div class="header-content">
        <el-button text class="back-btn" @click="router.push('/questionnaire')">
          <el-icon><ArrowLeft /></el-icon> 返回测评
        </el-button>
      </div>
    </div>

    <div class="content-section" v-if="record">
      <div class="result-card">
        <div class="result-header">
          <div class="score-circle" :class="levelClass">
            <span class="score-num">{{ record.totalScore }}</span>
            <span class="score-label">分</span>
          </div>
          <div class="result-info">
            <h2>{{ data.questionnaireName }}</h2>
            <el-tag :type="levelTagType" size="large" effect="dark">{{ record.level }}</el-tag>
            <p class="result-time">{{ record.createdAt }}</p>
          </div>
        </div>

        <div class="suggestion-box">
          <div class="suggestion-icon">
            <el-icon :size="24" color="#8b5cf6"><InfoFilled /></el-icon>
          </div>
          <div class="suggestion-content">
            <h4>评估建议</h4>
            <p>{{ record.suggestion }}</p>
          </div>
        </div>

        <div class="scoring-rules" v-if="scoringRules">
          <h4>评分标准</h4>
          <div class="rules-list">
            <div v-for="(rule, key) in scoringRules" :key="key" class="rule-item" :class="{ active: isCurrentLevel(key) }">
              <span class="rule-level">{{ getLevelName(key) }}</span>
              <span class="rule-range">{{ rule }}</span>
            </div>
          </div>
        </div>

        <div class="answers-section" v-if="questions.length > 0">
          <h4>答题详情</h4>
          <div v-for="(q, index) in questions" :key="q.id" class="answer-item">
            <div class="answer-header">
              <span class="q-num">{{ index + 1 }}</span>
              <span class="q-text">{{ q.text }}</span>
            </div>
            <div class="answer-body">
              <span class="answer-label">您的回答：</span>
              <el-tag size="small" :type="answersList[index] === 0 ? 'success' : answersList[index] <= 1 ? 'info' : answersList[index] <= 2 ? 'warning' : 'danger'">
                {{ q.options[answersList[index]] }} ({{ answersList[index] }}分)
              </el-tag>
            </div>
          </div>
        </div>

        <div class="action-section">
          <el-button type="primary" round @click="router.push('/questionnaire')">
            再测一次
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getQuestionnaireRecordDetail } from '@/api/frontend'

const router = useRouter()
const route = useRoute()

const data = ref({})
const record = ref(null)
const questions = ref([])
const answersList = ref([])
const scoringRules = ref(null)

const levelTagType = computed(() => {
  const map = { '正常': 'success', '轻度': 'info', '中度': 'warning', '中重度': 'danger', '重度': 'danger' }
  return map[record.value?.level] || 'info'
})

const levelClass = computed(() => {
  const map = { '正常': 'normal', '轻度': 'mild', '中度': 'moderate', '中重度': 'severe', '重度': 'severe' }
  return map[record.value?.level] || 'normal'
})

const isCurrentLevel = (key) => {
  const levelMap = { '0': '正常', '1': '轻度', '2': '中度', '3': '中重度', '4': '重度' }
  return levelMap[key] === record.value?.level
}

const getLevelName = (key) => {
  const map = { '0': '正常', '1': '轻度', '2': '中度', '3': '中重度', '4': '重度' }
  return map[key] || key
}

onMounted(async () => {
  const id = route.params.id
  try {
    const result = await getQuestionnaireRecordDetail(id)
    data.value = result
    record.value = result.record
    questions.value = result.questions || []
    answersList.value = result.answersList || []
    if (record.value) {
      // Fetch questionnaire for scoring rules
      const { getQuestionnaireDetail } = await import('@/api/frontend')
      const q = await getQuestionnaireDetail(record.value.questionnaireId)
      scoringRules.value = typeof q.scoringRules === 'string' ? JSON.parse(q.scoringRules) : q.scoringRules
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped lang="scss">
.result-container {
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 50%, #f2f6fa 100%);
}
.header-section {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  padding: 24px 48px;
  .header-content { max-width: 800px; margin: 0 auto; }
  .back-btn { color: white !important; }
}
.content-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 48px 48px;
}
.result-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}
.result-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f3f4f6;
}
.score-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  &.normal { background: linear-gradient(135deg, #34d399, #10b981); color: white; }
  &.mild { background: linear-gradient(135deg, #60a5fa, #3b82f6); color: white; }
  &.moderate { background: linear-gradient(135deg, #fbbf24, #f59e0b); color: white; }
  &.severe { background: linear-gradient(135deg, #f87171, #ef4444); color: white; }
  .score-num { font-size: 32px; font-weight: 700; line-height: 1; }
  .score-label { font-size: 14px; opacity: 0.9; }
}
.result-info {
  h2 { margin: 0 0 8px; font-size: 22px; color: #1f2937; }
  .result-time { margin: 8px 0 0; color: #9ca3af; font-size: 13px; }
}
.suggestion-box {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border-radius: 12px;
  margin-bottom: 24px;
  .suggestion-icon { flex-shrink: 0; padding-top: 2px; }
  .suggestion-content {
    h4 { margin: 0 0 8px; color: #6d28d9; font-size: 15px; }
    p { margin: 0; color: #4c1d95; line-height: 1.6; font-size: 14px; }
  }
}
.scoring-rules {
  margin-bottom: 24px;
  h4 { margin: 0 0 12px; font-size: 16px; color: #374151; }
  .rules-list { display: flex; flex-direction: column; gap: 8px; }
  .rule-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    border-radius: 8px;
    background: #f9fafb;
    font-size: 14px;
    &.active { background: #ede9fe; border: 1px solid #c4b5fd; font-weight: 600; }
    .rule-level { color: #6b7280; min-width: 50px; }
    .rule-range { color: #374151; }
  }
}
.answers-section {
  margin-bottom: 24px;
  h4 { margin: 0 0 12px; font-size: 16px; color: #374151; }
}
.answer-item {
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  .answer-header {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 6px;
    .q-num {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: #e5e7eb;
      color: #6b7280;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      flex-shrink: 0;
    }
    .q-text { font-size: 14px; color: #374151; line-height: 1.5; }
  }
  .answer-body {
    margin-left: 32px;
    .answer-label { font-size: 13px; color: #9ca3af; margin-right: 8px; }
  }
}
.action-section { text-align: center; padding-top: 16px; }
</style>
