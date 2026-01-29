import CIcon from '@coreui/icons-react'
import {
  cilSpeedometer,
  cilFile,
  cilList,
  cilPlus,
  cilCheckCircle,
  cilCommentSquare,
  cilWarning,
  cilCloudUpload,
  cilPrint,
  cilChart,
  cilShieldAlt,
  cilEnvelopeLetter,
  cilUser,
} from '@coreui/icons'
import { CNavItem, CNavTitle } from '@coreui/react'

/**
 * Navigation menu cho UTH-ConfMS
 * Menu items được hiển thị dựa trên role của user
 */
const _nav: any[] = [
  {
    component: CNavItem,
    name: 'Dashboard',
    to: '/app/dashboard',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
    // Dashboard hiển thị cho tất cả users
  },
  {
    component: CNavTitle,
    name: 'Author',
    roles: ['AUTHOR'],
  },
  {
    component: CNavItem,
    name: 'Dashboard',
    to: '/app/author',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
    roles: ['AUTHOR'],
  },
  {
    component: CNavItem,
    name: 'author.mySubmissions',
    to: '/app/author/submissions',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['AUTHOR'],
  },
  {
    component: CNavItem,
    name: 'author.newSubmission',
    to: '/app/author/submissions/new',
    icon: <CIcon icon={cilPlus} customClassName="nav-icon" />,
    roles: ['AUTHOR'],
  },
  {
    component: CNavTitle,
    name: 'PC / Reviewer',
    roles: ['PC'],
  },
  {
    component: CNavItem,
    name: 'Bài được giao',
    to: '/app/pc/assignments',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['PC'],
  },
  {
    component: CNavItem,
    name: 'Thảo luận nội bộ',
    to: '/app/pc/discussions',
    icon: <CIcon icon={cilCommentSquare} customClassName="nav-icon" />,
    roles: ['PC'],
  },
  {
    component: CNavItem,
    name: 'Mâu thuẫn lợi ích (COI)',
    to: '/app/pc/cois',
    icon: <CIcon icon={cilWarning} customClassName="nav-icon" />,
    roles: ['PC'],
  },
  {
    component: CNavItem,
    name: 'Khối lượng công việc',
    to: '/app/pc/workload',
    icon: <CIcon icon={cilChart} customClassName="nav-icon" />,
    roles: ['PC'],
  },
  {
    component: CNavTitle,
    name: 'Chair / Admin',
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'conference.conferenceList',
    to: '/app/chair/conferences',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Quản lý bài nộp',
    to: '/app/chair/submissions',
    icon: <CIcon icon={cilFile} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Decision Board',
    to: '/app/chair/decisions',
    icon: <CIcon icon={cilCheckCircle} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Camera-ready',
    to: '/app/chair/camera-ready',
    icon: <CIcon icon={cilCloudUpload} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Export Proceedings',
    to: '/app/chair/proceedings',
    icon: <CIcon icon={cilPrint} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Reporting',
    to: '/app/chair/reports',
    icon: <CIcon icon={cilChart} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },

  {
    component: CNavTitle,
    name: 'Hệ thống',
    roles: ['ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Quản lý người dùng',
    to: '/app/admin/users',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
    roles: ['ADMIN'],
  },
  /* Duplicate với 'Danh sách hội nghị' ở trên
  {
    component: CNavItem,
    name: 'admin.conferenceManagement',
    to: '/app/chair/conferences',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['ADMIN'],
  },
  */
  {
    component: CNavItem,
    name: 'Quản lý Sao lưu',
    to: '/app/admin/backup',
    icon: <CIcon icon={cilShieldAlt} customClassName="nav-icon" />,
    roles: ['ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Nhật ký Email',
    to: '/app/admin/email-logs',
    icon: <CIcon icon={cilEnvelopeLetter} customClassName="nav-icon" />,
    roles: ['ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Nhật ký hệ thống',
    to: '/app/admin/audit-logs',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['ADMIN'],
  },
  {
    component: CNavItem,
    name: 'common.profile',
    to: '/app/profile',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
    roles: ['AUTHOR', 'PC', 'CHAIR', 'ADMIN'],
  },
]

export default _nav
