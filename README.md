# UTH-ConfMS  

Hệ thống quản lý giấy tờ cho Hội nghị Nghiên cứu Khoa học Trường ĐH UTH  
Mô phỏng workflow kiểu EasyChair nhưng gọn, sạch, tối ưu cho vận hành nội bộ.

---

## 1. Mô tả dự án

Hệ thống hỗ trợ toàn bộ quy trình hội nghị khoa học:  
**CFP → Nộp bài → Gán reviewer → Phản biện → Quyết định → Camera-ready → Xuất kỷ yếu**

### Chức năng chính

| Module | Mô tả |
|--------|-------|
| **Auth** | Đăng ký, đăng nhập, JWT + Refresh Token, OAuth2/Google SSO |
| **Conference** | Tạo/cấu hình hội nghị, track, deadline, template email, CFP |
| **Submission** | Tác giả nộp bài, sửa, rút, upload PDF + metadata |
| **PC** | Quản lý PC member, mời reviewer, COI declaration |
| **Assignment** | Gán bài cho reviewer, quản lý workload |
| **Review** | Chấm điểm (thang điểm học thuật chuẩn hóa), nhận xét, thảo luận nội bộ |
| **Decision** | Accept/Reject, gửi email kết quả hàng loạt |
| **Camera-ready** | Upload bản cuối, kiểm tra PDF, xác nhận copyright |
| **Reporting** | Dashboard, thống kê, snapshot, export báo cáo (PDF/Excel/CSV), Audit Logs với bộ lọc & export |
| **Storage** | Lưu trữ file với MinIO hoặc Local filesystem |
| **Email** | Gửi email với template, hàng đợi email với Redis |
| **AI** | Hỗ trợ tóm tắt, gợi ý keyword, COI detection (Gemini AI) |

---

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ & Phiên bản |
|-----------|----------------------|
| **Backend** | Java 17, Spring Boot 3.3.5, Spring Web, Spring Security, Spring Data JPA |
| **Auth** | JWT (Access + Refresh Token), OAuth2/OpenID Connect (Google SSO) |
| **Frontend** | React 18 + Vite, CoreUI React, Axios |
| **Database** | PostgreSQL 15+ |
| **Cache/Queue** | Redis – cache token, rate limiting, email queue |
| **Storage** | MinIO (S3-compatible) hoặc Local Storage |
| **Email** | SMTP + Spring Mail (UTF-8 support) |
| **ORM** | Hibernate / JPA |
| **DTO Mapping** | MapStruct |
| **Validation** | Jakarta Validation (Spring Validation) |
| **API Docs** | Swagger / OpenAPI 3 |
| **PDF Toolkit** | Apache PDFBox (kiểm tra/merge PDF) |
| **AI** | Google Gemini 2.5 Flash |

---

## 3. Kiến trúc hệ thống

### Cấu trúc Project

```
UTH-ConfMS/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/uth/confms/
│   │   ├── ai/                 # AI Service (Gemini integration)
│   │   ├── assignment/         # Module gán bài cho reviewer
│   │   ├── auth/               # Module xác thực (JWT + OAuth2)
│   │   ├── cameraready/        # Module camera-ready & proceedings
│   │   ├── common/             # Utilities, DTOs, Exceptions chung
│   │   ├── conference/         # Module quản lý hội nghị & track
│   │   ├── config/             # Cấu hình Security, Redis, CORS, etc.
│   │   ├── decision/           # Module quyết định Accept/Reject
│   │   ├── email/              # Module email queue & templates
│   │   ├── pc/                 # Module PC member & invitations
│   │   ├── reporting/          # Module báo cáo & thống kê
│   │   ├── review/             # Module review & scoring
│   │   ├── storage/            # Module storage (MinIO/Local)
│   │   └── submission/         # Module nộp bài & files
│   ├── src/main/resources/
│   │   ├── application.yaml    # Cấu hình chính
│   │   └── application-prod.yml # Cấu hình production
│   ├── .env                    # Environment variables
│   └── pom.xml
├── frontend/                   # React Frontend
│   ├── src/
│   │   ├── components/         # Reusable components
│   │   ├── contexts/           # React Context (Auth, etc.)
│   │   ├── i18n/               # Internationalization (vi/en)
│   │   ├── layouts/            # Layout components
│   │   ├── pages/              # Các trang theo role
│   │   │   ├── auth/           # Login, Register, OAuth
│   │   │   ├── author/         # Tác giả: submissions, camera-ready
│   │   │   ├── chair/          # Chair: management, decisions
│   │   │   ├── pc/             # PC: assignments, COI
│   │   │   └── reviewer/       # Reviewer: review papers
│   │   ├── routes/             # React Router config
│   │   └── services/           # API services (Axios)
│   └── package.json
├── SETUP.md                    # Hướng dẫn setup chi tiết
└── README.md                   # File này
```

---

## 4. Phân chia module

| Thành viên | Module phụ trách |
|------------|------------------|
| Ngô Hoàng Thức | Auth + User + SSO |
| Nguyễn Đăng Thịnh | Conference + CFP |
| Võ Trần Ngọc Anh | Submission + Upload PDF |
| Hoàng Hữu Nghĩa | PC Management + COI Detection |
| Nguyễn Viết Đạt | Assignment + Review Workflow |
| Nguyễn Nhật Khánh | Decision + Notification + Bulk Email |
| Đặng Ngọc Anh Đức | Camera-ready + Proceedings + Reporting |

---

## 5. Quy trình nghiệp vụ (Workflow)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CONFERENCE WORKFLOW                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. CREATE CONFERENCE                                               │
│     └── Chair tạo conference, tracks, deadlines                     │
│                    ↓                                                │
│  2. OPEN CFP (Call for Papers)                                      │
│     └── Công bố và mở nhận bài                                      │
│                    ↓                                                │
│  3. SUBMISSION PHASE                                                │
│     └── Tác giả nộp bài (PDF + metadata)                            │
│                    ↓                                                │
│  4. PC INVITATION                                                   │
│     └── Chair mời PC members → COI declaration                      │
│                    ↓                                                │
│  5. PAPER ASSIGNMENT                                                │
│     └── Gán bài cho reviewer (check COI)                            │
│                    ↓                                                │
│  6. REVIEW PHASE                                                    │
│     └── Reviewer đọc, chấm điểm, nhận xét                           │
│                    ↓                                                │
│  7. DECISION                                                        │
│     └── Chair quyết định Accept/Reject                              │
│     └── Gửi email thông báo kết quả                                 │
│                    ↓                                                │
│  8. CAMERA-READY PHASE                                              │
│     └── Tác giả upload bản cuối cùng                                │
│     └── Chair review và phê duyệt                                   │
│     └── Copyright confirmation                                      │
│                    ↓                                                │
│  9. PROCEEDINGS EXPORT                                              │
│     └── Xuất kỷ yếu (PDF/JSON/CSV)                                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. Quick Start

### Yêu cầu hệ thống

- Java 17+
- Maven 3.9+
- Node.js 18+ & npm
- PostgreSQL 15+
- Redis 7+
- MinIO (optional, for object storage)

### Cài đặt

Xem hướng dẫn cài đặt chi tiết tại tài liệu [SETUP.md](SETUP.md).


#### 7. Truy cập hệ thống

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

#### Tài khoản mặc định
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@uth.edu.vn | admin123 |

> ⚠️ **Lưu ý**: Đổi mật khẩu admin ngay sau khi setup!

---

## 8. API Endpoints

### Authentication
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/refresh` | Refresh token |
| GET | `/api/auth/me` | Lấy thông tin user |

### Conference
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/v1/conferences` | Danh sách hội nghị |
| POST | `/api/v1/conferences` | Tạo hội nghị mới |
| GET | `/api/v1/conferences/{id}` | Chi tiết hội nghị |

### Submission
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/v1/submissions` | Nộp bài mới |
| GET | `/api/v1/submissions/my` | Bài nộp của tôi |
| PUT | `/api/v1/submissions/{id}` | Cập nhật bài |

### Camera-Ready
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/v1/conferences/{id}/camera-ready` | Danh sách submissions |
| POST | `/api/v1/conferences/{id}/camera-ready/{paperId}/upload` | Upload camera-ready |
| POST | `/api/v1/conferences/{id}/camera-ready/{paperId}/review` | Review submission |

Xem đầy đủ tại: **http://localhost:8080/swagger-ui.html**

---

## 9. Cấu hình nâng cao

Xem hướng dẫn cấu hình chi tiết (Storage, AI, Rate Limiting) tại tài liệu [SETUP.md](SETUP.md#cấu-hình-nâng-cao).

---

## 10. Ghi chú quan trọng

- ✅ Hỗ trợ **double-blind review** đầy đủ
- ✅ AI chỉ gợi ý, **luôn yêu cầu xác nhận thủ công**
- ✅ **Audit log** với mọi hành động quan trọng
- ✅ Hỗ trợ **đa hội nghị** (multi-conference)
- ✅ **Email queue** với Redis để xử lý email bất đồng bộ
- ✅ **UTF-8 encoding** đầy đủ cho tiếng Việt
- ✅ **PDF validation** với Apache PDFBox

---

## 11. Tài liệu yêu cầu

- User Requirements
- SRS (Software Requirements Specification)
- Architecture Design
- Detail Design
- Implementation
- Test Plan
- Installation Guide
- User Manual
- Source code + Package deploy

---

## 12. License & Team

**Sinh viên Khoa CNTT – Trường Đại học Giao thông Vận tải TP.HCM (UTH)**  
Đồ án môn học Lập trình Java - Năm 2025

---

*Cập nhật lần cuối: 28/01/2026*
