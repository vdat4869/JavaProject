# UTH-ConfMS  
Hệ thống quản lý giấy tờ cho Hội nghị Nghiên cứu Khoa học Trường ĐH UTH  
Mô phỏng workflow kiểu EasyChair nhưng gọn, sạch, tối ưu cho vận hành nội bộ.

---

## 1. Mô tả dự án

Hệ thống hỗ trợ toàn bộ quy trình hội nghị khoa học:  
CFP → Nộp bài → Gán reviewer → Phản biện → Quyết định → Camera-ready → Xuất kỷ yếu.

#### Chức năng chính
- Tạo và cấu hình hội nghị, track, deadline, template email.  
- Tác giả nộp bài, sửa, rút, upload PDF, metadata.  
- PC quản lý COI, mời reviewer, theo dõi tiến độ.  
- Gán giấy, review, chấm điểm, thảo luận nội bộ, rebuttal.  
- Quyết định Accept/Reject, gửi email hàng loạt.  
- Camera-ready, kiểm tra file, xuất dữ liệu làm proceedings.  
- Dashboard, báo cáo thống kê, audit log.  
- Tùy chọn AI hỗ trợ tóm tắt và gợi ý keyword (có kiểm duyệt người).

---

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ & Mục đích |
|-----------|----------------------|
| **Backend** | Java 17, Spring Boot 3, Spring Web, Spring Security, Spring Data JPA |
| **Auth** | JWT (Access + Refresh), tùy chọn SSO (OAuth2 / OpenID Connect) |
| **Frontend** | ReactJS + Vite, TailwindCSS hoặc MUI, Axios |
| **Database** | PostgreSQL 15 |
| **Cache / Queue** | Redis – cache token, COI lookup, hàng đợi email |
| **Storage** | MinIO hoặc Local Storage – lưu PDF submission và camera-ready |
| **Email** | SMTP + Spring Mail |
| **ORM** | Hibernate / JPA |
| **DTO Mapping** | MapStruct |
| **Validation** | Spring Validation (Jakarta Validation) |
| **API Docs** | Swagger / OpenAPI 3 |
| **PDF Toolkit** | Apache PDFBox (kiểm tra PDF camera-ready) |

---
## 3. Kiến trúc hệ thống

### Cấu trúc Project

```
UTH-ConfMS/
├── backend/              # Spring Boot Backend
│   ├── src/main/java/com/uth/confms/
│   │   ├── auth/          # Module xác thực
│   │   ├── conference/    # Module quản lý hội nghị
│   │   ├── submission/    # Module nộp bài
│   │   ├── pc/            # Module PC member
│   │   ├── review/        # Module review
│   │   ├── decision/      # Module quyết định
│   │   └── cameraready/   # Module camera-ready
│   └── pom.xml
├── frontend/            # React Frontend
│   ├── src/
│   │   ├── pages/        # Các trang
│   │   ├── components/   # Components
│   │   └── contexts/      # Context API
│   └── package.json
├── database/            # Database scripts
│   ├── schema.sql       # Schema hoàn chỉnh
│   ├── init.sql         # Script đơn giản
│   └── README.md        # Hướng dẫn database
├── SETUP.md             # Hướng dẫn setup chi tiết
└── README.md            # File này
```

---

## 4. Chia module cho 7 người

| Thành viên | Module phụ trách |
|------------|------------------|
| Ngô Hoàng Thức | Auth + User + SSO |
| Nguyễn Đăng Thịnh | Conference + CFP |
| Võ Trần Ngọc Anh | Submission + Upload PDF |
| Hoàng Hữu Nghĩa | PC Management + COI Detection |
| Nguyễn Viết Đạt | Assignment + Review Workflow |
| Nguyễn Nhật Khánh | Decision + Notification + Bulk Email |
| Đặng Ngọc Anh Đức | Camera-ready + Proceedings + Reporting |

Tất cả phải đụng backend + frontend + database theo yêu cầu giảng viên.

---

## 5. Quy trình nghiệp vụ (Workflow)

- CFP mở → tác giả nộp bài  
- PC mời reviewer → check COI  
- Chair gán bài → reviewer review  
- Reviewer gửi điểm + nhận xét  
- Chair tổng hợp → Accept/Reject  
- Gửi email kết quả  
- Mở đợt camera-ready  
- Xuất chương trình, proceedings, báo cáo

---

## 6. Tài liệu yêu cầu (theo đề bài)

- User Requirements  
- SRS  
- Architecture Design  
- Detail Design  
- Implementation  
- Test Plan  
- Installation Guide  
- User Manual  
- Source code + Package deploy

---

## 7. Ghi chú mở rộng

- Hỗ trợ double-blind review đầy đủ.  
- AI luôn yêu cầu xác nhận thủ công, không auto-edit nội dung bài.  
- Audit log bắt buộc với mọi hành động quan trọng.  
- Hệ thống hỗ trợ đa hội nghị (multi-conference).

---

## 8. Quick Start

### Yêu cầu
- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15+

### Setup nhanh

1. **Tạo database:**
```bash
createdb uth_confms
psql -U postgres -d uth_confms -f database/schema.sql
```

2. **Cấu hình backend:**
Chỉnh sửa `backend/src/main/resources/application.yml` với thông tin database của bạn.

3. **Chạy backend:**
```bash
cd backend
mvn spring-boot:run
```

4. **Chạy frontend:**
```bash
cd frontend
npm install
npm run dev
```

5. **Đăng nhập:**
- URL: http://localhost:3000
- Email: admin@uth.edu.vn
- Password: admin123

**⚠️ Lưu ý**: Đổi mật khẩu admin ngay sau khi setup!

Xem chi tiết tại [SETUP.md](SETUP.md) và [database/README.md](database/README.md)

---

## 9. License & Team
Sinh viên Khoa CNTT – UTH  
Đồ án môn học Lập trình Java

