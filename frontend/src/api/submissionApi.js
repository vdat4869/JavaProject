import axios from 'axios';

const API_BASE_URL = '/api/submissions';

// Get user ID from localStorage or context (tạm thời hardcode, sẽ tích hợp với auth sau)
const getUserId = () => {
  return localStorage.getItem('userId') || '1'; // Tạm thời
};

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor để thêm X-User-Id header
api.interceptors.request.use((config) => {
  config.headers['X-User-Id'] = getUserId();
  return config;
});

export const submissionApi = {
  // Tạo submission mới
  createSubmission: async (data) => {
    const response = await api.post('', data);
    return response.data;
  },

  // Lấy submission theo ID
  getSubmission: async (id) => {
    const response = await api.get(`/${id}`);
    return response.data;
  },

  // Lấy danh sách submissions của user
  getMySubmissions: async (conferenceId = null, page = 0, size = 20) => {
    const params = { page, size };
    if (conferenceId) {
      params.conferenceId = conferenceId;
    }
    const response = await api.get('/my', { params });
    return response.data;
  },

  // Cập nhật submission
  updateSubmission: async (id, data) => {
    const response = await api.put(`/${id}`, data);
    return response.data;
  },

  // Submit submission
  submitSubmission: async (id) => {
    const response = await api.post(`/${id}/submit`);
    return response.data;
  },

  // Rút bài
  withdrawSubmission: async (id, reason) => {
    const response = await api.post(`/${id}/withdraw`, null, {
      params: { reason }
    });
    return response.data;
  },

  // Upload file
  uploadFile: async (submissionId, file, category) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', category);
    
    const response = await api.post(`/${submissionId}/files`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Xóa file
  deleteFile: async (submissionId, fileId) => {
    await api.delete(`/${submissionId}/files/${fileId}`);
  },

  // Download file
  downloadFile: async (submissionId, fileId) => {
    const response = await api.get(`/${submissionId}/files/${fileId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

