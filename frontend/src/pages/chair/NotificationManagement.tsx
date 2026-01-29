import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow,
    CButton,
    CSpinner,
    CAlert,
    CBadge,
    CModal,
    CModalBody,
    CModalFooter,
    CModalHeader,
    CModalTitle,
    CFormInput,
    CFormTextarea,
    CFormLabel,
    CFormCheck,
    CFormSelect,
} from '@coreui/react'
import {
    decisionService,
    Decision,
    BulkNotificationRequest,
} from '../../services/decision.service'
import { aiService } from '../../services/ai.service'

/**
 * NotificationManagement - Quản lý gửi notifications
 *
 * Features:
 * - Hiển thị pending notifications
 * - Gửi notification đơn
 * - Gửi bulk notifications
 * - Preview email trước khi gửi
 */
const NotificationManagement: React.FC = () => {
    const [searchParams] = useSearchParams()
    const conferenceId = searchParams.get('conferenceId')
        ? parseInt(searchParams.get('conferenceId')!)
        : null

    const [pendingNotifications, setPendingNotifications] = useState<Decision[]>([])
    const [loading, setLoading] = useState(true)
    const [selectedIds, setSelectedIds] = useState<number[]>([])
    const [showBulkModal, setShowBulkModal] = useState(false)
    const [notificationType, setNotificationType] = useState('DECISION_ACCEPT')
    const [customSubject, setCustomSubject] = useState('')
    const [customMessage, setCustomMessage] = useState('')
    const [sending, setSending] = useState(false)
    const [sendingSingle, setSendingSingle] = useState<number | null>(null)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    useEffect(() => {
        loadPendingNotifications()
    }, [])

    const loadPendingNotifications = async () => {
        try {
            setLoading(true)
            const data = await decisionService.getPendingNotifications()
            // Filter theo conference nếu có
            const filtered = conferenceId
                ? data.filter((d) => d.submissionId) // TODO: cần thêm conferenceId vào Decision
                : data
            setPendingNotifications(filtered)
        } catch (error) {
            console.error('Error loading pending notifications:', error)
            setError('Không thể tải danh sách pending notifications')
        } finally {
            setLoading(false)
        }
    }

    const handleSendSingle = async (decisionId: number) => {
        try {
            setSendingSingle(decisionId)
            setError('')
            await decisionService.sendNotification(decisionId)
            setSuccess('Đã gửi notification thành công')
            await loadPendingNotifications()
        } catch (error: any) {
            setError(error.response?.data?.message || 'Không thể gửi notification')
        } finally {
            setSendingSingle(null)
        }
    }

    const handleSendBulk = async () => {
        if (selectedIds.length === 0) {
            setError('Vui lòng chọn ít nhất một decision')
            return
        }

        try {
            setSending(true)
            setError('')

            // Lấy submissionIds từ selectedIds (decisionIds)
            const submissionIds = pendingNotifications
                .filter((d) => selectedIds.includes(d.id))
                .map((d) => d.submissionId)

            const request: BulkNotificationRequest = {
                submissionIds,
                notificationType,
                customSubject: customSubject.trim() || undefined,
                customMessage: customMessage.trim() || undefined,
            }

            await decisionService.sendBulkNotifications(request)
            setSuccess(`Đã gửi ${selectedIds.length} notifications thành công`)
            setShowBulkModal(false)
            setSelectedIds([])
            setCustomSubject('')
            setCustomMessage('')
            await loadPendingNotifications()
        } catch (error: any) {
            setError(error.response?.data?.message || 'Không thể gửi bulk notifications')
        } finally {
            setSending(false)
        }
    }

    const handleSelectAll = () => {
        if (selectedIds.length === pendingNotifications.length) {
            setSelectedIds([])
        } else {
            setSelectedIds(pendingNotifications.map((d) => d.id))
        }
    }

    const handleSelectOne = (id: number) => {
        if (selectedIds.includes(id)) {
            setSelectedIds(selectedIds.filter((i) => i !== id))
        } else {
            setSelectedIds([...selectedIds, id])
        }
    }

    const getDecisionBadge = (type: string) => {
        const colorMap: Record<string, string> = {
            ACCEPT: 'success',
            REJECT: 'danger',
            CONDITIONAL_ACCEPT: 'warning',
        }
        const labelMap: Record<string, string> = {
            ACCEPT: 'Chấp nhận',
            REJECT: 'Từ chối',
            CONDITIONAL_ACCEPT: 'Có điều kiện',
        }
        return <CBadge color={colorMap[type] || 'secondary'}>{labelMap[type] || type}</CBadge>
    }

    if (loading) {
        return (
            <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
            </div>
        )
    }

    return (
        <>
            <CCard>
                <CCardHeader>
                    <div className="d-flex justify-content-between align-items-center">
                        <h4>Quản lý Notifications</h4>
                        {selectedIds.length > 0 && (
                            <CButton color="primary" onClick={() => setShowBulkModal(true)}>
                                Gửi hàng loạt ({selectedIds.length})
                            </CButton>
                        )}
                    </div>
                </CCardHeader>
                <CCardBody>
                    {error && (
                        <CAlert color="danger" className="mb-3" dismissible onClose={() => setError('')}>
                            {error}
                        </CAlert>
                    )}
                    {success && (
                        <CAlert color="success" className="mb-3" dismissible onClose={() => setSuccess('')}>
                            {success}
                        </CAlert>
                    )}

                    {pendingNotifications.length === 0 ? (
                        <p className="text-muted">Không có notification nào đang chờ gửi</p>
                    ) : (
                        <CTable hover>
                            <CTableHead>
                                <CTableRow>
                                    <CTableHeaderCell>
                                        <CFormCheck
                                            checked={selectedIds.length === pendingNotifications.length}
                                            onChange={handleSelectAll}
                                        />
                                    </CTableHeaderCell>
                                    <CTableHeaderCell>ID</CTableHeaderCell>
                                    <CTableHeaderCell>Tiêu đề bài báo</CTableHeaderCell>
                                    <CTableHeaderCell>Quyết định</CTableHeaderCell>
                                    <CTableHeaderCell>Ngày quyết định</CTableHeaderCell>
                                    <CTableHeaderCell>Người quyết định</CTableHeaderCell>
                                    <CTableHeaderCell>Thao tác</CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {pendingNotifications.map((item) => (
                                    <CTableRow key={item.id}>
                                        <CTableDataCell>
                                            <CFormCheck
                                                checked={selectedIds.includes(item.id)}
                                                onChange={() => handleSelectOne(item.id)}
                                            />
                                        </CTableDataCell>
                                        <CTableDataCell>{item.submissionId}</CTableDataCell>
                                        <CTableDataCell>{item.submissionTitle}</CTableDataCell>
                                        <CTableDataCell>{getDecisionBadge(item.type)}</CTableDataCell>
                                        <CTableDataCell>
                                            {new Date(item.decidedAt).toLocaleDateString('vi-VN')}
                                        </CTableDataCell>
                                        <CTableDataCell>{item.decidedByName}</CTableDataCell>
                                        <CTableDataCell>
                                            <CButton
                                                color="info"
                                                size="sm"
                                                onClick={() => handleSendSingle(item.id)}
                                                disabled={sendingSingle === item.id}
                                            >
                                                {sendingSingle === item.id ? <CSpinner size="sm" /> : 'Gửi'}
                                            </CButton>
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                            </CTableBody>
                        </CTable>
                    )}
                </CCardBody>
            </CCard>

            {/* Bulk Notification Modal */}
            <CModal visible={showBulkModal} onClose={() => setShowBulkModal(false)} size="lg">
                <CModalHeader>
                    <CModalTitle>Gửi Bulk Notifications</CModalTitle>
                </CModalHeader>
                <CModalBody>
                    <div className="mb-3">
                        <CFormLabel>Loại notification *</CFormLabel>
                        <CFormSelect
                            value={notificationType}
                            onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                                setNotificationType(e.target.value)
                            }
                        >
                            <option value="DECISION_ACCEPT">Thông báo chấp nhận</option>
                            <option value="DECISION_REJECT">Thông báo từ chối</option>
                            <option value="DECISION_CONDITIONAL">Thông báo chấp nhận có điều kiện</option>
                        </CFormSelect>
                    </div>

                    <div className="mb-3">
                        <CFormLabel>Tiêu đề email (tùy chọn)</CFormLabel>
                        <CFormInput
                            value={customSubject}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                                setCustomSubject(e.target.value)
                            }
                            placeholder="Để trống để sử dụng tiêu đề mặc định"
                        />
                    </div>

                    <div className="mb-3">
                        <div className="d-flex justify-content-between align-items-center mb-2">
                            <CFormLabel className="mb-0">Nội dung bổ sung (tùy chọn)</CFormLabel>
                            <CButton
                                color="warning"
                                size="sm"
                                variant="outline"
                                onClick={async () => {
                                    try {
                                        setSending(true);
                                        const res = await aiService.emailDraft({
                                            conferenceId: conferenceId || undefined,
                                            emailType: 'DECISION',
                                            context: `Decision: ${notificationType}, Count: ${selectedIds.length}`,
                                            tone: 'formal',
                                            language: 'vi'
                                        });
                                        if (res.body) {
                                            setCustomMessage(res.body);
                                            if (res.subject) setCustomSubject(res.subject);
                                        }
                                    } catch (err) {
                                        console.error('AI Draft failed', err);
                                        alert('Không thể gọi AI soạn thảo. Vui lòng thử lại sau.');
                                    } finally {
                                        setSending(false);
                                    }
                                }}
                                disabled={sending}
                            >
                                {sending ? <CSpinner size="sm" /> : <>AI Soạn thảo</>}
                            </CButton>
                        </div>
                        <CFormTextarea
                            value={customMessage}
                            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                                setCustomMessage(e.target.value)
                            }
                            rows={5}
                            placeholder="Nhập nội dung bổ sung cho email"
                        />
                    </div>

                    <div className="mb-3">
                        <strong>Số lượng:</strong> {selectedIds.length} decisions sẽ được gửi thông báo
                    </div>

                    <CAlert color="info">
                        <strong>Submissions được chọn:</strong>
                        <ul className="mb-0 mt-2">
                            {pendingNotifications
                                .filter((d) => selectedIds.includes(d.id))
                                .map((d) => (
                                    <li key={d.id}>
                                        #{d.submissionId} - {d.submissionTitle} ({d.type})
                                    </li>
                                ))}
                        </ul>
                    </CAlert>
                </CModalBody>
                <CModalFooter>
                    <CButton color="secondary" onClick={() => setShowBulkModal(false)}>
                        Hủy
                    </CButton>
                    <CButton color="primary" onClick={handleSendBulk} disabled={sending}>
                        {sending ? <CSpinner size="sm" /> : 'Gửi thông báo'}
                    </CButton>
                </CModalFooter>
            </CModal>
        </>
    )
}

export default NotificationManagement
