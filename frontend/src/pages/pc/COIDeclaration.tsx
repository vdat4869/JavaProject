import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormSelect,
  CFormTextarea,
  CFormLabel,
  CButton,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { pcService, COIDeclaration as COIDecl, COIType } from '../../services/pc.service'
import CIcon from '@coreui/icons-react'
import { cilTrash } from '@coreui/icons'
import { reviewService, Assignment } from '../../services/review.service'

/**
 * COIDeclaration - Trang khai báo Conflict of Interest
 *
 * Features:
 * - Khai báo COI cho submission
 * - Hiển thị COI hiện tại (nếu có)
 * - Update COI declaration
 */
const COIDeclaration: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const submissionId = searchParams.get('submissionId')
    ? parseInt(searchParams.get('submissionId')!)
    : null
  const [coiType, setCoiType] = useState<COIType | ''>('')
  const [reason, setReason] = useState('')
  const [existingCOI, setExistingCOI] = useState<COIDecl | null>(null)
  const [assignment, setAssignment] = useState<Assignment | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (submissionId) {
      loadData()
    }
  }, [submissionId])

  const loadData = async () => {
    try {
      setLoading(true)
      const [coisData, assignments] = await Promise.all([
        pcService.getCOIsBySubmission(submissionId!),
        reviewService.getAssignments(),
      ])

      // Get current user's active COI (if exists)
      // Note: getCOIsBySubmission returns all COIs for the submission
      // We need to find the one for current user - for now, get first active one
      // In production, filter by current user ID
      const myCOI = coisData.find((coi) => coi.active) || null
      setExistingCOI(myCOI)
      const assignmentData = assignments.find((a) => a.submissionId === submissionId)
      setAssignment(assignmentData || null)

      if (myCOI) {
        setCoiType(myCOI.type)
        setReason(myCOI.reason || '')
      }
    } catch (error: any) {
      console.error('Error loading COI data:', error)
      if (error.response?.status === 403 || error.response?.status === 401) {
        console.error('DEBUG: Auth error in COIDeclaration:', error.response.status, error.response.data)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!coiType) {
      setError('Vui lòng chọn loại COI')
      return
    }

    if (coiType !== 'OTHER' && !reason.trim()) {
      setError('Vui lòng nhập lý do COI')
      return
    }

    try {
      setSaving(true)
      await pcService.declareCOI({
        submissionId: submissionId!,
        type: coiType as COIType,
        reason: reason.trim() || undefined,
      })
      navigate('/app/pc/assignments')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể khai báo COI')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    if (!existingCOI) return

    if (!window.confirm('Bạn có chắc chắn muốn xóa khai báo COI này?')) {
      return
    }

    try {
      setSaving(true)
      await pcService.deleteCOI(existingCOI.id)
      navigate('/app/pc/assignments')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể xóa COI')
    } finally {
      setSaving(false)
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

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!submissionId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing submissionId</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Khai báo Conflict of Interest</h4>
      </CCardHeader>
      <CCardBody>
        {assignment && (
          <div className="mb-4">
            <h5>Bài báo: {assignment.submissionTitle}</h5>
            <p className="text-muted">{assignment.submissionAbstract}</p>
          </div>
        )}

        {existingCOI && (
          <CAlert color="info" className="mb-3">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                Bạn đã khai báo COI ({getCOITypeLabel(existingCOI.type)}) vào:{' '}
                {new Date(existingCOI.declaredAt).toLocaleString('vi-VN')}
              </div>
              <CButton color="danger" size="sm" onClick={handleDelete} disabled={saving}>
                <CIcon icon={cilTrash} className="me-1" />
                Xóa
              </CButton>
            </div>
          </CAlert>
        )}

        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        <CForm onSubmit={handleSubmit}>
          <div className="mb-3">
            <CFormLabel>
              Loại Conflict of Interest <span className="text-danger">*</span>
            </CFormLabel>
            <CFormSelect
              value={coiType}
              onChange={(e) => setCoiType(e.target.value as COIType | '')}
              required
            >
              <option value="">Chọn loại COI</option>
              <option value="CO_AUTHOR">Đồng tác giả</option>
              <option value="COLLABORATOR">Cộng tác viên</option>
              <option value="ADVISOR">Cố vấn</option>
              <option value="INSTITUTIONAL">Cùng tổ chức</option>
              <option value="OTHER">Khác</option>
            </CFormSelect>
            <small className="text-muted">
              Vui lòng chọn loại Conflict of Interest giữa bạn và bài báo này
            </small>
          </div>

          {coiType && (
            <div className="mb-3">
              <CFormLabel>
                Lý do / Mô tả {coiType !== 'OTHER' && <span className="text-danger">*</span>}
              </CFormLabel>
              <CFormTextarea
                value={reason}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setReason(e.target.value)}
                required={coiType !== 'OTHER'}
                rows={5}
                placeholder={
                  coiType === 'CO_AUTHOR'
                    ? 'Mô tả mối quan hệ đồng tác giả...'
                    : coiType === 'COLLABORATOR'
                      ? 'Mô tả mối quan hệ cộng tác...'
                      : coiType === 'ADVISOR'
                        ? 'Mô tả mối quan hệ cố vấn...'
                        : coiType === 'INSTITUTIONAL'
                          ? 'Mô tả mối quan hệ tổ chức...'
                          : 'Mô tả lý do Conflict of Interest...'
                }
              />
            </div>
          )}

          <div className="d-flex justify-content-end gap-2">
            <CButton
              color="secondary"
              onClick={() => navigate('/app/pc/assignments')}
              disabled={saving}
            >
              Hủy
            </CButton>
            <CButton color="primary" type="submit" disabled={saving}>
              {saving ? <CSpinner size="sm" /> : 'Lưu'}
            </CButton>
          </div>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default COIDeclaration
