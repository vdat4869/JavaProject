# UTH-ConfMS Frontend - Phase FE-0-Foundation

Frontend foundation cho UTH Scientific Conference Paper Management System.

## Phase: FE-0-Foundation

**Goal**: Thiết lập khung frontend, chưa triển khai nghiệp vụ

## Tech Stack

- **React 19.2.3** - UI Framework
- **TypeScript** - Type safety
- **React Router v6** - Routing
- **Context API** - State management
- **Axios** - HTTP Client
- **React i18next** - Internationalization (vi, en)
- **CoreUI React** - UI Component Library
- **Vite** - Build Tool

## Cấu trúc Project

```
src/
├── context/
│   └── AuthContext.tsx      # Authentication context (JWT, role, login/logout)
├── services/
│   └── api.ts               # Axios instance với interceptors
├── layouts/
│   ├── AuthLayout.tsx       # Layout cho auth pages (login, verify)
│   └── AppLayout.tsx        # Layout cho app pages (sau đăng nhập)
├── routes/
│   ├── index.tsx             # Route configuration
│   └── RouteGuard.tsx       # Route guard theo role
├── pages/
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   ├── VerifyEmailPage.tsx
│   │   └── SSOCallbackPage.tsx
│   └── app/
│       └── DashboardPage.tsx
├── i18n/
│   ├── config.js
│   └── locales/
│       ├── messages_vi.json
│       └── messages_en.json
└── components/              # UI components từ template
```

## Routes

- `/login` - Login page (AuthLayout)
- `/auth/sso/callback` - SSO callback (AuthLayout)
- `/verify-email` - Email verification (AuthLayout)
- `/app/*` - Application routes (AppLayout)
  - `/app/dashboard` - Dashboard (placeholder)

## Core Features

### Authentication
- JWT token management
- Auto token refresh
- Role-based access control
- Login/Logout functions

### API Client
- Axios instance với base URL `/api`
- Request interceptor: Tự động thêm JWT token
- Response interceptor: Tự động refresh token khi 401
- Error handling và redirect

### Route Guards
- Protected routes yêu cầu authentication
- Role-based route protection
- Auto redirect nếu không có quyền

### Internationalization
- Hỗ trợ tiếng Việt (default) và English
- Translation files: `messages_vi.json`, `messages_en.json`

## Setup

### Install Dependencies

```bash
npm install
```

### Development

```bash
npm start
```

Application sẽ chạy tại `http://localhost:3000`

### Build

```bash
npm run build
```

## Notes

- **Không hardcode dữ liệu**: Tất cả data sẽ lấy từ API
- **Không mock API**: Chỉ xây dựng cấu trúc, chưa implement nghiệp vụ
- **TypeScript**: Sử dụng TypeScript cho type safety
- **Context API**: Sử dụng Context API thay vì Redux cho state management

## Next Phases

Các phase tiếp theo sẽ implement:
- Conference module
- Submission module
- Review module
- Decision module
- Reporting module
- PC Management module
