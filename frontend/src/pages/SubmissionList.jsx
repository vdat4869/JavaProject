import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Button,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Alert,
} from '@mui/material';
import { Edit, Delete, Upload, Download, Visibility } from '@mui/icons-material';
import { submissionApi } from '../api/submissionApi';
import { useNavigate } from 'react-router-dom';

const SubmissionList = () => {
  const navigate = useNavigate();
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [withdrawDialogOpen, setWithdrawDialogOpen] = useState(false);
  const [selectedSubmission, setSelectedSubmission] = useState(null);
  const [withdrawReason, setWithdrawReason] = useState('');

  useEffect(() => {
    loadSubmissions();
  }, []);

  const loadSubmissions = async () => {
    try {
      setLoading(true);
      const data = await submissionApi.getMySubmissions();
      setSubmissions(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (id) => {
    try {
      await submissionApi.submitSubmission(id);
      loadSubmissions();
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra');
    }
  };

  const handleWithdraw = async () => {
    try {
      await submissionApi.withdrawSubmission(selectedSubmission.id, withdrawReason);
      setWithdrawDialogOpen(false);
      setWithdrawReason('');
      loadSubmissions();
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra');
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      DRAFT: 'default',
      SUBMITTED: 'info',
      UNDER_REVIEW: 'warning',
      REVISION: 'warning',
      ACCEPTED: 'success',
      REJECTED: 'error',
      WITHDRAWN: 'default',
    };
    return colors[status] || 'default';
  };

  const getStatusLabel = (status) => {
    const labels = {
      DRAFT: 'Nháp',
      SUBMITTED: 'Đã nộp',
      UNDER_REVIEW: 'Đang phản biện',
      REVISION: 'Yêu cầu chỉnh sửa',
      ACCEPTED: 'Chấp nhận',
      REJECTED: 'Từ chối',
      WITHDRAWN: 'Đã rút',
    };
    return labels[status] || status;
  };

  if (loading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
          <Typography variant="h4">Danh sách Submissions</Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/submissions/new')}
          >
            Tạo Submission Mới
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Submission Number</TableCell>
                <TableCell>Tiêu đề</TableCell>
                <TableCell>Trạng thái</TableCell>
                <TableCell>Ngày tạo</TableCell>
                <TableCell>Thao tác</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {submissions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    Chưa có submission nào
                  </TableCell>
                </TableRow>
              ) : (
                submissions.map((submission) => (
                  <TableRow key={submission.id}>
                    <TableCell>{submission.submissionNumber}</TableCell>
                    <TableCell>{submission.title}</TableCell>
                    <TableCell>
                      <Chip
                        label={getStatusLabel(submission.status)}
                        color={getStatusColor(submission.status)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {new Date(submission.createdAt).toLocaleDateString('vi-VN')}
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <IconButton
                          size="small"
                          onClick={() => navigate(`/submissions/${submission.id}`)}
                        >
                          <Visibility />
                        </IconButton>
                        {submission.status === 'DRAFT' && (
                          <>
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/submissions/${submission.id}/edit`)}
                            >
                              <Edit />
                            </IconButton>
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => handleSubmit(submission.id)}
                            >
                              Submit
                            </Button>
                          </>
                        )}
                        {submission.status !== 'WITHDRAWN' &&
                          submission.status !== 'ACCEPTED' &&
                          submission.status !== 'REJECTED' && (
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              onClick={() => {
                                setSelectedSubmission(submission);
                                setWithdrawDialogOpen(true);
                              }}
                            >
                              Rút bài
                            </Button>
                          )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Withdraw Dialog */}
      <Dialog open={withdrawDialogOpen} onClose={() => setWithdrawDialogOpen(false)}>
        <DialogTitle>Rút bài</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            multiline
            rows={4}
            label="Lý do rút bài"
            value={withdrawReason}
            onChange={(e) => setWithdrawReason(e.target.value)}
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWithdrawDialogOpen(false)}>Hủy</Button>
          <Button onClick={handleWithdraw} variant="contained" color="error">
            Xác nhận
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default SubmissionList;




