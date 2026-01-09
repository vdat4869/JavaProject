# Hướng dẫn Setup UTH-ConfMS

## Yêu cầu hệ thống

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 15+
- Redis (optional, cho cache)

## Setup Backend

### 1. Cấu hình Database

```bash
# Tạo database
createdb uth_confms

# Hoặc sử dụng SQL script
psql -U postgres -f database/init.sql
```

### 2. Cấu hình application.yml

Chỉnh sửa `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/uth_confms
    username: your_username
    password: your_password
```

### 3. Chạy Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend sẽ chạy tại: http://localhost:8080

API Documentation: http://localhost:8080/swagger-ui.html

## Setup Frontend

### 1. Cài đặt dependencies

```bash
cd frontend
npm install
```

### 2. Chạy Frontend

```bash
npm run dev
```

Frontend sẽ chạy tại: http://localhost:3000

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

