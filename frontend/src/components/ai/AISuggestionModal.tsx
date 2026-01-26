import React from 'react';
import {
    CModal,
    CModalHeader,
    CModalTitle,
    CModalBody,
    CModalFooter,
    CButton,
    CTable,
    CTableBody,
    CTableRow,
    CTableDataCell,
    CBadge,
} from '@coreui/react';
import { useTranslation } from 'react-i18next';

interface Recommendation {
    before: string;
    after: string;
    changeType?: string;
    explanation: string;
    field?: string;
}

interface AISuggestionModalProps {
    visible: boolean;
    onClose: () => void;
    title: string;
    recommendations: Recommendation[];
    onApply: (selectedIndices: number[]) => void;
    loading?: boolean;
}

/**
 * AISuggestionModal - Hiển thị so sánh giữa văn bản gốc và gợi ý từ AI
 */
const AISuggestionModal: React.FC<AISuggestionModalProps> = ({
    visible,
    onClose,
    title,
    recommendations,
    onApply,
    loading = false,
}) => {
    const { t } = useTranslation();
    const [selectedIndices, setSelectedIndices] = React.useState<number[]>([]);

    React.useEffect(() => {
        if (visible) {
            // Mặc định chọn tất cả gợi ý khi mở modal
            setSelectedIndices(recommendations.map((_, i) => i));
        }
    }, [visible, recommendations]);

    const toggleSelect = (index: number) => {
        setSelectedIndices((prev) =>
            prev.includes(index) ? prev.filter((i) => i !== index) : [...prev, index]
        );
    };

    const handleApply = () => {
        onApply(selectedIndices);
        onClose();
    };

    const getBadgeColor = (type?: string) => {
        switch (type) {
            case 'CLARITY': return 'info';
            case 'GRAMMAR': return 'warning';
            case 'CONCISENESS': return 'success';
            case 'FLOW': return 'primary';
            default: return 'secondary';
        }
    };

    return (
        <CModal visible={visible} onClose={onClose} size="lg" scrollable>
            <CModalHeader>
                <CModalTitle>{title}</CModalTitle>
            </CModalHeader>
            <CModalBody>
                <p className="text-muted mb-3 small">
                    {t('ai.auditNotice') || 'Lưu ý: Mọi thao tác sử dụng AI đều được ghi lại trong nhật ký hệ thống.'}
                </p>
                <CTable hover responsive align="middle">
                    <CTableBody>
                        {recommendations.map((rec, index) => (
                            <CTableRow key={index} className={selectedIndices.includes(index) ? 'table-active' : ''}>
                                <CTableDataCell style={{ width: '40px' }}>
                                    <input
                                        type="checkbox"
                                        className="form-check-input"
                                        checked={selectedIndices.includes(index)}
                                        onChange={() => toggleSelect(index)}
                                    />
                                </CTableDataCell>
                                <CTableDataCell>
                                    <div className="d-flex flex-column gap-2 py-2">
                                        {rec.changeType && (
                                            <div className="mb-1">
                                                <CBadge color={getBadgeColor(rec.changeType)}>{rec.changeType}</CBadge>
                                                {rec.field && <span className="ms-2 small text-uppercase">[{rec.field}]</span>}
                                            </div>
                                        )}
                                        <div className="row g-2">
                                            <div className="col-6 border-end">
                                                <small className="text-muted d-block mb-1">Gốc:</small>
                                                <del className="text-danger">{rec.before}</del>
                                            </div>
                                            <div className="col-6">
                                                <small className="text-muted d-block mb-1">Gợi ý:</small>
                                                <ins className="text-success text-decoration-none fw-bold">{rec.after}</ins>
                                            </div>
                                        </div>
                                        <div className="mt-2 p-2 bg-light rounded border-start border-4 border-info">
                                            <small className="fst-italic">{rec.explanation}</small>
                                        </div>
                                    </div>
                                </CTableDataCell>
                            </CTableRow>
                        ))}
                    </CTableBody>
                </CTable>
            </CModalBody>
            <CModalFooter>
                <CButton color="secondary" onClick={onClose} disabled={loading}>
                    {t('common.cancel') || 'Hủy'}
                </CButton>
                <CButton
                    color="primary"
                    onClick={handleApply}
                    disabled={loading || selectedIndices.length === 0}
                >
                    {t('ai.applySelected') || `Áp dụng ${selectedIndices.length} thay đổi`}
                </CButton>
            </CModalFooter>
        </CModal>
    );
};

export default AISuggestionModal;
