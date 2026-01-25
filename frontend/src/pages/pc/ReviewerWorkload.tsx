import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CProgress,
  CBadge,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { pcService, Workload } from '../../services/pc.service'
import { useAuth } from '../../context/AuthContext'

/**
 * ReviewerWorkload - Trang hiển thị workload của reviewer hiện tại
 *
 * Features:
 * - Hiển thị số lượng assignments theo status
 * - Hiển thị workload status (LOW, NORMAL, HIGH, OVERLOADED)
 * - Hiển thị workload percentage
 */
const ReviewerWorkload: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [workload, setWorkload] = useState<Workload | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (conferenceId && user?.id) {
      loadWorkload()
    }
  }, [conferenceId, user?.id])

  const loadWorkload = async () => {
    try {
      setLoading(true)
      const data = await pcService.getReviewerWorkload(user!.id, conferenceId!)
      setWorkload(data)
    } catch (error: any) {
      console.error('Error loading workload:', error)
      setError(error.response?.data?.message || 'Không thể tải workload')
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

  const getWorkloadProgressColor = (percentage: number) => {
    if (percentage >= 100) return 'danger'
    if (percentage >= 75) return 'warning'
    if (percentage >= 50) return 'info'
    return 'success'
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

  if (!workload) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="info">Không có dữ liệu workload</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Workload của tôi</h4>
      </CCardHeader>
      <CCardBody>
        <div className="mb-4">
          <h5>{workload.conferenceName}</h5>
          <div className="d-flex align-items-center gap-3 mt-3">
            <div>
              <strong>Trạng thái: </strong>
              {getWorkloadStatusBadge(workload.workloadStatus)}
            </div>
            <div>
              <strong>Tổng số bài: </strong>
              {workload.totalAssignments} / {workload.maxAssignments}
            </div>
            <div>
              <strong>Tỷ lệ: </strong>
              {workload.workloadPercentage.toFixed(1)}%
            </div>
          </div>
          <div className="mt-3">
            <CProgress
              value={workload.workloadPercentage}
              color={getWorkloadProgressColor(workload.workloadPercentage)}
              className="mb-2"
            />
            {workload.workloadStatus === 'OVERLOADED' && (
              <CAlert color="danger" className="mt-2">
                Bạn đã vượt quá giới hạn workload. Vui lòng hoàn thành các bài đánh giá hiện tại
                trước khi nhận thêm bài mới.
              </CAlert>
            )}
            {workload.workloadStatus === 'HIGH' && (
              <CAlert color="warning" className="mt-2">
                Workload của bạn đang ở mức cao. Hãy cân nhắc trước khi nhận thêm bài đánh giá.
              </CAlert>
            )}
          </div>
        </div>

        <div className="mb-4">
          <h6>Chi tiết assignments</h6>
          <CTable hover>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                <CTableHeaderCell>Số lượng</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              <CTableRow>
                <CTableDataCell>
                  <CBadge color="secondary">Đã giao</CBadge>
                </CTableDataCell>
                <CTableDataCell>{workload.assignedCount}</CTableDataCell>
              </CTableRow>
              <CTableRow>
                <CTableDataCell>
                  <CBadge color="info">Đã chấp nhận</CBadge>
                </CTableDataCell>
                <CTableDataCell>{workload.acceptedCount}</CTableDataCell>
              </CTableRow>
              <CTableRow>
                <CTableDataCell>
                  <CBadge color="success">Đã hoàn thành</CBadge>
                </CTableDataCell>
                <CTableDataCell>{workload.completedCount}</CTableDataCell>
              </CTableRow>
              <CTableRow>
                <CTableDataCell>
                  <CBadge color="danger">Đã từ chối</CBadge>
                </CTableDataCell>
                <CTableDataCell>{workload.declinedCount}</CTableDataCell>
              </CTableRow>
              <CTableRow>
                <CTableDataCell>
                  <strong>Tổng cộng</strong>
                </CTableDataCell>
                <CTableDataCell>
                  <strong>{workload.totalAssignments}</strong>
                </CTableDataCell>
              </CTableRow>
            </CTableBody>
          </CTable>
        </div>
      </CCardBody>
    </CCard>
  )
}

export default ReviewerWorkload
