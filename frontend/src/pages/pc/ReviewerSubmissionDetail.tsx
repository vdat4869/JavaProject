import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CSpinner,
    CButton,
    CAlert,
    CRow,
    CCol,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { AINeuralSummary, AIKeyPoints } from '../../components/ai/AIRenderers'
import { reviewService, Assignment } from '../../services/review.service'
import { submissionService, Submission } from '../../services/submission.service'
import { aiService, NeutralSummaryResponse, KeyPointsResponse } from '../../services/ai.service'
import { useAuth } from '../../context/AuthContext'
import CIcon from '@coreui/icons-react'
import { cilArrowLeft, cilCloudDownload, cilStar, cilCommentSquare } from '@coreui/icons'

/**
 * ReviewerSubmissionDetail - Trang chi tiết bài nộp dành cho Reviewer với AI Insights
 */
const ReviewerSubmissionDetail: React.FC = () => {
    const { id } = useParams<{ id: string }>()
    const navigate = useNavigate()
    const { t } = useTranslation()
    const [submission, setSubmission] = useState<Submission | null>(null)
    const [assignment, setAssignment] = useState<Assignment | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const { user } = useAuth()

    // AI Insights State
    const [aiLoading, setAiLoading] = useState(false)
    const [summary, setSummary] = useState<NeutralSummaryResponse | null>(null)
    const [keyPoints, setKeyPoints] = useState<KeyPointsResponse | null>(null)

    useEffect(() => {
        if (id) {
            loadSubmission(parseInt(id))
        }
    }, [id])

    const loadSubmission = async (submissionId: number) => {
        try {
            setLoading(true)
            const [submissionData, myAssignments] = await Promise.all([
                submissionService.getSubmissionById(submissionId),
                reviewService.getAssignments()
            ])
            setSubmission(submissionData)

            // Find the assignment for this submission
            const myAssignment = myAssignments.find((a: Assignment) => a.submissionId === submissionId)
            setAssignment(myAssignment || null)
        } catch (err: any) {
            setError(err.message || 'Không thể tải chi tiết bài nộp')
        } finally {
            setLoading(false)
        }
    }

    const handleGenerateAIInsights = async () => {
        if (!submission) return

        try {
            setAiLoading(true)
            setError('')

            const [summaryRes, keyPointsRes] = await Promise.all([
                aiService.neutralSummary({
                    conferenceId: submission.conferenceId,
                    abstractText: submission.abstractText
                }),
                aiService.keyPoints({
                    conferenceId: submission.conferenceId,
                    abstractText: submission.abstractText
                })
            ])

            if (summaryRes.success) {
                setSummary(summaryRes)
            } else {
                setError(summaryRes.message || 'AI Summary failed')
            }

            if (keyPointsRes.success) {
                setKeyPoints(keyPointsRes)
            } else {
                setError(prev => prev ? prev + ' | ' + (keyPointsRes.message || 'AI Key Points failed') : (keyPointsRes.message || 'AI Key Points failed'))
            }
        } catch (err: any) {
            setError(t('ai.error', { message: err.message }))
        } finally {
            setAiLoading(false)
        }
    }

    const handleDownloadPdf = async () => {
        if (!submission) return
        try {
            const blob = await submissionService.downloadFile(submission.id)
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.setAttribute('download', `submission_${submission.id}.pdf`)
            document.body.appendChild(link)
            link.click()
            link.remove()
        } catch (err: any) {
            setError('Không thể tải file PDF. Có thể bạn không có quyền hoặc file không tồn tại.')
        }
    }

    const handleJoinDiscussion = () => {
        if (submission) {
            navigate(`/app/pc/submissions/${submission.id}/discussion`)
        } else {
            navigate('/app/pc/discussions')
        }
    }

    if (loading) {
        return (
            <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
            </div>
        )
    }

    if (!submission) {
        return <CAlert color="danger">Không tìm thấy bài nộp</CAlert>
    }

    return (
        <div className="reviewer-submission-detail">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <CButton color="secondary" onClick={() => navigate(-1)}>
                    <CIcon icon={cilArrowLeft} className="me-2" />
                    {t('common.back')}
                </CButton>
                <div className="d-flex gap-2">
                    {!summary && !keyPoints && (
                        <CButton
                            color="primary"
                            onClick={handleGenerateAIInsights}
                            disabled={aiLoading}
                        >
                            {aiLoading ? <CSpinner size="sm" className="me-2" /> : <CIcon icon={cilStar} className="me-2" />}
                            {t('ai.extractKeyPoints') || 'AI Insights'}
                        </CButton>
                    )}
                    <CButton color="info" className="text-white" onClick={handleDownloadPdf}>
                        <CIcon icon={cilCloudDownload} className="me-2" />
                        Download PDF
                    </CButton>
                </div>
            </div>

            {error && (
                <CAlert color="danger" className="mb-4" onClose={() => setError('')} dismissible>
                    {error}
                </CAlert>
            )}

            <CRow>
                <CCol md={8}>
                    <CCard className="mb-4 shadow-sm border-0">
                        <CCardHeader className="py-3">
                            <h4 className="mb-1 text-white">{submission.title}</h4>
                            <div className="d-flex gap-2 mt-2">
                                {submission.keywords?.split(',').map((kw, i) => (
                                    <span key={i} className="badge bg-light text-dark border">{kw.trim()}</span>
                                ))}
                            </div>
                        </CCardHeader>
                        <CCardBody className="py-4">
                            <h6 className="text-uppercase text-muted fw-bold small mb-3">Abstract</h6>
                            <div className="lh-lg" style={{ textAlign: 'justify' }}>
                                {submission.abstractText}
                            </div>
                        </CCardBody>
                    </CCard>
                </CCol>

                <CCol md={4}>
                    {aiLoading && (
                        <div className="text-center p-4">
                            <CSpinner color="primary" className="mb-2" />
                            <p className="text-muted small">{t('ai.processing') || 'AI đang phân tích...'}</p>
                        </div>
                    )}

                    {summary && summary.success && (
                        <AINeuralSummary
                            summary={summary.summary}
                            wordCount={summary.wordCount}
                        />
                    )}

                    {keyPoints && keyPoints.success && (
                        <AIKeyPoints
                            claims={keyPoints.claims}
                            methods={keyPoints.methods}
                            findings={keyPoints.findings}
                            datasets={keyPoints.datasets}
                        />
                    )}

                    <CCard className="border-0 shadow-sm">
                        <CCardHeader className="bg-white">
                            <h6 className="mb-0 fw-bold">Reviewer Actions</h6>
                        </CCardHeader>
                        <CCardBody>
                            <CButton
                                color="primary"
                                className="w-100 mb-2"
                                onClick={() => {
                                    const path = assignment
                                        ? `/app/pc/reviews/new?submissionId=${submission.id}&assignmentId=${assignment.id}`
                                        : `/app/pc/reviews/new?submissionId=${submission.id}`;
                                    navigate(path);
                                }}
                                disabled={!assignment || assignment.status === 'ASSIGNED'}
                                title={!assignment || assignment.status === 'ASSIGNED' ? 'Bạn cần chấp nhận assignment trước khi đánh giá' : ''}
                            >
                                Write Review
                            </CButton>
                            <CButton
                                color="outline-secondary"
                                className="w-100"
                                onClick={handleJoinDiscussion}
                            >
                                <CIcon icon={cilCommentSquare} className="me-2" />
                                Join Discussion
                            </CButton>
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>
        </div>
    )
}

export default ReviewerSubmissionDetail
