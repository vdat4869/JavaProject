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
import { aiService } from '../../services/ai.service'
import AIAssistantOverlay from '../ai/AIAssistantOverlay'
import AISuggestionModal from '../ai/AISuggestionModal'
import AIKeywordPicker from '../ai/AIKeywordPicker'
import { cilCheckAlt, cilBrush, cilTags } from '@coreui/icons'

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

  // AI State
  const [aiLoading, setAiLoading] = useState(false)
  const [aiSuggestions, setAiSuggestions] = useState<any[]>([])
  const [aiKeywords, setAiKeywords] = useState<any[]>([])
  const [showAiModal, setShowAiModal] = useState(false)
  const [showKeywordPicker, setShowKeywordPicker] = useState(false)
  const [aiModalTitle, setAiModalTitle] = useState('')

  const loadCFP = useCallback(async () => {
    try {
      setLoadingCfp(true)
      const cfpData = await conferenceService.getCFP(conferenceId)
      setCfp(cfpData as any)
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

  // AI Actions
  const handleAIAction = async (field: 'abstract' | 'keywords', action: string) => {
    try {
      setAiLoading(true)
      setError('')

      if (action === 'spell_check') {
        const result = await aiService.spellCheck({
          conferenceId,
          abstractText: abstract,
          title: title,
          language: t('common.locale') === 'vi' ? 'vi' : 'en'
        })
        if (result.success && result.suggestions.length > 0) {
          setAiSuggestions(result.suggestions.map(s => ({
            before: s.original,
            after: s.replacement,
            explanation: s.explanation,
            changeType: s.type,
            field: s.field
          })))
          setAiModalTitle(t('ai.spellCheck'))
          setShowAiModal(true)
        } else {
          alert(t('ai.noIssues'))
        }
      } else if (action === 'polish') {
        const result = await aiService.abstractPolish({
          conferenceId,
          abstractText: abstract,
          language: t('common.locale') === 'vi' ? 'vi' : 'en'
        })
        if (result.success) {
          setAiSuggestions(result.changes)
          setAiModalTitle(t('ai.abstractPolish'))
          setShowAiModal(true)
        }
      } else if (action === 'suggest_keywords') {
        const result = await aiService.keywordSuggest({
          conferenceId,
          abstractText: abstract,
          title: title,
        })
        if (result.success) {
          setAiKeywords(result.keywords)
          setShowKeywordPicker(true)
        }
      }
    } catch (err: any) {
      setError(t('ai.error', { message: err.message }))
    } finally {
      setAiLoading(false)
    }
  }

  const applyAISuggestions = (selectedIndices: number[]) => {
    let newAbstract = abstract
    let newTitle = title

    // Áp dụng từ dưới lên để không làm hỏng index (nếu AI có trả về index, 
    // hiện tại chúng ta thay thế dựa trên text match nên cần cẩn thận)
    selectedIndices.forEach(idx => {
      const suggestion = aiSuggestions[idx]
      if (suggestion.field === 'title') {
        newTitle = newTitle.replace(suggestion.before, suggestion.after)
      } else {
        newAbstract = newAbstract.replace(suggestion.before, suggestion.after)
      }
    })

    setAbstract(newAbstract)
    setTitle(newTitle)
  }

  const applyAIKeywords = (selectedKeywords: string[]) => {
    const currentKeywordsArray = keywords.split(',').map(k => k.trim()).filter(k => k)
    const combined = Array.from(new Set([...currentKeywordsArray, ...selectedKeywords]))
    setKeywords(combined.join(', '))
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

      <div className="mb-3 position-relative">
        <CFormLabel>Tóm tắt *</CFormLabel>
        <AIAssistantOverlay
          loading={aiLoading}
          onAction={(action) => handleAIAction('abstract', action)}
          actions={[
            { code: 'spell_check', label: t('ai.spellCheck'), icon: cilCheckAlt },
            { code: 'polish', label: t('ai.abstractPolish'), icon: cilBrush },
          ]}
        />
        <CFormTextarea
          value={abstract}
          onChange={(e) => setAbstract(e.target.value)}
          required
          rows={8}
          placeholder="Nhập tóm tắt bài báo"
        />
      </div>

      <div className="mb-3 position-relative">
        <CFormLabel>Từ khóa (phân cách bằng dấu phẩy)</CFormLabel>
        <AIAssistantOverlay
          loading={aiLoading}
          onAction={(action) => handleAIAction('keywords', action)}
          actions={[
            { code: 'suggest_keywords', label: t('ai.suggestKeywords'), icon: cilTags },
          ]}
        />
        <CFormInput
          type="text"
          value={keywords}
          onChange={(e) => setKeywords(e.target.value)}
          placeholder="keyword1, keyword2, keyword3"
        />
      </div>

      {cfp && cfp.tracks && cfp.tracks.length > 0 && (
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

      <AISuggestionModal
        visible={showAiModal}
        onClose={() => setShowAiModal(false)}
        title={aiModalTitle}
        recommendations={aiSuggestions}
        onApply={applyAISuggestions}
      />

      <AIKeywordPicker
        visible={showKeywordPicker}
        onClose={() => setShowKeywordPicker(false)}
        keywords={aiKeywords}
        onApply={applyAIKeywords}
      />
    </CForm>
  )
}

export default SubmissionForm
