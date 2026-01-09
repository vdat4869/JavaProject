import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CButton,
  CAlert,
  CSpinner,
  CTabs,
  CNav,
  CNavItem,
  CNavLink,
  CTabContent,
  CTabPane,
} from '@coreui/react'
import { conferenceService, Conference, CFP } from '../../services/conference.service'

/**
 * ConferenceConfig - Trang cấu hình conference
 *
 * Features:
 * - Cập nhật thông tin conference
 * - Cấu hình CFP (tracks, topics, deadlines)
 * - Chỉ CHAIR mới có quyền
 */
const ConferenceConfig: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [conference, setConference] = useState<Conference | null>(null)
  const [cfp, setCfp] = useState<CFP | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [activeTab, setActiveTab] = useState('basic')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (id) {
      loadData()
    }
  }, [id])

  const loadData = async () => {
    try {
      setLoading(true)
      const [confData, cfpData] = await Promise.all([
        conferenceService.getConference(parseInt(id!)),
        conferenceService.getCFP(parseInt(id!)),
      ])
      setConference(confData)
      setCfp(cfpData)
    } catch (error) {
      console.error('Error loading conference data:', error)
      setError('Không thể tải thông tin hội nghị')
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateConference = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    try {
      setSaving(true)
      await conferenceService.updateConference(parseInt(id!), {
        name: conference!.name,
        description: conference!.description,
        startDate: conference!.startDate,
        endDate: conference!.endDate,
        submissionDeadline: conference!.submissionDeadline,
        reviewDeadline: conference!.reviewDeadline,
        active: conference!.active,
      })
      setSuccess('Cập nhật thành công')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể cập nhật')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateCFP = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    try {
      setSaving(true)
      await conferenceService.updateCFP(parseInt(id!), {
        description: cfp!.description,
        topics: cfp!.topics,
        tracks: cfp!.tracks,
        deadlines: cfp!.deadlines,
      })
      setSuccess('Cập nhật CFP thành công')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể cập nhật CFP')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!conference || !cfp) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Không tìm thấy hội nghị</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Cấu hình hội nghị: {conference.name}</h4>
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

        <CTabs activeTab={activeTab} onActiveTabChange={setActiveTab}>
          <CNav variant="tabs">
            <CNavItem>
              <CNavLink>Thông tin cơ bản</CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink>CFP</CNavLink>
            </CNavItem>
          </CNav>
          <CTabContent>
            <CTabPane>
              <CForm onSubmit={handleUpdateConference}>
                <div className="mb-3">
                  <CFormLabel>Tên hội nghị</CFormLabel>
                  <CFormInput
                    type="text"
                    value={conference.name}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setConference({ ...conference, name: e.target.value })
                    }
                    required
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>Mô tả</CFormLabel>
                  <CFormTextarea
                    value={conference.description}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                      setConference({ ...conference, description: e.target.value })
                    }
                    rows={5}
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>Ngày bắt đầu</CFormLabel>
                  <CFormInput
                    type="date"
                    value={conference.startDate.split('T')[0]}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setConference({ ...conference, startDate: e.target.value })
                    }
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>Ngày kết thúc</CFormLabel>
                  <CFormInput
                    type="date"
                    value={conference.endDate.split('T')[0]}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setConference({ ...conference, endDate: e.target.value })
                    }
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>Hạn nộp bài</CFormLabel>
                  <CFormInput
                    type="datetime-local"
                    value={conference.submissionDeadline}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setConference({ ...conference, submissionDeadline: e.target.value })
                    }
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>Hạn đánh giá</CFormLabel>
                  <CFormInput
                    type="datetime-local"
                    value={conference.reviewDeadline}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setConference({ ...conference, reviewDeadline: e.target.value })
                    }
                  />
                </div>
                <CButton type="submit" color="primary" disabled={saving}>
                  {saving ? <CSpinner size="sm" /> : 'Lưu'}
                </CButton>
              </CForm>
            </CTabPane>
            <CTabPane>
              <CForm onSubmit={handleUpdateCFP}>
                <div className="mb-3">
                  <CFormLabel>Mô tả CFP</CFormLabel>
                  <CFormTextarea
                    value={cfp.description}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                      setCfp({ ...cfp, description: e.target.value })
                    }
                    rows={5}
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>Topics (phân cách bằng dấu phẩy)</CFormLabel>
                  <CFormInput
                    type="text"
                    value={cfp.topics.join(', ')}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setCfp({
                        ...cfp,
                        topics: e.target.value
                          .split(',')
                          .map((t) => t.trim())
                          .filter((t) => t),
                      })
                    }
                  />
                </div>
                <CButton type="submit" color="primary" disabled={saving}>
                  {saving ? <CSpinner size="sm" /> : 'Lưu CFP'}
                </CButton>
              </CForm>
            </CTabPane>
          </CTabContent>
        </CTabs>
      </CCardBody>
    </CCard>
  )
}

export default ConferenceConfig
