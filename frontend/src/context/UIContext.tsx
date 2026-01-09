import React, { createContext, useContext, useState, ReactNode } from 'react'

/**
 * UI Context State
 */
interface UIContextType {
  sidebarShow: boolean
  sidebarUnfoldable: boolean
  setSidebarShow: (show: boolean) => void
  setSidebarUnfoldable: (unfoldable: boolean) => void
  toggleSidebar: () => void
}

const UIContext = createContext<UIContextType | undefined>(undefined)

/**
 * UIProvider Props
 */
interface UIProviderProps {
  children: ReactNode
}

/**
 * UIProvider - Quản lý UI state (sidebar, theme)
 * Thay thế Redux cho UI state management
 */
export const UIProvider: React.FC<UIProviderProps> = ({ children }) => {
  const [sidebarShow, setSidebarShow] = useState(true)
  const [sidebarUnfoldable, setSidebarUnfoldable] = useState(false)

  const toggleSidebar = () => {
    setSidebarShow((prev) => !prev)
  }

  const value: UIContextType = {
    sidebarShow,
    sidebarUnfoldable,
    setSidebarShow,
    setSidebarUnfoldable,
    toggleSidebar,
  }

  return <UIContext.Provider value={value}>{children}</UIContext.Provider>
}

/**
 * Hook để sử dụng UI Context
 */
export const useUI = (): UIContextType => {
  const context = useContext(UIContext)
  if (context === undefined) {
    throw new Error('useUI must be used within a UIProvider')
  }
  return context
}
