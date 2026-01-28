import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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

/**
 * ForgotPasswordPage - Trang yêu cầu khôi phục mật khẩu
 */
const ForgotPasswordPage: React.FC = () => {
    const { t } = useTranslation()
    const navigate = useNavigate()
    const [email, setEmail] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState(false)
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        setSuccess(false)
        setLoading(true)

        try {
            await authService.forgotPassword({ email })
            setSuccess(true)
        } catch (err: any) {
            setError(err.response?.data?.message || t('auth.forgotPasswordFailed') || 'Gửi yêu cầu thất bại. Vui lòng kiểm tra lại email.')
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
        },
        backLink: {
            color: colors.teal,
            textDecoration: 'none',
            fontSize: '1rem',
            display: 'block',
            textAlign: 'center' as const,
            marginTop: '20px',
            fontWeight: 600
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
                        <h2 style={styles.title}>{t('auth.forgotPassword') || 'QUÊN MẬT KHẨU'}</h2>

                        {success ? (
                            <div className="text-center py-3">
                                <CAlert color="success" className="text-start mb-4">
                                    {t('auth.forgotPasswordSuccess') || 'Một liên kết đặt lại mật khẩu đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư (và thư rác).'}
                                </CAlert>
                                <CButton
                                    style={{ backgroundColor: colors.teal, borderColor: colors.teal, color: '#fff' }}
                                    onClick={() => navigate('/login')}
                                    className="w-100"
                                >
                                    {t('common.backToLogin') || 'Quay lại đăng nhập'}
                                </CButton>
                            </div>
                        ) : (
                            <CForm onSubmit={handleSubmit}>
                                <p className="text-muted text-center mb-4">
                                    {t('auth.forgotPasswordDesc') || 'Nhập địa chỉ email của bạn và chúng tôi sẽ gửi liên kết để đặt lại mật khẩu.'}
                                </p>

                                {error && (
                                    <CAlert color="danger" className="mb-3">
                                        {error}
                                    </CAlert>
                                )}

                                <div className="mb-3">
                                    <CFormInput
                                        type="email"
                                        placeholder="Địa chỉ email của bạn"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        style={styles.input}
                                        required
                                    />
                                </div>

                                <div className="d-grid gap-2">
                                    <CButton type="submit" style={styles.btnSubmit} disabled={loading}>
                                        {loading ? <CSpinner size="sm" /> : 'GỬI YÊU CẦU'}
                                    </CButton>
                                </div>

                                <Link to="/login" style={styles.backLink}>
                                    {t('common.backToLogin') || 'QUAY LẠI ĐĂNG NHẬP'}
                                </Link>
                            </CForm>
                        )}
                    </CCardBody>
                </CCard>
            </CCol>
        </CRow>
    )
}

export default ForgotPasswordPage
