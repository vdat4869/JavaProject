import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow,
    CButton,
    CSpinner,
    CBadge,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilCommentSquare } from '@coreui/icons'
import { reviewService, Assignment } from '../../services/review.service'

/**
 * PaperDiscussionBoard - Bảng theo dõi thảo luận cho PC Member
 */
const PaperDiscussionBoard: React.FC = () => {
    const navigate = useNavigate()
    const [assignments, setAssignments] = useState<Assignment[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        loadAssignments()
    }, [])

    const loadAssignments = async () => {
        try {
            setLoading(true)
            const data = await reviewService.getAssignments()
            // Lọc các bài đã submit review (thường thảo luận bắt đầu sau khi submit)
            setAssignments(data.filter(a => a.status === 'COMPLETED' || a.status === 'ACCEPTED'))
        } catch (error) {
            console.error('Error loading assignments for discussion:', error)
        } finally {
            setLoading(false)
        }
    }

    if (loading) {
        return (
            <div className="text-center p-5">
                <CSpinner color="primary" />
            </div>
        )
    }

    return (
        <CCard>
            <CCardHeader>
                <h4>Thảo luận nội bộ</h4>
                <small className="text-muted">Danh sách các bài báo bạn đang tham gia đánh giá và thảo luận</small>
            </CCardHeader>
            <CCardBody>
                <CTable hover responsive align="middle">
                    <CTableHead color="light">
                        <CTableRow>
                            <CTableHeaderCell>ID</CTableHeaderCell>
                            <CTableHeaderCell>Tiêu đề bài báo</CTableHeaderCell>
                            <CTableHeaderCell>Hội nghị</CTableHeaderCell>
                            <CTableHeaderCell>Trạng thái Review</CTableHeaderCell>
                            <CTableHeaderCell>Thao tác</CTableHeaderCell>
                        </CTableRow>
                    </CTableHead>
                    <CTableBody>
                        {assignments.length === 0 ? (
                            <CTableRow>
                                <CTableDataCell colSpan={5} className="text-center py-4 text-muted">
                                    Bạn hiện không có bài báo nào trong giai đoạn thảo luận.
                                </CTableDataCell>
                            </CTableRow>
                        ) : (
                            assignments.map((item) => (
                                <CTableRow key={item.id}>
                                    <CTableDataCell>{item.submissionId}</CTableDataCell>
                                    <CTableDataCell>
                                        <div className="fw-semibold text-primary">{item.submissionTitle}</div>
                                        <small className="text-muted">{item.trackName}</small>
                                    </CTableDataCell>
                                    <CTableDataCell>{item.conferenceName}</CTableDataCell>
                                    <CTableDataCell>
                                        <CBadge color={item.status === 'COMPLETED' ? 'success' : 'warning'}>
                                            {item.status}
                                        </CBadge>
                                    </CTableDataCell>
                                    <CTableDataCell>
                                        <CButton
                                            color="info"
                                            size="sm"
                                            variant="outline"
                                            onClick={() => navigate(`/app/pc/submissions/${item.submissionId}/discussion`)}
                                            disabled={item.status !== 'COMPLETED' && item.status !== 'ACCEPTED'}
                                        >
                                            <CIcon icon={cilCommentSquare} className="me-1" />
                                            Vào thảo luận
                                        </CButton>
                                    </CTableDataCell>
                                </CTableRow>
                            ))
                        )}
                    </CTableBody>
                </CTable>
            </CCardBody>
        </CCard>
    )
}

export default PaperDiscussionBoard
