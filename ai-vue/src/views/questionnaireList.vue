<template>
  <div class="questionnaire-container">
    <div class="header-section">
      <div class="header-content">
        <el-icon :size="48" color="white"><FirstAidKit /></el-icon>
        <div>
          <h1>心理测评</h1>
          <p>通过专业量表了解自己的心理状态</p>
        </div>
      </div>
    </div>

    <div class="content-section">
      <div class="section-header">
        <h2>可用量表</h2>
        <p>选择一份量表开始测评，所有量表均基于国际标准</p>
      </div>

      <div class="questionnaire-grid">
        <div v-for="item in questionnaireList" :key="item.id" class="questionnaire-card" @click="goToTake(item.id)">
          <div class="card-icon" :class="item.code === 'PHQ-9' ? 'phq' : 'gad'">
            <el-icon :size="32"><EditPen /></el-icon>
          </div>
          <div class="card-body">
            <h3>{{ item.name }}</h3>
            <p class="card-desc">{{ item.description }}</p>
            <div class="card-meta">
              <span><el-icon><Document /></el-icon> {{ item.questionCount }} 题</span>
              <span><el-icon><Trophy /></el-icon> 满分 {{ item.totalScore }} 分</span>
            </div>
          </div>
          <div class="card-action">
            <el-button type="primary" round>开始测评</el-button>
          </div>
        </div>
      </div>

      <div class="section-header" style="margin-top: 40px" v-if="records.length > 0">
        <h2>测评记录</h2>
      </div>
      <div class="records-list" v-if="records.length > 0">
        <div v-for="record in records" :key="record.id" class="record-item" @click="goToResult(record.id)">
          <div class="record-info">
            <el-tag :type="getLevelType(record.level)">{{ record.level }}</el-tag>
            <span class="record-name">{{ getQuestionnaireName(record.questionnaireId) }}</span>
            <span class="record-score">得分 {{ record.totalScore }}</span>
          </div>
          <div class="record-time">{{ record.createdAt }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getQuestionnaireList, getQuestionnaireRecords } from '@/api/frontend'

const router = useRouter()
const questionnaireList = ref([])
const records = ref([])

const goToTake = (id) => {
  router.push(`/questionnaire/${id}`)
}

const goToResult = (id) => {
  router.push(`/questionnaire/result/${id}`)
}

const getLevelType = (level) => {
  const map = { '正常': 'success', '轻度': 'info', '中度': 'warning', '中重度': 'danger', '重度': 'danger' }
  return map[level] || 'info'
}

const getQuestionnaireName = (id) => {
  const q = questionnaireList.value.find(q => q.id === id)
  return q ? q.name : '测评'
}

onMounted(async () => {
  try {
    questionnaireList.value = await getQuestionnaireList()
  } catch (e) { console.error(e) }
  try {
    const res = await getQuestionnaireRecords()
    records.value = Array.isArray(res) ? res : (res.records || [])
  } catch (e) { console.error(e) }
})
</script>

<style scoped lang="scss">
.questionnaire-container {
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 50%, #f2f6fa 100%);
}
.header-section {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  padding: 48px;
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    gap: 16px;
    color: white;
    h1 { margin: 0; font-size: 32px; }
    p { margin: 4px 0 0; opacity: 0.9; }
  }
}
.content-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 48px;
}
.section-header {
  margin-bottom: 24px;
  h2 { margin: 0 0 8px; font-size: 22px; color: #1f2937; }
  p { margin: 0; color: #6b7280; }
}
.questionnaire-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.questionnaire-card {
  background: white;
  border-radius: 16px;
  padding: 24px 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: #8b5cf6;
  }
  .card-icon {
    width: 72px;
    height: 72px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
    &.phq { background: linear-gradient(135deg, #f59e0b, #ef4444); }
    &.gad { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
  }
  .card-body {
    flex: 1;
    h3 { margin: 0 0 8px; font-size: 18px; color: #1f2937; }
    .card-desc { margin: 0 0 12px; color: #6b7280; font-size: 14px; line-height: 1.5; }
    .card-meta {
      display: flex;
      gap: 20px;
      span { font-size: 13px; color: #9ca3af; display: flex; align-items: center; gap: 4px; }
    }
  }
}
.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.record-item {
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
  &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }
  .record-info {
    display: flex;
    align-items: center;
    gap: 12px;
    .record-name { font-weight: 500; color: #374151; }
    .record-score { color: #6b7280; font-size: 14px; }
  }
  .record-time { color: #9ca3af; font-size: 13px; }
}
</style>
