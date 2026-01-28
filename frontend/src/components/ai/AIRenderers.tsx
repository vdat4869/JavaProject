import React from 'react';
import { CCard, CCardBody, CCardHeader, CNav, CNavItem, CNavLink, CTabContent, CTabPane, CListGroup, CListGroupItem } from '@coreui/react';
import { useTranslation } from 'react-i18next';

interface NeuralSummaryProps {
    summary: string;
    wordCount: number;
}

/**
 * AINeuralSummary - Hiển thị tóm tắt khách quan cho PC/Reviewer
 */
export const AINeuralSummary: React.FC<NeuralSummaryProps> = ({ summary, wordCount }) => {
    const { t } = useTranslation();
    return (
        <CCard className="mb-3 border-start-info border-start-4 shadow-sm">
            <CCardHeader className="border-0 d-flex justify-content-between align-items-center">
                <h6 className="mb-0 text-white fw-bold">AI Neutral Summary</h6>
                <small className="text-muted">{wordCount} words</small>
            </CCardHeader>
            <CCardBody>
                <p className="mb-0 text-dark" style={{ lineHeight: '1.6' }}>{summary}</p>
                <div className="mt-2 pt-2 border-top">
                    <small className="text-muted fst-italic">
                        {t('ai.neutralNotice') || 'Thông tin này được tạo khách quan từ abstract, không chứa nhận xét cá nhân hoặc thông tin tác giả.'}
                    </small>
                </div>
            </CCardBody>
        </CCard>
    );
};

interface KeyPointsProps {
    claims: string[];
    methods: string[];
    datasets: string[];
    findings: string[];
}

/**
 * AIKeyPoints - Hiển thị các điểm chính được trích xuất từ abstract
 */
export const AIKeyPoints: React.FC<KeyPointsProps> = ({ claims, methods, datasets, findings }) => {
    const [activeTab, setActiveTab] = React.useState(1);
    const { t } = useTranslation();

    const renderList = (items: string[], emptyMsg: string) => (
        items && items.length > 0 ? (
            <CListGroup flush>
                {items.map((item, i) => (
                    <CListGroupItem key={i} className="border-0 ps-0">
                        <span className="me-2 text-primary">•</span> {item}
                    </CListGroupItem>
                ))}
            </CListGroup>
        ) : <p className="text-muted mb-0">{emptyMsg}</p>
    );

    return (
        <CCard className="mb-3 shadow-sm border-0">
            <CCardHeader>
                <CNav variant="tabs" layout="fill">
                    <CNavItem>
                        <CNavLink active={activeTab === 1} onClick={() => setActiveTab(1)} style={{ cursor: 'pointer' }}>
                            Claims
                        </CNavLink>
                    </CNavItem>
                    <CNavItem>
                        <CNavLink active={activeTab === 2} onClick={() => setActiveTab(2)} style={{ cursor: 'pointer' }}>
                            Methods
                        </CNavLink>
                    </CNavItem>
                    <CNavItem>
                        <CNavLink active={activeTab === 3} onClick={() => setActiveTab(3)} style={{ cursor: 'pointer' }}>
                            Findings
                        </CNavLink>
                    </CNavItem>
                    {datasets && datasets.length > 0 && (
                        <CNavItem>
                            <CNavLink active={activeTab === 4} onClick={() => setActiveTab(4)} style={{ cursor: 'pointer' }}>
                                Datasets
                            </CNavLink>
                        </CNavItem>
                    )}
                </CNav>
            </CCardHeader>
            <CCardBody>
                <CTabContent>
                    <CTabPane visible={activeTab === 1}>
                        {renderList(claims, 'No specific claims extracted.')}
                    </CTabPane>
                    <CTabPane visible={activeTab === 2}>
                        {renderList(methods, 'No specific methods extracted.')}
                    </CTabPane>
                    <CTabPane visible={activeTab === 3}>
                        {renderList(findings, 'No specific results extracted.')}
                    </CTabPane>
                    <CTabPane visible={activeTab === 4}>
                        {renderList(datasets, 'No specific datasets mentioned.')}
                    </CTabPane>
                </CTabContent>
            </CCardBody>
        </CCard>
    );
};
