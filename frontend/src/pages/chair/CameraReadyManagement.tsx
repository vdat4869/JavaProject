import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CTable,
    CTableHead,
    CTableRow,
    CTableHeaderCell,
    CTableBody,
    CTableDataCell,
    CBadge,
    CButton,
    CSpinner,
    CAlert,
    CModal,
    CModalHeader,
    CModalTitle,
    CModalBody,
    CModalFooter,
    CFormTextarea,
    CFormSelect,
    CRow,
    CCol,
    CWidgetStatsC,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilFile, cilCheckCircle, cilXCircle, cilCloudDownload, cilChartPie } from '@coreui/icons'
import {
    cameraReadyService,
    CameraReadySubmissionListItem,
    CameraReadyStatistics,
    ReviewDecision,
} from '../../services/camera-ready.service'

/**
 * CameraReadyManagement - Dashboard quản lý Camera-ready dành cho Chair
 */
const CameraReadyManagement: React.FC = () => {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const conferenceId = searchParams.get('conferenceId')

    const [submissions, setSubmissions] = useState<CameraReadySubmissionListItem[]>([])
    const [stats, setStats] = useState<CameraReadyStatistics | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // Modal review state
    const [showReviewModal, setShowReviewModal] = useState(false)
    const [selectedSubmission, setSelectedSubmission] = useState<CameraReadySubmissionListItem | null>(null)
    const [decision, setDecision] = useState<ReviewDecision>('APPROVE')
    const [note, setNote] = useState('')
    const [reviewing, setReviewing] = useState(false)

    useEffect(() => {
        if (conferenceId) {
            loadData()
        } else {
            setError('Thiếu tham số conferenceId trong URL.')
            setLoading(false)
        }
    }, [conferenceId])

    const loadData = async () => {
        try {
            setLoading(true)
            const [submissionsData, statsData] = await Promise.all([
                cameraReadyService.listSubmissions(conferenceId!),
                cameraReadyService.getStatistics(conferenceId!)
            ])
            setSubmissions(submissionsData)
            setStats(statsData)
        } catch (err: any) {
            setError('Không thể tải dữ liệu camera-ready. Vui lòng kiểm tra quyền truy cập.')
        } finally {
            setLoading(false)
        }
    }

    const handleOpenReview = (sub: CameraReadySubmissionListItem) => {
        setSelectedSubmission(sub)
        setDecision('APPROVE')
        setNote('')
        setShowReviewModal(true)
    }

    const handleReview = async () => {
        if (!selectedSubmission || !conferenceId) return

        try {
            setReviewing(true)
            await cameraReadyService.reviewSubmission(conferenceId, selectedSubmission.id, {
                decision,
                note
            })
            setShowReviewModal(false)
            loadData()
        } catch (err: any) {
            alert('Lỗi khi lưu kết quả duyệt: ' + (err.response?.data?.message || err.message))
        } finally {
            setReviewing(false)
        }
    }

    const handleExport = () => {
        navigate(`/app/chair/proceedings?conferenceId=${conferenceId}`)
    }

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'APPROVED': return <CBadge color="success">APPROVED</CBadge>
            case 'REJECTED': return <CBadge color="danger">REJECTED</CBadge>
            case 'NEEDS_REVISION': return <CBadge color="warning">REVISION</CBadge>
            default: return <CBadge color="info">PENDING</CBadge>
        }
    }

    if (loading) return <div className="text-center p-5"><CSpinner color="primary" /></div>
    if (error) return <CAlert color="danger">{error}</CAlert>

    return (
        <div className="container-fluid">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h3>Quản lý Camera-Ready</h3>
                <CButton color="primary" onClick={handleExport}>
                    <CIcon icon={cilCloudDownload} className="me-2" /> Xuất bản kỷ yếu (Export)
                </CButton>
            </div>

            {stats && (
                <CRow className="mb-4">
                    <CCol sm={6} lg={3}>
                        <CWidgetStatsC
                            icon={<CIcon icon={cilFile} height={36} />}
                            value={stats.totalAcceptedPapers}
                            title="Tổng bài được chấp nhận"
                            color="primary"
                            inverse
                        />
                    </CCol>
                    <CCol sm={6} lg={3}>
                        <CWidgetStatsC
                            icon={<CIcon icon={cilCheckCircle} height={36} />}
                            value={stats.statistics.byStatus['APPROVED'] || 0}
                            title="Đã phê duyệt"
                            color="success"
                            inverse
                        />
                    </CCol>
                    <CCol sm={6} lg={3}>
                        <CWidgetStatsC
                            icon={<CIcon icon={cilChartPie} height={36} />}
                            value={stats.statistics.copyrightConfirmed}
                            title="Đã ký bản quyền"
                            color="info"
                            inverse
                        />
                    </CCol>
                    <CCol sm={6} lg={3}>
                        <CWidgetStatsC
                            icon={<CIcon icon={cilXCircle} height={36} />}
                            value={stats.statistics.copyrightPending}
                            title="Chưa ký bản quyền"
                            color="warning"
                            inverse
                        />
                    </CCol>
                </CRow>
            )}

            <CCard>
                <CCardHeader>Danh sách bài nộp camera-ready</CCardHeader>
                <CCardBody>
                    <CTable hover responsive align="middle">
                        <CTableHead>
                            <CTableRow>
                                <CTableHeaderCell>Bài báo</CTableHeaderCell>
                                <CTableHeaderCell>Lĩnh vực</CTableHeaderCell>
                                <CTableHeaderCell>Tác giả liên hệ</CTableHeaderCell>
                                <CTableHeaderCell className="text-center">Bản quyền</CTableHeaderCell>
                                <CTableHeaderCell className="text-center">Phiên bản</CTableHeaderCell>
                                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                                <CTableHeaderCell>Thao tác</CTableHeaderCell>
                            </CTableRow>
                        </CTableHead>
                        <CTableBody>
                            {submissions.map((sub) => (
                                <CTableRow key={sub.id}>
                                    <CTableDataCell>
                                        <div className="fw-bold">{sub.paperTitle}</div>
                                        <div className="small text-muted">ID: {sub.paperId}</div>
                                    </CTableDataCell>
                                    <CTableDataCell>{sub.trackName}</CTableDataCell>
                                    <CTableDataCell>
                                        <div>{sub.correspondingAuthor.fullName}</div>
                                        <div className="small text-muted">{sub.correspondingAuthor.email}</div>
                                    </CTableDataCell>
                                    <CTableDataCell className="text-center">
                                        {sub.copyrightConfirmed ?
                                            <CBadge color="success">YES</CBadge> :
                                            <CBadge color="warning">NO</CBadge>
                                        }
                                    </CTableDataCell>
                                    <CTableDataCell className="text-center">v{sub.currentVersionNumber}</CTableDataCell>
                                    <CTableDataCell>{getStatusBadge(sub.status)}</CTableDataCell>
                                    <CTableDataCell>
                                        <div className="d-flex gap-2">
                                            <CButton
                                                color="info"
                                                size="sm"
                                                title="Xem chi tiết và PDF"
                                                onClick={() => navigate(`/app/author/submissions/${sub.paperId}/camera-ready`)} // Tận dụng trang author để xem
                                            >
                                                <CIcon icon={cilFile} />
                                            </CButton>
                                            <CButton
                                                color="success"
                                                size="sm"
                                                title="Duyệt bài"
                                                onClick={() => handleOpenReview(sub)}
                                            >
                                                <CIcon icon={cilCheckCircle} />
                                            </CButton>
                                        </div>
                                    </CTableDataCell>
                                </CTableRow>
                            ))}
                            {submissions.length === 0 && (
                                <CTableRow>
                                    <CTableDataCell colSpan={7} className="text-center text-muted p-4">
                                        Chưa có bài báo nào nộp bản Camera-ready.
                                    </CTableDataCell>
                                </CTableRow>
                            )}
                        </CTableBody>
                    </CTable>
                </CCardBody>
            </CCard>

            {/* Review Modal */}
            <CModal visible={showReviewModal} onClose={() => setShowReviewModal(false)}>
                <CModalHeader>
                    <CModalTitle>Phê duyệt Camera-Ready</CModalTitle>
                </CModalHeader>
                <CModalBody>
                    {selectedSubmission && (
                        <div className="mb-3">
                            <h6>Bài báo: {selectedSubmission.paperTitle}</h6>
                            <p className="small text-muted">Phiên bản hiện tại: v{selectedSubmission.currentVersionNumber}</p>
                        </div>
                    )}
                    <div className="mb-3">
                        <CFormSelect
                            label="Quyết định"
                            value={decision}
                            onChange={(e) => setDecision(e.target.value as ReviewDecision)}
                        >
                            <option value="APPROVE">Phê duyệt (APPROVE)</option>
                            <option value="NEEDS_REVISION">Yêu cầu sửa lại (REVISION)</option>
                            <option value="REJECT">Từ chối (REJECT)</option>
                        </CFormSelect>
                    </div>
                    <div className="mb-3">
                        <CFormTextarea
                            label="Ghi chú / Nhận xét"
                            rows={3}
                            value={note}
                            onChange={(e) => setNote(e.target.value)}
                            placeholder="Nhập nhận xét cho tác giả..."
                        />
                    </div>
                </CModalBody>
                <CModalFooter>
                    <CButton color="secondary" onClick={() => setShowReviewModal(false)}>Hủy</CButton>
                    <CButton color="primary" onClick={handleReview} disabled={reviewing}>
                        {reviewing ? <CSpinner size="sm" /> : 'Lưu kết quả'}
                    </CButton>
                </CModalFooter>
            </CModal>
        </div>
    )
}

export default CameraReadyManagement
