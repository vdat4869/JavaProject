import React, { useState } from 'react'
import {
  CButton,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CFormInput,
  CFormLabel,
  CFormCheck,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPlus, cilPencil, cilTrash, cilArrowTop, cilArrowBottom } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { SubmissionAuthor } from '../../services/submission.service'
import OrganizationSelect from '../common/OrganizationSelect'

interface AuthorEditorProps {
  authors: SubmissionAuthor[]
  onChange: (authors: SubmissionAuthor[]) => void
}

/**
 * AuthorEditor - Component để quản lý authors (thêm, sửa, xóa, sắp xếp)
 */
const AuthorEditor: React.FC<AuthorEditorProps> = ({ authors, onChange }) => {
  const { t } = useTranslation()
  const [showModal, setShowModal] = useState(false)
  const [editingIndex, setEditingIndex] = useState<number | null>(null)
  const [formData, setFormData] = useState<SubmissionAuthor>({
    firstName: '',
    lastName: '',
    email: '',
    affiliation: '',
    isCorresponding: false,
    orderIndex: 0,
  })

  const handleAdd = () => {
    setEditingIndex(null)
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      affiliation: '',
      isCorresponding: authors.length === 0, // First author is corresponding by default
      orderIndex: authors.length,
    })
    setShowModal(true)
  }

  const handleEdit = (index: number) => {
    setEditingIndex(index)
    setFormData({ ...authors[index] })
    setShowModal(true)
  }

  const handleDelete = (index: number) => {
    if (window.confirm(t('submission.confirmDeleteAuthor') || 'Bạn có chắc chắn muốn xóa tác giả này?')) {
      const newAuthors = authors.filter((_, i) => i !== index)
      // Reorder indices
      const reordered = newAuthors.map((author, i) => ({ ...author, orderIndex: i }))
      onChange(reordered)
    }
  }

  const handleSave = () => {
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      alert(t('submission.authorNameRequired') || 'Họ và tên là bắt buộc')
      return
    }

    const newAuthors = [...authors]
    if (editingIndex !== null) {
      // Update existing author
      newAuthors[editingIndex] = { ...formData }
      // If setting as corresponding, unset others
      if (formData.isCorresponding) {
        newAuthors.forEach((author, i) => {
          if (i !== editingIndex) {
            author.isCorresponding = false
          }
        })
      }
    } else {
      // Add new author
      const newAuthor = {
        ...formData,
        orderIndex: authors.length,
      }
      // If setting as corresponding, unset others
      if (formData.isCorresponding) {
        newAuthors.forEach((author) => {
          author.isCorresponding = false
        })
      }
      newAuthors.push(newAuthor)
    }
    onChange(newAuthors)
    setShowModal(false)
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      affiliation: '',
      isCorresponding: false,
      orderIndex: 0,
    })
  }

  const handleMoveUp = (index: number) => {
    if (index === 0) return
    const newAuthors = [...authors]
    const temp = newAuthors[index]
    newAuthors[index] = { ...newAuthors[index - 1], orderIndex: index }
    newAuthors[index - 1] = { ...temp, orderIndex: index - 1 }
    onChange(newAuthors)
  }

  const handleMoveDown = (index: number) => {
    if (index === authors.length - 1) return
    const newAuthors = [...authors]
    const temp = newAuthors[index]
    newAuthors[index] = { ...newAuthors[index + 1], orderIndex: index }
    newAuthors[index + 1] = { ...temp, orderIndex: index + 1 }
    onChange(newAuthors)
  }

  const handleSetCorresponding = (index: number) => {
    const newAuthors = authors.map((author, i) => ({
      ...author,
      isCorresponding: i === index,
    }))
    onChange(newAuthors)
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h6>{t('submission.authors') || 'Tác giả'}</h6>
        <CButton color="primary" size="sm" onClick={handleAdd}>
          <CIcon icon={cilPlus} /> {t('submission.addAuthor') || 'Thêm tác giả'}
        </CButton>
      </div>

      {authors.length === 0 ? (
        <p className="text-muted">{t('submission.noAuthors') || 'Chưa có tác giả nào'}</p>
      ) : (
        <CTable hover responsive>
          <CTableHead>
            <CTableRow>
              <CTableHeaderCell style={{ width: '50px' }}>#</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.authorName') || 'Họ tên'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.email') || 'Email'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.organization') || 'Tổ chức/Đơn vị'}</CTableHeaderCell>
              <CTableHeaderCell>{t('submission.corresponding') || 'Tác giả liên hệ'}</CTableHeaderCell>
              <CTableHeaderCell>{t('common.actions') || 'Thao tác'}</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {authors.map((author, index) => (
              <CTableRow key={index}>
                <CTableDataCell>{index + 1}</CTableDataCell>
                <CTableDataCell>
                  {author.firstName} {author.lastName}
                </CTableDataCell>
                <CTableDataCell>{author.email || '-'}</CTableDataCell>
                <CTableDataCell>{author.affiliation || '-'}</CTableDataCell>
                <CTableDataCell>
                  {author.isCorresponding ? (
                    <span className="badge bg-success">
                      {t('submission.corresponding') || 'Tác giả liên hệ'}
                    </span>
                  ) : (
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => handleSetCorresponding(index)}
                    >
                      {t('submission.setCorresponding') || 'Đặt làm tác giả liên hệ'}
                    </CButton>
                  )}
                </CTableDataCell>
                <CTableDataCell>
                  <div className="d-flex gap-1">
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => handleMoveUp(index)}
                      disabled={index === 0}
                      title={t('submission.moveUp') || 'Lên'}
                    >
                      <CIcon icon={cilArrowTop} />
                    </CButton>
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => handleMoveDown(index)}
                      disabled={index === authors.length - 1}
                      title={t('submission.moveDown') || 'Xuống'}
                    >
                      <CIcon icon={cilArrowBottom} />
                    </CButton>
                    <CButton
                      color="primary"
                      size="sm"
                      onClick={() => handleEdit(index)}
                      title={t('common.edit') || 'Sửa'}
                    >
                      <CIcon icon={cilPencil} />
                    </CButton>
                    <CButton
                      color="danger"
                      size="sm"
                      onClick={() => handleDelete(index)}
                      title={t('common.delete') || 'Xóa'}
                    >
                      <CIcon icon={cilTrash} />
                    </CButton>
                  </div>
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
              ? t('submission.editAuthor') || 'Sửa tác giả'
              : t('submission.addAuthor') || 'Thêm tác giả'}
          </CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="row mb-3">
            <div className="col-md-6">
              <CFormLabel>
                {t('submission.firstName') || 'Họ'} <span className="text-danger">*</span>
              </CFormLabel>
              <CFormInput
                type="text"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                placeholder={t('submission.firstNamePlaceholder') || 'Nhập họ'}
                required
              />
            </div>
            <div className="col-md-6">
              <CFormLabel>
                {t('submission.lastName') || 'Tên'} <span className="text-danger">*</span>
              </CFormLabel>
              <CFormInput
                type="text"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                placeholder={t('submission.lastNamePlaceholder') || 'Nhập tên'}
                required
              />
            </div>
          </div>
          <div className="mb-3">
            <CFormLabel>{t('submission.email') || 'Email'}</CFormLabel>
            <CFormInput
              type="email"
              value={formData.email || ''}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder={t('submission.emailPlaceholder') || 'email@example.com'}
            />
          </div>
          <div className="mb-3">
            <CFormLabel>{t('submission.organization') || 'Đơn vị công tác'}</CFormLabel>
            <OrganizationSelect
              value={undefined} // Since we store name in affiliation, we don't have ID here easily without lookup
              onChange={(_, name) => setFormData({ ...formData, affiliation: name || '' })}
              placeholder={t('submission.organizationPlaceholder') || 'Chọn trường/viện/tổ chức'}
            // If affiliation already exists, we might want to show it as searchTerm but OrganizationSelect currently syncs with value
            />
          </div>
          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="isCorresponding"
              label={t('submission.correspondingAuthor') || 'Tác giả liên hệ'}
              checked={formData.isCorresponding || false}
              onChange={(e) => setFormData({ ...formData, isCorresponding: e.target.checked })}
            />
            <small className="text-muted d-block">
              {t('submission.correspondingAuthorNote') ||
                'Tác giả liên hệ sẽ nhận được thông báo về submission'}
            </small>
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

export default AuthorEditor
