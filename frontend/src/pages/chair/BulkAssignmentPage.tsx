import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CFormInput,
  CFormLabel,
  CFormTextarea,
  CAlert,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
  CFormSelect,
} from '@coreui/react'
import {
  assignmentService,
  BulkAssignRequestDTO,
  BulkAssignResponse,
  AssignmentCreateDTO,
} from '../../services/assignment.service'
import { pcService, PCMember } from '../../services/pc.service'
import { submissionService, Submission } from '../../services/submission.service'

/**
 * BulkAssignmentPage - Page để bulk assign reviewers cho nhiều submissions
 *
 * Note: This page requires manual input of submission IDs since there's no API
 * to list all submissions for a conference. Once the backend API is available,
 * this can be enhanced to show a list of submissions to select from.
 */
const BulkAssignmentPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const conferenceId = id ? parseInt(id) : null
  const [pcMembers, setPCMembers] = useState<PCMember[]>([])
  const [submissions, setSubmissions] = useState<Submission[]>([])
  const [loadingMembers, setLoadingMembers] = useState(false)
  const [loadingSubmissions, setLoadingSubmissions] = useState(false)
  const [submissionIdsInput, setSubmissionIdsInput] = useState('')
  const [assignments, setAssignments] = useState<AssignmentCreateDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [result, setResult] = useState<BulkAssignResponse | null>(null)
  const [useManualInput, setUseManualInput] = useState(false)

  useEffect(() => {
    if (conferenceId) {
      loadPCMembers()
      loadSubmissions()
    }
  }, [conferenceId])

  const loadPCMembers = async () => {
    try {
      setLoadingMembers(true)
      const members = await pcService.getPCMembers(conferenceId!)
      const acceptedMembers = members.filter((m) => m.status === 'ACCEPTED')
      setPCMembers(acceptedMembers)
    } catch (error: any) {
      setError('Không thể tải danh sách PC members')
    } finally {
      setLoadingMembers(false)
    }
  }

  const loadSubmissions = async () => {
    try {
      setLoadingSubmissions(true)
      setError('')
      const data = await submissionService.getSubmissionsByConference(conferenceId!)
      setSubmissions(data)
    } catch (error: any) {
      console.error('Error loading submissions:', error)
      // If API fails, fall back to manual input
      setUseManualInput(true)
      setError('Không thể tải danh sách submissions. Vui lòng sử dụng chế độ nhập thủ công.')
    } finally {
      setLoadingSubmissions(false)
    }
  }

  const parseSubmissionIds = (input: string): number[] => {
    // Support comma-separated or newline-separated IDs
    return input
      .split(/[,\n]/)
      .map((id) => id.trim())
      .filter((id) => id.length > 0)
      .map((id) => parseInt(id))
      .filter((id) => !isNaN(id))
  }

  const handleAddAssignment = () => {
    const ids = parseSubmissionIds(submissionIdsInput)
    if (ids.length === 0) {
      setError('Vui lòng nhập ít nhất một submission ID')
      return
    }

    // Filter out submissions that are already in assignments
    const existingIds = new Set(assignments.map((a) => a.submissionId))
    const newIds = ids.filter((id) => !existingIds.has(id))

    if (newIds.length === 0) {
      setError('Tất cả submission IDs đã được thêm vào danh sách')
      return
    }

    // Create assignments for all submission IDs with first reviewer (default)
    // User can edit the reviewer for each assignment
    const newAssignments: AssignmentCreateDTO[] = newIds.map((submissionId) => ({
      submissionId,
      reviewerId: pcMembers.length > 0 ? pcMembers[0].userId : 0,
      isPrimary: false,
    }))

    setAssignments([...assignments, ...newAssignments])
    setSubmissionIdsInput('')
    setError('')
  }

  const handleSelectAll = () => {
    const existingIds = new Set(assignments.map((a) => a.submissionId))
    const newAssignments: AssignmentCreateDTO[] = submissions
      .filter((s) => !existingIds.has(s.id))
      .map((submission) => ({
        submissionId: submission.id,
        reviewerId: pcMembers.length > 0 ? pcMembers[0].userId : 0,
        isPrimary: false,
      }))
    setAssignments([...assignments, ...newAssignments])
  }

  const handleDeselectAll = () => {
    setAssignments([])
  }

  const handleRemoveAssignment = (index: number) => {
    setAssignments(assignments.filter((_, i) => i !== index))
  }

  const handleUpdateAssignment = (index: number, field: keyof AssignmentCreateDTO, value: any) => {
    const updated = [...assignments]
    updated[index] = { ...updated[index], [field]: value }
    setAssignments(updated)
  }

  const handleSubmit = async () => {
    if (assignments.length === 0) {
      setError('Vui lòng thêm ít nhất một assignment')
      return
    }

    // Validate all assignments have valid reviewer IDs
    const invalidAssignments = assignments.filter(
      (a) => !a.reviewerId || a.reviewerId === 0
    )
    if (invalidAssignments.length > 0) {
      setError('Vui lòng chọn reviewer cho tất cả assignments')
      return
    }

    try {
      setLoading(true)
      setError('')
      setSuccess('')
      setResult(null)

      const data: BulkAssignRequestDTO = {
        assignments,
      }

      const response = await assignmentService.bulkAssign(data)
      setResult(response)
      setSuccess(
        `Đã tạo ${response.totalCreated} assignments thành công. ${response.totalFailed > 0 ? `${response.totalFailed} assignments thất bại.` : ''}`
      )
      setAssignments([])
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          error.response?.data?.error ||
          'Không thể thực hiện bulk assignment'
      )
    } finally {
      setLoading(false)
    }
  }

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing conference ID</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Bulk Assignment</h4>
            <CButton color="secondary" onClick={() => navigate(-1)}>
              Quay lại
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {error && (
            <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
              {error}
            </CAlert>
          )}
          {success && (
            <CAlert color="success" className="mb-3" onClose={() => setSuccess('')} dismissible>
              {success}
            </CAlert>
          )}

          {loadingSubmissions ? (
            <div className="text-center py-4">
              <CSpinner color="primary" />
              <p className="mt-2">Đang tải danh sách submissions...</p>
            </div>
          ) : submissions.length > 0 && !useManualInput ? (
            <>
              <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <CFormLabel className="mb-0">
                    Chọn Submissions ({submissions.length} submissions)
                  </CFormLabel>
                  <div>
                    <CButton color="link" size="sm" onClick={handleSelectAll}>
                      Chọn tất cả
                    </CButton>
                    <CButton color="link" size="sm" onClick={handleDeselectAll}>
                      Bỏ chọn tất cả
                    </CButton>
                  </div>
                </div>
                <div className="border rounded p-3" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  {submissions.map((submission) => {
                    const isSelected = assignments.some((a) => a.submissionId === submission.id)
                    return (
                      <div key={submission.id} className="mb-2 p-2 border-bottom">
                        <input
                          type="checkbox"
                          id={`submission-${submission.id}`}
                          checked={isSelected}
                          onChange={(e) => {
                            if (e.target.checked) {
                              // Add to assignments
                              setAssignments([
                                ...assignments,
                                {
                                  submissionId: submission.id,
                                  reviewerId: pcMembers.length > 0 ? pcMembers[0].userId : 0,
                                  isPrimary: false,
                                },
                              ])
                            } else {
                              // Remove from assignments
                              setAssignments(assignments.filter((a) => a.submissionId !== submission.id))
                            }
                          }}
                          className="form-check-input me-2"
                        />
                        <label htmlFor={`submission-${submission.id}`} className="form-check-label">
                          <strong>#{submission.id}</strong> - {submission.title}
                          <br />
                          <small className="text-muted">
                            Status: {submission.status}
                            {submission.trackName && ` | Track: ${submission.trackName}`}
                          </small>
                        </label>
                      </div>
                    )
                  })}
                </div>
                <CButton
                  color="link"
                  size="sm"
                  onClick={() => setUseManualInput(true)}
                  className="mt-2"
                >
                  Hoặc nhập IDs thủ công
                </CButton>
              </div>
            </>
          ) : (
            <>
              <div className="mb-3">
                <CAlert color="info">
                  <strong>Nhập Submission IDs thủ công</strong>
                </CAlert>
                <CFormLabel>Nhập Submission IDs</CFormLabel>
                <CFormTextarea
                  value={submissionIdsInput}
                  onChange={(e) => setSubmissionIdsInput(e.target.value)}
                  rows={3}
                  placeholder="Nhập submission IDs, cách nhau bởi dấu phẩy hoặc xuống dòng (ví dụ: 1, 2, 3 hoặc 1&#10;2&#10;3)"
                />
                <small className="text-muted">
                  Ví dụ: 1, 2, 3 hoặc mỗi ID trên một dòng
                </small>
              </div>
              <CButton color="primary" onClick={handleAddAssignment} className="mb-4">
                Thêm vào danh sách
              </CButton>
              {submissions.length > 0 && (
                <CButton
                  color="link"
                  size="sm"
                  onClick={() => setUseManualInput(false)}
                  className="mb-4"
                >
                  Hoặc chọn từ danh sách submissions
                </CButton>
              )}
            </>
          )}

          {assignments.length > 0 && (
            <>
              <h5 className="mb-3">Danh sách Assignments ({assignments.length})</h5>
              <CTable hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Submission ID</CTableHeaderCell>
                    <CTableHeaderCell>Reviewer</CTableHeaderCell>
                    <CTableHeaderCell>Primary</CTableHeaderCell>
                    <CTableHeaderCell>Thao tác</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {assignments.map((assignment, index) => (
                    <CTableRow key={index}>
                      <CTableDataCell>
                        <strong>#{assignment.submissionId}</strong>
                      </CTableDataCell>
                      <CTableDataCell>
                        {loadingMembers ? (
                          <CSpinner size="sm" />
                        ) : (
                          <CFormSelect
                            value={assignment.reviewerId || ''}
                            onChange={(e) =>
                              handleUpdateAssignment(
                                index,
                                'reviewerId',
                                parseInt(e.target.value)
                              )
                            }
                            size="sm"
                          >
                            <option value="">-- Chọn reviewer --</option>
                            {pcMembers.map((member) => (
                              <option key={member.id} value={member.userId}>
                                {member.fullName} ({member.email})
                              </option>
                            ))}
                          </CFormSelect>
                        )}
                      </CTableDataCell>
                      <CTableDataCell>
                        <input
                          type="checkbox"
                          checked={assignment.isPrimary || false}
                          onChange={(e) =>
                            handleUpdateAssignment(index, 'isPrimary', e.target.checked)
                          }
                          className="form-check-input"
                        />
                      </CTableDataCell>
                      <CTableDataCell>
                        <CButton
                          color="danger"
                          size="sm"
                          onClick={() => handleRemoveAssignment(index)}
                        >
                          Xóa
                        </CButton>
                      </CTableDataCell>
                    </CTableRow>
                  ))}
                </CTableBody>
              </CTable>

              <div className="mt-4">
                <CButton
                  color="success"
                  onClick={handleSubmit}
                  disabled={loading || assignments.length === 0}
                >
                  {loading ? <CSpinner size="sm" /> : `Tạo ${assignments.length} Assignment(s)`}
                </CButton>
              </div>
            </>
          )}

          {result && result.failedAssignments.length > 0 && (
            <div className="mt-4">
              <h5>Failed Assignments</h5>
              <CTable>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Submission ID</CTableHeaderCell>
                    <CTableHeaderCell>Reviewer ID</CTableHeaderCell>
                    <CTableHeaderCell>Lý do</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {result.failedAssignments.map((failed, index) => (
                    <CTableRow key={index}>
                      <CTableDataCell>#{failed.submissionId}</CTableDataCell>
                      <CTableDataCell>{failed.reviewerId}</CTableDataCell>
                      <CTableDataCell>
                        <CBadge color="danger">{failed.reason}</CBadge>
                      </CTableDataCell>
                    </CTableRow>
                  ))}
                </CTableBody>
              </CTable>
            </div>
          )}
        </CCardBody>
      </CCard>
    </>
  )
}

export default BulkAssignmentPage
