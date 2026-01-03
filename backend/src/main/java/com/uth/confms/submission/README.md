# Module Submission + Upload PDF

Module này xử lý toàn bộ chức năng liên quan đến submission và upload file PDF.

## Cấu trúc Module


submission/
├── config/
│   └── StorageProperties.java      # Cấu hình storage (MinIO/Local)
├── controller/
│   └── SubmissionController.java   # REST API endpoints
├── dto/
│   ├── AuthorResponse.java
│   ├── FileResponse.java
│   ├── SubmissionRequest.java
│   ├── SubmissionResponse.java
│   └── UpdateSubmissionRequest.java
├── entity/
│   ├── Author.java                 # Entity tác giả
│   ├── Submission.java             # Entity submission
│   └── SubmissionFile.java         # Entity file
├── exception/
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java # Exception handler
│   └── SubmissionException.java
├── mapper/
│   └── SubmissionMapper.java      # MapStruct mapper
├── repository/
│   ├── AuthorRepository.java
│   ├── SubmissionFileRepository.java
│   └── SubmissionRepository.java
└── service/
    ├── FileStorageService.java     # Service xử lý file storage
    └── SubmissionService.java      # Business logic
```

## API Endpoints

### 1. Tạo Submission (Draft)
```
POST /api/submissions
Headers: X-User-Id: {userId}
Body: SubmissionRequest
```

### 2. Lấy Submission theo ID
```
GET /api/submissions/{id}
Headers: X-User-Id: {userId}
```

### 3. Lấy danh sách Submissions của user
```
GET /api/submissions/my?conferenceId={id}&page=0&size=20
Headers: X-User-Id: {userId}
```

### 4. Cập nhật Submission
```
PUT /api/submissions/{id}
Headers: X-User-Id: {userId}
Body: UpdateSubmissionRequest
```

### 5. Submit Submission
```
POST /api/submissions/{id}/submit
Headers: X-User-Id: {userId}
```

### 6. Rút bài (Withdraw)
```
POST /api/submissions/{id}/withdraw?reason={reason}
Headers: X-User-Id: {userId}
```

### 7. Upload File
```
POST /api/submissions/{id}/files
Headers: X-User-Id: {userId}
Content-Type: multipart/form-data
Body: file, category (MANUSCRIPT/SUPPLEMENTARY/REVISION)
```

### 8. Xóa File
```
DELETE /api/submissions/{id}/files/{fileId}
Headers: X-User-Id: {userId}
```

### 9. Download File
```
GET /api/submissions/{id}/files/{fileId}/download
Headers: X-User-Id: {userId}
```

## Cấu hình Storage

Trong `application.yml`:

```yaml
storage:
  type: local  # hoặc "minio"
  local:
    base-path: ./uploads
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket-name: uth-confms
```

## Workflow

1. **Tạo Submission (DRAFT)**
   - User tạo submission với thông tin cơ bản và danh sách tác giả
   - Submission được tạo ở trạng thái DRAFT

2. **Upload File**
   - User có thể upload file PDF (MANUSCRIPT, SUPPLEMENTARY, REVISION)
   - File được lưu vào storage (Local hoặc MinIO)
   - Mỗi file có version và checksum

3. **Submit Submission**
   - Chỉ có thể submit khi submission ở trạng thái DRAFT
   - Phải có ít nhất 1 file MANUSCRIPT
   - Chuyển trạng thái từ DRAFT → SUBMITTED

4. **Cập nhật Submission**
   - Chỉ có thể cập nhật khi ở trạng thái DRAFT
   - Có thể thêm/sửa/xóa tác giả
   - Có thể upload file mới

5. **Rút bài (Withdraw)**
   - Có thể rút bài khi chưa được ACCEPT/REJECT
   - Cần cung cấp lý do rút bài

## Validation

- File chỉ chấp nhận PDF (application/pdf)
- File size tối đa: 50MB
- Submission phải có ít nhất 1 tác giả
- Phải có ít nhất 1 corresponding author
- Title tối đa 500 ký tự
- Abstract tối đa 5000 ký tự

## Security

- Mỗi request cần header `X-User-Id` để xác định user
- User chỉ có thể xem/sửa submission của chính mình
- File chỉ có thể upload/download bởi submitter

## Frontend

Frontend được tạo bằng React + Material-UI, bao gồm:
- `SubmissionList.jsx` - Danh sách submissions
- `SubmissionForm.jsx` - Form tạo/sửa submission
- `SubmissionDetail.jsx` - Chi tiết submission và quản lý file

## Lưu ý

- Hiện tại sử dụng header `X-User-Id` để xác thực, cần tích hợp với JWT sau
- File storage hỗ trợ cả Local và MinIO
- Cần cấu hình database PostgreSQL trước khi chạy
- Frontend cần chạy trên port 3000, backend trên port 8080


