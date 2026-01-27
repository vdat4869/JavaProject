import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CButton,
    CRow,
    CCol,
    CSpinner,
    CAlert,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow,
    CBadge,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilUserPlus, cilStar, cilList } from '@coreui/icons'
import { submissionService, Submission } from '../../services/submission.service'
import { assignmentService, Assignment, AutoAssignResponse } from '../../services/assignment.service'
import ManualAssignmentForm from '../../components/assignment/ManualAssignmentForm'
import AutoAssignWithSuggestions from '../../components/assignment/AutoAssignWithSuggestions'

/**
 * SubmissionBoard - Bảng quản lý bài nộp dành cho Chair
 * Cho phép xem danh sách bài nộp và thực hiện phân công nhanh
 */
const SubmissionBoard: React.FC = () => {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const conferenceId = searchParams.get('conferenceId')
        ? parseInt(searchParams.get('conferenceId')!)
        : null

    const [submissions, setSubmissions] = useState<Submission[]>([])
    const [submissionAssignments, setSubmissionAssignments] = useState<Record<number, Assignment[]>>({})
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    // Modal states
    const [showManualModal, setShowManualModal] = useState(false)
    const [showAutoModal, setShowAutoModal] = useState(false)
    const [selectedSubId, setSelectedSubId] = useState<number | null>(null)

    useEffect(() => {
        if (conferenceId) {
            loadData()
        }
    }, [conferenceId])

    const loadData = async () => {
        try {
            setLoading(true)
            setError('')

            // 1. Tải danh sách submissions
            const subData = await submissionService.getSubmissionsByConference(conferenceId!)
            setSubmissions(subData)

            // 2. Tải assignments cho từng submission (có thể tối ưu sau bằng batch API)
            const assignmentPromises = subData.map(async (sub) => {
                try {
                    const assignments = await assignmentService.getAssignmentsBySubmission(sub.id)
                    return { id: sub.id, assignments }
                } catch (err) {
                    return { id: sub.id, assignments: [] }
                }
            })

            const assignmentResults = await Promise.all(assignmentPromises)
            const assignmentMap: Record<number, Assignment[]> = {}
            assignmentResults.forEach((res) => {
                assignmentMap[res.id] = res.assignments
            })
            setSubmissionAssignments(assignmentMap)
        } catch (error: any) {
            console.error('Error loading submission board:', error)
            setError('Không thể tải danh sách bài nộp')
        } finally {
            setLoading(false)
        }
    }

    const handleManualAssign = (subId: number) => {
        setSelectedSubId(subId)
        setShowManualModal(true)
    }

    const handleAutoAssign = (subId: number) => {
        setSelectedSubId(subId)
        setShowAutoModal(true)
    }

    const handleAssignmentSuccess = () => {
        setSuccess('Phân công thành công')
        loadData() // Refresh data
    }

    const handleAutoSuccess = (result: AutoAssignResponse) => {
        setSuccess(`Đã tạo tự động ${result.totalCreated} assignments`)
        loadData()
    }

    const getStatusBadge = (status: string) => {
        const colorMap: Record<string, string> = {
            SUBMITTED: 'info',
            UNDER_REVIEW: 'warning',
            REVIEWED: 'primary',
            ACCEPTED: 'success',
            REJECTED: 'danger',
            WITHDRAWN: 'dark',
        }
        return <CBadge color={colorMap[status] || 'secondary'}>{status}</CBadge>
    }

    if (!conferenceId) {
        return (
            <CAlert color="danger">Thiếu conferenceId trên URL</CAlert>
        )
    }

    if (loading) {
        return (
            <div className="text-center p-5">
                <CSpinner color="primary" />
                <p className="mt-2 text-muted">Đang tải danh sách bài nộp...</p>
            </div>
        )
    }

    return (
        <>
            <CCard className="mb-4">
                <CCardHeader className="d-flex justify-content-between align-items-center">
                    <div>
                        <h4 className="mb-0">Submission Board</h4>
                        <small className="text-muted">Quản lý và phân công reviewer tập trung</small>
                    </div>
                    <CButton color="secondary" onClick={() => navigate(-1)}>Quay lại</CButton>
                </CCardHeader>
                <CCardBody>
                    {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
                    {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

                    <CTable hover responsive align="middle">
                        <CTableHead color="light">
                            <CTableRow>
                                <CTableHeaderCell style={{ width: '80px' }}>ID</CTableHeaderCell>
                                <CTableHeaderCell>Tiêu đề bài báo</CTableHeaderCell>
                                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                                <CTableHeaderCell>Lĩnh vực</CTableHeaderCell>
                                <CTableHeaderCell>Reviewers (Accepted)</CTableHeaderCell>
                                <CTableHeaderCell style={{ width: '180px' }}>Hành động</CTableHeaderCell>
                            </CTableRow>
                        </CTableHead>
                        <CTableBody>
                            {submissions.length === 0 ? (
                                <CTableRow>
                                    <CTableDataCell colSpan={6} className="text-center text-muted py-4">
                                        Chưa có bài nộp nào cho hội nghị này.
                                    </CTableDataCell>
                                </CTableRow>
                            ) : (
                                submissions.map((sub) => {
                                    const assignments = submissionAssignments[sub.id] || []
                                    const acceptedCount = assignments.filter(a => a.status === 'ACCEPTED' || a.status === 'COMPLETED').length
                                    const totalCount = assignments.length

                                    return (
                                        <CTableRow key={sub.id}>
                                            <CTableDataCell><strong>#{sub.id}</strong></CTableDataCell>
                                            <CTableDataCell>
                                                <div className="fw-semibold text-primary" style={{ cursor: 'pointer' }} onClick={() => navigate(`/app/author/submissions/${sub.id}`)}>
                                                    {sub.title}
                                                </div>
                                                <small className="text-muted d-block mt-1">
                                                    {sub.authors?.map(a => `${a.firstName} ${a.lastName}`).join(', ')}
                                                </small>
                                            </CTableDataCell>
                                            <CTableDataCell>{getStatusBadge(sub.status)}</CTableDataCell>
                                            <CTableDataCell>{sub.trackName || 'N/A'}</CTableDataCell>
                                            <CTableDataCell>
                                                <CBadge color={acceptedCount < 3 ? 'warning' : 'success'} shape="rounded-pill">
                                                    {acceptedCount} / {totalCount} (Total)
                                                </CBadge>
                                                <div className="small text-muted mt-1">
                                                    {assignments.length > 0 ? assignments.map(a => a.reviewerName).join(', ') : 'Chưa phân công'}
                                                </div>
                                            </CTableDataCell>
                                            <CTableDataCell>
                                                <div className="d-flex gap-2">
                                                    <CButton color="info" size="sm" variant="outline" title="Manual Assign" onClick={() => handleManualAssign(sub.id)}>
                                                        <CIcon icon={cilUserPlus} />
                                                    </CButton>
                                                    <CButton color="primary" size="sm" variant="outline" title="Auto Assign" onClick={() => handleAutoAssign(sub.id)}>
                                                        <CIcon icon={cilStar} />
                                                    </CButton>
                                                    <CButton color="secondary" size="sm" variant="outline" title="Chi tiết" onClick={() => navigate(`/app/author/submissions/${sub.id}`)}>
                                                        <CIcon icon={cilList} />
                                                    </CButton>
                                                </div>
                                            </CTableDataCell>
                                        </CTableRow>
                                    )
                                })
                            )}
                        </CTableBody>
                    </CTable>
                </CCardBody>
            </CCard>

            {/* Manual Assign Modal */}
            {selectedSubId && (
                <ManualAssignmentForm
                    visible={showManualModal}
                    onClose={() => {
                        setShowManualModal(false)
                        setSelectedSubId(null)
                    }}
                    submissionId={selectedSubId}
                    conferenceId={conferenceId!}
                    onSuccess={handleAssignmentSuccess}
                />
            )}

            {/* Auto Assign Modal */}
            {selectedSubId && (
                <AutoAssignWithSuggestions
                    visible={showAutoModal}
                    onClose={() => {
                        setShowAutoModal(false)
                        setSelectedSubId(null)
                    }}
                    submissionId={selectedSubId}
                    onSuccess={handleAutoSuccess}
                />
            )}
        </>
    )
}

export default SubmissionBoard
