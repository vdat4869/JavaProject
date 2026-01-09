import React, { useState, useEffect, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
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
  CButton,
  CFormLabel,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from '@coreui/react'
import {
  reportsService,
  ReportStatistics,
  ReportExportRequest,
} from '../../services/reports.service'

/**
 * ReportingDashboard - Dashboard thống kê và báo cáo cho CHAIR
 *
 * Features:
 * - Thống kê tổng quan
 * - Submissions by track
 * - Submissions by status
 * - Review progress
 * - Export reports
 */
const ReportingDashboard: React.FC = () => {
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [stats, setStats] = useState<ReportStatistics | null>(null)
  const [loading, setLoading] = useState(true)
  const [showExportModal, setShowExportModal] = useState(false)
  const [reportType, setReportType] = useState<
    'STATISTICS' | 'SUBMISSIONS' | 'REVIEWS' | 'DECISIONS' | 'ALL'
  >('ALL')
  const [exportFormat, setExportFormat] = useState<'PDF' | 'EXCEL' | 'CSV'>('PDF')
  const [exporting, setExporting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const loadStatistics = useCallback(async () => {
    try {
      setLoading(true)
      const data = await reportsService.getStatistics(conferenceId!)
      setStats(data)
    } catch (error) {
      console.error('Error loading statistics:', error)
    } finally {
      setLoading(false)
    }
  }, [conferenceId])

  useEffect(() => {
    if (conferenceId) {
      loadStatistics()
    }
  }, [conferenceId, loadStatistics])

  const handleExport = async () => {
    if (!conferenceId) return

    try {
      setExporting(true)
      setError('')
      const result = await reportsService.export({
        conferenceId,
        reportType,
        format: exportFormat,
      })
      setSuccess(`Export thành công! File: ${result.fileName}`)
      setShowExportModal(false)

      // Download file
      await reportsService.download(result.downloadUrl, result.fileName)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể export report')
    } finally {
      setExporting(false)
    }
  }

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing conferenceId</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!stats) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Không thể tải thống kê</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Reporting Dashboard</h4>
            <CButton color="primary" onClick={() => setShowExportModal(true)}>
              Export Report
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {error && (
            <CAlert color="danger" className="mb-3">
              {error}
            </CAlert>
          )}
          {success && (
            <CAlert color="success" className="mb-3">
              {success}
            </CAlert>
          )}

          {/* Summary Cards */}
          <CRow className="mb-4">
            <CCol md={3}>
              <CCard>
                <CCardBody>
                  <h5>Tổng số bài nộp</h5>
                  <h2>{stats.totalSubmissions}</h2>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={3}>
              <CCard>
                <CCardBody>
                  <h5>Chấp nhận</h5>
                  <h2 className="text-success">{stats.acceptedSubmissions}</h2>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={3}>
              <CCard>
                <CCardBody>
                  <h5>Từ chối</h5>
                  <h2 className="text-danger">{stats.rejectedSubmissions}</h2>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={3}>
              <CCard>
                <CCardBody>
                  <h5>Đang chờ</h5>
                  <h2 className="text-warning">{stats.pendingSubmissions}</h2>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          {/* Review Statistics */}
          <CRow className="mb-4">
            <CCol md={4}>
              <CCard>
                <CCardBody>
                  <h5>Tổng số reviews</h5>
                  <h2>{stats.totalReviews}</h2>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={4}>
              <CCard>
                <CCardBody>
                  <h5>Đã hoàn thành</h5>
                  <h2>{stats.completedReviews}</h2>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={4}>
              <CCard>
                <CCardBody>
                  <h5>Điểm trung bình</h5>
                  <h2>{stats.averageRating.toFixed(2)}</h2>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          {/* Submissions by Track */}
          <CCard className="mb-3">
            <CCardHeader>
              <h5>Bài nộp theo Track</h5>
            </CCardHeader>
            <CCardBody>
              <CTable hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Track</CTableHeaderCell>
                    <CTableHeaderCell>Tổng số</CTableHeaderCell>
                    <CTableHeaderCell>Chấp nhận</CTableHeaderCell>
                    <CTableHeaderCell>Từ chối</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {stats.submissionsByTrack.map((track, index) => (
                    <CTableRow key={index}>
                      <CTableDataCell>{track.trackName}</CTableDataCell>
                      <CTableDataCell>{track.count}</CTableDataCell>
                      <CTableDataCell className="text-success">{track.accepted}</CTableDataCell>
                      <CTableDataCell className="text-danger">{track.rejected}</CTableDataCell>
                    </CTableRow>
                  ))}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>

          {/* Submissions by Status */}
          <CCard className="mb-3">
            <CCardHeader>
              <h5>Bài nộp theo Trạng thái</h5>
            </CCardHeader>
            <CCardBody>
              <CTable hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                    <CTableHeaderCell>Số lượng</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {stats.submissionsByStatus.map((status, index) => (
                    <CTableRow key={index}>
                      <CTableDataCell>{status.status}</CTableDataCell>
                      <CTableDataCell>{status.count}</CTableDataCell>
                    </CTableRow>
                  ))}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>

          {/* Review Progress */}
          <CCard>
            <CCardHeader>
              <h5>Tiến độ Review</h5>
            </CCardHeader>
            <CCardBody>
              <CRow>
                <CCol md={4}>
                  <p>Đã hoàn thành: {stats.reviewProgress.completed}</p>
                </CCol>
                <CCol md={4}>
                  <p>Đang thực hiện: {stats.reviewProgress.inProgress}</p>
                </CCol>
                <CCol md={4}>
                  <p>Chờ xử lý: {stats.reviewProgress.pending}</p>
                </CCol>
              </CRow>
            </CCardBody>
          </CCard>
        </CCardBody>
      </CCard>

      {/* Export Modal */}
      <CModal visible={showExportModal} onClose={() => setShowExportModal(false)}>
        <CModalHeader>
          <CModalTitle>Export Report</CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="mb-3">
            <CFormLabel>Loại báo cáo *</CFormLabel>
            <select
              className="form-select"
              value={reportType}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setReportType(e.target.value as any)
              }
            >
              <option value="ALL">Tất cả</option>
              <option value="STATISTICS">Thống kê</option>
              <option value="SUBMISSIONS">Bài nộp</option>
              <option value="REVIEWS">Reviews</option>
              <option value="DECISIONS">Quyết định</option>
            </select>
          </div>
          <div className="mb-3">
            <CFormLabel>Format *</CFormLabel>
            <select
              className="form-select"
              value={exportFormat}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setExportFormat(e.target.value as any)
              }
            >
              <option value="PDF">PDF</option>
              <option value="EXCEL">Excel</option>
              <option value="CSV">CSV</option>
            </select>
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowExportModal(false)}>
            Hủy
          </CButton>
          <CButton color="primary" onClick={handleExport} disabled={exporting}>
            {exporting ? <CSpinner size="sm" /> : 'Export'}
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default ReportingDashboard
