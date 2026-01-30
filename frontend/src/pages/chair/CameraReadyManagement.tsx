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
    CListGroup,
    CListGroupItem,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilFile, cilCheckCircle, cilXCircle, cilCloudDownload, cilChartPie } from '@coreui/icons'
import {
    cameraReadyService,
    CameraReadySubmissionListItem,
    CameraReadyStatistics,
    ReviewDecision,
} from '../../services/camera-ready.service'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'

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
    const [success, setSuccess] = useState('')

    // Conference selection states
    const [myConferences, setMyConferences] = useState<ConferenceResponse[]>([])
    const [isSelectingConference, setIsSelectingConference] = useState(false)

    // Modal review state
    const [showReviewModal, setShowReviewModal] = useState(false)
    const [selectedSubmission, setSelectedSubmission] = useState<CameraReadySubmissionListItem | null>(null)
    const [decision, setDecision] = useState<ReviewDecision>('APPROVED')
    const [note, setNote] = useState('')
    const [reviewing, setReviewing] = useState(false)

    useEffect(() => {
        if (conferenceId) {
            loadData()
        } else {
            loadMyConferences()
        }
    }, [conferenceId])

    const loadMyConferences = async () => {
        try {
            setLoading(true)
            const data = await conferenceService.getMyConferences()
            if (data.length === 1) {
                // Auto select if only one
                navigate(`?conferenceId=${data[0].id}`, { replace: true })
            } else {
                setMyConferences(data)
                setIsSelectingConference(true)
                setLoading(false)
            }
        } catch (error) {
            console.error('Error loading conferences:', error)
            // setError('Không thể tải danh sách hội nghị') // Don't show error immediately
            setLoading(false)
        }
    }

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

    // Open Camera-Ready states
    const [showOpenModal, setShowOpenModal] = useState(false)
    const [openDeadline, setOpenDeadline] = useState('')
    const [opening, setOpening] = useState(false)

    // ... existing useEffect ...

    // ... existing loadMyConferences ...

    // ... existing loadData ...

    const handleOpenCameraReady = async () => {
        if (!conferenceId) return
        try {
            setOpening(true)
            await cameraReadyService.openCameraReady(conferenceId.toString(), openDeadline ? openDeadline : undefined)
            setSuccess('Đã mở nộp Camera-Ready thành công!')
            setShowOpenModal(false)
            loadData()
        } catch (err: any) {
            alert('Lỗi khi mở Camera-Ready: ' + (err.response?.data?.message || err.message))
        } finally {
            setOpening(false)
        }
    }

    const handleExport = () => {
        navigate(`/app/chair/proceedings?conferenceId=${conferenceId}`)
    }

    const handleOpenReview = (submission: CameraReadySubmissionListItem) => {
        setSelectedSubmission(submission)
        setDecision('APPROVED')
        setNote('')
        setShowReviewModal(true)
    }

    const handleReview = async () => {
        if (!selectedSubmission || !conferenceId) return
        try {
            setReviewing(true)
            await cameraReadyService.reviewSubmission(conferenceId.toString(), selectedSubmission.id.toString(), {
                decision,
                note
            })
            setSuccess('Đã lưu kết quả đánh giá')
            setShowReviewModal(false)
            loadData()
        } catch (err: any) {
            alert('Lỗi: ' + (err.response?.data?.message || err.message))
        } finally {
            setReviewing(false)
        }
    }

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'APPROVED': return <CBadge color="success">Đã duyệt</CBadge>
            case 'REQUEST_CHANGES': return <CBadge color="warning">Yêu cầu sửa</CBadge>
            case 'SUBMITTED': return <CBadge color="info">Đã nộp</CBadge>
            default: return <CBadge color="secondary">{status}</CBadge>
        }
    }

    if (!conferenceId) {
        if (loading) {
            return (
                <div className="d-flex justify-content-center p-5">
                    <CSpinner color="primary" />
                </div>
            )
        }

        if (isSelectingConference) {
            return (
                <div className="container-fluid">
                    <CCard className="mx-auto shadow-sm" style={{ maxWidth: '800px' }}>
                        <CCardHeader className="bg-primary text-white">
                            <h4 className="mb-0">Chọn hội nghị để quản lý Camera-Ready</h4>
                        </CCardHeader>
                        <CCardBody className="p-4">
                            {myConferences.length === 0 ? (
                                <CAlert color="warning">Bạn chưa được gán vào hội nghị nào với quyền Chair.</CAlert>
                            ) : (
                                <CListGroup flush>
                                    {myConferences.map(conf => (
                                        <CListGroupItem
                                            key={conf.id}
                                            as="button"
                                            className="d-flex justify-content-between align-items-center list-group-item-action py-3"
                                            onClick={() => navigate(`?conferenceId=${conf.id}`)}
                                        >
                                            <span className="fw-semibold">{conf.name}</span>
                                            <CBadge color="primary" shape="rounded-pill">ID: {conf.id}</CBadge>
                                        </CListGroupItem>
                                    ))}
                                </CListGroup>
                            )}
                        </CCardBody>
                    </CCard>
                </div>
            )
        }

        return (
            <div className="container-fluid">
                <CAlert color="danger">Thiếu conferenceId và không thể tải danh sách hội nghị.</CAlert>
            </div>
        )
    }

    if (loading) return (
        <div className="d-flex justify-content-center p-5">
            <CSpinner color="primary" />
        </div>
    )

    return (
        <div className="container-fluid">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h3>Quản lý Camera-Ready</h3>
                <div className="d-flex gap-2">
                    <CButton color="success" onClick={() => setShowOpenModal(true)}>
                        <CIcon icon={cilCheckCircle} className="me-2" /> Mở nộp Camera-Ready
                    </CButton>
                    <CButton color="secondary" variant="outline" onClick={() => navigate('/app/chair/conferences')}>
                        Đổi hội nghị
                    </CButton>
                    <CButton color="primary" onClick={handleExport}>
                        <CIcon icon={cilCloudDownload} className="me-2" /> Xuất bản kỷ yếu (Export)
                    </CButton>
                </div>
            </div>

            {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
            {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

            {/* ... stats ... */}

            {/* ... table ... */}

            {/* Review Modal */}
            {/* ... */}

            {/* Open Camera-Ready Modal */}
            <CModal visible={showOpenModal} onClose={() => setShowOpenModal(false)}>
                <CModalHeader>
                    <CModalTitle>Mở nộp Camera-Ready</CModalTitle>
                </CModalHeader>
                <CModalBody>
                    <p>Bạn sắp mở chức năng nộp bản Camera-Ready cho các bài báo đã được chấp nhận (Accepted).</p>
                    <div className="mb-3">
                        <label className="form-label">Hạn chót (Deadline)</label>
                        <input
                            type="datetime-local"
                            className="form-control"
                            value={openDeadline}
                            onChange={(e) => setOpenDeadline(e.target.value)}
                        />
                        <div className="form-text">Nếu để trống, sẽ không có hạn chót trên hệ thống (nhưng bạn vẫn nên đặt).</div>
                    </div>
                </CModalBody>
                <CModalFooter>
                    <CButton color="secondary" onClick={() => setShowOpenModal(false)}>Hủy</CButton>
                    <CButton color="success" onClick={handleOpenCameraReady} disabled={opening}>
                        {opening ? <CSpinner size="sm" /> : 'Xác nhận mở'}
                    </CButton>
                </CModalFooter>
            </CModal>


            {
                stats && (
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
                )
            }

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
                                        <div>{sub.correspondingAuthor?.fullName || 'N/A'}</div>
                                        <div className="small text-muted">{sub.correspondingAuthor?.email}</div>
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
                                                onClick={() => navigate(`/app/chair/camera-ready/${sub.paperId}?conferenceId=${conferenceId}`)}
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
                            <option value="APPROVED">Phê duyệt (APPROVED)</option>
                            <option value="NEED_FIX">Yêu cầu sửa lại (NEED_FIX)</option>
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
        </div >
    )
}

export default CameraReadyManagement
