import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
  CProgress,
  CRow,
  CCol,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { pcService, WorkloadStats, WorkloadAlert, Workload } from '../../services/pc.service'

/**
 * WorkloadDashboard - Dashboard hiển thị workload statistics và alerts cho chairs
 *
 * Features:
 * - Overall workload statistics
 * - Workload distribution
 * - Workload alerts (overloaded reviewers)
 * - Reviewer workloads list
 */
const WorkloadDashboard: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [stats, setStats] = useState<WorkloadStats | null>(null)
  const [alerts, setAlerts] = useState<WorkloadAlert[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadData()
    }
  }, [conferenceId])

  const loadData = async () => {
    try {
      setLoading(true)
      const [statsData, alertsData] = await Promise.all([
        pcService.getWorkloadStats(conferenceId!),
        pcService.getWorkloadAlerts(conferenceId!),
      ])
      setStats(statsData)
      setAlerts(alertsData)
    } catch (error: any) {
      console.error('Error loading workload data:', error)
      setError(error.response?.data?.message || 'Không thể tải dữ liệu workload')
    } finally {
      setLoading(false)
    }
  }

  const getWorkloadStatusBadge = (status: Workload['workloadStatus']) => {
    const colorMap: Record<Workload['workloadStatus'], string> = {
      LOW: 'success',
      NORMAL: 'info',
      HIGH: 'warning',
      OVERLOADED: 'danger',
    }
    const labelMap: Record<Workload['workloadStatus'], string> = {
      LOW: 'Thấp',
      NORMAL: 'Bình thường',
      HIGH: 'Cao',
      OVERLOADED: 'Quá tải',
    }
    return <CBadge color={colorMap[status]}>{labelMap[status]}</CBadge>
  }

  const getAlertBadge = (alertType: WorkloadAlert['alertType']) => {
    return alertType === 'OVERLOADED' ? (
      <CBadge color="danger">Quá tải</CBadge>
    ) : (
      <CBadge color="warning">Gần giới hạn</CBadge>
    )
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
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

  if (error) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{error}</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  if (!stats) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="info">Không có dữ liệu workload</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      {/* Statistics Cards */}
      <CRow className="mb-4">
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Tổng số Reviewers</h6>
              <h3>{stats.totalReviewers}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Tổng số Assignments</h6>
              <h3>{stats.totalAssignments}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Trung bình/Reviewer</h6>
              <h3>{stats.averageAssignmentsPerReviewer.toFixed(1)}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Reviewers quá tải</h6>
              <h3 className="text-danger">{stats.overloadedCount}</h3>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      {/* Workload Alerts */}
      {alerts.length > 0 && (
        <CCard className="mb-4">
          <CCardHeader>
            <h5>
              Workload Alerts <CBadge color="danger">{alerts.length}</CBadge>
            </h5>
          </CCardHeader>
          <CCardBody>
            <CTable hover>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Reviewer</CTableHeaderCell>
                  <CTableHeaderCell>Email</CTableHeaderCell>
                  <CTableHeaderCell>Loại cảnh báo</CTableHeaderCell>
                  <CTableHeaderCell>Số bài hiện tại</CTableHeaderCell>
                  <CTableHeaderCell>Giới hạn</CTableHeaderCell>
                  <CTableHeaderCell>Tỷ lệ</CTableHeaderCell>
                  <CTableHeaderCell>Thông báo</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {alerts.map((alert) => (
                  <CTableRow key={alert.reviewerId}>
                    <CTableDataCell>{alert.reviewerName}</CTableDataCell>
                    <CTableDataCell>{alert.reviewerEmail}</CTableDataCell>
                    <CTableDataCell>{getAlertBadge(alert.alertType)}</CTableDataCell>
                    <CTableDataCell>{alert.currentAssignments}</CTableDataCell>
                    <CTableDataCell>{alert.maxAssignments}</CTableDataCell>
                    <CTableDataCell>
                      <CProgress
                        value={alert.workloadPercentage}
                        color={alert.workloadPercentage >= 100 ? 'danger' : 'warning'}
                        className="mb-0"
                        style={{ minWidth: '100px' }}
                      />
                      <small>{alert.workloadPercentage.toFixed(1)}%</small>
                    </CTableDataCell>
                    <CTableDataCell>
                      <small className="text-muted">{alert.message}</small>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          </CCardBody>
        </CCard>
      )}

      {/* Workload Distribution */}
      <CCard className="mb-4">
        <CCardHeader>
          <h5>Phân bố Workload</h5>
        </CCardHeader>
        <CCardBody>
          <CRow>
            <CCol md={3}>
              <div className="text-center">
                <h4 className="text-success">{stats.lowWorkloadCount}</h4>
                <p className="text-muted">Thấp (&lt; 50%)</p>
              </div>
            </CCol>
            <CCol md={3}>
              <div className="text-center">
                <h4 className="text-info">{stats.normalWorkloadCount}</h4>
                <p className="text-muted">Bình thường (50-75%)</p>
              </div>
            </CCol>
            <CCol md={3}>
              <div className="text-center">
                <h4 className="text-warning">{stats.highWorkloadCount}</h4>
                <p className="text-muted">Cao (75-100%)</p>
              </div>
            </CCol>
            <CCol md={3}>
              <div className="text-center">
                <h4 className="text-danger">{stats.overloadedCount}</h4>
                <p className="text-muted">Quá tải (&gt;= 100%)</p>
              </div>
            </CCol>
          </CRow>
        </CCardBody>
      </CCard>

      {/* Reviewer Workloads List */}
      <CCard>
        <CCardHeader>
          <h5>Chi tiết Workload của Reviewers</h5>
        </CCardHeader>
        <CCardBody>
          {stats.reviewerWorkloads.length === 0 ? (
            <p className="text-muted">Chưa có reviewer nào</p>
          ) : (
            <CTable hover responsive>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Reviewer</CTableHeaderCell>
                  <CTableHeaderCell>Email</CTableHeaderCell>
                  <CTableHeaderCell>Đã giao</CTableHeaderCell>
                  <CTableHeaderCell>Đã chấp nhận</CTableHeaderCell>
                  <CTableHeaderCell>Đã hoàn thành</CTableHeaderCell>
                  <CTableHeaderCell>Đã từ chối</CTableHeaderCell>
                  <CTableHeaderCell>Tổng</CTableHeaderCell>
                  <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                  <CTableHeaderCell>Tỷ lệ</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {stats.reviewerWorkloads.map((workload) => (
                  <CTableRow key={workload.reviewerId}>
                    <CTableDataCell>{workload.reviewerName}</CTableDataCell>
                    <CTableDataCell>{workload.reviewerEmail}</CTableDataCell>
                    <CTableDataCell>{workload.assignedCount}</CTableDataCell>
                    <CTableDataCell>{workload.acceptedCount}</CTableDataCell>
                    <CTableDataCell>{workload.completedCount}</CTableDataCell>
                    <CTableDataCell>{workload.declinedCount}</CTableDataCell>
                    <CTableDataCell>
                      <strong>{workload.totalAssignments}</strong> / {workload.maxAssignments}
                    </CTableDataCell>
                    <CTableDataCell>{getWorkloadStatusBadge(workload.workloadStatus)}</CTableDataCell>
                    <CTableDataCell>
                      <CProgress
                        value={workload.workloadPercentage}
                        color={
                          workload.workloadPercentage >= 100
                            ? 'danger'
                            : workload.workloadPercentage >= 75
                            ? 'warning'
                            : workload.workloadPercentage >= 50
                            ? 'info'
                            : 'success'
                        }
                        className="mb-0"
                        style={{ minWidth: '80px' }}
                      />
                      <small>{workload.workloadPercentage.toFixed(1)}%</small>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>
    </>
  )
}

export default WorkloadDashboard
