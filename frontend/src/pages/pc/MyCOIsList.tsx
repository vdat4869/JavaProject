import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
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
  CBadge,
  CSpinner,
  CAlert,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilTrash } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { pcService, COIDeclaration, COIType } from '../../services/pc.service'

/**
 * MyCOIsList - Danh sách COIs của reviewer hiện tại
 *
 * Features:
 * - Hiển thị tất cả COIs đã khai báo
 * - Xóa COI
 * - Xem chi tiết submission
 */
const MyCOIsList: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [cois, setCois] = useState<COIDeclaration[]>([])
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState<number | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    loadCOIs()
  }, [])

  const loadCOIs = async () => {
    try {
      setLoading(true)
      const data = await pcService.getMyCOIs()
      setCois(data)
    } catch (error) {
      console.error('Error loading COIs:', error)
      setError('Không thể tải danh sách COI')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (coiId: number) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa khai báo COI này?')) {
      return
    }

    try {
      setDeleting(coiId)
      await pcService.deleteCOI(coiId)
      await loadCOIs()
    } catch (error: any) {
      alert(error.response?.data?.message || 'Không thể xóa COI')
    } finally {
      setDeleting(null)
    }
  }

  const getCOITypeLabel = (type: COIType) => {
    const labels: Record<COIType, string> = {
      CO_AUTHOR: 'Đồng tác giả',
      COLLABORATOR: 'Cộng tác viên',
      ADVISOR: 'Cố vấn',
      INSTITUTIONAL: 'Cùng tổ chức',
      OTHER: 'Khác',
    }
    return labels[type] || type
  }

  const getCOITypeBadge = (type: COIType) => {
    const colorMap: Record<COIType, string> = {
      CO_AUTHOR: 'danger',
      COLLABORATOR: 'warning',
      ADVISOR: 'info',
      INSTITUTIONAL: 'secondary',
      OTHER: 'dark',
    }
    return <CBadge color={colorMap[type] || 'secondary'}>{getCOITypeLabel(type)}</CBadge>
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Danh sách Conflict of Interest</h4>
      </CCardHeader>
      <CCardBody>
        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        {cois.length === 0 ? (
          <div className="text-center py-5">
            <p className="text-muted">Bạn chưa khai báo COI nào</p>
            <CButton color="primary" onClick={() => navigate('/app/pc/assignments')}>
              Xem bài được giao
            </CButton>
          </div>
        ) : (
          <CTable hover responsive>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>Submission ID</CTableHeaderCell>
                <CTableHeaderCell>Loại COI</CTableHeaderCell>
                <CTableHeaderCell>Lý do</CTableHeaderCell>
                <CTableHeaderCell>Ngày khai báo</CTableHeaderCell>
                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                <CTableHeaderCell>Thao tác</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {cois.map((coi) => (
                <CTableRow key={coi.id}>
                  <CTableDataCell>
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => navigate(`/app/pc/coi?submissionId=${coi.submissionId}`)}
                    >
                      #{coi.submissionId}
                    </CButton>
                  </CTableDataCell>
                  <CTableDataCell>{getCOITypeBadge(coi.type)}</CTableDataCell>
                  <CTableDataCell>{coi.reason || '-'}</CTableDataCell>
                  <CTableDataCell>
                    {new Date(coi.declaredAt).toLocaleString('vi-VN')}
                  </CTableDataCell>
                  <CTableDataCell>
                    {coi.active ? (
                      <CBadge color="success">Active</CBadge>
                    ) : (
                      <CBadge color="secondary">Inactive</CBadge>
                    )}
                  </CTableDataCell>
                  <CTableDataCell>
                    <CButton
                      color="danger"
                      size="sm"
                      onClick={() => handleDelete(coi.id)}
                      disabled={deleting === coi.id}
                    >
                      {deleting === coi.id ? (
                        <CSpinner size="sm" />
                      ) : (
                        <>
                          <CIcon icon={cilTrash} className="me-1" />
                          Xóa
                        </>
                      )}
                    </CButton>
                  </CTableDataCell>
                </CTableRow>
              ))}
            </CTableBody>
          </CTable>
        )}
      </CCardBody>
    </CCard>
  )
}

export default MyCOIsList
