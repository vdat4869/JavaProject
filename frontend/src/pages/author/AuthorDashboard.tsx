import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CButton, CRow, CCol, CSpinner, CBadge } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { submissionService, Submission } from '../../services/submission.service'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'

/**
 * AuthorDashboard - Dashboard cho Author
 *
 * Features:
 * - Hiển thị thống kê submissions
 * - Danh sách conferences đang mở
 * - Quick actions
 */
const AuthorDashboard: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [submissions, setSubmissions] = useState<Submission[]>([])
  const [conferences, setConferences] = useState<ConferenceResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [submissionsData, conferencesData] = await Promise.all([
        submissionService.getMySubmissions(),
        conferenceService.getConferences(),
      ])
      setSubmissions(Array.isArray(submissionsData) ? submissionsData : [])
      setConferences(Array.isArray(conferencesData) ? conferencesData : []) // conferencesData are already public/active
    } catch (error) {
      console.error('Error loading dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadge = (status: Submission['status']) => {
    const colorMap: Record<string, string> = {
      DRAFT: 'secondary',
      SUBMITTED: 'info',
      UNDER_REVIEW: 'warning',
      REVIEWED: 'primary',
      ACCEPTED: 'success',
      REJECTED: 'danger',
      WITHDRAWN: 'dark',
      CAMERA_READY: 'success',
    }
    return <CBadge color={colorMap[status] || 'secondary'}>{status}</CBadge>
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  const activeSubmissions = submissions.filter((s) => s.status !== 'WITHDRAWN')
  const recentSubmissions = submissions.slice(0, 5)

  return (
    <>
      <CRow className="mb-4">
        <CCol md={4}>
          <CCard>
            <CCardHeader>
              <h5>Tổng số bài nộp</h5>
            </CCardHeader>
            <CCardBody>
              <h2>{activeSubmissions.length}</h2>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={4}>
          <CCard>
            <CCardHeader>
              <h5>Đang được đánh giá</h5>
            </CCardHeader>
            <CCardBody>
              <h2>{submissions.filter((s) => s.status === 'UNDER_REVIEW').length}</h2>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={4}>
          <CCard>
            <CCardHeader>
              <h5>Đã chấp nhận</h5>
            </CCardHeader>
            <CCardBody>
              <h2>{submissions.filter((s) => s.status === 'ACCEPTED').length}</h2>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      <CRow>
        <CCol md={8}>
          <CCard>
            <CCardHeader>
              <div className="d-flex justify-content-between align-items-center">
                <h5>Bài nộp gần đây</h5>
                <CButton color="primary" size="sm" onClick={() => navigate('/app/author/submissions')}>
                  Xem tất cả
                </CButton>
              </div>
            </CCardHeader>
            <CCardBody>
              {recentSubmissions.length === 0 ? (
                <p className="text-muted">Chưa có bài nộp nào</p>
              ) : (
                <div className="table-responsive">
                  <table className="table table-hover">
                    <thead>
                      <tr>
                        <th>Tiêu đề</th>
                        <th>Hội nghị</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentSubmissions.map((submission) => (
                        <tr key={submission.id}>
                          <td>{submission.title}</td>
                          <td>{submission.conferenceName}</td>
                          <td>{getStatusBadge(submission.status)}</td>
                          <td>
                            <CButton
                              color="link"
                              size="sm"
                              onClick={() => navigate(`/app/author/submissions/${submission.id}`)}
                            >
                              Xem
                            </CButton>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CCardBody>
          </CCard>
        </CCol>

        <CCol md={4}>
          <CCard>
            <CCardHeader>
              <h5>Hội nghị đang mở</h5>
            </CCardHeader>
            <CCardBody>
              {conferences.length === 0 ? (
                <p className="text-muted">Không có hội nghị nào đang mở</p>
              ) : (
                <ul className="list-unstyled">
                  {conferences.map((conference) => (
                    <li key={conference.id} className="mb-3">
                      <h6>{conference.name}</h6>
                      <p className="text-muted small">{conference.description}</p>
                      <CButton
                        color="primary"
                        size="sm"
                        onClick={() =>
                          navigate(`/app/author/submissions/new?conferenceId=${conference.id}`)
                        }
                      >
                        Nộp bài
                      </CButton>
                    </li>
                  ))}
                </ul>
              )}
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
    </>
  )
}

export default AuthorDashboard
