import React, { useState, useEffect, useCallback } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
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
  CBadge,
  CListGroup,
  CListGroupItem,
} from '@coreui/react'
import {
  reportsService,
  ReportStatistics,
} from '../../services/reports.service'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'

/**
 * ReportingDashboard - Dashboard thống kê và báo cáo cho CHAIR (API v1)
 */
const ReportingDashboard: React.FC = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null

  const [stats, setStats] = useState<ReportStatistics | null>(null)
  const [history, setHistory] = useState<ReportStatistics[]>([])
  const [loading, setLoading] = useState(true)
  const [showExportModal, setShowExportModal] = useState(false)
  const [reportType, setReportType] = useState<
    'STATISTICS' | 'SUBMISSIONS' | 'REVIEWS' | 'DECISIONS' | 'ALL'
  >('ALL')
  const [exportFormat, setExportFormat] = useState<'PDF' | 'EXCEL' | 'CSV'>('PDF')
  const [exporting, setExporting] = useState(false)
  const [snapshotting, setSnapshotting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Conference selection states
  const [myConferences, setMyConferences] = useState<ConferenceResponse[]>([])
  const [isSelectingConference, setIsSelectingConference] = useState(false)

  const loadData = useCallback(async () => {
    try {
      setLoading(true)
      const [latest, snapshots] = await Promise.all([
        reportsService.getLatestReport(conferenceId!),
        reportsService.getReportHistory(conferenceId!)
      ])
      setStats(latest)
      setHistory(snapshots)
    } catch (error) {
      console.error('Error loading reporting data:', error)
      setError('Không thể tải dữ liệu báo cáo.')
    } finally {
      setLoading(false)
    }
  }, [conferenceId])

  useEffect(() => {
    if (conferenceId) {
      loadData()
    } else {
      loadMyConferences()
    }
  }, [conferenceId, loadData])

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
      setLoading(false)
    }
  }

  const handleCreateSnapshot = async () => {
    if (!conferenceId) return
    try {
      setSnapshotting(true)
      await reportsService.createSnapshot(conferenceId)
      setSuccess('Đã lưu snapshot báo cáo thành công.')
      loadData()
    } catch (error) {
      setError('Lỗi khi tạo snapshot.')
    } finally {
      setSnapshotting(false)
    }
  }

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
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể export report')
    } finally {
      setExporting(false)
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
        <CCard className="mx-auto" style={{ maxWidth: '800px' }}>
          <CCardHeader>
            <h4>Chọn hội nghị để xem báo cáo</h4>
          </CCardHeader>
          <CCardBody>
            {myConferences.length === 0 ? (
              <CAlert color="warning">Bạn chưa được gán vào hội nghị nào.</CAlert>
            ) : (
              <CListGroup>
                {myConferences.map(conf => (
                  <CListGroupItem
                    key={conf.id}
                    onClick={() => navigate(`?conferenceId=${conf.id}`)}
                    className="d-flex justify-content-between align-items-center list-group-item-action"
                    style={{ cursor: 'pointer' }}
                  >
                    <span>{conf.name}</span>
                    <CBadge color="primary" shape="rounded-pill">ID: {conf.id}</CBadge>
                  </CListGroupItem>
                ))}
              </CListGroup>
            )}
          </CCardBody>
        </CCard>
      )
    }

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

  return (
    <div className="container-fluid">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4>Reporting & Analytics Dashboard</h4>
        <div className="d-flex gap-2">
          <CButton color="secondary" variant="outline" onClick={() => navigate('/app/chair/conferences')}>
            Đổi hội nghị
          </CButton>
          <CButton color="info" variant="outline" onClick={handleCreateSnapshot} disabled={snapshotting}>
            {snapshotting ? <CSpinner size="sm" /> : 'Capture Snapshot'}
          </CButton>
          <CButton color="primary" onClick={() => setShowExportModal(true)}>
            Export Report
          </CButton>
        </div>
      </div>

      {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
      {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

      {stats && (
        <>
          {/* Submissions Section */}
          <h5 className="mb-3">Submission Statistics</h5>
          <CRow className="mb-4">
            <CCol md={3}>
              <CCard className="h-100 border-start border-start-4 border-start-primary">
                <CCardBody>
                  <div className="text-muted small">Tổng số bài nộp</div>
                  <div className="h2 fw-bold">{stats.totalSubmissions}</div>
                  <div className="small text-muted">
                    Acceptance Rate: {stats.acceptanceRate.toFixed(1)}%
                  </div>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={3}>
              <CCard className="h-100 border-start border-start-4 border-start-success">
                <CCardBody>
                  <div className="text-muted small">Đã chấp nhận</div>
                  <div className="h2 fw-bold text-success">{stats.acceptedCount}</div>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={3}>
              <CCard className="h-100 border-start border-start-4 border-start-danger">
                <CCardBody>
                  <div className="text-muted small">Đã từ chối</div>
                  <div className="h2 fw-bold text-danger">{stats.rejectedCount}</div>
                </CCardBody>
              </CCard>
            </CCol>
            <CCol md={3}>
              <CCard className="h-100 border-start border-start-4 border-start-warning">
                <CCardBody>
                  <div className="text-muted small">Đang chờ xử lý</div>
                  <div className="h2 fw-bold text-warning">{stats.pendingCount}</div>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow className="mb-4">
            {/* Review Section */}
            <CCol md={6}>
              <CCard className="h-100">
                <CCardHeader>Review Progress</CCardHeader>
                <CCardBody>
                  <CRow className="mb-3">
                    <CCol xs={4} className="text-center">
                      <div className="text-muted small">Tổng số</div>
                      <div className="h4">{stats.totalReviews}</div>
                    </CCol>
                    <CCol xs={4} className="text-center border-start">
                      <div className="text-muted small">Hoàn thành</div>
                      <div className="h4 text-success">{stats.completedReviews}</div>
                    </CCol>
                    <CCol xs={4} className="text-center border-start">
                      <div className="text-muted small">Đang chờ</div>
                      <div className="h4 text-warning">{stats.pendingReviews}</div>
                    </CCol>
                  </CRow>
                  <div className="progress" style={{ height: '10px' }}>
                    <div
                      className="progress-bar bg-success"
                      style={{ width: `${(stats.completedReviews / stats.totalReviews) * 100}%` }}
                    />
                  </div>
                </CCardBody>
              </CCard>
            </CCol>

            {/* Assignment Section */}
            <CCol md={6}>
              <CCard className="h-100">
                <CCardHeader>Assignment Overview</CCardHeader>
                <CCardBody>
                  <CRow>
                    <CCol xs={4} className="text-center">
                      <div className="text-muted small">Tổng số gán</div>
                      <div className="h4">{stats.totalAssignments}</div>
                    </CCol>
                    <CCol xs={4} className="text-center border-start">
                      <div className="text-muted small">Đồng ý</div>
                      <div className="h4 text-success">{stats.acceptedAssignments}</div>
                    </CCol>
                    <CCol xs={4} className="text-center border-start">
                      <div className="text-muted small">Từ chối</div>
                      <div className="h4 text-danger">{stats.declinedAssignments}</div>
                    </CCol>
                  </CRow>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>
        </>
      )}

      {/* Snapshot History Table */}
      <CCard className="mb-4">
        <CCardHeader>Historical Reports (Snapshots)</CCardHeader>
        <CCardBody>
          <CTable align="middle" hover responsive>
            <CTableHead color="light">
              <CTableRow>
                <CTableHeaderCell>Thời điểm snapshot</CTableHeaderCell>
                <CTableHeaderCell className="text-center">Submissions</CTableHeaderCell>
                <CTableHeaderCell className="text-center">Acceptance</CTableHeaderCell>
                <CTableHeaderCell className="text-center">Reviews (C/T)</CTableHeaderCell>
                <CTableHeaderCell>Hành động</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {history.map((snapshot) => (
                <CTableRow key={snapshot.id}>
                  <CTableDataCell>
                    {new Date(snapshot.snapshotAt!).toLocaleString('vi-VN')}
                  </CTableDataCell>
                  <CTableDataCell className="text-center">{snapshot.totalSubmissions}</CTableDataCell>
                  <CTableDataCell className="text-center">
                    <CBadge color="info">{snapshot.acceptanceRate.toFixed(1)}%</CBadge>
                  </CTableDataCell>
                  <CTableDataCell className="text-center">
                    {snapshot.completedReviews} / {snapshot.totalReviews}
                  </CTableDataCell>
                  <CTableDataCell>
                    <CButton color="light" size="sm" onClick={() => setStats(snapshot)}>
                      Xem lại
                    </CButton>
                  </CTableDataCell>
                </CTableRow>
              ))}
              {history.length === 0 && (
                <CTableRow>
                  <CTableDataCell colSpan={5} className="text-center text-muted p-4">
                    Chưa có snapshot lịch sử nào được ghi lại.
                  </CTableDataCell>
                </CTableRow>
              )}
            </CTableBody>
          </CTable>
        </CCardBody>
      </CCard>

      {/* Export Modal (Giữ nguyên logic cũ) */}
      <CModal visible={showExportModal} onClose={() => setShowExportModal(false)}>
        <CModalHeader>
          <CModalTitle>Export Report</CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="mb-3">
            <CFormLabel>Loại báo cáo *</CFormLabel>
            <select className="form-select" value={reportType} onChange={(e) => setReportType(e.target.value as any)}>
              <option value="ALL">Tất cả</option>
              <option value="STATISTICS">Thống kê</option>
              <option value="REVIEWS">Reviews</option>
            </select>
          </div>
          <div className="mb-3">
            <CFormLabel>Định dạng *</CFormLabel>
            <select className="form-select" value={exportFormat} onChange={(e) => setExportFormat(e.target.value as any)}>
              <option value="PDF">PDF</option>
              <option value="EXCEL">Excel</option>
              <option value="CSV">CSV</option>
            </select>
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowExportModal(false)}>Đóng</CButton>
          <CButton color="primary" onClick={handleExport} disabled={exporting}>
            {exporting ? <CSpinner size="sm" /> : 'Export'}
          </CButton>
        </CModalFooter>
      </CModal>
    </div>
  )
}

export default ReportingDashboard
