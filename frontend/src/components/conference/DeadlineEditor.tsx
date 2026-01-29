import React, { useState } from 'react'
import {
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CButton,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CFormSelect,
  CFormCheck,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPlus, cilPencil, cilTrash } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { Deadline } from '../../services/conference.service'

interface DeadlineEditorProps {
  deadlines: Deadline[]
  onChange: (deadlines: Deadline[]) => void
}

/**
 * DeadlineEditor - Component để quản lý deadlines (thêm, sửa, xóa)
 */
const DeadlineEditor: React.FC<DeadlineEditorProps> = ({ deadlines, onChange }) => {
  const { t } = useTranslation()
  const [showModal, setShowModal] = useState(false)
  const [editingIndex, setEditingIndex] = useState<number | null>(null)
  const [formData, setFormData] = useState<Deadline>({
    type: 'SUBMISSION',
    dueDate: '',
    description: '',
    hardDeadline: true,
  })

  const handleAdd = () => {
    setEditingIndex(null)
    setFormData({
      type: 'SUBMISSION',
      dueDate: '',
      description: '',
      hardDeadline: true,
    })
    setShowModal(true)
  }

  const handleEdit = (index: number) => {
    setEditingIndex(index)
    const deadline = deadlines[index]

    // Convert dueDate to datetime-local format (yyyy-MM-ddThh:mm)
    // We use local time, not UTC (toISOString) to avoid timezone shifts
    let dueDate = ''
    if (deadline.dueDate) {
      const d = new Date(deadline.dueDate)
      const pad = (n: number) => n.toString().padStart(2, '0')
      dueDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
    }

    setFormData({ ...deadline, dueDate })
    setShowModal(true)
  }

  const handleDelete = (index: number) => {
    if (window.confirm(t('conference.confirmDeleteDeadline'))) {
      const newDeadlines = deadlines.filter((_, i) => i !== index)
      onChange(newDeadlines)
    }
  }

  const handleSave = () => {
    if (!formData.dueDate) {
      alert(t('conference.deadlineDateRequired'))
      return
    }

    const newDeadlines = [...deadlines]

    // Ensure seconds are included for backend compatibility
    const dateStr = formData.dueDate.length === 16 ? `${formData.dueDate}:00` : formData.dueDate

    const deadlineToSave: Deadline = {
      ...formData,
      dueDate: dateStr,
    }
    if (editingIndex !== null) {
      newDeadlines[editingIndex] = deadlineToSave
    } else {
      newDeadlines.push(deadlineToSave)
    }
    onChange(newDeadlines)
    setShowModal(false)
    setFormData({
      type: 'SUBMISSION',
      dueDate: '',
      description: '',
      hardDeadline: true,
    })
  }

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleString('vi-VN')
    } catch {
      return dateString
    }
  }

  const getTypeLabel = (type: string) => {
    switch (type) {
      case 'SUBMISSION':
        return t('conference.deadlineSubmission') || 'Nộp bài'
      case 'REVIEW':
        return t('conference.deadlineReview') || 'Đánh giá'
      case 'CAMERA_READY':
        return t('conference.deadlineCameraReady') || 'Camera-ready'
      default:
        return type
    }
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h6>{t('conference.deadlines') || 'Deadlines'}</h6>
        <CButton color="primary" size="sm" onClick={handleAdd}>
          <CIcon icon={cilPlus} /> {t('conference.addDeadline') || 'Thêm Deadline'}
        </CButton>
      </div>

      {deadlines.length === 0 ? (
        <p className="text-muted">{t('conference.noDeadlines')}</p>
      ) : (
        <CTable hover responsive>
          <CTableHead>
            <CTableRow>
              <CTableHeaderCell>{t('conference.deadlineType') || 'Loại'}</CTableHeaderCell>
              <CTableHeaderCell>{t('conference.dueDate') || 'Hạn chót'}</CTableHeaderCell>
              <CTableHeaderCell>{t('conference.description') || 'Mô tả'}</CTableHeaderCell>
              <CTableHeaderCell>{t('conference.hardDeadline') || 'Hard Deadline'}</CTableHeaderCell>
              <CTableHeaderCell>{t('common.actions') || 'Thao tác'}</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {deadlines.map((deadline, index) => (
              <CTableRow key={index}>
                <CTableDataCell>{getTypeLabel(deadline.type)}</CTableDataCell>
                <CTableDataCell>{formatDate(deadline.dueDate)}</CTableDataCell>
                <CTableDataCell>{deadline.description || '-'}</CTableDataCell>
                <CTableDataCell>
                  {deadline.hardDeadline !== false ? (
                    <span className="badge bg-danger">
                      {t('conference.hard') || 'Hard'}
                    </span>
                  ) : (
                    <span className="badge bg-warning">
                      {t('conference.soft') || 'Soft'}
                    </span>
                  )}
                </CTableDataCell>
                <CTableDataCell>
                  <CButton
                    color="primary"
                    size="sm"
                    className="me-2"
                    onClick={() => handleEdit(index)}
                  >
                    <CIcon icon={cilPencil} />
                  </CButton>
                  <CButton color="danger" size="sm" onClick={() => handleDelete(index)}>
                    <CIcon icon={cilTrash} />
                  </CButton>
                </CTableDataCell>
              </CTableRow>
            ))}
          </CTableBody>
        </CTable>
      )}

      <CModal visible={showModal} onClose={() => setShowModal(false)}>
        <CModalHeader>
          <CModalTitle>
            {editingIndex !== null
              ? t('conference.editDeadline') || 'Sửa Deadline'
              : t('conference.addDeadline') || 'Thêm Deadline'}
          </CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="mb-3">
            <CFormLabel>
              {t('conference.deadlineType') || 'Loại Deadline'} <span className="text-danger">*</span>
            </CFormLabel>
            <CFormSelect
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value as Deadline['type'] })}
              required
            >
              <option value="SUBMISSION">{t('conference.deadlineSubmission') || 'Nộp bài'}</option>
              <option value="REVIEW">{t('conference.deadlineReview') || 'Đánh giá'}</option>
              <option value="CAMERA_READY">
                {t('conference.deadlineCameraReady') || 'Camera-ready'}
              </option>
            </CFormSelect>
          </div>
          <div className="mb-3">
            <CFormLabel>
              {t('conference.dueDate') || 'Hạn chót'} <span className="text-danger">*</span>
            </CFormLabel>
            <CFormInput
              type="datetime-local"
              value={formData.dueDate}
              onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
              required
            />
          </div>
          <div className="mb-3">
            <CFormLabel>{t('conference.description') || 'Mô tả'}</CFormLabel>
            <CFormTextarea
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              rows={3}
              placeholder={t('conference.deadlineDescriptionPlaceholder')}
            />
          </div>
          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="hardDeadline"
              label={t('conference.hardDeadline') || 'Hard Deadline (không cho phép nộp sau hạn)'}
              checked={formData.hardDeadline !== false}
              onChange={(e) => setFormData({ ...formData, hardDeadline: e.target.checked })}
            />
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowModal(false)}>
            {t('common.cancel') || 'Hủy'}
          </CButton>
          <CButton color="primary" onClick={handleSave}>
            {t('common.save') || 'Lưu'}
          </CButton>
        </CModalFooter>
      </CModal>
    </div>
  )
}

export default DeadlineEditor
