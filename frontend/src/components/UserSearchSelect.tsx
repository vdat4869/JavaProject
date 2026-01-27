import React, { useState, useEffect, useRef } from 'react'
import { CFormInput, CListGroup, CListGroupItem, CSpinner } from '@coreui/react'
import { userService, UserDTO } from '../services/user.service'

interface UserSearchSelectProps {
    value?: number
    onChange: (userId: number | undefined) => void
    placeholder?: string
    invalid?: boolean
}

const UserSearchSelect: React.FC<UserSearchSelectProps> = ({
    value,
    onChange,
    placeholder = 'Tìm kiếm user...',
    invalid = false,
}) => {
    const [keyword, setKeyword] = useState('')
    const [results, setResults] = useState<UserDTO[]>([])
    const [loading, setLoading] = useState(false)
    const [showDropdown, setShowDropdown] = useState(false)
    const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null)

    const wrapperRef = useRef<HTMLDivElement>(null)

    // Load selected user info if value is present (for initial load or edit mode)
    useEffect(() => {
        if (value && !selectedUser) {
            // In a real scenario, you might want to fetch the specific user by ID if not already available
            // For now, if we have a way to get user by ID, we should do it.
            // Based on userService, we have getUserById.
            const fetchUser = async () => {
                try {
                    const user = await userService.getUserById(value)
                    setSelectedUser(user)
                    setKeyword(`${user.firstName} ${user.lastName} (${user.email})`)
                } catch (error) {
                    console.error('Failed to fetch user', error)
                }
            }
            fetchUser()
        } else if (!value) {
            setSelectedUser(null)
            setKeyword('')
        }
    }, [value])

    // Click outside to close dropdown
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setShowDropdown(false)
            }
        }
        document.addEventListener('mousedown', handleClickOutside)
        return () => {
            document.removeEventListener('mousedown', handleClickOutside)
        }
    }, [wrapperRef])

    // Debounce search
    useEffect(() => {
        const timer = setTimeout(async () => {
            if (keyword.trim() && keyword !== selectedUser?.email && !keyword.includes('(')) {
                try {
                    setLoading(true)
                    const data = await userService.searchUsers(keyword)
                    setResults(data.content || [])
                    setShowDropdown(true)
                } catch (error) {
                    console.error('Search failed', error)
                    setResults([])
                } finally {
                    setLoading(false)
                }
            } else if (!keyword.trim()) {
                setShowDropdown(false)
                setResults([])
            }
        }, 500)

        return () => clearTimeout(timer)
    }, [keyword])

    const handleSelect = (user: UserDTO) => {
        setSelectedUser(user)
        setKeyword(`${user.firstName} ${user.lastName} (${user.email})`)
        onChange(user.id)
        setShowDropdown(false)
    }

    const handleClear = () => {
        setKeyword('')
        setSelectedUser(null)
        onChange(undefined)
        setShowDropdown(false)
    }

    const fetchDefaultUsers = async () => {
        try {
            setLoading(true)
            const data = await userService.getActiveUsers(0, 5) // Get first 5 active users
            setResults(data.content || [])
            console.log('Default users loaded:', data.content)
            setShowDropdown(true)
        } catch (error) {
            console.error('Failed to fetch default users', error)
        } finally {
            setLoading(false)
        }
    }

    const handleFocus = () => {
        if (!keyword.trim() && !selectedUser) {
            fetchDefaultUsers()
        } else if (results.length > 0) {
            setShowDropdown(true)
        }
    }

    return (
        <div className="position-relative" ref={wrapperRef}>
            <div className="d-flex">
                <CFormInput
                    type="text"
                    value={keyword}
                    onChange={(e) => {
                        setKeyword(e.target.value)
                        if (selectedUser && e.target.value !== `${selectedUser.firstName} ${selectedUser.lastName} (${selectedUser.email})`) {
                            // If user modifies the input after selection, clear selection
                            if (!e.target.value) {
                                handleClear();
                            }
                        }
                    }}
                    placeholder={placeholder}
                    invalid={invalid}
                    autoComplete="off"
                    onFocus={handleFocus}
                />
                {loading && (
                    <div className="position-absolute end-0 top-50 translate-middle-y me-2">
                        <CSpinner size="sm" color="secondary" />
                    </div>
                )}
            </div>

            {showDropdown && results.length > 0 && (
                <CListGroup className="position-absolute w-100 shadow-sm" style={{ zIndex: 9999, maxHeight: '200px', overflowY: 'auto' }}>
                    {results.map((user) => (
                        <CListGroupItem
                            key={user.id}
                            as="button"
                            type="button"
                            onClick={() => handleSelect(user)}
                            className="list-group-item-action"
                        >
                            <div>
                                <strong>{user.firstName} {user.lastName}</strong>
                            </div>
                            <small className="text-muted">{user.email} - ID: {user.id}</small>
                        </CListGroupItem>
                    ))}
                </CListGroup>
            )}

            {showDropdown && results.length === 0 && keyword.trim() && !loading && (
                <div className="position-absolute w-100 p-2 bg-white border rounded shadow-sm text-center text-muted" style={{ zIndex: 1000 }}>
                    No users found
                </div>
            )}
        </div>
    )
}

export default UserSearchSelect
