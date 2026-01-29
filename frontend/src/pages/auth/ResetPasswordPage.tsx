import React, { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import {
    CButton,
    CCard,
    CCardBody,
    CCol,
    CForm,
    CFormInput,
    CRow,
    CAlert,
    CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'
import uthLogoFull from '../../assets/images/idrV1VcT-T_logos.jpeg'
// Reset password handler

/**
 * ResetPasswordPage - Trang thiết lập mật khẩu mới
 */
const ResetPasswordPage: React.FC = () => {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()
    const token = searchParams.get('token')

    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState(false)
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')

        if (newPassword !== confirmPassword) {
            setError(t('auth.passwordsNotMatch') || 'Mật khẩu xác nhận không khớp.')
            return
        }

        if (!token) {
            setError(t('auth.invalidToken') || 'Token không hợp lệ hoặc đã hết hạn.')
            return
        }

        setLoading(true)

        try {
            await authService.resetPassword({ token, newPassword })
            setSuccess(true)
            setTimeout(() => navigate('/login'), 3000)
        } catch (err: any) {
            setError(err.response?.data?.message || t('auth.resetPasswordFailed') || 'Đặt lại mật khẩu thất bại. Vui lòng thử lại.')
        } finally {
            setLoading(false)
        }
    }

    const colors = {
        teal: '#008585',
        red: '#b31d1d',
        border: '#abb5be',
        text: '#212529'
    }

    const styles = {
        card: {
            borderRadius: '8px',
            border: 'none',
            boxShadow: '0 10px 40px rgba(0,0,0,0.25)',
            backgroundColor: '#fff',
            padding: '30px 50px',
            maxWidth: '550px',
            width: '100%'
        },
        logoHeader: {
            textAlign: 'center' as const,
            marginBottom: '5px',
            marginTop: '-15px',
            overflow: 'hidden',
            maxHeight: '120px'
        },
        logoImage: {
            maxWidth: '240px',
            height: 'auto',
            display: 'inline-block'
        },
        title: {
            color: colors.red,
            fontSize: '1.5rem',
            fontWeight: 700,
            textAlign: 'center' as const,
            marginBottom: '20px',
            textTransform: 'uppercase' as const,
            letterSpacing: '1px'
        },
        input: {
            backgroundColor: '#fff',
            border: `1px solid ${colors.border}`,
            borderRadius: '4px',
            padding: '14px 18px',
            fontSize: '1rem',
            color: colors.text
        },
        btnSubmit: {
            backgroundColor: colors.teal,
            borderColor: colors.teal,
            color: '#fff',
            fontWeight: 'bold',
            padding: '14px',
            borderRadius: '4px',
            marginTop: '10px',
            letterSpacing: '1px'
        }
    }

    return (
        <CRow className="justify-content-end align-items-center min-vh-100 pe-md-5 me-md-5">
            <CCol xs={12} sm={10} md={8} lg={6} xl={5} className="d-flex justify-content-end pe-lg-5">
                <CCard style={styles.card}>
                    <CCardBody className="p-0">
                        {/* Logo */}
                        <div style={styles.logoHeader}>
                            <img src={uthLogoFull} alt="UTH Logo" style={styles.logoImage} />
                        </div>

                        {/* Title */}
                        <h2 style={styles.title}>{t('auth.resetPassword') || 'ĐỔI MẬT KHẨU'}</h2>

                        {success ? (
                            <div className="text-center py-3">
                                <CAlert color="success" className="text-start mb-4">
                                    {t('auth.resetPasswordSuccess') || 'Mật khẩu của bạn đã được thay đổi thành công. Đang chuyển hướng đến trang đăng nhập...'}
                                </CAlert>
                                <CSpinner style={{ color: colors.teal }} />
                            </div>
                        ) : (
                            <CForm onSubmit={handleSubmit}>
                                <p className="text-muted text-center mb-4">
                                    {t('auth.resetPasswordDesc') || 'Vui lòng nhập mật khẩu mới cho tài khoản của bạn.'}
                                </p>

                                {error && (
                                    <CAlert color="danger" className="mb-3">
                                        {error}
                                    </CAlert>
                                )}

                                <div className="mb-3">
                                    <CFormInput
                                        type="password"
                                        placeholder="Mật khẩu mới"
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        style={styles.input}
                                        required
                                    />
                                </div>

                                <div className="mb-3">
                                    <CFormInput
                                        type="password"
                                        placeholder="Xác nhận mật khẩu mới"
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        style={styles.input}
                                        required
                                    />
                                </div>

                                <div className="d-grid gap-2">
                                    <CButton type="submit" style={styles.btnSubmit} disabled={loading || !token}>
                                        {loading ? <CSpinner size="sm" /> : 'XÁC NHẬN ĐỔI MẬT KHẨU'}
                                    </CButton>
                                </div>
                            </CForm>
                        )}
                    </CCardBody>
                </CCard>
            </CCol>
        </CRow>
    )
}

export default ResetPasswordPage
