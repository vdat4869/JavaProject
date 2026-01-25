import React, { useState, useEffect, useCallback } from 'react'
import {
  CForm,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CButton,
  CAlert,
  CInputGroup,
  CInputGroupText,
  CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { conferenceService, CFP, Track } from '../../services/conference.service'
import AuthorEditor from './AuthorEditor'
import { SubmissionAuthor } from '../../services/submission.service'

/**
 * SubmissionForm Props
 */
interface SubmissionFormProps {
  initialData?: {
    title?: string
    abstract?: string
    keywords?: string[]
    trackId?: number
    authors?: SubmissionAuthor[]
  }
  conferenceId: number
  onSubmit: (data: {
    title: string
    abstract: string
    keywords: string[]
    trackId?: number
    file?: File
    authors?: SubmissionAuthor[]
  }) => Promise<void>
  onCancel: () => void
  loading?: boolean
}

/**
 * SubmissionForm - Form component cho create/edit submission
 *
 * Features:
 * - Title, Abstract, Keywords input
 * - Track selection
 * - PDF file upload
 * - Validation
 */
const SubmissionForm: React.FC<SubmissionFormProps> = ({
  initialData,
  conferenceId,
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const { t } = useTranslation()
  const [title, setTitle] = useState(initialData?.title || '')
  const [abstract, setAbstract] = useState(initialData?.abstract || '')
  const [keywords, setKeywords] = useState(initialData?.keywords?.join(', ') || '')
  const [trackId, setTrackId] = useState<number | undefined>(initialData?.trackId)
  const [file, setFile] = useState<File | null>(null)
  const [authors, setAuthors] = useState<SubmissionAuthor[]>(initialData?.authors || [])
  const [cfp, setCfp] = useState<CFP | null>(null)
  const [loadingCfp, setLoadingCfp] = useState(true)
  const [error, setError] = useState('')

  const loadCFP = useCallback(async () => {
    try {
      setLoadingCfp(true)
      const cfpData = await conferenceService.getCFP(conferenceId)
      setCfp(cfpData)
    } catch (error) {
      console.error('Error loading CFP:', error)
      setError('Không thể tải thông tin CFP')
    } finally {
      setLoadingCfp(false)
    }
  }, [conferenceId])

  useEffect(() => {
    void loadCFP()
  }, [loadCFP])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!title.trim()) {
      setError('Vui lòng nhập tiêu đề')
      return
    }

    if (!abstract.trim()) {
      setError('Vui lòng nhập tóm tắt')
      return
    }

    // File is optional for create (can upload later)
    // File is optional for update (only upload if changing)

    const keywordsArray = keywords
      .split(',')
      .map((k) => k.trim())
      .filter((k) => k.length > 0)

    try {
      await onSubmit({
        title: title.trim(),
        abstract: abstract.trim(),
        keywords: keywordsArray,
        trackId,
        file: file || undefined, // Optional file
        authors: authors.length > 0 ? authors : undefined,
      })
    } catch (err: any) {
      setError(err.message || 'Có lỗi xảy ra')
    }
  }

  if (loadingCfp) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  return (
    <CForm onSubmit={handleSubmit}>
      {error && (
        <CAlert color="danger" className="mb-3">
          {error}
        </CAlert>
      )}

      <div className="mb-3">
        <CFormLabel>Tiêu đề *</CFormLabel>
        <CFormInput
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          placeholder="Nhập tiêu đề bài báo"
        />
      </div>

      <div className="mb-3">
        <CFormLabel>Tóm tắt *</CFormLabel>
        <CFormTextarea
          value={abstract}
          onChange={(e) => setAbstract(e.target.value)}
          required
          rows={8}
          placeholder="Nhập tóm tắt bài báo"
        />
      </div>

      <div className="mb-3">
        <CFormLabel>Từ khóa (phân cách bằng dấu phẩy)</CFormLabel>
        <CFormInput
          type="text"
          value={keywords}
          onChange={(e) => setKeywords(e.target.value)}
          placeholder="keyword1, keyword2, keyword3"
        />
      </div>

      {cfp && cfp.tracks.length > 0 && (
        <div className="mb-3">
          <CFormLabel>Lĩnh vực</CFormLabel>
          <select
            className="form-select"
            value={trackId || ''}
            onChange={(e) => setTrackId(e.target.value ? parseInt(e.target.value) : undefined)}
          >
            <option value="">Chọn lĩnh vực</option>
            {cfp.tracks.map((track) => (
              <option key={track.id} value={track.id}>
                {track.name}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Authors Section */}
      <div className="mb-3">
        <AuthorEditor authors={authors} onChange={setAuthors} />
      </div>

      {!initialData && (
        <div className="mb-3">
          <CFormLabel>File PDF (tùy chọn)</CFormLabel>
          <CFormInput
            type="file"
            accept=".pdf"
            onChange={(e) => {
              const selectedFile = e.target.files?.[0]
              if (selectedFile) {
                if (selectedFile.size > 20 * 1024 * 1024) {
                  setError('File không được vượt quá 20MB')
                  return
                }
                setFile(selectedFile)
              }
            }}
          />
          <small className="text-muted">File PDF, tối đa 20MB. Có thể upload sau khi tạo submission.</small>
        </div>
      )}

      {initialData && (
        <div className="mb-3">
          <CFormLabel>File PDF mới (tùy chọn)</CFormLabel>
          <CFormInput
            type="file"
            accept=".pdf"
            onChange={(e) => {
              const selectedFile = e.target.files?.[0]
              if (selectedFile) {
                if (selectedFile.size > 20 * 1024 * 1024) {
                  setError('File không được vượt quá 20MB')
                  return
                }
                setFile(selectedFile)
              }
            }}
          />
          <small className="text-muted">Để trống nếu không muốn thay đổi file</small>
        </div>
      )}

      <div className="d-flex justify-content-end gap-2">
        <CButton color="secondary" onClick={onCancel} disabled={loading}>
          Hủy
        </CButton>
        <CButton color="primary" type="submit" disabled={loading}>
          {loading ? <CSpinner size="sm" /> : 'Lưu'}
        </CButton>
      </div>
    </CForm>
  )
}

export default SubmissionForm
