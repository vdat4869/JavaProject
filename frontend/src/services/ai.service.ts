import apiClient from './api'

/**
 * AI Service - Gọi các API AI hỗ trợ
 *
 * Lưu ý quan trọng:
 * - AI chỉ gợi ý, KHÔNG tự động thay đổi dữ liệu
 * - User phải xác nhận trước khi áp dụng gợi ý
 * - Mọi lời gọi AI đều được ghi log
 */

// ==================== Request Types ====================

export interface SpellCheckRequest {
    conferenceId?: number
    title?: string
    abstractText: string
    keywords?: string
    language?: 'en' | 'vi'
}

export interface AbstractPolishRequest {
    conferenceId?: number
    abstractText: string
    tone?: 'formal' | 'concise' | 'expanded'
    language?: 'en' | 'vi'
}

export interface KeywordSuggestRequest {
    conferenceId?: number
    title?: string
    abstractText: string
    existingKeywords?: string
    maxKeywords?: number
}

export interface NeutralSummaryRequest {
    conferenceId?: number
    abstractText: string
    targetWordCount?: number
}

export interface KeyPointsRequest {
    conferenceId?: number
    abstractText: string
}

export interface SimilarityHintRequest {
    conferenceId?: number
    submissionId: number
    paperKeywords?: string[]
    paperTopics?: string[]
    reviewerId: number
    reviewerExpertise?: string[]
}

export interface EmailDraftRequest {
    conferenceId?: number
    emailType: 'DECISION' | 'REMINDER' | 'INVITATION' | 'NOTIFICATION'
    context?: string
    conferenceName?: string
    recipientName?: string
    tone?: 'formal' | 'friendly' | 'urgent'
    language?: 'en' | 'vi'
}

// ==================== Response Types ====================

export interface SpellCheckResponse {
    success: boolean
    message: string
    suggestions: Array<{
        type: 'SPELLING' | 'GRAMMAR' | 'STYLE'
        original: string
        replacement: string
        explanation: string
        field: 'title' | 'abstract' | 'keywords'
    }>
    auditLogId: number
    processingTimeMs: number
}

export interface AbstractPolishResponse {
    success: boolean
    message: string
    originalAbstract: string
    polishedAbstract: string
    changes: Array<{
        before: string
        after: string
        changeType: 'CLARITY' | 'GRAMMAR' | 'CONCISENESS' | 'FLOW'
        explanation: string
    }>
    auditLogId: number
    processingTimeMs: number
}

export interface KeywordSuggestResponse {
    success: boolean
    message: string
    keywords: Array<{
        keyword: string
        relevanceScore: number
        explanation: string
        isCommon: boolean
    }>
    auditLogId: number
    processingTimeMs: number
}

export interface NeutralSummaryResponse {
    success: boolean
    message: string
    summary: string
    wordCount: number
    auditLogId: number
    processingTimeMs: number
}

export interface KeyPointsResponse {
    success: boolean
    message: string
    claims: string[]
    methods: string[]
    datasets: string[]
    findings: string[]
    auditLogId: number
    processingTimeMs: number
}

export interface SimilarityHintResponse {
    success: boolean
    message: string
    similarityScore: number
    overlappingKeywords: string[]
    fitLevel: 'HIGH' | 'MEDIUM' | 'LOW'
    explanation: string
    auditLogId: number
    processingTimeMs: number
}

export interface EmailDraftResponse {
    success: boolean
    message: string
    subject: string
    body: string
    auditLogId: number
    processingTimeMs: number
}

// ==================== AI Service ====================

export const aiService = {
    // ========== Author AI ==========

    /**
     * Kiểm tra chính tả và ngữ pháp
     */
    spellCheck: async (data: SpellCheckRequest): Promise<SpellCheckResponse> => {
        const response = await apiClient.post<{ success: boolean; data: SpellCheckResponse }>(
            '/ai/author/spell-check',
            data
        )
        return response.data.data || response.data
    },

    /**
     * Cải thiện abstract
     */
    abstractPolish: async (data: AbstractPolishRequest): Promise<AbstractPolishResponse> => {
        const response = await apiClient.post<{ success: boolean; data: AbstractPolishResponse }>(
            '/ai/author/abstract-polish',
            data
        )
        return response.data.data || response.data
    },

    /**
     * Gợi ý keywords
     */
    keywordSuggest: async (data: KeywordSuggestRequest): Promise<KeywordSuggestResponse> => {
        const response = await apiClient.post<{ success: boolean; data: KeywordSuggestResponse }>(
            '/ai/author/keyword-suggest',
            data
        )
        return response.data.data || response.data
    },

    // ========== Reviewer/PC AI ==========

    /**
     * Tạo tóm tắt trung lập (cho PC bidding)
     */
    neutralSummary: async (data: NeutralSummaryRequest): Promise<NeutralSummaryResponse> => {
        const response = await apiClient.post<{ success: boolean; data: NeutralSummaryResponse }>(
            '/ai/pc/neutral-summary',
            data
        )
        return response.data.data || response.data
    },

    /**
     * Trích xuất key points
     */
    keyPoints: async (data: KeyPointsRequest): Promise<KeyPointsResponse> => {
        const response = await apiClient.post<{ success: boolean; data: KeyPointsResponse }>(
            '/ai/pc/key-points',
            data
        )
        return response.data.data || response.data
    },

    /**
     * Gợi ý độ tương đồng reviewer-paper (cho Chair)
     */
    similarityHint: async (data: SimilarityHintRequest): Promise<SimilarityHintResponse> => {
        const response = await apiClient.post<{ success: boolean; data: SimilarityHintResponse }>(
            '/ai/assignment/similarity-hint',
            data
        )
        return response.data.data || response.data
    },

    // ========== Chair AI ==========

    /**
     * Soạn email thông báo
     */
    emailDraft: async (data: EmailDraftRequest): Promise<EmailDraftResponse> => {
        const response = await apiClient.post<{ success: boolean; data: EmailDraftResponse }>(
            '/ai/chair/email-draft',
            data
        )
        return response.data.data || response.data
    },
}
