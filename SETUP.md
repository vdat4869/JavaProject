# Hướng dẫn Setup UTH-ConfMS

## Yêu cầu hệ thống

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15+
- Redis (optional, cho cache)

git clone https://github.com/vdat4869/JavaProject.git
cd JavaProject
```

### 2. Tạo database
createdb uth_confms
psql -U postgres -d uth_confms -f database/schema.sql
```

### 3. Cấu hình Backend

Tạo file `backend/.env` từ template:
```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/uth_confms
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key-min-32-chars

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Google OAuth2
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Frontend URL
FRONTEND_URL=http://localhost:3000

# Storage (local hoặc minio)
STORAGE_BACKEND=local
# Nếu dùng MinIO:
# STORAGE_BACKEND=minio
# STORAGE_S3_ENDPOINT=http://127.0.0.1:9000
# STORAGE_S3_BUCKET=java
# STORAGE_S3_ACCESS_KEY=minioadmin
# STORAGE_S3_SECRET_KEY=minioadmin123

# AI (optional)
AI_ENABLED=true
GEMINI_API_KEY=your-gemini-api-key
```

### 4. Chạy Backend
```bash
cd backend
mvn spring-boot:run
```

### 5. Chạy Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend sẽ chạy tại: http://localhost:3000
Backend API: http://localhost:8080

## Khởi tạo dữ liệu

Sau khi chạy backend lần đầu, hệ thống sẽ tự động tạo:
- Các roles: ADMIN, CHAIR, PC, REVIEWER, AUTHOR
- Các permissions cơ bản

## Tài khoản mặc định

Tạo tài khoản đầu tiên qua trang đăng ký tại `/register`. Tài khoản đầu tiên sẽ có role AUTHOR.

Để tạo ADMIN, chạy SQL:
```sql
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.email = 'admin@uth.edu.vn' AND r.name = 'ADMIN';
```

## Cấu trúc Project

```
.
├── backend/              # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/uth/confms/
│   │   │   │   ├── auth/          # Module xác thực
│   │   │   │   ├── conference/    # Module quản lý hội nghị
│   │   │   │   ├── submission/    # Module nộp bài
│   │   │   │   ├── pc/            # Module PC member
│   │   │   │   ├── review/        # Module review
│   │   │   │   ├── decision/      # Module quyết định
│   │   │   │   └── cameraready/   # Module camera-ready
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/            # React Frontend
│   ├── src/
│   │   ├── pages/       # Các trang
│   │   ├── components/  # Components
│   │   ├── contexts/    # Context API
│   │   └── ...
│   └── package.json
└── database/            # Database scripts
```

## API Endpoints chính

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - Đăng xuất

### Conference
- `GET /api/conferences/public` - Danh sách hội nghị công khai
- `GET /api/conferences/{id}` - Chi tiết hội nghị
- `POST /api/conferences` - Tạo hội nghị (CHAIR/ADMIN)

### Submission
- `GET /api/submissions/my` - Danh sách bài nộp của tôi (AUTHOR)
- `POST /api/submissions` - Tạo bài nộp mới (AUTHOR)
- `GET /api/submissions/{id}` - Chi tiết bài nộp

## Cấu hình nâng cao

### Storage Backend

Hệ thống hỗ trợ 2 storage backend:

**Local Storage** (mặc định):
```env
STORAGE_BACKEND=local
STORAGE_BASE_DIR=/data/uploads
```

**MinIO** (S3-compatible):
```env
STORAGE_BACKEND=minio
STORAGE_S3_ENDPOINT=http://127.0.0.1:9000
STORAGE_S3_BUCKET=java
STORAGE_S3_ACCESS_KEY=minioadmin
STORAGE_S3_SECRET_KEY=minioadmin123
```

### AI Configuration

```env
AI_ENABLED=true
AI_PROVIDER=gemini
GEMINI_API_KEY=your-api-key
GEMINI_MODEL=gemini-2.5-flash
AI_CONFIDENCE_THRESHOLD=0.5
```

### Rate Limiting

```env
RATE_LIMITING_ENABLED=true
RATE_LIMITING_USE_REDIS=true
RATE_LIMITING_AUTH_MAX=5
RATE_LIMITING_AUTH_WINDOW=60
RATE_LIMITING_API_MAX=100
RATE_LIMITING_API_WINDOW=60
```

## Troubleshooting

### Lỗi kết nối database
- Kiểm tra PostgreSQL đã chạy chưa
- Kiểm tra username/password trong application.yml
- Kiểm tra database đã được tạo chưa

### Lỗi CORS
- Kiểm tra cấu hình CORS trong SecurityConfig
- Đảm bảo frontend URL được thêm vào allowedOrigins

### Lỗi JWT
- Kiểm tra JWT_SECRET trong application.yml
- Secret key phải có ít nhất 32 ký tự

### Lỗi JDBC / Hibernate Type
- Nếu gặp lỗi `could not determine data type of parameter...` hoặc `JDBC exception`:
- Nguyên nhân: Class file cũ không đồng bộ với source code.
- Khắc phục: Chạy clean build:
  ```bash
  mvn clean spring-boot:run
  ```

## Development

### Backend
- Sử dụng Spring Boot DevTools để auto-reload
- Logs: kiểm tra console hoặc file log

### Frontend
- Hot reload tự động với Vite
- React DevTools để debug

## Production Deployment

1. Build backend:
```bash
cd backend
mvn clean package -DskipTests
```

2. Build frontend:
```bash
cd frontend
npm run build
```

3. Deploy manual setup:
   - Backend: Chạy JAR file với `java -jar target/confms-1.0.0.jar`
   - Frontend: Serve thư mục `dist` với web server (nginx, Apache, etc.)

## Liên hệ

Sinh viên Khoa CNTT - UTH
Đồ án môn học Lập trình Java

