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
  const { isColorModeSet, setColorMode } = useColorModes('uth-confms-theme')

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.href.split('?')[1])
    const theme = urlParams.get('theme') && urlParams.get('theme').match(/^[A-Za-z0-9\s]+/)[0]
    if (theme) {
      setColorMode(theme)
    }

    if (isColorModeSet()) {
      return
    }

    // Default theme
    setColorMode('light')
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

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
