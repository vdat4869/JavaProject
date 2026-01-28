import React from 'react'
import { CFooter } from '@coreui/react'

const AppFooter = () => {
  return (
    <CFooter className="px-4">
      <div>
        <strong>UTH-ConfMS</strong>
        <span className="ms-1">&copy; 2025 University of Transport Ho Chi Minh City.</span>
      </div>
      <div className="ms-auto">
        <span className="me-1">Conference Management System</span>
      </div>
    </CFooter>
  )
}

export default React.memo(AppFooter)
