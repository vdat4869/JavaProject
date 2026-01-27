import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate, useLocation } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CAlert,
  CSpinner,
  CBadge,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { pcService, PCMember } from '../../services/pc.service'
import { conferenceService } from '../../services/conference.service'
import { useAuth } from '../../context/AuthContext'

/**
 * InvitationAcceptPage - Trang chấp nhận/từ chối PC invitation
 *
 * Features:
 * - Hiển thị thông tin invitation và conference
 * - Accept/Decline invitation
 * - Handle invitation token from URL
 */
const InvitationAcceptPage: React.FC = () => {
  const { t } = useTranslation()
  const { logout } = useAuth()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const location = useLocation()
  const isDeclinePage = location.pathname.includes('/decline')
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState('')
  const [conferenceName, setConferenceName] = useState<string>('')
  const [showConfirmDecline, setShowConfirmDecline] = useState(false)

  useEffect(() => {
    // Token is required
    if (!token) {
      setError('Invalid invitation link. Missing token.')
    } else if (isDeclinePage) {
      setShowConfirmDecline(true)
    }
    setLoading(false)
  }, [token, isDeclinePage])

  const handleAccept = async () => {
    if (!token) return

    if (!window.confirm('Bạn có chắc chắn muốn chấp nhận lời mời làm PC member?')) {
      return
    }

    try {
      setProcessing(true)
      setError('')
      const member: PCMember = await pcService.acceptInvitation(token)
      // Load conference name for display
      try {
        const conference = await conferenceService.getConference(member.conferenceId)
        setConferenceName(conference.name)
      } catch {
        // Conference name not critical
      }
      alert('Bạn đã chấp nhận lời mời thành công! Hệ thống sẽ đăng xuất để cập nhật quyền hạn mới. Vui lòng đăng nhập lại và tiến hành khai báo COI.')
      await logout()
      navigate('/login')
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
        'Không thể chấp nhận lời mời. Có thể invitation đã hết hạn hoặc đã được xử lý.'
      )
    } finally {
      setProcessing(false)
    }
  }

  const handleDecline = async () => {
    if (!token) return

    if (!window.confirm('Bạn có chắc chắn muốn từ chối lời mời này?')) {
      return
    }

    try {
      setProcessing(true)
      setError('')
      await pcService.declineInvitation(token)
      alert('Bạn đã từ chối lời mời.')
      navigate('/app')
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
        'Không thể từ chối lời mời. Có thể invitation đã hết hạn hoặc đã được xử lý.'
      )
    } finally {
      setProcessing(false)
    }
  }

  if (loading && !error) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>PC Member Invitation</h4>
      </CCardHeader>
      <CCardBody>
        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        {!error && (
          <>
            <div className="mb-4">
              <h5>Bạn đã được mời làm PC Member</h5>
              {conferenceName && (
                <p className="text-muted">
                  Hội nghị: <strong>{conferenceName}</strong>
                </p>
              )}
              <p className="text-muted">
                Vui lòng chấp nhận hoặc từ chối lời mời này. Sau khi chấp nhận, bạn sẽ có thể
                review các bài báo được giao.
              </p>
            </div>

            {showConfirmDecline ? (
              <div className="text-center">
                <CAlert color="warning">
                  Bạn có chắc chắn muốn từ chối lời mời này không?
                  <br />
                  Hành động này không thể hoàn tác.
                </CAlert>
                <div className="d-flex justify-content-center gap-3 mt-3">
                  <CButton
                    color="secondary"
                    onClick={() => {
                      setShowConfirmDecline(false)
                      navigate('/app/pc/invitation/accept?token=' + token)
                    }}>
                    Quay lại
                  </CButton>
                  <CButton
                    color="danger"
                    onClick={handleDecline}
                    disabled={processing}>
                    {processing ? <CSpinner size="sm" /> : 'Xác nhận Từ chối'}
                  </CButton>
                </div>
              </div>
            ) : (
              <div className="d-flex justify-content-end gap-2">
                <CButton
                  color="danger"
                  onClick={() => setShowConfirmDecline(true)}
                  disabled={processing}
                  className="me-2"
                >
                  Từ chối
                </CButton>
                <CButton color="success" onClick={handleAccept} disabled={processing}>
                  {processing ? (
                    <>
                      <CSpinner size="sm" className="me-2" />
                      Đang xử lý...
                    </>
                  ) : (
                    'Chấp nhận'
                  )}
                </CButton>
              </div>
            )}
          </>
        )}

        {error && (
          <div className="mt-3">
            <CButton color="secondary" onClick={() => navigate('/app')}>
              Quay lại
            </CButton>
          </div>
        )}
      </CCardBody>
    </CCard>
  )
}

export default InvitationAcceptPage
