import { lazy, Suspense } from 'react'
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
const ForgotPasswordPage = lazy(() => import('../pages/auth/ForgotPasswordPage'))
const ResetPasswordPage = lazy(() => import('../pages/auth/ResetPasswordPage'))

// Preload layouts ngay sau khi app load
const PublicPortal = lazy(() => import('../pages/PublicPortal'))
const AppLayout = lazy(() => {
  // Preload ngay
  return import('../layouts/AppLayout')
})
const AuthLayout = lazy(() => {
  return import('../layouts/AuthLayout')
})

const DashboardPage = lazy(() => import('../pages/app/DashboardPage'))
const ProfilePage = lazy(() => import('../pages/app/ProfilePage'))
const SubmissionReviewsView = lazy(() => import('../pages/app/SubmissionReviewsView'))
const UserManagementPage = lazy(() => import('../pages/admin/UserManagementPage'))
const AuditLogPage = lazy(() => import('../pages/admin/AuditLogPage'))
const BackupManagement = lazy(() => import('../pages/admin/BackupManagement'))
const EmailLogPage = lazy(() => import('../pages/admin/EmailLogPage'))

// Author pages
const AuthorDashboard = lazy(() => import('../pages/author/AuthorDashboard'))
const SubmissionList = lazy(() => import('../pages/author/SubmissionList'))
const SubmissionFormPage = lazy(() => import('../pages/author/SubmissionFormPage'))
const SubmissionDetail = lazy(() => import('../pages/author/SubmissionDetail'))
const SubmissionEdit = lazy(() => import('../pages/author/SubmissionEdit'))
const CameraReadyUpload = lazy(() => import('../pages/author/CameraReadyUpload'))
const RebuttalFormPage = lazy(() => import('../pages/author/RebuttalFormPage'))

// PC/Reviewer pages
const AssignedPaperList = lazy(() => import('../pages/pc/AssignedPaperList'))
const ReviewFormPage = lazy(() => import('../pages/pc/ReviewFormPage'))
const DiscussionPage = lazy(() => import('../pages/pc/DiscussionPage'))
const PaperDiscussionBoard = lazy(() => import('../pages/pc/PaperDiscussionBoard'))
const COIDeclaration = lazy(() => import('../pages/pc/COIDeclaration'))
const InvitationAcceptPage = lazy(() => import('../pages/pc/InvitationAcceptPage'))
const MyCOIsList = lazy(() => import('../pages/pc/MyCOIsList'))
const ReviewerWorkload = lazy(() => import('../pages/pc/ReviewerWorkload'))
const ReviewerSubmissionDetail = lazy(() => import('../pages/pc/ReviewerSubmissionDetail'))

// Chair pages
const ConferenceListPage = lazy(() => import('../pages/chair/ConferenceListPage'))
const CreateConferencePage = lazy(() => import('../pages/chair/CreateConferencePage'))
const ConferenceConfig = lazy(() => import('../pages/chair/ConferenceConfig'))
const PCManagement = lazy(() => import('../pages/chair/PCManagement'))
const AssignmentDashboard = lazy(() => import('../pages/chair/AssignmentDashboard'))
const DecisionBoard = lazy(() => import('../pages/chair/DecisionBoard'))
const BulkEmailPreview = lazy(() => import('../pages/chair/BulkEmailPreview'))
const ProceedingsExport = lazy(() => import('../pages/chair/ProceedingsExport'))
const ReportingDashboard = lazy(() => import('../pages/chair/ReportingDashboard'))
const WorkloadDashboard = lazy(() => import('../pages/chair/WorkloadDashboard'))
const COIHistory = lazy(() => import('../pages/chair/COIHistory'))
const COIStatistics = lazy(() => import('../pages/chair/COIStatistics'))
const ReviewStatisticsDashboard = lazy(() => import('../pages/chair/ReviewStatisticsDashboard'))
const AssignmentStatisticsDashboard = lazy(() => import('../pages/chair/AssignmentStatisticsDashboard'))
const AssignmentQualityMetrics = lazy(() => import('../pages/chair/AssignmentQualityMetrics'))
const BulkAssignmentPage = lazy(() => import('../pages/chair/BulkAssignmentPage'))
const SubmissionBoard = lazy(() => import('../pages/chair/SubmissionBoard'))
const CameraReadyManagement = lazy(() => import('../pages/chair/CameraReadyManagement'))
const CameraReadyDetail = lazy(() => import('../pages/chair/CameraReadyDetail'))
const NotificationManagement = lazy(() => import('../pages/chair/NotificationManagement'))

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
        <PublicPortal />
      </Suspense>
    ),
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
    path: '/forgot-password',
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
          <ForgotPasswordPage />
        </AuthLayout>
      </Suspense>
    ),
  },
  {
    path: '/reset-password',
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
          <ResetPasswordPage />
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
      },
      {
        path: 'author/submissions/:id/rebuttal',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <RebuttalFormPage />
          </Suspense>
        ),
      },
      // PC/Reviewer routes
      {
        path: 'pc/submissions/:id',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ReviewerSubmissionDetail />
          </Suspense>
        ),
        handle: { roles: ['PC'] as UserRole[] },
      },
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
        handle: { roles: ['PC'] as UserRole[] },
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
        handle: { roles: ['PC'] as UserRole[] },
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
        handle: { roles: ['PC'] as UserRole[] },
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
        handle: { roles: ['PC'] as UserRole[] },
      },
      {
        path: 'pc/submissions/:submissionId/discussion',
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
        handle: { roles: ['PC'] as UserRole[] },
      },
      {
        path: 'pc/discussions',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <PaperDiscussionBoard />
          </Suspense>
        ),
        handle: { roles: ['PC'] as UserRole[] },
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
        handle: { roles: ['PC'] as UserRole[] },
      },
      {
        path: 'pc/cois',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <MyCOIsList />
          </Suspense>
        ),
        handle: { roles: ['PC'] as UserRole[] },
      },
      {
        path: 'pc/invitation',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <InvitationAcceptPage />
          </Suspense>
        ),
      },
      {
        path: 'pc/workload',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ReviewerWorkload />
          </Suspense>
        ),
        handle: { roles: ['PC'] as UserRole[] },
      },
      // Chair routes
      {
        path: 'chair/conferences',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ConferenceListPage />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conferences/new',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <CreateConferencePage />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/assignment-statistics',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <AssignmentStatisticsDashboard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/assignment-quality',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <AssignmentQualityMetrics />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/bulk-assign',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <BulkAssignmentPage />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/submissions',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <SubmissionBoard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/submissions/:submissionId/discussion',
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/camera-ready',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <CameraReadyManagement />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/camera-ready/:paperId',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <CameraReadyDetail />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/notifications',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <NotificationManagement />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
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
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/workload',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <WorkloadDashboard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/coi/history',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <COIHistory />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/coi/statistics',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <COIStatistics />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'chair/conference/:id/review-statistics',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ReviewStatisticsDashboard />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      {
        path: 'submissions/:id/reviews',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <SubmissionReviewsView />
          </Suspense>
        ),
        handle: { roles: ['CHAIR', 'ADMIN'] as UserRole[] },
      },
      // User profile route
      {
        path: 'profile',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <ProfilePage />
          </Suspense>
        ),
      },

      // Admin routes
      {
        path: 'admin/users',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <UserManagementPage />
          </Suspense>
        ),
        handle: { roles: ['ADMIN'] as UserRole[] },
      },
      {
        path: 'admin/audit-logs',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <AuditLogPage />
          </Suspense>
        ),
        handle: { roles: ['ADMIN'] as UserRole[] },
      },
      {
        path: 'admin/backup',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <BackupManagement />
          </Suspense>
        ),
        handle: { roles: ['ADMIN'] as UserRole[] },
      },
      {
        path: 'admin/email-logs',
        element: (
          <Suspense
            fallback={
              <div className="d-flex justify-content-center p-5">
                <CSpinner color="primary" />
              </div>
            }
          >
            <EmailLogPage />
          </Suspense>
        ),
        handle: { roles: ['ADMIN'] as UserRole[] },
      },
    ],
  },
  // Catch-all route - redirect to login if no route matches
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]
