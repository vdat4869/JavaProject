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
    CRow,
    CCol,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilCloudDownload, cilCloudUpload, cilHistory, cilShieldAlt } from '@coreui/icons'
import { backupService, BackupRecord } from '../../services/backup.service'

/**
 * BackupManagement - Trang quản lý sao lưu và phục hồi cho ADMIN
 */
const BackupManagement: React.FC = () => {
    const [backups, setBackups] = useState<BackupRecord[]>([])
    const [loading, setLoading] = useState(true)
    const [actionLoading, setActionLoading] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    useEffect(() => {
        loadBackups()
    }, [])

    const loadBackups = async () => {
        try {
            setLoading(true)
            const data = await backupService.getAllBackups()
            setBackups(data)
        } catch (err: any) {
            setError('Không thể tải danh sách sao lưu.')
        } finally {
            setLoading(false)
        }
    }

    const handleBackupAll = async () => {
        try {
            setActionLoading(true)
            setError('')
            setSuccess('')
            await backupService.backupAll()
            setSuccess('Quá trình sao lưu toàn hệ thống đã được bắt đầu.')
            loadBackups()
        } catch (err: any) {
            setError('Lỗi khi tạo bản sao lưu toàn hệ thống.')
        } finally {
            setActionLoading(false)
        }
    }

    const handleRestore = async (id: number) => {
        if (!window.confirm('CẢNH BÁO: Quá trình khôi phục sẽ ghi đè lên dữ liệu hiện tại. Bạn có chắc chắn muốn tiếp tục?')) {
            return
        }

        try {
            setActionLoading(true)
            setError('')
            setSuccess('')
            const result = await backupService.restore(id)
            if (result) {
                setSuccess('Khôi phục dữ liệu thành công!')
            } else {
                setError('Khôi phục thất bại.')
            }
        } catch (err: any) {
            setError('Lỗi hệ thống trong quá trình khôi phục.')
        } finally {
            setActionLoading(false)
        }
    }

    if (loading) return <div className="text-center p-5"><CSpinner color="primary" /></div>

    return (
        <div className="container-fluid">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h3>Quản lý Sao lưu & Phục hồi</h3>
                <CButton color="danger" onClick={handleBackupAll} disabled={actionLoading}>
                    <CIcon icon={cilShieldAlt} className="me-2" /> Sao lưu toàn bộ (Full Backup)
                </CButton>
            </div>

            {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
            {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

            <CRow className="mb-4">
                <CCol md={4}>
                    <CCard className="h-100 border-start border-start-4 border-start-info">
                        <CCardBody>
                            <h5>Sẵn sàng bảo vệ dữ liệu</h5>
                            <p className="text-muted small">Các bản sao lưu bao gồm cơ sở dữ liệu và toàn bộ tệp tin PDF của các bài báo đã nộp.</p>
                            <CButton color="info" variant="outline" size="sm" onClick={loadBackups}>
                                <CIcon icon={cilHistory} className="me-1" /> Làm mới lịch sử
                            </CButton>
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>

            <CCard>
                <CCardHeader>Lịch sử Sao lưu</CCardHeader>
                <CCardBody>
                    <CTable hover responsive align="middle">
                        <CTableHead>
                            <CTableRow>
                                <CTableHeaderCell>ID</CTableHeaderCell>
                                <CTableHeaderCell>Loại</CTableHeaderCell>
                                <CTableHeaderCell>Hội nghị</CTableHeaderCell>
                                <CTableHeaderCell>Dung lượng</CTableHeaderCell>
                                <CTableHeaderCell>Thời gian</CTableHeaderCell>
                                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                                <CTableHeaderCell>Thao tác</CTableHeaderCell>
                            </CTableRow>
                        </CTableHead>
                        <CTableBody>
                            {backups.map((b) => (
                                <CTableRow key={b.id}>
                                    <CTableDataCell>#{b.id}</CTableDataCell>
                                    <CTableDataCell>
                                        <CBadge color={b.backupType === 'FULL' ? 'danger' : 'primary'}>{b.backupType}</CBadge>
                                    </CTableDataCell>
                                    <CTableDataCell>{b.conferenceId || 'ALL'}</CTableDataCell>
                                    <CTableDataCell>{(b.fileSize / 1024 / 1024).toFixed(2)} MB</CTableDataCell>
                                    <CTableDataCell>{new Date(b.createdAt).toLocaleString('vi-VN')}</CTableDataCell>
                                    <CTableDataCell>
                                        <CBadge color={b.status === 'COMPLETED' ? 'success' : 'warning'}>{b.status}</CBadge>
                                    </CTableDataCell>
                                    <CTableDataCell>
                                        <CButton
                                            color="warning"
                                            size="sm"
                                            onClick={() => handleRestore(b.id)}
                                            disabled={actionLoading || b.status !== 'COMPLETED'}
                                        >
                                            <CIcon icon={cilCloudUpload} className="me-1" /> Khôi phục
                                        </CButton>
                                    </CTableDataCell>
                                </CTableRow>
                            ))}
                            {backups.length === 0 && (
                                <CTableRow>
                                    <CTableDataCell colSpan={7} className="text-center text-muted p-4">
                                        Chưa có hồ sơ sao lưu nào.
                                    </CTableDataCell>
                                </CTableRow>
                            )}
                        </CTableBody>
                    </CTable>
                </CCardBody>
            </CCard>
        </div>
    )
}

export default BackupManagement
