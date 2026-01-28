import React, { useState, useEffect } from 'react';
import { CFormInput, CListGroup, CListGroupItem } from '@coreui/react';
import axios from 'axios';

interface Organization {
    id: number;
    name: string;
    code: string;
}

interface OrganizationSelectProps {
    value?: number;
    onChange: (id: number, name: string) => void;
    placeholder?: string;
    className?: string;
    style?: React.CSSProperties;
}

/**
 * OrganizationSelect - Searchable select for universities/organizations
 */
const OrganizationSelect: React.FC<OrganizationSelectProps> = ({
    value,
    onChange,
    placeholder = 'Chọn trường/vị...',
    className = '',
    style
}) => {
    const [organizations, setOrganizations] = useState<Organization[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filtered, setFiltered] = useState<Organization[]>([]);
    const [showDropdown, setShowDropdown] = useState(false);

    useEffect(() => {
        const fetchOrgs = async () => {
            try {
                const response = await axios.get('/api/organizations');
                setOrganizations(response.data);
            } catch (error) {
                console.error('Error fetching organizations:', error);
            }
        };
        fetchOrgs();
    }, []);

    useEffect(() => {
        if (searchTerm.trim() === '') {
            setFiltered([]);
            return;
        }
        const lower = searchTerm.toLowerCase();
        setFiltered(organizations.filter(org =>
            org.name.toLowerCase().includes(lower) ||
            org.code.toLowerCase().includes(lower)
        ));
    }, [searchTerm, organizations]);

    const handleSelect = (org: Organization) => {
        setSearchTerm(org.name);
        onChange(org.id, org.name);
        setShowDropdown(false);
    };

    return (
        <div className="position-relative w-100" style={style}>
            <CFormInput
                type="text"
                placeholder={placeholder}
                value={searchTerm}
                onChange={(e) => {
                    setSearchTerm(e.target.value);
                    setShowDropdown(true);
                }}
                onFocus={() => setShowDropdown(true)}
                className={`${className} custom-input`}
                autoComplete="off"
            />
            {showDropdown && filtered.length > 0 && (
                <CListGroup className="position-absolute w-100 shadow-sm" style={{ zIndex: 1000, maxHeight: '200px', overflowY: 'auto' }}>
                    {filtered.map(org => (
                        <CListGroupItem
                            key={org.id}
                            onClick={() => handleSelect(org)}
                            className="text-start list-group-item-action"
                            style={{ cursor: 'pointer' }}
                        >
                            <div className="fw-bold">{org.code}</div>
                            <small className="text-muted">{org.name}</small>
                        </CListGroupItem>
                    ))}
                </CListGroup>
            )}
        </div>
    );
};

export default OrganizationSelect;
