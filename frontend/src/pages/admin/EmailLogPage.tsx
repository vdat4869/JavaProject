import React, { useState, useEffect } from 'react'
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
    CButton,
    CSpinner,
    CAlert,
    CBadge,
    CModal,
    CModalHeader,
    CModalTitle,
    CModalBody,
    CModalFooter,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilEnvelopeOpen, cilReload, cilWarning, cilCheckCircle } from '@coreui/icons'
import { notificationService, NotificationLog } from '../../services/notification.service'

/**
 * EmailLogPage - Trang theo dõi nhật ký gửi email
 */
const EmailLogPage: React.FC = () => {
    const [logs, setLogs] = useState<NotificationLog[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [selectedLog, setSelectedLog] = useState<NotificationLog | null>(null)
    const [showModal, setShowModal] = useState(false)

    useEffect(() => {
        loadLogs()
    }, [])

    const loadLogs = async () => {
        try {
            setLoading(true)
            const data = await notificationService.getLogs()
            // Sắp xếp theo thời gian mới nhất
            setLogs(data.sort((a, b) => new Date(b.sentAt).getTime() - new Date(a.sentAt).getTime()))
        } catch (err: any) {
            setError('Không thể tải nhật ký thông báo.')
        } finally {
            setLoading(false)
        }
    }

    const handleShowDetail = (log: NotificationLog) => {
        setSelectedLog(log)
        setShowModal(true)
    }

    const getTypeBadge = (type: string) => {
        const colorMap: Record<string, string> = {
            DECISION_ACCEPT: 'success',
            DECISION_REJECT: 'danger',
            DECISION_CONDITIONAL_ACCEPT: 'warning',
            REVIEW_REQUEST: 'info',
            DEADLINE_REMINDER: 'primary',
        }
        return <CBadge color={colorMap[type] || 'secondary'}>{type}</CBadge>
    }

    const getStatusBadge = (status: string) => {
        if (status === 'SENT') return <CBadge color="success"><CIcon icon={cilCheckCircle} size="sm" /> SENT</CBadge>
        if (status === 'FAILED') return <CBadge color="danger"><CIcon icon={cilWarning} size="sm" /> FAILED</CBadge>
        return <CBadge color="warning">{status}</CBadge>
    }

    if (loading && logs.length === 0) return <div className="text-center p-5"><CSpinner color="primary" /></div>

    return (
        <div className="container-fluid">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h3>Nhật ký Email & Thông báo</h3>
                <CButton color="info" onClick={loadLogs} disabled={loading}>
                    <CIcon icon={cilReload} className="me-2" /> Làm mới
                </CButton>
            </div>

            {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}

            <CCard>
                <CCardHeader>Lịch sử gửi tin</CCardHeader>
                <CCardBody>
                    <CTable hover responsive align="middle">
                        <CTableHead>
                            <CTableRow>
                                <CTableHeaderCell>Thời gian</CTableHeaderCell>
                                <CTableHeaderCell>Người nhận (ID)</CTableHeaderCell>
                                <CTableHeaderCell>Loại</CTableHeaderCell>
                                <CTableHeaderCell>Tiêu đề</CTableHeaderCell>
                                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                                <CTableHeaderCell>Thao tác</CTableHeaderCell>
                            </CTableRow>
                        </CTableHead>
                        <CTableBody>
                            {logs.map((log) => (
                                <CTableRow key={log.id}>
                                    <CTableDataCell>
                                        <small>{new Date(log.sentAt).toLocaleString('vi-VN')}</small>
                                    </CTableDataCell>
                                    <CTableDataCell>User #{log.userId}</CTableDataCell>
                                    <CTableDataCell>{getTypeBadge(log.type)}</CTableDataCell>
                                    <CTableDataCell className="text-truncate" style={{ maxWidth: '250px' }}>
                                        {log.subject}
                                    </CTableDataCell>
                                    <CTableDataCell>{getStatusBadge(log.status)}</CTableDataCell>
                                    <CTableDataCell>
                                        <CButton color="primary" variant="ghost" size="sm" onClick={() => handleShowDetail(log)}>
                                            <CIcon icon={cilEnvelopeOpen} /> Xem chi tiết
                                        </CButton>
                                    </CTableDataCell>
                                </CTableRow>
                            ))}
                            {logs.length === 0 && (
                                <CTableRow>
                                    <CTableDataCell colSpan={6} className="text-center text-muted p-4">
                                        Chưa có nhật ký thông báo nào.
                                    </CTableDataCell>
                                </CTableRow>
                            )}
                        </CTableBody>
                    </CTable>
                </CCardBody>
            </CCard>

            {/* Detail Modal */}
            <CModal visible={showModal} onClose={() => setShowModal(false)} size="lg">
                <CModalHeader>
                    <CModalTitle>Chi tiết Email</CModalTitle>
                </CModalHeader>
                <CModalBody>
                    {selectedLog && (
                        <div>
                            <div className="mb-3 border-bottom pb-2">
                                <strong>Tiêu đề:</strong> {selectedLog.subject}
                            </div>
                            <div className="mb-3 border-bottom pb-2">
                                <strong>Người nhận:</strong> User #{selectedLog.userId} | <strong>Submission:</strong> {selectedLog.submissionId ? `#${selectedLog.submissionId}` : 'N/A'}
                            </div>
                            <div className="mb-3">
                                <strong>Nội dung:</strong>
                                <div
                                    className="p-3 bg-light border rounded mt-2"
                                    style={{ whiteSpace: 'pre-wrap', maxHeight: '400px', overflowY: 'auto' }}
                                    dangerouslySetInnerHTML={{ __html: selectedLog.content.replace(/\n/g, '<br>') }}
                                />
                            </div>
                        </div>
                    )}
                </CModalBody>
                <CModalFooter>
                    <CButton color="secondary" onClick={() => setShowModal(false)}>Đóng</CButton>
                </CModalFooter>
            </CModal>
        </div>
    )
}

export default EmailLogPage
