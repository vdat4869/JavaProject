import { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CAlert,
  CSpinner,
} from '@coreui/react'
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
  const { logout } = useAuth()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState('')
  const [conferenceName, setConferenceName] = useState<string>('')

  useEffect(() => {
    // Token is required
    if (!token) {
      setError('Invalid invitation link. Missing token.')
    }
    setLoading(false)
  }, [token])

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
    <div className="container-sm mt-5" style={{ maxWidth: '600px' }}>
      <CCard className="shadow">
        <CCardHeader className="bg-primary text-white">
          <h4 className="mb-0">PC Member Invitation</h4>
        </CCardHeader>
        <CCardBody className="p-4">
          {error && (
            <CAlert color="danger" className="mb-3">
              {error}
            </CAlert>
          )}

          {!error && (
            <>
              <div className="mb-4 text-center">
                <h5>Bạn đã được mời làm Program Committee (PC) Member</h5>
                {conferenceName && (
                  <div className="my-3 p-3 bg-light rounded">
                    <p className="mb-0 text-muted">Hội nghị:</p>
                    <h5 className="text-primary">{conferenceName}</h5>
                  </div>
                )}
                <p className="text-muted">
                  Vui lòng phản hồi lời mời này. Sau khi chấp nhận, bạn sẽ tham gia vào đội ngũ chuyên gia của hội nghị và có thể review các bài báo được giao.
                </p>
              </div>

              <div className="d-grid gap-3 d-md-flex justify-content-center mt-5">
                <CButton
                  color="danger"
                  variant="outline"
                  onClick={handleDecline}
                  disabled={processing}
                  className="px-4 py-2"
                >
                  Từ chối lời mời
                </CButton>
                <CButton
                  color="success"
                  onClick={handleAccept}
                  disabled={processing}
                  className="px-4 py-2"
                >
                  {processing ? (
                    <>
                      <CSpinner size="sm" className="me-2" />
                      Đang xử lý...
                    </>
                  ) : (
                    'Chấp nhận lời mời'
                  )}
                </CButton>
              </div>
              <div className="mt-4 text-center">
                <small className="text-muted">
                  Sau khi chấp nhận, hệ thống sẽ đăng xuất để cập nhật quyền hạn. Bạn cần đăng nhập lại để tiếp tục.
                </small>
              </div>
            </>
          )}

          {error && (
            <div className="mt-3 text-center">
              <CButton color="secondary" onClick={() => navigate('/app')}>
                Quay lại Trang chủ
              </CButton>
            </div>
          )}
        </CCardBody>
      </CCard>
    </div>
  )
}

export default InvitationAcceptPage
