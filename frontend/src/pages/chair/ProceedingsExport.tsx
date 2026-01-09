import React, { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormLabel,
  CFormCheck,
  CButton,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { proceedingsService, ProceedingsExportRequest } from '../../services/proceedings.service'

/**
 * ProceedingsExport - Trang export proceedings cho CHAIR
 *
 * Features:
 * - Export proceedings (PDF, ZIP, BOTH)
 * - Options: include accepted only, include abstracts
 * - Download proceedings file
 */
const ProceedingsExport: React.FC = () => {
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [format, setFormat] = useState<'PDF' | 'ZIP' | 'BOTH'>('PDF')
  const [includeAcceptedOnly, setIncludeAcceptedOnly] = useState(true)
  const [includeAbstracts, setIncludeAbstracts] = useState(true)
  const [exporting, setExporting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleExport = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!conferenceId) {
      setError('Missing conferenceId')
      return
    }

    try {
      setExporting(true)
      setError('')
      const result = await proceedingsService.export({
        conferenceId,
        format,
        includeAcceptedOnly,
        includeAbstracts,
      })
      setSuccess(`Export thành công! File: ${result.fileName}`)

      // Download file
      await proceedingsService.download(result.downloadUrl, result.fileName)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể export proceedings')
    } finally {
      setExporting(false)
    }
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

  return (
    <CCard>
      <CCardHeader>
        <h4>Export Proceedings</h4>
      </CCardHeader>
      <CCardBody>
        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}
        {success && (
          <CAlert color="success" className="mb-3">
            {success}
          </CAlert>
        )}

        <CForm onSubmit={handleExport}>
          <div className="mb-3">
            <CFormLabel>Format *</CFormLabel>
            <select
              className="form-select"
              value={format}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setFormat(e.target.value as 'PDF' | 'ZIP' | 'BOTH')
              }
            >
              <option value="PDF">PDF</option>
              <option value="ZIP">ZIP</option>
              <option value="BOTH">PDF + ZIP</option>
            </select>
          </div>

          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="includeAcceptedOnly"
              label="Chỉ bao gồm các bài đã được chấp nhận"
              checked={includeAcceptedOnly}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setIncludeAcceptedOnly(e.target.checked)
              }
            />
          </div>

          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="includeAbstracts"
              label="Bao gồm abstracts"
              checked={includeAbstracts}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setIncludeAbstracts(e.target.checked)
              }
            />
          </div>

          <CButton type="submit" color="primary" disabled={exporting}>
            {exporting ? <CSpinner size="sm" /> : 'Export Proceedings'}
          </CButton>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default ProceedingsExport
