import React, { useMemo } from 'react'
import { useLocation } from 'react-router-dom'
import { useUI } from '../context/UIContext'

import {
  CCloseButton,
  CSidebar,
  CSidebarBrand,
  CSidebarFooter,
  CSidebarHeader,
  CSidebarToggler,
} from '@coreui/react'

import { AppSidebarNav } from './AppSidebarNav.jsx'
import { useAuth } from '../context/AuthContext'

// UTH Logos

import uthLogoSmall from '../assets/images/image copy.png'

// sidebar nav config
import navigation from '../_nav'

/**
 * Filter navigation items based on user roles
 * 
 * Logic:
 * - Items không có roles property: hiển thị cho tất cả users
 * - Items có roles property: chỉ hiển thị nếu user có ít nhất 1 role trong danh sách
 * - ADMIN role: có thể thấy tất cả menu items (nếu cần)
 */
const filterNavigationByRole = (items, userRoles) => {
  // Nếu user là ADMIN, có thể cho thấy tất cả (hoặc filter như bình thường)
  const isAdmin = userRoles && userRoles.includes('ADMIN')

  if (!userRoles || userRoles.length === 0) {
    // Nếu không có roles, chỉ hiển thị items không có roles requirement
    return items.filter((item) => !item.roles)
  }

  return items
    .map((item) => {
      // Nếu item có roles, kiểm tra user có role nào trong đó không
      if (item.roles && item.roles.length > 0) {
        const hasRole = item.roles.some((role) => userRoles.includes(role))
        // ADMIN có thể thấy tất cả (optional - comment out nếu muốn ADMIN chỉ thấy items có ADMIN role)
        // if (isAdmin) hasRole = true
        if (!hasRole) {
          return null // Loại bỏ item này
        }
      }

      // Nếu item có items (submenu), filter recursive
      if (item.items && item.items.length > 0) {
        const filteredItems = filterNavigationByRole(item.items, userRoles)
        if (filteredItems.length === 0) {
          return null // Loại bỏ group nếu không còn items nào
        }
        return {
          ...item,
          items: filteredItems,
        }
      }

      return item
    })
    .filter((item) => item !== null)
}

/**
 * Dynamically updates navigation items to include the current conferenceId
 */
const updateNavigationWithId = (items, activeId) => {
  if (!activeId) return items

  return items.map((item) => {
    let newItem = { ...item }

    // Replace conferenceId=1 with activeId in 'to' property
    if (newItem.to && typeof newItem.to === 'string' && newItem.to.includes('conferenceId=1')) {
      newItem.to = newItem.to.replace('conferenceId=1', `conferenceId=${activeId}`)
    }

    // Replace /conference/1/ with /conference/:activeId/
    if (newItem.to && typeof newItem.to === 'string' && newItem.to.includes('/conference/1/')) {
      newItem.to = newItem.to.replace('/conference/1/', `/conference/${activeId}/`)
    }

    if (newItem.items && newItem.items.length > 0) {
      newItem.items = updateNavigationWithId(newItem.items, activeId)
    }

    return newItem
  })
}

const AppSidebar = () => {
  const { sidebarShow, sidebarUnfoldable, setSidebarShow, setSidebarUnfoldable } = useUI()
  const { user } = useAuth()
  const location = useLocation()

  // Extract conferenceId from URL
  const activeId = useMemo(() => {
    // 1. Try to find ID in path: /app/chair/conference/:id/...
    const pathMatch = location.pathname.match(/\/app\/chair\/conference\/(\d+)/)
    if (pathMatch) return pathMatch[1]

    // 2. Try to find in query params: ?conferenceId=:id
    const queryParams = new URLSearchParams(location.search)
    const queryId = queryParams.get('conferenceId')
    if (queryId) return queryId

    // 3. Fallback to localStorage or null
    return localStorage.getItem('activeConferenceId')
  }, [location])

  // Save activeId to localStorage if found
  useMemo(() => {
    if (activeId) {
      localStorage.setItem('activeConferenceId', activeId)
    }
  }, [activeId])

  // Filter navigation based on user roles and inject activeId
  const filteredNavigation = useMemo(() => {
    const userRoles = user?.roles || []

    // Step 1: Filter by role
    let processedNav = filterNavigationByRole(navigation, userRoles)

    // Step 2: Inject current conference ID
    if (activeId) {
      processedNav = updateNavigationWithId(processedNav, activeId)
    }

    return processedNav
  }, [user?.roles, activeId])

  return (
    <CSidebar
      className="border-end"
      colorScheme="light"
      position="fixed"
      unfoldable={sidebarUnfoldable}
      visible={sidebarShow}
      onVisibleChange={(visible) => {
        setSidebarShow(visible)
      }}
    >
      <CSidebarHeader className="border-bottom" style={{ backgroundColor: '#fff', padding: '0 1rem', height: '64px', display: 'flex', alignItems: 'center' }}>
        <CSidebarBrand to="/" className="d-flex align-items-center text-decoration-none">
          <img
            src={uthLogoSmall}
            alt="UTH"
            height={36}
            className="sidebar-brand-full me-2"
            style={{ objectFit: 'contain' }}
          />
          <img
            src={uthLogoSmall}
            alt="UTH"
            height={28}
            className="sidebar-brand-narrow"
            style={{ objectFit: 'contain' }}
          />
        </CSidebarBrand>
        <CCloseButton className="d-lg-none" onClick={() => setSidebarShow(false)} />
      </CSidebarHeader>
      <AppSidebarNav items={filteredNavigation} />
      <CSidebarFooter className="border-top d-none d-lg-flex">
        <CSidebarToggler onClick={() => setSidebarUnfoldable(!sidebarUnfoldable)} />
      </CSidebarFooter>
    </CSidebar>
  )
}

export default React.memo(AppSidebar)
