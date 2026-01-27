import React, { useState, useEffect } from 'react'
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
  CBadge,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilCloudDownload, cilCheckCircle } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { submissionService, SubmissionFile } from '../../services/submission.service'

interface FileVersionHistoryProps {
  submissionId: number
}

/**
 * FileVersionHistory - Component hiển thị lịch sử các version của PDF file
 */
const FileVersionHistory: React.FC<FileVersionHistoryProps> = ({ submissionId }) => {
  const { t } = useTranslation()
  const [files, setFiles] = useState<SubmissionFile[]>([])
  const [loading, setLoading] = useState(true)
  const [downloading, setDownloading] = useState<number | null>(null)

  useEffect(() => {
    loadFileVersions()
  }, [submissionId])

  const loadFileVersions = async () => {
    try {
      setLoading(true)
      const data = await submissionService.getFileVersions(submissionId)
      setFiles(data)
    } catch (error) {
      console.error('Error loading file versions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDownloadVersion = async (fileId: number, fileName: string) => {
    try {
      setDownloading(fileId)
      const blob = await submissionService.downloadFileVersion(submissionId, fileId)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName || `submission-${submissionId}-v${fileId}.pdf`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (error) {
      alert('Không thể tải file. Vui lòng thử lại.')
    } finally {
      setDownloading(null)
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleString('vi-VN')
    } catch {
      return dateString
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-3">
        <CSpinner size="sm" />
      </div>
    )
  }

  if (files.length === 0) {
    return (
      <CCard>
        <CCardHeader>
          <h6>{t('submission.fileVersions') || 'Lịch sử file'}</h6>
        </CCardHeader>
        <CCardBody>
          <p className="text-muted">{t('submission.noFileVersions') || 'Chưa có file nào được upload'}</p>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h6>{t('submission.fileVersions') || 'Lịch sử file'}</h6>
      </CCardHeader>
      <CCardBody>
        <CTable hover responsive>
          <CTableHead>
            <CTableRow>
              <CTableHeaderCell>{t('submission.version') || 'Version'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.fileName') || 'Tên file'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.fileSize') || 'Kích thước'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.uploadedAt') || 'Ngày upload'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.status') || 'Trạng thái'}</CTableHeaderCell>
              <CTableHeaderCell>{t('common.actions') || 'Thao tác'}</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {files.map((file) => (
              <CTableRow key={file.id}>
                <CTableDataCell>
                  <strong>v{file.versionNumber}</strong>
                </CTableDataCell>
                <CTableDataCell>{file.fileName}</CTableDataCell>
                <CTableDataCell>{formatFileSize(file.fileSize)}</CTableDataCell>
                <CTableDataCell>{formatDate(file.uploadedAt)}</CTableDataCell>
                <CTableDataCell>
                  {file.isCurrent ? (
                    <CBadge color="success">
                      <CIcon icon={cilCheckCircle} className="me-1" />
                      {t('submission.current') || 'Hiện tại'}
                    </CBadge>
                  ) : (
                    <CBadge color="secondary">{t('submission.old') || 'Cũ'}</CBadge>
                  )}
                </CTableDataCell>
                <CTableDataCell>
                  <CButton
                    color="primary"
                    size="sm"
                    onClick={() => handleDownloadVersion(file.id, file.fileName)}
                    disabled={downloading === file.id}
                  >
                    {downloading === file.id ? (
                      <>
                        <CSpinner size="sm" className="me-2" />
                        {t('common.downloading') || 'Đang tải...'}
                      </>
                    ) : (
                      <>
                        <CIcon icon={cilCloudDownload} className="me-1" />
                        {t('common.download') || 'Tải'}
                      </>
                    )}
                  </CButton>
                </CTableDataCell>
              </CTableRow>
            ))}
          </CTableBody>
        </CTable>
      </CCardBody>
    </CCard>
  )
}

export default FileVersionHistory
