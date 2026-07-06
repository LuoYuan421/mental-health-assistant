<template>
  <div class="take-container">
    <div class="header-section">
      <div class="header-content">
        <el-button text class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h1>{{ questionnaire.name }}</h1>
        <p>{{ questionnaire.description }}</p>
      </div>
    </div>

    <div class="content-section" v-if="questionnaire.id">
      <div class="progress-bar">
        <div class="progress-text">已完成 {{ answeredCount }} / {{ questions.length }} 题</div>
        <el-progress :percentage="progressPercent" :stroke-width="8" color="#8b5cf6" />
      </div>

      <div class="questions-list">
        <div v-for="(q, index) in questions" :key="q.id" class="question-card">
          <div class="question-header">
            <span class="question-num">{{ index + 1 }}</span>
            <span class="question-text">{{ q.text }}</span>
          </div>
          <p class="question-hint">过去两周内，以下问题困扰您的频率是？</p>
          <div class="options-list">
            <div
              v-for="(opt, optIdx) in q.options"
              :key="optIdx"
              class="option-item"
              :class="{ active: answers[index] === optIdx }"
              @click="selectOption(index, optIdx)"
            >
              <span class="option-score">{{ optIdx }}</span>
              <span class="option-text">{{ opt }}</span>
              <el-icon v-if="answers[index] === optIdx" class="check-icon"><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="submit-section">
        <el-button
          type="primary"
          size="large"
          round
          :disabled="answeredCount < questions.length"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ answeredCount < questions.length ? `还有 ${questions.length - answeredCount} 题未答` : '提交测评' }}
        </el-button>
      </div>
    </div>

    <div class="loading-section" v-else>
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getQuestionnaireDetail, submitQuestionnaire } from '@/api/frontend'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const questionnaire = ref({})
const questions = ref([])
const answers = ref([])
const submitting = ref(false)

const answeredCount = computed(() => answers.value.filter(a => a !== undefined && a !== null).length)
const progressPercent = computed(() => questions.value.length ? Math.round(answeredCount.value / questions.value.length * 100) : 0)

const selectOption = (qIndex, optIndex) => {
  answers.value[qIndex] = optIndex
}

const handleSubmit = async () => {
  if (answeredCount.value < questions.value.length) {
    ElMessage.warning('请完成所有题目后再提交')
    return
  }
  submitting.value = true
  try {
    const result = await submitQuestionnaire({
      questionnaireId: questionnaire.value.id,
      answers: answers.value
    })
    ElMessage.success('测评提交成功')
    router.push(`/questionnaire/result/${result.recordId}`)
  } catch (e) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const id = route.params.id
  try {
    const data = await getQuestionnaireDetail(id)
    questionnaire.value = data
    questions.value = typeof data.questions === 'string' ? JSON.parse(data.questions) : data.questions
    answers.value = new Array(questions.value.length)
  } catch (e) {
    ElMessage.error('加载问卷失败')
  }
})
</script>

<style scoped lang="scss">
.take-container {
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 50%, #f2f6fa 100%);
}
.header-section {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  padding: 32px 48px 40px;
  .header-content {
    max-width: 800px;
    margin: 0 auto;
    color: white;
    h1 { margin: 12px 0 8px; font-size: 28px; }
    p { margin: 0; opacity: 0.9; font-size: 15px; }
  }
  .back-btn { color: white !important; margin-bottom: 8px; }
}
.content-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 48px 48px;
}
.progress-bar {
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  .progress-text { font-size: 14px; color: #6b7280; margin-bottom: 8px; }
}
.questions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.question-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  .question-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 8px;
    .question-num {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      font-weight: 600;
      flex-shrink: 0;
    }
    .question-text { font-size: 16px; color: #1f2937; font-weight: 500; line-height: 1.5; padding-top: 2px; }
  }
  .question-hint { font-size: 13px; color: #9ca3af; margin: 0 0 16px 40px; }
}
.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 40px;
}
.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
  &:hover { border-color: #c4b5fd; background: #f5f3ff; }
  &.active {
    border-color: #8b5cf6;
    background: linear-gradient(135deg, #ede9fe, #f5f3ff);
    .option-score { background: #8b5cf6; color: white; }
  }
  .option-score {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #e5e7eb;
    color: #6b7280;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    flex-shrink: 0;
  }
  .option-text { flex: 1; font-size: 14px; color: #374151; }
  .check-icon { color: #8b5cf6; font-size: 18px; }
}
.submit-section {
  text-align: center;
  margin-top: 32px;
  .el-button { min-width: 200px; font-size: 16px; }
}
.loading-section {
  text-align: center;
  padding: 80px 0;
  color: #9ca3af;
}
</style>
