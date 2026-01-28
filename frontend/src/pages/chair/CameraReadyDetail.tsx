import React, { useState, useEffect } from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CButton,
    CAlert,
    CSpinner,
    CTable,
    CTableHead,
    CTableRow,
    CTableHeaderCell,
    CTableBody,
    CTableDataCell,
    CBadge,
    CRow,
    CCol,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilArrowLeft, cilCloudDownload, cilCheckCircle, cilFile } from '@coreui/icons'
import {
    cameraReadyService,
    CameraReadySubmission,
    CameraReadyVersion,
} from '../../services/camera-ready.service'
import { submissionService } from '../../services/submission.service'

/**
 * CameraReadyDetail - Trang xem chi tiết Camera-ready cho CHAIR (chỉ đọc)
 */
const CameraReadyDetail: React.FC = () => {
    const { paperId } = useParams<{ paperId: string }>()
    const [searchParams] = useSearchParams()
    const conferenceIdParam = searchParams.get('conferenceId')
    const navigate = useNavigate()

    const [submission, setSubmission] = useState<CameraReadySubmission | null>(null)
    const [versions, setVersions] = useState<CameraReadyVersion[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        if (paperId) {
            loadData()
        }
    }, [paperId])

    const loadData = async () => {
        try {
            setLoading(true)
            let conferenceId = conferenceIdParam

            if (!conferenceId) {
                // Lấy conferenceId từ submission gốc nếu không có trong URL
                const legacySub = await submissionService.getSubmission(parseInt(paperId!))
                conferenceId = legacySub.conferenceId.toString()
            }

            const data = await cameraReadyService.getSubmission(conferenceId!, paperId!)
            if (data) {
                setSubmission(data)
            } else {
                throw new Error('Không tìm thấy dữ liệu Camera-Ready')
            }

            const versionsData = await cameraReadyService.listVersions(conferenceId!, paperId!)
            setVersions(versionsData)
        } catch (error: any) {
            console.error('Error loading camera-ready data:', error)
            setError(error.response?.data?.message || 'Không thể tải dữ liệu camera-ready.')
        } finally {
            setLoading(false)
        }
    }

    const handleDownload = async (versionId: string, filename: string) => {
        if (!submission) return
        try {
            const blob = await cameraReadyService.downloadVersion(
                submission.conferenceId,
                submission.paperId,
                versionId
            )
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.setAttribute('download', filename)
            document.body.appendChild(link)
            link.click()
            link.remove()
        } catch (error) {
            setError('Không thể tải xuống file')
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
        return (
            <div className="container-lg mt-4">
                <CAlert color="danger">
                    {error || 'Không tìm thấy thông tin camera-ready cho bài báo này.'}
                </CAlert>
                <CButton color="secondary" onClick={() => navigate(-1)}>
                    <CIcon icon={cilArrowLeft} className="me-2" /> Quay lại
                </CButton>
            </div>
        )
    }

    return (
        <div className="container-lg">
            <CCard className="mb-4">
                <CCardHeader>
                    <div className="d-flex justify-content-between align-items-center">
                        <h4>Chi tiết Camera-Ready: {submission.paperTitle || `Paper #${submission.paperId}`}</h4>
                        <CButton color="secondary" size="sm" onClick={() => navigate(-1)}>
                            <CIcon icon={cilArrowLeft} className="me-2" /> Quay lại
                        </CButton>
                    </div>
                </CCardHeader>
                <CCardBody>
                    {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}

                    <CRow className="mb-4">
                        <CCol md={6}>
                            <h5>Thông tin chung</h5>
                            <table className="table table-borderless">
                                <tbody>
                                    <tr>
                                        <td><strong>Trạng thái:</strong></td>
                                        <td><CBadge color={submission.status === 'APPROVED' ? 'success' : submission.status === 'NEEDS_REVISION' ? 'warning' : 'info'}>{submission.status}</CBadge></td>
                                    </tr>
                                    <tr>
                                        <td><strong>Lĩnh vực:</strong></td>
                                        <td>{submission.trackName || 'N/A'}</td>
                                    </tr>
                                    <tr>
                                        <td><strong>Hạn nộp:</strong></td>
                                        <td><span className="text-danger">{submission.deadline ? new Date(submission.deadline).toLocaleString('vi-VN') : 'Không giới hạn'}</span></td>
                                    </tr>
                                    <tr>
                                        <td><strong>Bản quyền:</strong></td>
                                        <td>{submission.copyrightConfirmed ? <CBadge color="success">Đã xác nhận</CBadge> : <CBadge color="warning">Chưa xác nhận</CBadge>}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </CCol>
                        <CCol md={6}>
                            <h5>Phiên bản hiện tại</h5>
                            {submission.currentVersion ? (
                                <div className="p-3 bg-light rounded">
                                    <p className="mb-1"><strong>File:</strong> {submission.currentVersion.originalFilename}</p>
                                    <p className="mb-1"><strong>Kích thước:</strong> {((submission.currentVersion.fileSizeBytes || 0) / 1024 / 1024).toFixed(2)} MB</p>
                                    <p className="mb-1"><strong>Số trang:</strong> {submission.currentVersion.pageCount || 'N/A'}</p>
                                    <p className="mb-2"><strong>Validation:</strong> {submission.currentVersion.validationPassed ? <CBadge color="success">Hợp lệ</CBadge> : <CBadge color="danger">Lỗi</CBadge>}</p>
                                    <CButton color="primary" size="sm" onClick={() => handleDownload(submission.currentVersion!.id, submission.currentVersion!.originalFilename)}>
                                        <CIcon icon={cilCloudDownload} className="me-1" /> Tải PDF
                                    </CButton>
                                </div>
                            ) : (
                                <p className="text-muted">Chưa có phiên bản nào được nộp.</p>
                            )}
                        </CCol>
                    </CRow>

                    <hr />

                    <h5>Lịch sử các phiên bản</h5>
                    {versions.length === 0 ? (
                        <p className="text-muted">Chưa có phiên bản nào.</p>
                    ) : (
                        <CTable hover responsive align="middle">
                            <CTableHead>
                                <CTableRow>
                                    <CTableHeaderCell>#</CTableHeaderCell>
                                    <CTableHeaderCell>Tên file</CTableHeaderCell>
                                    <CTableHeaderCell>Kích thước</CTableHeaderCell>
                                    <CTableHeaderCell>Thời gian nộp</CTableHeaderCell>
                                    <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                                    <CTableHeaderCell>Thao tác</CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {versions.map((v) => (
                                    <CTableRow key={v.id} active={v.isCurrent}>
                                        <CTableDataCell>{v.versionNumber}</CTableDataCell>
                                        <CTableDataCell>{v.originalFilename}</CTableDataCell>
                                        <CTableDataCell>{(v.fileSizeBytes / 1024 / 1024).toFixed(2)} MB</CTableDataCell>
                                        <CTableDataCell>{new Date(v.uploadedAt).toLocaleString('vi-VN')}</CTableDataCell>
                                        <CTableDataCell>
                                            {v.isCurrent && <CBadge color="primary" className="me-1">Hiện tại</CBadge>}
                                            {v.validationPassed ? <CBadge color="success">Hợp lệ</CBadge> : <CBadge color="danger">Lỗi</CBadge>}
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <CButton color="info" size="sm" onClick={() => handleDownload(v.id, v.originalFilename)}>
                                                <CIcon icon={cilCloudDownload} /> Tải
                                            </CButton>
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                            </CTableBody>
                        </CTable>
                    )}
                </CCardBody>
            </CCard>
        </div>
    )
}

export default CameraReadyDetail
