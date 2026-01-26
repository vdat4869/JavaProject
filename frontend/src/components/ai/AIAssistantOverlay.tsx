import React from 'react';
import {
    CDropdown,
    CDropdownToggle,
    CDropdownMenu,
    CDropdownItem,
    CSpinner,
    CTooltip,
} from '@coreui/react';
import CIcon from '@coreui/icons-react';
import { cilStar } from '@coreui/icons';
import { useTranslation } from 'react-i18next';

interface AIAssistantOverlayProps {
    onAction: (actionCode: string) => void;
    loading?: boolean;
    actions: {
        code: string;
        label: string;
        icon?: any;
    }[];
}

/**
 * AIAssistantOverlay - Nút trợ lý AI đa năng nằm cạnh các trường nhập liệu
 */
const AIAssistantOverlay: React.FC<AIAssistantOverlayProps> = ({
    onAction,
    loading = false,
    actions,
}) => {
    const { t } = useTranslation();

    return (
        <div className="ai-assistant-overlay" style={{ position: 'absolute', right: '10px', top: '10px', zIndex: 10 }}>
            {loading ? (
                <CSpinner size="sm" color="primary" />
            ) : (
                <CDropdown alignment="end">
                    <CTooltip content={t('ai.assistant') || 'Trợ lý AI'}>
                        <CDropdownToggle color="link" caret={false} className="p-0 text-primary">
                            <CIcon icon={cilStar} size="lg" />
                        </CDropdownToggle>
                    </CTooltip>
                    <CDropdownMenu>
                        {actions.map((action) => (
                            <CDropdownItem key={action.code} onClick={() => onAction(action.code)}>
                                {action.icon && <CIcon icon={action.icon} className="me-2" />}
                                {action.label}
                            </CDropdownItem>
                        ))}
                    </CDropdownMenu>
                </CDropdown>
            )}
        </div>
    );
};

export default AIAssistantOverlay;
