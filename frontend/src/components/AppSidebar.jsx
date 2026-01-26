import React, { useMemo } from 'react'
import { useUI } from '../context/UIContext'

import {
  CCloseButton,
  CSidebar,
  CSidebarBrand,
  CSidebarFooter,
  CSidebarHeader,
  CSidebarToggler,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'

import { AppSidebarNav } from './AppSidebarNav.jsx'
import { useAuth } from '../context/AuthContext'

import { logo } from '../assets/brand/logo'
import { sygnet } from '../assets/brand/sygnet'

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

const AppSidebar = () => {
  const { sidebarShow, sidebarUnfoldable, setSidebarShow, setSidebarUnfoldable } = useUI()
  const { user } = useAuth()

  // Filter navigation based on user roles
  const filteredNavigation = useMemo(() => {
    const userRoles = user?.roles || []
    // Debug: log user roles để kiểm tra
    if (userRoles.length > 0) {
      console.log('User roles:', userRoles)
    }
    const filtered = filterNavigationByRole(navigation, userRoles)
    console.log('Filtered navigation items:', filtered.length, 'out of', navigation.length)
    return filtered
  }, [user?.roles])

  return (
    <CSidebar
      className="border-end"
      colorScheme="dark"
      position="fixed"
      unfoldable={sidebarUnfoldable}
      visible={sidebarShow}
      onVisibleChange={(visible) => {
        setSidebarShow(visible)
      }}
    >
      <CSidebarHeader className="border-bottom">
        <CSidebarBrand to="/">
          <CIcon customClassName="sidebar-brand-full" icon={logo} height={32} />
          <CIcon customClassName="sidebar-brand-narrow" icon={sygnet} height={32} />
        </CSidebarBrand>
        <CCloseButton className="d-lg-none" dark onClick={() => setSidebarShow(false)} />
      </CSidebarHeader>
      <AppSidebarNav items={filteredNavigation} />
      <CSidebarFooter className="border-top d-none d-lg-flex">
        <CSidebarToggler onClick={() => setSidebarUnfoldable(!sidebarUnfoldable)} />
      </CSidebarFooter>
    </CSidebar>
  )
}

export default React.memo(AppSidebar)
