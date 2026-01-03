import React, { useState } from 'react';
import {
  Box,
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Grid,
  MenuItem,
  Alert,
  CircularProgress,
} from '@mui/material';
import { submissionApi } from '../api/submissionApi';

/**
 * Page form submission
 */
const SubmissionForm = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  
  const [formData, setFormData] = useState({
    conferenceId: '',
    trackId: '',
    title: '',
    abstractText: '',
    keywords: '',
    notes: '',
    authors: [
      {
        firstName: '',
        lastName: '',
        email: '',
        affiliation: '',
        country: '',
        orderIndex: 1,
        isCorresponding: true,
        isPresenting: false,
      },
    ],
  });

  const handleInputChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAuthorChange = (index, field, value) => {
    setFormData((prev) => {
      const newAuthors = [...prev.authors];
      newAuthors[index] = {
        ...newAuthors[index],
        [field]: value,
      };
      return { ...prev, authors: newAuthors };
    });
  };

  const addAuthor = () => {
    setFormData((prev) => ({
      ...prev,
      authors: [
        ...prev.authors,
        {
          firstName: '',
          lastName: '',
          email: '',
          affiliation: '',
          country: '',
          orderIndex: prev.authors.length + 1,
          isCorresponding: false,
          isPresenting: false,
        },
      ],
    }));
  };

  const removeAuthor = (index) => {
    if (formData.authors.length > 1) {
      setFormData((prev) => {
        const newAuthors = prev.authors.filter((_, i) => i !== index);
        // Cập nhật orderIndex
        newAuthors.forEach((author, i) => {
          author.orderIndex = i + 1;
        });
        return { ...prev, authors: newAuthors };
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await submissionApi.createSubmission(formData);
      setSuccess(true);
      // Reset form
      setFormData({
        conferenceId: '',
        trackId: '',
        title: '',
        abstractText: '',
        keywords: '',
        notes: '',
        authors: [
          {
            firstName: '',
            lastName: '',
            email: '',
            affiliation: '',
            country: '',
            orderIndex: 1,
            isCorresponding: true,
            isPresenting: false,
          },
        ],
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi tạo submission');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Typography variant="h4" gutterBottom>
          Tạo Submission Mới
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(false)}>
            Tạo submission thành công!
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Conference ID"
                type="number"
                value={formData.conferenceId}
                onChange={(e) => handleInputChange('conferenceId', e.target.value)}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Track ID"
                type="number"
                value={formData.trackId}
                onChange={(e) => handleInputChange('trackId', e.target.value)}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Tiêu đề"
                value={formData.title}
                onChange={(e) => handleInputChange('title', e.target.value)}
                required
                multiline
                rows={2}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Abstract"
                value={formData.abstractText}
                onChange={(e) => handleInputChange('abstractText', e.target.value)}
                multiline
                rows={5}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Keywords (phân cách bằng dấu phẩy)"
                value={formData.keywords}
                onChange={(e) => handleInputChange('keywords', e.target.value)}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Ghi chú"
                value={formData.notes}
                onChange={(e) => handleInputChange('notes', e.target.value)}
                multiline
                rows={3}
              />
            </Grid>

            {/* Authors Section */}
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Danh sách tác giả
              </Typography>
              {formData.authors.map((author, index) => (
                <Paper key={index} sx={{ p: 2, mb: 2 }}>
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Họ"
                        value={author.firstName}
                        onChange={(e) =>
                          handleAuthorChange(index, 'firstName', e.target.value)
                        }
                        required
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Tên"
                        value={author.lastName}
                        onChange={(e) =>
                          handleAuthorChange(index, 'lastName', e.target.value)
                        }
                        required
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Email"
                        type="email"
                        value={author.email}
                        onChange={(e) =>
                          handleAuthorChange(index, 'email', e.target.value)
                        }
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Đơn vị công tác"
                        value={author.affiliation}
                        onChange={(e) =>
                          handleAuthorChange(index, 'affiliation', e.target.value)
                        }
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Quốc gia"
                        value={author.country}
                        onChange={(e) =>
                          handleAuthorChange(index, 'country', e.target.value)
                        }
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                        <Button
                          variant="outlined"
                          size="small"
                          onClick={() => removeAuthor(index)}
                          disabled={formData.authors.length === 1}
                        >
                          Xóa
                        </Button>
                      </Box>
                    </Grid>
                  </Grid>
                </Paper>
              ))}
              <Button variant="outlined" onClick={addAuthor} sx={{ mt: 1 }}>
                Thêm tác giả
              </Button>
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <Button variant="outlined" onClick={() => window.history.back()}>
                  Hủy
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={loading}
                  startIcon={loading ? <CircularProgress size={20} /> : null}
                >
                  {loading ? 'Đang tạo...' : 'Tạo Submission'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Container>
  );
};

export default SubmissionForm;

