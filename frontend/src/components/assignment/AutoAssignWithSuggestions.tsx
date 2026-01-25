import React, { useState, useEffect } from 'react'
import {
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CButton,
  CFormLabel,
  CFormInput,
  CAlert,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
  CFormCheck,
} from '@coreui/react'
import {
  assignmentService,
  AssignmentSuggestion,
  AutoAssignRequestDTO,
  AutoAssignResponse,
} from '../../services/assignment.service'

interface AutoAssignWithSuggestionsProps {
  visible: boolean
  onClose: () => void
  submissionId: number
  onSuccess: (result: AutoAssignResponse) => void
}

/**
 * AutoAssignWithSuggestions - Component để hiển thị suggestions và thực hiện auto-assign
 */
const AutoAssignWithSuggestions: React.FC<AutoAssignWithSuggestionsProps> = ({
  visible,
  onClose,
  submissionId,
  onSuccess,
}) => {
  const [suggestions, setSuggestions] = useState<AssignmentSuggestion[]>([])
  const [loadingSuggestions, setLoadingSuggestions] = useState(false)
  const [numberOfReviewers, setNumberOfReviewers] = useState(3)
  const [selectedSuggestions, setSelectedSuggestions] = useState<Set<number>>(new Set())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (visible && submissionId) {
      loadSuggestions()
    }
  }, [visible, submissionId])

  const loadSuggestions = async () => {
    try {
      setLoadingSuggestions(true)
      setError('')
      const data = await assignmentService.getSuggestions(submissionId)
      setSuggestions(data)
      // Auto-select top N suggestions
      const topN = data.slice(0, numberOfReviewers).map((s) => s.reviewerId)
      setSelectedSuggestions(new Set(topN))
    } catch (error: any) {
      setError('Không thể tải suggestions: ' + (error.response?.data?.message || error.message))
    } finally {
      setLoadingSuggestions(false)
    }
  }

  const handleToggleSelection = (reviewerId: number) => {
    const newSelected = new Set(selectedSuggestions)
    if (newSelected.has(reviewerId)) {
      newSelected.delete(reviewerId)
    } else {
      newSelected.add(reviewerId)
    }
    setSelectedSuggestions(newSelected)
  }

  const handleAutoSelect = () => {
    const topN = suggestions.slice(0, numberOfReviewers).map((s) => s.reviewerId)
    setSelectedSuggestions(new Set(topN))
  }

  const handleSubmit = async () => {
    if (selectedSuggestions.size === 0) {
      setError('Vui lòng chọn ít nhất một reviewer')
      return
    }

    try {
      setLoading(true)
      setError('')
      const data: AutoAssignRequestDTO = {
        submissionId,
        numberOfReviewers: selectedSuggestions.size,
      }
      const result = await assignmentService.autoAssign(data)
      onSuccess(result)
      handleClose()
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          error.response?.data?.error ||
          'Không thể thực hiện auto-assign'
      )
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setSuggestions([])
    setSelectedSuggestions(new Set())
    setError('')
    setNumberOfReviewers(3)
    onClose()
  }

  const getScoreColor = (score: number) => {
    if (score >= 0.8) return 'success'
    if (score >= 0.6) return 'info'
    if (score >= 0.4) return 'warning'
    return 'secondary'
  }

  return (
    <CModal visible={visible} onClose={handleClose} size="xl">
      <CModalHeader>
        <CModalTitle>Auto Assign với Suggestions</CModalTitle>
      </CModalHeader>
      <CModalBody>
        {error && (
          <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
            {error}
          </CAlert>
        )}

        <div className="mb-3">
          <CFormLabel>Số reviewers muốn assign</CFormLabel>
          <CFormInput
            type="number"
            min="1"
            max="10"
            value={numberOfReviewers}
            onChange={(e) => {
              const value = parseInt(e.target.value) || 3
              setNumberOfReviewers(value)
              handleAutoSelect()
            }}
          />
          <CButton color="link" size="sm" onClick={handleAutoSelect} className="mt-2">
            Tự động chọn top {numberOfReviewers} suggestions
          </CButton>
        </div>

        {loadingSuggestions ? (
          <div className="text-center py-4">
            <CSpinner color="primary" />
            <p className="mt-2">Đang tải suggestions...</p>
          </div>
        ) : suggestions.length === 0 ? (
          <CAlert color="info">Không có suggestions nào cho submission này.</CAlert>
        ) : (
          <>
            <h6 className="mb-3">
              Đã chọn {selectedSuggestions.size} reviewer(s) / Tổng {suggestions.length} suggestions
            </h6>
            <CTable hover>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell style={{ width: '50px' }}>Chọn</CTableHeaderCell>
                  <CTableHeaderCell>Reviewer</CTableHeaderCell>
                  <CTableHeaderCell>Score</CTableHeaderCell>
                  <CTableHeaderCell>Lý do</CTableHeaderCell>
                  <CTableHeaderCell>COI</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {suggestions.map((suggestion) => (
                  <CTableRow key={suggestion.reviewerId}>
                    <CTableDataCell>
                      <CFormCheck
                        checked={selectedSuggestions.has(suggestion.reviewerId)}
                        onChange={() => handleToggleSelection(suggestion.reviewerId)}
                      />
                    </CTableDataCell>
                    <CTableDataCell>
                      <div>
                        <strong>{suggestion.reviewerName}</strong>
                        <br />
                        <small className="text-muted">{suggestion.reviewerEmail}</small>
                      </div>
                    </CTableDataCell>
                    <CTableDataCell>
                      <CBadge color={getScoreColor(suggestion.score)}>
                        {(suggestion.score * 100).toFixed(1)}%
                      </CBadge>
                    </CTableDataCell>
                    <CTableDataCell>
                      <small>{suggestion.reason}</small>
                    </CTableDataCell>
                    <CTableDataCell>
                      {suggestion.hasCOI ? (
                        <CBadge color="danger">Có COI</CBadge>
                      ) : (
                        <CBadge color="success">Không</CBadge>
                      )}
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          </>
        )}
      </CModalBody>
      <CModalFooter>
        <CButton color="secondary" onClick={handleClose} disabled={loading}>
          Hủy
        </CButton>
        <CButton
          color="primary"
          onClick={handleSubmit}
          disabled={loading || selectedSuggestions.size === 0 || loadingSuggestions}
        >
          {loading ? <CSpinner size="sm" /> : `Assign ${selectedSuggestions.size} Reviewer(s)`}
        </CButton>
      </CModalFooter>
    </CModal>
  )
}

export default AutoAssignWithSuggestions
