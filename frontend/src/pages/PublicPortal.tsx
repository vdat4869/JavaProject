import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    CContainer,
    CRow,
    CCol,
    CCard,
    CCardBody,
    CButton,
    CBadge,
    CSpinner,
    CAlert,
    CHeader,
    CHeaderNav,
    CNavItem,
    CNavLink,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilEducation, cilCalendar, cilLocationPin, cilChevronRight } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import uthLogo from '../assets/images/image copy.png'
import { conferenceService, ConferenceResponse } from '../services/conference.service'

/**
 * PublicPortal - Trang chủ công khai cho khách xem danh sách hội nghị
 */
const PublicPortal: React.FC = () => {
    const { t, i18n } = useTranslation()
    const navigate = useNavigate()
    const [conferences, setConferences] = useState<ConferenceResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        loadPublicConferences()
    }, [])

    const loadPublicConferences = async () => {
        try {
            setLoading(true)
            const data = await conferenceService.getPublishedConferences()
            setConferences(data)
        } catch (err: any) {
            setError(t('common.error') || 'Lỗi khi tải dữ liệu')
        } finally {
            setLoading(false)
        }
    }

    const toggleLanguage = () => {
        const newLang = i18n.language === 'vi' ? 'en' : 'vi'
        i18n.changeLanguage(newLang)
    }

    return (
        <div className="min-vh-100 bg-light">
            {/* Header / Navbar */}
            <CHeader position="sticky" className="mb-4 shadow-sm public-header">
                <CContainer fluid className="px-4 d-flex align-items-center justify-content-between">
                    <div className="d-flex align-items-center">
                        <img
                            src={uthLogo}
                            alt="UTH Logo"
                            height="40"
                            className="me-3"
                            onError={(e) => (e.currentTarget.style.display = 'none')}
                        />

                    </div>
                    <CHeaderNav className="d-none d-md-flex me-auto ms-4">
                        <CNavItem>
                            <CNavLink href="#" active>{t('common.home') || 'Trang chủ'}</CNavLink>
                        </CNavItem>
                        <CNavItem>
                            <CNavLink href="#conferences">{t('conference.conference') || 'Hội nghị'}</CNavLink>
                        </CNavItem>
                    </CHeaderNav>
                    <div className="d-flex gap-3 align-items-center">
                        <CButton variant="ghost" size="sm" onClick={toggleLanguage} className="fw-bold">
                            {i18n.language === 'vi' ? 'EN' : 'VI'}
                        </CButton>
                        <CButton color="outline-primary" onClick={() => navigate('/login')}>
                            {t('common.login') || 'Đăng nhập'}
                        </CButton>
                        <CButton color="primary" onClick={() => navigate('/register')}>
                            {t('common.register') || 'Đăng ký'}
                        </CButton>
                    </div>
                </CContainer>
            </CHeader>

            {/* Hero Section */}
            <CContainer className="py-5 text-center">
                <CRow className="justify-content-center py-5">
                    <CCol md={10} lg={8}>
                        <h1 className="display-4 fw-bold text-dark mb-4">
                            Scientific Conference Paper Management System
                        </h1>
                        <p className="lead text-muted mb-5">
                            Hệ thống quản lý bài báo và hội nghị khoa học tập trung dành cho Trường Đại học Giao thông vận tải TP.HCM (UTH).
                        </p>
                        <div className="d-flex justify-content-center gap-3">
                            <CButton color="primary" size="lg" href="#conferences" className="px-4 py-2 shadow-sm rounded-pill">
                                {t('conference.browseConferences') || 'Xem danh sách hội nghị'}
                            </CButton>
                            <CButton color="light" size="lg" onClick={() => navigate('/register')} className="px-4 py-2 border shadow-sm rounded-pill">
                                {t('common.joinAsAuthor') || 'Tham gia nộp bài'}
                            </CButton>
                        </div>
                    </CCol>
                </CRow>
            </CContainer>

            {/* Conference List */}
            <CContainer id="conferences" className="pb-5">
                <div className="d-flex align-items-center mb-4">
                    <div className="bg-primary p-2 rounded-3 me-3">
                        <CIcon icon={cilEducation} className="text-white" size="xl" />
                    </div>
                    <h2 className="m-0 fw-bold">{t('conference.activeConferences') || 'Hội nghị đang diễn ra'}</h2>
                </div>

                {error && <CAlert color="danger">{error}</CAlert>}

                {loading ? (
                    <div className="text-center py-5">
                        <CSpinner color="primary" />
                    </div>
                ) : conferences.length === 0 ? (
                    <CCard className="border-0 shadow-sm text-center py-5">
                        <CCardBody>
                            <p className="text-muted lead mb-0">{t('conference.noPublicConferences') || 'Hiện chưa có hội nghị nào được công bố.'}</p>
                        </CCardBody>
                    </CCard>
                ) : (
                    <CRow>
                        {conferences.map((conf) => (
                            <CCol key={conf.id} md={6} lg={4} className="mb-4">
                                <CCard className="h-100 border-0 shadow-hover transition-3d shadow-sm">
                                    <CCardBody className="d-flex flex-column p-4">
                                        <div className="d-flex justify-content-between align-items-start mb-3">
                                            <CBadge color="primary" shape="rounded-pill" className="px-2 py-1">
                                                {conf.acronym}
                                            </CBadge>
                                            {conf.cfp?.open && (
                                                <CBadge color="success" shape="rounded-pill" className="px-2 py-1">
                                                    CFP Open
                                                </CBadge>
                                            )}
                                        </div>
                                        <h4 className="fw-bold mb-3">{conf.name}</h4>
                                        <p className="text-muted small mb-4 flex-grow-1" style={{
                                            display: '-webkit-box',
                                            WebkitLineClamp: 3,
                                            WebkitBoxOrient: 'vertical',
                                            overflow: 'hidden'
                                        }}>
                                            {conf.description || 'Tham gia và nộp bài báo nghiên cứu khoa học tại hội nghị.'}
                                        </p>

                                        <div className="mt-auto">
                                            <div className="d-flex align-items-center text-muted small mb-2">
                                                <CIcon icon={cilCalendar} className="me-2" />
                                                <span>Deadline: {
                                                    conf.deadlines?.find(d => d.type === 'SUBMISSION')?.dueDate
                                                        ? new Date(conf.deadlines.find(d => d.type === 'SUBMISSION')!.dueDate).toLocaleDateString()
                                                        : 'TBD'
                                                }</span>
                                            </div>
                                            <div className="d-flex align-items-center text-muted small mb-4">
                                                <CIcon icon={cilLocationPin} className="me-2" />
                                                <span>{'UTH, TP.HCM'}</span>
                                            </div>
                                            <CButton
                                                color="primary"
                                                variant="outline"
                                                className="w-100 d-flex justify-content-between align-items-center rounded-pill"
                                                onClick={() => navigate(`/login?conferenceId=${conf.id}`)}
                                            >
                                                <span>{t('conference.viewDetails') || 'Xem chi tiết'}</span>
                                                <CIcon icon={cilChevronRight} />
                                            </CButton>
                                        </div>
                                    </CCardBody>
                                </CCard>
                            </CCol>
                        ))}
                    </CRow>
                )}
            </CContainer>

            {/* Footer */}
            <footer className="bg-white border-top py-5 mt-5">
                <CContainer>
                    <CRow className="align-items-center">
                        <CCol md={6} className="text-center text-md-start mb-3 mb-md-0">
                            <h5 className="fw-bold text-primary mb-2">UTH-ConfMS</h5>
                            <p className="text-muted small m-0">© 2026 UTH University. All rights reserved.</p>
                        </CCol>
                        <CCol md={6} className="text-center text-md-end">
                            <div className="d-flex justify-content-center justify-content-md-end gap-3 text-muted small">
                                <a href="#" className="text-decoration-none text-reset">Terms</a>
                                <a href="#" className="text-decoration-none text-reset">Privacy</a>
                                <a href="#" className="text-decoration-none text-reset">Help</a>
                            </div>
                        </CCol>
                    </CRow>
                </CContainer>
            </footer>

            <style>{`
        .shadow-hover:hover {
          box-shadow: 0 1rem 3rem rgba(0,0,0,.175)!important;
        }
        .transition-3d {
          transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
        }
        .transition-3d:hover {
          transform: translateY(-5px);
        }
      `}</style>
        </div>
    )
}

export default PublicPortal
