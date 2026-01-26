import React from 'react';
import {
    CModal,
    CModalHeader,
    CModalTitle,
    CModalBody,
    CModalFooter,
    CButton,
    CBadge,
} from '@coreui/react';
import { useTranslation } from 'react-i18next';

interface AIKeyword {
    keyword: string;
    relevanceScore: number;
    explanation: string;
    isCommon: boolean;
}

interface AIKeywordPickerProps {
    visible: boolean;
    onClose: () => void;
    keywords: AIKeyword[];
    onApply: (selectedKeywords: string[]) => void;
    loading?: boolean;
}

const AIKeywordPicker: React.FC<AIKeywordPickerProps> = ({
    visible,
    onClose,
    keywords,
    onApply,
    loading = false,
}) => {
    const { t } = useTranslation();
    const [selected, setSelected] = React.useState<string[]>([]);

    const toggleSelect = (keyword: string) => {
        setSelected((prev) =>
            prev.includes(keyword) ? prev.filter((k) => k !== keyword) : [...prev, keyword]
        );
    };

    const handleApply = () => {
        onApply(selected);
        onClose();
    };

    return (
        <CModal visible={visible} onClose={onClose} size="lg">
            <CModalHeader>
                <CModalTitle>{t('ai.suggestedKeywords') || 'Gợi ý từ khóa'}</CModalTitle>
            </CModalHeader>
            <CModalBody>
                <div className="d-flex flex-wrap gap-2">
                    {keywords.map((item, index) => (
                        <div
                            key={index}
                            className={`p-3 border rounded cursor-pointer position-relative ${selected.includes(item.keyword) ? 'border-primary bg-light' : ''
                                }`}
                            style={{ width: '48%', cursor: 'pointer' }}
                            onClick={() => toggleSelect(item.keyword)}
                        >
                            <div className="d-flex justify-content-between align-items-center mb-1">
                                <strong className="text-primary">{item.keyword}</strong>
                                <CBadge color={item.relevanceScore > 0.8 ? 'success' : 'info'}>
                                    {Math.round(item.relevanceScore * 100)}%
                                </CBadge>
                            </div>
                            <p className="small text-muted mb-0">{item.explanation}</p>
                            {item.isCommon && (
                                <CBadge color="secondary" className="position-absolute bottom-0 end-0 m-2" style={{ fontSize: '0.6rem' }}>
                                    {t('ai.commonTerm') || 'Phổ biến'}
                                </CBadge>
                            )}
                        </div>
                    ))}
                </div>
            </CModalBody>
            <CModalFooter>
                <CButton color="secondary" onClick={onClose} disabled={loading}>
                    {t('common.cancel') || 'Hủy'}
                </CButton>
                <CButton color="primary" onClick={handleApply} disabled={loading || selected.length === 0}>
                    {t('common.add') || 'Thêm'} {selected.length} {t('ai.keywords') || 'từ khóa'}
                </CButton>
            </CModalFooter>
        </CModal>
    );
};

export default AIKeywordPicker;
