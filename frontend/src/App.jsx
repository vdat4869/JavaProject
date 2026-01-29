import React, { useEffect } from 'react'
import { BrowserRouter, useRoutes } from 'react-router-dom'

import { useColorModes } from '@coreui/react'
import './scss/style.scss'
import './i18n/config'

// Providers
import { AuthProvider } from './context/AuthContext'
import { UIProvider } from './context/UIContext'

// Routes
import { routes } from './routes'

/**
 * AppRoutes - Component để render routes
 */
const AppRoutes = () => {
  const element = useRoutes(routes)
  return element
}

/**
 * App - Root component
 */
const App = () => {
  const { setColorMode } = useColorModes('uth-confms-theme')

  useEffect(() => {
    // Force light mode
    setColorMode('light')
  }, [setColorMode])

  return (
    <UIProvider>
      <AuthProvider>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </AuthProvider>
    </UIProvider>
  )
}

export default App
