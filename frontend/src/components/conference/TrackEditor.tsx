import React, { useState } from 'react'
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
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CFormCheck,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CSpinner,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPlus, cilPencil, cilTrash, cilCheck, cilX } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { Track } from '../../services/conference.service'

interface TrackEditorProps {
  tracks: Track[]
  onChange: (tracks: Track[]) => void
}

/**
 * TrackEditor - Component để quản lý tracks (thêm, sửa, xóa)
 */
const TrackEditor: React.FC<TrackEditorProps> = ({ tracks, onChange }) => {
  const { t } = useTranslation()
  const [showModal, setShowModal] = useState(false)
  const [editingIndex, setEditingIndex] = useState<number | null>(null)
  const [formData, setFormData] = useState<Track>({
    name: '',
    description: '',
    active: true,
  })

  const handleAdd = () => {
    setEditingIndex(null)
    setFormData({ name: '', description: '', active: true })
    setShowModal(true)
  }

  const handleEdit = (index: number) => {
    setEditingIndex(index)
    setFormData({ ...tracks[index] })
    setShowModal(true)
  }

  const handleDelete = (index: number) => {
    if (window.confirm(t('conference.confirmDeleteTrack') || 'Bạn có chắc chắn muốn xóa track này?')) {
      const newTracks = tracks.filter((_, i) => i !== index)
      onChange(newTracks)
    }
  }

  const handleSave = () => {
    if (!formData.name.trim()) {
      alert(t('conference.trackNameRequired') || 'Tên track là bắt buộc')
      return
    }

    const newTracks = [...tracks]
    if (editingIndex !== null) {
      newTracks[editingIndex] = { ...formData }
    } else {
      newTracks.push({ ...formData })
    }
    onChange(newTracks)
    setShowModal(false)
    setFormData({ name: '', description: '', active: true })
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h6>{t('conference.tracks') || 'Tracks'}</h6>
        <CButton color="primary" size="sm" onClick={handleAdd}>
          <CIcon icon={cilPlus} /> {t('conference.addTrack') || 'Thêm Track'}
        </CButton>
      </div>

      {tracks.length === 0 ? (
        <p className="text-muted">{t('conference.noTracks') || 'Chưa có track nào'}</p>
      ) : (
        <CTable hover responsive>
          <CTableHead>
            <CTableRow>
              <CTableHeaderCell>{t('conference.trackName') || 'Tên'}</CTableHeaderCell>
              <CTableHeaderCell>{t('conference.description') || 'Mô tả'}</CTableHeaderCell>
              <CTableHeaderCell>{t('common.status') || 'Trạng thái'}</CTableHeaderCell>
              <CTableHeaderCell>{t('common.actions') || 'Thao tác'}</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {tracks.map((track, index) => (
              <CTableRow key={index}>
                <CTableDataCell>{track.name}</CTableDataCell>
                <CTableDataCell>{track.description || '-'}</CTableDataCell>
                <CTableDataCell>
                  {track.active !== false ? (
                    <CIcon icon={cilCheck} className="text-success" />
                  ) : (
                    <CIcon icon={cilX} className="text-danger" />
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
              ? t('conference.editTrack') || 'Sửa Track'
              : t('conference.addTrack') || 'Thêm Track'}
          </CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="mb-3">
            <CFormLabel>
              {t('conference.trackName') || 'Tên Track'} <span className="text-danger">*</span>
            </CFormLabel>
            <CFormInput
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder={t('conference.trackNamePlaceholder') || 'Nhập tên track'}
              required
            />
          </div>
          <div className="mb-3">
            <CFormLabel>{t('conference.description') || 'Mô tả'}</CFormLabel>
            <CFormTextarea
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              rows={3}
              placeholder={t('conference.trackDescriptionPlaceholder') || 'Mô tả về track...'}
            />
          </div>
          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="trackActive"
              label={t('conference.active') || 'Hoạt động'}
              checked={formData.active !== false}
              onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
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

export default TrackEditor
