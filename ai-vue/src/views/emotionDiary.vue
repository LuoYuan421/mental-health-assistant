<template>
    <div class="emotionDiary-container">
        <div class="header-section">
            <div class="header-content">
                <el-image :src="iconUrl" style="width: 60px;height: 60px"></el-image>
                <h1>情绪日志</h1>
            </div>
        </div>
        <div class="content">
            <!-- Tab 切换 -->
            <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="diary-tabs">
                <!-- 写日记 -->
                <el-tab-pane label="写日记" name="write">
                    <div class="diary-card">
                        <div class="title">今日情绪评分</div>
                        <div class="section">
                            <p>您今天的整体情绪状态如何？(1-10分)</p>
                            <div class="rate">
                                <el-rate
                                    v-model="diaryForm.moodScore"
                                    :texts="emotionStatus"
                                    show-texts
                                    :max="10"
                                    size="large"
                                />
                            </div>
                        </div>
                    </div>
                    <div class="diary-card">
                        <div class="title">主要情绪</div>
                        <div class="emotion-grid">
                            <div v-for="emotion in emotionOptions" :key="emotion.name" class="emotion-card" :class="{'selected': emotion.name === diaryForm.dominantEmotion}" @click="selectEmotion(emotion.name)">
                                <el-image :src="emotion.url" style="width: 50px;height: 50px"></el-image>
                                <div class="emotion-name">{{emotion.name}}</div>
                            </div>
                        </div>
                    </div>
                    <div class="diary-card">
                        <div class="title">详细记录</div>
                        <div class="detail-form">
                            <div class="form-group">
                                <div class="form-label">情绪触发因素</div>
                                <el-input v-model="diaryForm.emotionTriggers" placeholder="今天什么事情影响了您的情绪？" type="textarea" :rows="3" maxLength="1000" show-word-limit></el-input>
                            </div>
                            <div class="form-group">
                                <div class="form-label">今日感想</div>
                                <el-input v-model="diaryForm.diaryContent" placeholder="写下您今天的想法、感受或发生的有趣事情..." type="textarea" :rows="5" maxLength="2000" show-word-limit></el-input>
                            </div>
                            <div class="life-indicators">
                                <div class="indicator-group">
                                    <div class="form-label">睡眠质量</div>
                                    <el-select v-model="diaryForm.sleepQuality" placeholder="请选择">
                                        <el-option label="很差" :value="1"></el-option>
                                        <el-option label="较差" :value="2"></el-option>
                                        <el-option label="一般" :value="3"></el-option>
                                        <el-option label="良好" :value="4"></el-option>
                                        <el-option label="优秀" :value="5"></el-option>
                                    </el-select>
                                </div>
                                <div class="indicator-group">
                                    <div class="form-label">压力水平</div>
                                    <el-select v-model="diaryForm.stressLevel" placeholder="请选择">
                                        <el-option label="很低" :value="1"></el-option>
                                        <el-option label="较低" :value="2"></el-option>
                                        <el-option label="中等" :value="3"></el-option>
                                        <el-option label="较高" :value="4"></el-option>
                                        <el-option label="很高" :value="5"></el-option>
                                    </el-select>
                                </div>
                            </div>
                            <div class="action-buttons">
                                <el-button @click="resetForm">重置</el-button>
                                <el-button type="primary" @click="submit" :loading="submitting">提交记录</el-button>
                            </div>
                        </div>
                    </div>
                    <!-- AI分析结果 -->
                    <div class="diary-card ai-analysis-card" v-if="aiAnalysis">
                        <div class="title">
                            <span style="margin-right: 8px">🤖</span>AI情绪分析
                        </div>
                        <div class="analysis-content">
                            <div class="analysis-text">{{ aiAnalysis.analysis }}</div>
                            <div class="analysis-meta" v-if="aiAnalysis.suggestion">
                                <div class="suggestion-label">小建议：</div>
                                <div class="suggestion-text">{{ aiAnalysis.suggestion }}</div>
                            </div>
                        </div>
                    </div>
                </el-tab-pane>

                <!-- 历史记录 -->
                <el-tab-pane label="历史记录" name="history">
                    <div class="diary-card" v-if="historyList.length === 0">
                        <el-empty description="还没有写过日记，去记录今天的心情吧" />
                    </div>
                    <div v-for="item in historyList" :key="item.id" class="diary-card history-item">
                        <div class="history-header">
                            <div class="history-date">{{ item.diaryDate }}</div>
                            <div class="history-emotion">
                                <el-tag :type="getEmotionTagType(item.dominantEmotion)" effect="light" round>
                                    {{ item.dominantEmotion }}
                                </el-tag>
                                <span class="history-score">{{ item.moodScore }}/10</span>
                            </div>
                        </div>
                        <div class="history-body">
                            <div class="history-row" v-if="item.emotionTriggers">
                                <span class="label">触发因素：</span>
                                <span>{{ item.emotionTriggers }}</span>
                            </div>
                            <div class="history-row" v-if="item.diaryContent">
                                <span class="label">今日感想：</span>
                                <span>{{ item.diaryContent }}</span>
                            </div>
                            <div class="history-indicators">
                                <span v-if="item.sleepQuality">😴 睡眠：{{ ['', '很差','较差','一般','良好','优秀'][item.sleepQuality] }}</span>
                                <span v-if="item.stressLevel">😤 压力：{{ ['', '很低','较低','中等','较高','很高'][item.stressLevel] }}</span>
                            </div>
                        </div>
                    </div>
                    <div class="pagination-wrapper" v-if="historyTotal > historySize">
                        <el-pagination
                            :page-size="historySize"
                            layout="prev, pager, next"
                            :total="historyTotal"
                            @current-change="handleHistoryPageChange"
                        />
                    </div>
                </el-tab-pane>

                <!-- 情绪趋势 -->
                <el-tab-pane label="情绪趋势" name="trend">
                    <div class="diary-card">
                        <div class="trend-header">
                            <div class="title">情绪变化趋势</div>
                            <el-radio-group v-model="trendDays" @change="loadTrendData" size="small">
                                <el-radio-button :value="7">近7天</el-radio-button>
                                <el-radio-button :value="14">近14天</el-radio-button>
                                <el-radio-button :value="30">近30天</el-radio-button>
                            </el-radio-group>
                        </div>
                        <div class="trend-summary" v-if="trendData.length > 0">
                            <div class="summary-item">
                                <div class="summary-value">{{ trendSummary.recordedDays }}</div>
                                <div class="summary-label">记录天数</div>
                            </div>
                            <div class="summary-item">
                                <div class="summary-value">{{ trendSummary.avgScore }}</div>
                                <div class="summary-label">平均情绪</div>
                            </div>
                            <div class="summary-item">
                                <div class="summary-value">{{ trendSummary.totalEntries }}</div>
                                <div class="summary-label">总日记数</div>
                            </div>
                        </div>
                        <div class="chart-container" ref="chartRef">
                            <canvas ref="canvasRef" v-show="trendData.length > 0"></canvas>
                        </div>
                        <el-empty v-if="trendData.length === 0" description="暂无趋势数据，多记录几天心情吧" />
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>
<script setup>
    import { dayjs, ElMessage } from 'element-plus'
    import { ref, reactive, onMounted, nextTick, watch } from 'vue'
    import { addEmotionDiary, getEmotionDiaryList, getEmotionTrend } from '@/api/frontend'

    const iconUrl = new URL('@/assets/images/like.png', import.meta.url).href
    const activeTab = ref('write')
    const submitting = ref(false)
    const aiAnalysis = ref(null)

    // 情绪评分
    const emotionStatus = ['绝望崩溃', '消沉抑郁', '焦虑烦躁', '低落不悦', '平静淡然', '轻松惬意', '愉悦舒心', '欢欣满足', '兴奋欣喜', '极致幸福']

    // 情绪选项
    const emotionOptions = [
        { name: '开心', url: new URL('@/assets/images/开心.png', import.meta.url).href },
        { name: '平静', url: new URL('@/assets/images/平静.png', import.meta.url).href },
        { name: '焦虑', url: new URL('@/assets/images/焦虑.png', import.meta.url).href },
        { name: '悲伤', url: new URL('@/assets/images/悲伤.png', import.meta.url).href },
        { name: '兴奋', url: new URL('@/assets/images/兴奋.png', import.meta.url).href },
        { name: '疲惫', url: new URL('@/assets/images/疲惫.png', import.meta.url).href },
        { name: '惊讶', url: new URL('@/assets/images/惊讶.png', import.meta.url).href },
        { name: '困惑', url: new URL('@/assets/images/困惑.png', import.meta.url).href },
    ]

    const selectEmotion = (emotion) => {
        diaryForm.dominantEmotion = emotion
    }

    const diaryForm = reactive({
        diaryDate: dayjs().format('YYYY-MM-DD'),
        moodScore: null,
        dominantEmotion: '',
        emotionTriggers: '',
        diaryContent: '',
        sleepQuality: null,
        stressLevel: null
    })

    const resetForm = () => {
        Object.assign(diaryForm, {
            diaryDate: dayjs().format('YYYY-MM-DD'),
            moodScore: null,
            dominantEmotion: '',
            emotionTriggers: '',
            diaryContent: '',
            sleepQuality: null,
            stressLevel: null
        })
        aiAnalysis.value = null
    }

    const submit = () => {
        if (!diaryForm.moodScore) {
            ElMessage.error('请选择情绪评分')
            return
        }
        if (!diaryForm.dominantEmotion) {
            ElMessage.error('请选择主要情绪')
            return
        }
        submitting.value = true
        addEmotionDiary(diaryForm).then((res) => {
            ElMessage.success('提交成功')
            aiAnalysis.value = res.aiAnalysis || null
            submitting.value = false
            resetForm()
            // 提交后切到历史记录tab
            activeTab.value = 'history'
            loadHistoryList()
        }).catch((err) => {
            const msg = err?.response?.data?.msg || err?.msg || err?.message || '提交失败，请重试'
            ElMessage.error(msg)
            submitting.value = false
        })
    }

    // ========== 历史记录 ==========
    const historyList = ref([])
    const historyPage = ref(1)
    const historySize = ref(10)
    const historyTotal = ref(0)

    const loadHistoryList = () => {
        getEmotionDiaryList({ currentPage: historyPage.value, size: historySize.value }).then(res => {
            historyList.value = res.records || []
            historyTotal.value = res.total || 0
        })
    }

    const handleHistoryPageChange = (page) => {
        historyPage.value = page
        loadHistoryList()
    }

    const getEmotionTagType = (emotion) => {
        const map = { '开心': 'success', '平静': 'info', '焦虑': 'warning', '悲伤': 'danger', '兴奋': 'success', '疲惫': 'warning', '惊讶': '', '困惑': 'info' }
        return map[emotion] || 'info'
    }

    // ========== 情绪趋势 ==========
    const trendDays = ref(7)
    const trendData = ref([])
    const trendSummary = reactive({ recordedDays: 0, avgScore: '-', totalEntries: 0 })
    const canvasRef = ref(null)

    const loadTrendData = () => {
        getEmotionTrend({ days: trendDays.value }).then(res => {
            trendData.value = (res.trend || []).filter(d => d.moodScore !== null)
            trendSummary.recordedDays = res.recordedDays || 0
            // 计算平均分
            if (trendData.value.length > 0) {
                const total = trendData.value.reduce((sum, d) => sum + d.moodScore, 0)
                trendSummary.avgScore = (total / trendData.value.length).toFixed(1)
                trendSummary.totalEntries = trendData.value.reduce((sum, d) => sum + d.count, 0)
            } else {
                trendSummary.avgScore = '-'
                trendSummary.totalEntries = 0
            }
            // 渲染图表
            nextTick(() => renderChart())
        })
    }

    const renderChart = () => {
        if (!canvasRef.value || trendData.value.length === 0) return
        const canvas = canvasRef.value
        const ctx = canvas.getContext('2d')
        const data = trendData.value
        const width = canvas.parentElement.clientWidth
        const height = 280
        canvas.width = width
        canvas.height = height

        const padding = { top: 30, right: 20, bottom: 40, left: 40 }
        const chartW = width - padding.left - padding.right
        const chartH = height - padding.top - padding.bottom

        // 清空
        ctx.clearRect(0, 0, width, height)

        // 找数据范围
        const scores = data.map(d => d.moodScore)
        const minScore = Math.max(0, Math.min(...scores) - 1)
        const maxScore = Math.min(10, Math.max(...scores) + 1)

        const getX = (i) => padding.left + (i / (data.length - 1 || 1)) * chartW
        const getY = (v) => padding.top + chartH - ((v - minScore) / (maxScore - minScore)) * chartH

        // 画网格线
        ctx.strokeStyle = '#e5e7eb'
        ctx.lineWidth = 1
        for (let i = 0; i <= 5; i++) {
            const y = padding.top + (chartH / 5) * i
            ctx.beginPath()
            ctx.moveTo(padding.left, y)
            ctx.lineTo(width - padding.right, y)
            ctx.stroke()
            // Y轴标签
            const score = maxScore - ((maxScore - minScore) / 5) * i
            ctx.fillStyle = '#9ca3af'
            ctx.font = '12px sans-serif'
            ctx.textAlign = 'right'
            ctx.fillText(score.toFixed(0), padding.left - 8, y + 4)
        }

        // 画填充区域
        ctx.beginPath()
        ctx.moveTo(getX(0), padding.top + chartH)
        data.forEach((d, i) => ctx.lineTo(getX(i), getY(d.moodScore)))
        ctx.lineTo(getX(data.length - 1), padding.top + chartH)
        ctx.closePath()
        const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartH)
        gradient.addColorStop(0, 'rgba(126, 211, 33, 0.3)')
        gradient.addColorStop(1, 'rgba(126, 211, 33, 0.02)')
        ctx.fillStyle = gradient
        ctx.fill()

        // 画折线
        ctx.beginPath()
        ctx.strokeStyle = '#7ED321'
        ctx.lineWidth = 2.5
        ctx.lineJoin = 'round'
        data.forEach((d, i) => {
            if (i === 0) ctx.moveTo(getX(i), getY(d.moodScore))
            else ctx.lineTo(getX(i), getY(d.moodScore))
        })
        ctx.stroke()

        // 画数据点
        data.forEach((d, i) => {
            ctx.beginPath()
            ctx.arc(getX(i), getY(d.moodScore), 5, 0, Math.PI * 2)
            ctx.fillStyle = '#7ED321'
            ctx.fill()
            ctx.strokeStyle = '#fff'
            ctx.lineWidth = 2
            ctx.stroke()

            // X轴日期标签
            ctx.fillStyle = '#9ca3af'
            ctx.font = '11px sans-serif'
            ctx.textAlign = 'center'
            const label = d.date ? d.date.slice(5) : ''  // MM-DD
            ctx.fillText(label, getX(i), height - 10)
        })
    }

    // Tab 切换时加载数据
    const handleTabChange = (tab) => {
        if (tab === 'history') loadHistoryList()
        if (tab === 'trend') loadTrendData()
    }

    onMounted(() => {
        // 初始加载情绪图片
    })
</script>
<style lang="scss" scoped>
    .emotionDiary-container {
    background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 50%, #f2f6fa 100%);
    min-height: 100vh;
    .header-section {
        background: linear-gradient(135deg, #7ED321 0%, #F5A623 100%);
        color: white;
        padding: 48px;
        .header-content {
            display: flex;
            align-items: center;
            gap: 12px;
        }
    }
    .content {
        margin: 0 auto;
        width: 980px;
        padding: 20px;
        .diary-card {
            margin-bottom: 20px;
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            .title {
                margin-bottom: 20px;
                font-size: 25px;
                font-weight: 600;
                color: #374151;
            }
            .section {
                margin-bottom: 20px;
                p {
                    font-size: 15px;
                    color: #6B7280;
                    margin-bottom: 15px;
                }
            }
            .emotion-grid {
                display: flex;
                flex-wrap: wrap;
                gap: 10px;
                .emotion-card {
                    padding: 15px;
                    border: 2px solid #E5E7EB;
                    border-radius: 15px;
                    text-align: center;
                    cursor: pointer;
                    background: #F9FAFB;
                    .emotion-name {
                        margin-top: 10px;
                        padding: 0 75px;
                        color: #374151;
                    }
                    &.selected {
                        border-color: #7ED321;
                        background: #F0FDF4;
                        transform: translateY(-3px);
                    }
                }
            }
            .detail-form {
                .form-label {
                    margin: 10px 0;
                    color: #374151;
                }
                .life-indicators {
                    display: flex;
                    gap: 20px;
                    .indicator-group {
                        flex: 1;
                    }
                }
                .action-buttons {
                    margin-top: 40px
                }
            }
        }
        // AI分析卡片
        .ai-analysis-card {
            border-left: 4px solid #7ED321;
            background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
            .analysis-content {
                .analysis-text {
                    font-size: 15px;
                    color: #374151;
                    line-height: 1.8;
                    white-space: pre-wrap;
                }
                .analysis-meta {
                    margin-top: 15px;
                    padding-top: 15px;
                    border-top: 1px dashed #e5e7eb;
                    .suggestion-label {
                        color: #7ED321;
                        font-weight: 600;
                        margin-bottom: 5px;
                    }
                    .suggestion-text {
                        color: #6b7280;
                        font-size: 14px;
                    }
                }
            }
        }
        // 历史记录
        .history-item {
            .history-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 12px;
                .history-date {
                    font-size: 18px;
                    font-weight: 600;
                    color: #374151;
                }
                .history-emotion {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    .history-score {
                        font-size: 16px;
                        font-weight: 600;
                        color: #7ED321;
                    }
                }
            }
            .history-body {
                .history-row {
                    margin-bottom: 8px;
                    font-size: 14px;
                    color: #4b5563;
                    line-height: 1.6;
                    .label {
                        color: #9ca3af;
                        font-weight: 500;
                    }
                }
                .history-indicators {
                    margin-top: 12px;
                    display: flex;
                    gap: 20px;
                    font-size: 13px;
                    color: #6b7280;
                }
            }
        }
        .pagination-wrapper {
            display: flex;
            justify-content: center;
            margin-top: 20px;
        }
        // 情绪趋势
        .trend-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .trend-summary {
            display: flex;
            justify-content: space-around;
            margin-bottom: 25px;
            padding: 15px 0;
            border-bottom: 1px solid #f3f4f6;
            .summary-item {
                text-align: center;
                .summary-value {
                    font-size: 28px;
                    font-weight: 700;
                    color: #7ED321;
                }
                .summary-label {
                    font-size: 13px;
                    color: #9ca3af;
                    margin-top: 5px;
                }
            }
        }
        .chart-container {
            width: 100%;
            min-height: 280px;
            canvas {
                width: 100%;
            }
        }
    }
}
</style>
