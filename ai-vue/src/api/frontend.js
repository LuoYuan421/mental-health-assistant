import service from '@/utils/request'

export const register = (data) => {
    return service.post('/user/add', data)
}

export const startSession = (data) => {
    return service.post('/psychological-chat/session/start', data)
}

export const getSessionList = (params) => {
    return service.get('/psychological-chat/sessions', { params })
}

export const deleteSession = (sessionId) => {
    return service.delete(`/psychological-chat/sessions/${sessionId}`)
}

export const getSessionDetail = (sessionId) => {
    return service.get(`/psychological-chat/sessions/${sessionId}/messages`)
}

export const getSessionEmotion = (sessionId) => {
    return service.get(`/psychological-chat/session/${sessionId}/emotion`)
}

export const addEmotionDiary = (data) => {
    return service.post('/emotion-diary', data)
}

export const getEmotionDiaryList = (params) => {
    return service.get('/emotion-diary/list', { params })
}

export const getEmotionTrend = (params) => {
    return service.get('/emotion-diary/trend', { params })
}

export const analyzeEmotionDiary = (data) => {
    return service.post('/emotion-diary/analyze', data)
}

export const getKnowledgeList = (params) => {
    return service.get('/knowledge/article/page', { params })
}

export const getKnowledgeDetail = (articleId) => {
    return service.get(`/knowledge/article/${articleId}`)
}

// 心理测评问卷
export const getQuestionnaireList = () => {
    return service.get('/questionnaire/list')
}

export const getQuestionnaireDetail = (id) => {
    return service.get(`/questionnaire/${id}`)
}

export const submitQuestionnaire = (data) => {
    return service.post('/questionnaire/submit', data)
}

export const getQuestionnaireRecords = (params) => {
    return service.get('/questionnaire/records', { params })
}

export const getQuestionnaireRecordDetail = (id) => {
    return service.get(`/questionnaire/records/${id}`)
}