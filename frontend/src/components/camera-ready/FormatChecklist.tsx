import React, { useState } from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CAlert,
  CSpinner,
  CListGroup,
  CListGroupItem,
  CBadge,
} from '@coreui/react'
import { cameraReadyService, FormatCheckResult } from '../../services/camera-ready.service'

/**
 * FormatChecklist Props
 */
interface FormatChecklistProps {
  cameraReadyId: number
  submissionId: number
  formatChecked: boolean
  formatIssues?: string[]
}

/**
 * FormatChecklist - Component hiển thị format checklist và kết quả format check
 *
 * Features:
 * - Format checklist requirements
 * - Run format check
 * - Hiển thị format issues
 */
const FormatChecklist: React.FC<FormatChecklistProps> = ({
  cameraReadyId,
  submissionId,
  formatChecked,
  formatIssues,
}) => {
  const [checking, setChecking] = useState(false)
  const [checkResult, setCheckResult] = useState<FormatCheckResult | null>(null)
  const [error, setError] = useState('')

  const handleCheckFormat = async () => {
    try {
      setChecking(true)
      setError('')
      const result = await cameraReadyService.checkFormat(cameraReadyId)
      setCheckResult(result)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể kiểm tra format')
    } finally {
      setChecking(false)
    }
  }

  const formatRequirements = [
    'File PDF hợp lệ',
    'Kích thước trang A4',
    'Font size tối thiểu 10pt',
    'Margins đúng quy định',
    'Số trang không vượt quá giới hạn',
    'Không có watermark',
    'Metadata đúng format',
  ]

  return (
    <CCard>
      <CCardHeader>
        <div className="d-flex justify-content-between align-items-center">
          <h5>Format Checklist</h5>
          <CButton color="primary" onClick={handleCheckFormat} disabled={checking}>
            {checking ? <CSpinner size="sm" /> : 'Kiểm tra Format'}
          </CButton>
        </div>
      </CCardHeader>
      <CCardBody>
        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        <div className="mb-3">
          <h6>Yêu cầu format:</h6>
          <CListGroup>
            {formatRequirements.map((req, index) => (
              <CListGroupItem
                key={index}
                className="d-flex justify-content-between align-items-center"
              >
                {req}
                {checkResult && (
                  <CBadge color={checkResult.passed ? 'success' : 'danger'}>
                    {checkResult.passed ? '✓' : '✗'}
                  </CBadge>
                )}
              </CListGroupItem>
            ))}
          </CListGroup>
        </div>

        {formatChecked && !checkResult && (
          <CAlert color="info">
            Format đã được kiểm tra.{' '}
            {formatIssues && formatIssues.length > 0 && 'Có một số vấn đề.'}
          </CAlert>
        )}

        {formatIssues && formatIssues.length > 0 && (
          <div className="mb-3">
            <h6>Vấn đề format:</h6>
            <CListGroup>
              {formatIssues.map((issue, index) => (
                <CListGroupItem key={index} color="warning">
                  {issue}
                </CListGroupItem>
              ))}
            </CListGroup>
          </div>
        )}

        {checkResult && (
          <div className="mt-3">
            {checkResult.passed ? (
              <CAlert color="success">
                <strong>Format check passed!</strong> File đáp ứng tất cả yêu cầu format.
              </CAlert>
            ) : (
              <CAlert color="warning">
                <strong>Format check failed!</strong> Có {checkResult.issues.length} vấn đề cần sửa.
              </CAlert>
            )}

            {checkResult.issues.length > 0 && (
              <div className="mt-3">
                <h6>Chi tiết vấn đề:</h6>
                <CListGroup>
                  {checkResult.issues.map((issue, index) => (
                    <CListGroupItem
                      key={index}
                      color={
                        issue.type === 'ERROR'
                          ? 'danger'
                          : issue.type === 'WARNING'
                            ? 'warning'
                            : 'info'
                      }
                    >
                      <strong>{issue.type}:</strong> {issue.message}
                      {issue.page && <span className="ms-2">(Trang {issue.page})</span>}
                    </CListGroupItem>
                  ))}
                </CListGroup>
              </div>
            )}
          </div>
        )}
      </CCardBody>
    </CCard>
  )
}

export default FormatChecklist
