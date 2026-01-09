import React, { lazy, Suspense } from 'react'
import { RouteObject, Navigate } from 'react-router-dom'
import { CSpinner } from '@coreui/react'
import { UserRole } from '../context/AuthContext'

/**
 * Lazy load components để code splitting
 * Preload các components quan trọng để tăng tốc độ
 */
const LoginPage = lazy(() => import('../pages/auth/LoginPage'))
const RegisterPage = lazy(() => import('../pages/auth/RegisterPage'))
const VerifyEmailPage = lazy(() => import('../pages/auth/EmailVerificationPage'))
const SsoRedirectPage = lazy(() => import('../pages/auth/SsoRedirectPage'))
const AuthCallbackPage = lazy(() => import('../pages/auth/AuthCallbackPage'))

// Preload layouts ngay sau khi app load
const AppLayout = lazy(() => {
  // Preload ngay
  return import('../layouts/AppLayout')
})
const AuthLayout = lazy(() => {
  return import('../layouts/AuthLayout')
})

const DashboardPage = lazy(() => import('../pages/app/DashboardPage'))

// Author pages
const AuthorDashboard = lazy(() => import('../pages/author/AuthorDashboard'))
const SubmissionList = lazy(() => import('../pages/author/SubmissionList'))
const SubmissionFormPage = lazy(() => import('../pages/author/SubmissionFormPage'))
const SubmissionDetail = lazy(() => import('../pages/author/SubmissionDetail'))
const SubmissionEdit = lazy(() => import('../pages/author/SubmissionEdit'))

// PC/Reviewer pages
const AssignedPaperList = lazy(() => import('../pages/pc/AssignedPaperList'))
const ReviewFormPage = lazy(() => import('../pages/pc/ReviewFormPage'))
const DiscussionPage = lazy(() => import('../pages/pc/DiscussionPage'))
const COIDeclaration = lazy(() => import('../pages/pc/COIDeclaration'))

// Chair pages
const ConferenceConfig = lazy(() => import('../pages/chair/ConferenceConfig'))
const PCManagement = lazy(() => import('../pages/chair/PCManagement'))
const AssignmentDashboard = lazy(() => import('../pages/chair/AssignmentDashboard'))
const DecisionBoard = lazy(() => import('../pages/chair/DecisionBoard'))
const BulkEmailPreview = lazy(() => import('../pages/chair/BulkEmailPreview'))
const ProceedingsExport = lazy(() => import('../pages/chair/ProceedingsExport'))
const ReportingDashboard = lazy(() => import('../pages/chair/ReportingDashboard'))

// Camera-ready pages
const CameraReadyUpload = lazy(() => import('../pages/author/CameraReadyUpload'))

/**
 * Route configuration cho UTH-ConfMS
 *
 * Routes:
 * - / - Redirect to /login
 * - /login - Login page (AuthLayout)
 * - /auth/sso/callback - SSO callback (AuthLayout)
 * - /verify-email - Email verification (AuthLayout)
 * - /app/* - Application routes (AppLayout)
 */
export const routes: RouteObject[] = [
  {
    path: '/',
    element: <Navigate to="/login" replace />,
  },
  {
    path: '/login',
    element: (
      <Suspense
        fallback={
          <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: '100vh' }}
          >
            <CSpinner color="primary" />
          </div>
        }
      >
        <AuthLayout>
          <LoginPage />
        </AuthLayout>
      </Suspense>
    ),
  },
  {
    path: '/register',
    element: (
      <Suspense
        fallback={
          <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: '100vh' }}
          >
            <CSpinner color="primary" />
          </div>
        }
      >
        <AuthLayout>
          <RegisterPage />
        </AuthLayout>
      </Suspense>
    ),
  },
  {
    path: '/auth/sso/redirect',
    element: (
      <Suspense
        fallback={
          <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: '100vh' }}
          >
            <CSpinner color="primary" />
          </div>
        }
      >
        <AuthLayout>
          <SsoRedirectPage />
        </AuthLayout>
      </Suspense>
    ),
  },
  {
    path: '/auth/sso/callback',
    element: (
      <Suspense
        fallback={
          <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: '100vh' }}
          >
            <CSpinner color="primary" />
          </div>
        }
      >
        <AuthLayout>
          <AuthCallbackPage />
        </AuthLayout>
      </Suspense>
    ),
  },
  {
    path: '/verify-email',
    element: (
      <Suspense
        fallback={
          <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: '100vh' }}
          >
            <CSpinner color="primary" />
          </div>
        }
      >
        <AuthLayout>
          <VerifyEmailPage />
        </AuthLayout>
      </Suspense>
    ),
  },
  {
    path: '/app/*',
    element: (
      <Suspense
        fallback={
          <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: '100vh' }}
          >
            <CSpinner color="primary" />
          </div>
        }
      >
        <AppLayout />
      </Suspense>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="dashboard" replace />,
      },
      {
        path: 'dashboard',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <DashboardPage />
          </Suspense>
        ),
      },
      // Author routes
      {
        path: 'author',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <AuthorDashboard />
          </Suspense>
        ),
      },
      {
        path: 'author/submissions',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <SubmissionList />
          </Suspense>
        ),
      },
      {
        path: 'author/submissions/new',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <SubmissionFormPage />
          </Suspense>
        ),
      },
      {
        path: 'author/submissions/:id',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <SubmissionDetail />
          </Suspense>
        ),
      },
      {
        path: 'author/submissions/:id/edit',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <SubmissionEdit />
          </Suspense>
        ),
      },
      {
        path: 'author/submissions/:id/camera-ready',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <CameraReadyUpload />
          </Suspense>
        ),
        handle: { roles: ['AUTHOR'] as UserRole[] },
      },
      // PC/Reviewer routes
      {
        path: 'pc/assignments',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <AssignedPaperList />
          </Suspense>
        ),
      },
      {
        path: 'pc/reviews/new',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ReviewFormPage />
          </Suspense>
        ),
      },
      {
        path: 'pc/reviews/:id/edit',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ReviewFormPage />
          </Suspense>
        ),
      },
      {
        path: 'pc/reviews/:id/discussion',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <DiscussionPage />
          </Suspense>
        ),
      },
      {
        path: 'pc/coi',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <COIDeclaration />
          </Suspense>
        ),
      },
      // Chair routes
      {
        path: 'chair/conference/:id/config',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ConferenceConfig />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
      {
        path: 'chair/pc',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <PCManagement />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
      {
        path: 'chair/assignments',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <AssignmentDashboard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
      {
        path: 'chair/decisions',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <DecisionBoard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
      {
        path: 'chair/email',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <BulkEmailPreview />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
      {
        path: 'chair/proceedings',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ProceedingsExport />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
      {
        path: 'chair/reports',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ReportingDashboard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR'] as UserRole[] },
      },
    ],
  },
  // Catch-all route - redirect to login if no route matches
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]
