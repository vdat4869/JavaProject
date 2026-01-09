import React from 'react'
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
} from '@coreui/icons'
import { CNavGroup, CNavItem, CNavTitle } from '@coreui/react'

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
    name: 'Bài nộp',
    to: '/app/author/submissions',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['AUTHOR'],
  },
  {
    component: CNavItem,
    name: 'Nộp bài mới',
    to: '/app/author/submissions/new',
    icon: <CIcon icon={cilPlus} customClassName="nav-icon" />,
    roles: ['AUTHOR'],
  },
  {
    component: CNavTitle,
    name: 'PC / Reviewer',
    roles: ['PC', 'REVIEWER', 'PC_MEMBER'],
  },
  {
    component: CNavItem,
    name: 'Bài được giao',
    to: '/app/pc/assignments',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['PC', 'REVIEWER', 'PC_MEMBER'],
  },
  {
    component: CNavTitle,
    name: 'Chair / Admin',
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Cấu hình hội nghị',
    to: '/app/chair/conference/1/config',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Quản lý PC',
    to: '/app/chair/pc?conferenceId=1',
    icon: <CIcon icon={cilCheckCircle} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Assignment Dashboard',
    to: '/app/chair/assignments?conferenceId=1',
    icon: <CIcon icon={cilList} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Decision Board',
    to: '/app/chair/decisions?conferenceId=1',
    icon: <CIcon icon={cilCheckCircle} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Bulk Email',
    to: '/app/chair/email?conferenceId=1',
    icon: <CIcon icon={cilCommentSquare} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Export Proceedings',
    to: '/app/chair/proceedings?conferenceId=1',
    icon: <CIcon icon={cilPrint} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Reporting',
    to: '/app/chair/reports?conferenceId=1',
    icon: <CIcon icon={cilChart} customClassName="nav-icon" />,
    roles: ['CHAIR', 'ADMIN'],
  },
  {
    component: CNavItem,
    name: 'Camera-ready',
    to: '/app/author/submissions/1/camera-ready',
    icon: <CIcon icon={cilCloudUpload} customClassName="nav-icon" />,
    roles: ['AUTHOR'],
  },
]

export default _nav
