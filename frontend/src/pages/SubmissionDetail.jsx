import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Button,
  Chip,
  Grid,
  List,
  ListItem,
  ListItemText,
  Divider,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
} from '@mui/material';
import { Upload, Download, Delete } from '@mui/icons-material';
import { submissionApi } from '../api/submissionApi';
import { useParams, useNavigate } from 'react-router-dom';

/**
 * Page detail submission
 */
const SubmissionDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [fileCategory, setFileCategory] = useState('MANUSCRIPT');

  useEffect(() => {
    loadSubmission();
  }, [id]);

  const loadSubmission = async () => {
    try {
      setLoading(true);
      const data = await submissionApi.getSubmission(id);
      setSubmission(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải submission');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async () => {
    if (!selectedFile) return;

    try {
      await submissionApi.uploadFile(id, selectedFile, fileCategory);
      setUploadDialogOpen(false);
      setSelectedFile(null);
      loadSubmission();
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi upload file');
    }
  };

  const handleFileDelete = async (fileId) => {
    if (!window.confirm('Bạn có chắc muốn xóa file này?')) return;

    try {
      await submissionApi.deleteFile(id, fileId);
      loadSubmission();
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi xóa file');
    }
  };

  const handleFileDownload = async (fileId, fileName) => {
    try {
      const blob = await submissionApi.downloadFile(id, fileId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi download file');
    }
  };

  const handleSubmit = async () => {
    try {
      await submissionApi.submitSubmission(id);
      loadSubmission();
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

  if (error || !submission) {
    return (
      <Container>
        <Alert severity="error" sx={{ mt: 4 }}>
          {error || 'Không tìm thấy submission'}
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
          <Typography variant="h4">Chi tiết Submission</Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            {submission.status === 'DRAFT' && (
              <Button variant="contained" onClick={handleSubmit}>
                Submit
              </Button>
            )}
            <Button
              variant="outlined"
              startIcon={<Upload />}
              onClick={() => setUploadDialogOpen(true)}
              disabled={submission.status !== 'DRAFT' && submission.status !== 'REVISION'}
            >
              Upload File
            </Button>
          </Box>
        </Box>

        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Thông tin cơ bản
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Submission Number
                </Typography>
                <Typography variant="body1">{submission.submissionNumber}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Trạng thái
                </Typography>
                <Chip
                  label={getStatusLabel(submission.status)}
                  color={getStatusColor(submission.status)}
                  size="small"
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary">
                  Tiêu đề
                </Typography>
                <Typography variant="body1">{submission.title}</Typography>
              </Grid>
              {submission.abstractText && (
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">
                    Abstract
                  </Typography>
                  <Typography variant="body1">{submission.abstractText}</Typography>
                </Grid>
              )}
              {submission.keywords && (
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">
                    Keywords
                  </Typography>
                  <Typography variant="body1">{submission.keywords}</Typography>
                </Grid>
              )}
            </Grid>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Danh sách tác giả
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <List>
              {submission.authors?.map((author, index) => (
                <React.Fragment key={author.id}>
                  <ListItem>
                    <ListItemText
                      primary={`${index + 1}. ${author.firstName} ${author.lastName}`}
                      secondary={
                        <>
                          {author.email && <div>Email: {author.email}</div>}
                          {author.affiliation && <div>Affiliation: {author.affiliation}</div>}
                          {author.country && <div>Country: {author.country}</div>}
                          {author.isCorresponding && (
                            <Chip label="Corresponding Author" size="small" sx={{ mt: 1 }} />
                          )}
                        </>
                      }
                    />
                  </ListItem>
                  {index < submission.authors.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Files
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {submission.files && submission.files.length > 0 ? (
              <List>
                {submission.files.map((file) => (
                  <React.Fragment key={file.id}>
                    <ListItem
                      secondaryAction={
                        <Box>
                          <IconButton
                            edge="end"
                            onClick={() => handleFileDownload(file.id, file.fileName)}
                          >
                            <Download />
                          </IconButton>
                          {(submission.status === 'DRAFT' ||
                            submission.status === 'REVISION') && (
                            <IconButton
                              edge="end"
                              color="error"
                              onClick={() => handleFileDelete(file.id)}
                            >
                              <Delete />
                            </IconButton>
                          )}
                        </Box>
                      }
                    >
                      <ListItemText
                        primary={file.fileName}
                        secondary={
                          <>
                            <div>Category: {file.category}</div>
                            <div>Version: {file.version}</div>
                            <div>Size: {(file.fileSize / 1024 / 1024).toFixed(2)} MB</div>
                            {file.isLatest && (
                              <Chip label="Latest" size="small" sx={{ mt: 0.5 }} />
                            )}
                          </>
                        }
                      />
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
              </List>
            ) : (
              <Typography variant="body2" color="text.secondary">
                Chưa có file nào
              </Typography>
            )}
          </Grid>
        </Grid>
      </Paper>

      {/* Upload Dialog */}
      <Dialog open={uploadDialogOpen} onClose={() => setUploadDialogOpen(false)}>
        <DialogTitle>Upload File</DialogTitle>
        <DialogContent>
          <TextField
            select
            fullWidth
            label="Category"
            value={fileCategory}
            onChange={(e) => setFileCategory(e.target.value)}
            sx={{ mt: 2, mb: 2 }}
            SelectProps={{
              native: true,
            }}
          >
            <option value="MANUSCRIPT">Manuscript</option>
            <option value="SUPPLEMENTARY">Supplementary</option>
            <option value="REVISION">Revision</option>
          </TextField>
          <input
            type="file"
            accept="application/pdf"
            onChange={(e) => setSelectedFile(e.target.files[0])}
            style={{ marginTop: '16px' }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUploadDialogOpen(false)}>Hủy</Button>
          <Button
            onClick={handleFileUpload}
            variant="contained"
            disabled={!selectedFile}
          >
            Upload
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default SubmissionDetail;

