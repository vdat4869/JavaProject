import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormCheck,
  CFormTextarea,
  CFormLabel,
  CButton,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { pcService, COIDeclaration } from '../../services/pc.service'
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
  const [hasCOI, setHasCOI] = useState(false)
  const [reason, setReason] = useState('')
  const [existingCOI, setExistingCOI] = useState<COIDeclaration | null>(null)
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
      const [coiData, assignments] = await Promise.all([
        pcService.getCOI(submissionId!),
        reviewService.getAssignments(),
      ])

      setExistingCOI(coiData)
      const assignmentData = assignments.find((a) => a.submissionId === submissionId)
      setAssignment(assignmentData || null)

      if (coiData) {
        setHasCOI(coiData.hasCOI)
        setReason(coiData.reason || '')
      }
    } catch (error) {
      console.error('Error loading COI data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (hasCOI && !reason.trim()) {
      setError('Vui lòng nhập lý do COI')
      return
    }

    try {
      setSaving(true)
      await pcService.declareCOI({
        submissionId: submissionId!,
        hasCOI,
        reason: hasCOI ? reason.trim() : undefined,
      })
      navigate('/pc/assignments')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể khai báo COI')
    } finally {
      setSaving(false)
    }
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
            Bạn đã khai báo COI vào: {new Date(existingCOI.declaredAt).toLocaleString('vi-VN')}
          </CAlert>
        )}

        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        <CForm onSubmit={handleSubmit}>
          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="hasCOI"
              label="Tôi có Conflict of Interest với bài báo này"
              checked={hasCOI}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setHasCOI(e.target.checked)}
            />
          </div>

          {hasCOI && (
            <div className="mb-3">
              <CFormLabel>Lý do COI *</CFormLabel>
              <CFormTextarea
                value={reason}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setReason(e.target.value)}
                required={hasCOI}
                rows={5}
                placeholder="Mô tả lý do Conflict of Interest (ví dụ: đồng tác giả, cố vấn, quan hệ tài chính, ...)"
              />
            </div>
          )}

          <div className="d-flex justify-content-end gap-2">
            <CButton
              color="secondary"
              onClick={() => navigate('/pc/assignments')}
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
