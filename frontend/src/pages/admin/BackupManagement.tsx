import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
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
    const { t } = useTranslation()
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
            setError(t('admin.loadBackupsError'))
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
            setSuccess(t('admin.backupStarted'))
            loadBackups()
        } catch (err: any) {
            setError(t('admin.backupFailed'))
        } finally {
            setActionLoading(false)
        }
    }

    const handleRestore = async (id: number) => {
        if (!window.confirm(t('admin.restoreConfirm'))) {
            return
        }

        try {
            setActionLoading(true)
            setError('')
            setSuccess('')
            const result = await backupService.restore(id)
            if (result) {
                setSuccess(t('admin.restoreSuccess'))
            } else {
                setError(t('admin.restoreFailed'))
            }
        } catch (err: any) {
            setError(t('admin.systemError'))
        } finally {
            setActionLoading(false)
        }
    }

    if (loading) return <div className="text-center p-5"><CSpinner color="primary" /></div>

    return (
        <div className="container-fluid">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h3>{t('admin.backupManagement')}</h3>
                <CButton color="danger" onClick={handleBackupAll} disabled={actionLoading}>
                    <CIcon icon={cilShieldAlt} className="me-2" /> {t('admin.fullBackup')}
                </CButton>
            </div>

            {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
            {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

            <CRow className="mb-4">
                <CCol md={4}>
                    <CCard className="h-100 border-start border-start-4 border-start-info">
                        <CCardBody>
                            <h5>{t('admin.backupIntro')}</h5>
                            <p className="text-muted small">{t('admin.backupDesc')}</p>
                            <CButton color="info" variant="outline" size="sm" onClick={loadBackups}>
                                <CIcon icon={cilHistory} className="me-1" /> {t('admin.refreshHistory')}
                            </CButton>
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>

            <CCard>
                <CCardHeader>{t('admin.backupHistory')}</CCardHeader>
                <CCardBody>
                    {!Array.isArray(backups) || backups.length === 0 ? (
                        <p className="text-muted">{t('admin.noBackups')}</p>
                    ) : (
                        <CTable hover responsive align="middle">
                            <CTableHead>
                                <CTableRow>
                                    <CTableHeaderCell>ID</CTableHeaderCell>
                                    <CTableHeaderCell>Loại</CTableHeaderCell>
                                    <CTableHeaderCell>Kích thước</CTableHeaderCell>
                                    <CTableHeaderCell>Ngày tạo</CTableHeaderCell>
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
                                                <CIcon icon={cilCloudUpload} className="me-1" /> {t('admin.restore')}
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

export default BackupManagement
