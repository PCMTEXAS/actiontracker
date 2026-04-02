# PCM Texas Action Tracker

Full-stack action item tracker for the DigitalChalk team — built with Spring Boot 3 (backend) and Angular 19 (frontend).

## Features

- **AI Paste-to-Tasks** — paste meeting notes, Claude extracts action items automatically
- **Task CRUD** with inline status updates, bulk actions, and CSV export
- **Kanban view** with drag-and-drop status management
- **Task comments** with @mention support
- **Dashboard / My Day** — overdue tasks, waiting-on-others, recent activity
- **Recurring tasks** — weekly or monthly auto-reset via scheduled job
- **Gmail notifications** — assignment, due-date reminders, @mentions, daily digest
- **Google OAuth** — restricted to `@digitalchalk.com` domain
- **Role-based access** — Owner sees all tasks; Members see their own

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Angular CLI | 19+ |
| PostgreSQL | 15+ (or Supabase/Railway) |

---

## Local Development Setup

### 1. Clone the repo

```bash
git clone https://github.com/pcmtexas/actiontracker.git
cd actiontracker
```

### 2. Configure environment

```bash
cp .env.example .env
# Edit .env with your values
```

### 3. Set up PostgreSQL

Create a database named `actiontracker`:

```sql
CREATE DATABASE actiontracker;
```

The schema is applied automatically on startup via `schema.sql` (`spring.sql.init.mode=always`).

### 4. Google Cloud Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → Enable **Google People API**, **Gmail API**, **Admin SDK API**
3. Create **OAuth 2.0 credentials** (Web Application type)
   - Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Secret into `.env`

### 5. Start the backend

```bash
cd backend
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### 6. Start the frontend

```bash
cd frontend
npm install
ng serve
# App available at http://localhost:4200
```

---

## Project Structure

```
actiontracker/
├── backend/                    # Spring Boot 3 application
│   ├── src/main/java/com/pcmtexas/actiontracker/
│   │   ├── config/             # Security, CORS, OAuth config
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Request/response DTOs
│   │   ├── entity/             # JPA entities
│   │   ├── enums/              # Priority, Status, Recurrence enums
│   │   ├── repository/         # Spring Data JPA repositories
│   │   └── service/            # Business logic services
│   └── src/main/resources/
│       ├── application.yml     # App config (reads from env vars)
│       └── schema.sql          # PostgreSQL DDL
├── frontend/                   # Angular 19 application
│   └── src/app/
│       ├── components/         # Standalone Angular components
│       ├── guards/             # Functional route guards
│       ├── models/             # TypeScript interfaces
│       └── services/           # HTTP services
├── .env.example                # Environment variable template
└── README.md
```

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | List tasks (filter via query params) |
| POST | `/api/tasks` | Create task |
| PATCH | `/api/tasks/:id` | Update task (partial) |
| DELETE | `/api/tasks/:id` | Delete task |
| POST | `/api/tasks/extract` | AI extract tasks from notes |
| POST | `/api/tasks/bulk` | Bulk create tasks |
| GET | `/api/tasks/export` | CSV export |
| GET | `/api/tasks/dashboard` | Dashboard data |
| GET | `/api/tasks/:id/comments` | List comments |
| POST | `/api/tasks/:id/comments` | Add comment |
| GET | `/api/users` | List team members |
| GET | `/api/users/me` | Current user info |

---

## Deployment

### Frontend — Vercel

1. Push to GitHub
2. Import repo at [vercel.com](https://vercel.com)
3. Set root directory: `frontend`
4. Build command: `npm run build:prod`
5. Output directory: `dist/pcm-texas-action-tracker/browser`
6. Add environment variable: `VITE_API_URL=https://your-backend-url`

The `vercel.json` handles SPA routing (all paths → `index.html`).

### Backend — Google Cloud Run

```bash
cd backend
docker build -t gcr.io/YOUR_PROJECT/actiontracker-api .
docker push gcr.io/YOUR_PROJECT/actiontracker-api
gcloud run deploy actiontracker-api \
  --image gcr.io/YOUR_PROJECT/actiontracker-api \
  --platform managed \
  --region us-central1 \
  --set-env-vars DATABASE_URL=...,GOOGLE_CLIENT_ID=...
```

### Backend — Railway

1. Connect GitHub repo at [railway.app](https://railway.app)
2. Set root directory to `backend`
3. Add PostgreSQL service — Railway auto-sets `DATABASE_URL`
4. Add all other env vars from `.env.example`

---

## Build Phases

| Phase | Status | Scope |
|-------|--------|-------|
| 1 | ✅ | Scaffold, Google OAuth, PostgreSQL, Task CRUD, Angular shell, task list |
| 2 | 🔲 | AI Paste-to-Tasks (Claude API) |
| 3 | 🔲 | Dashboard, Kanban view |
| 4 | 🔲 | Comments, @mention, activity log |
| 5 | 🔲 | Gmail notifications, daily digest |
| 6 | 🔲 | Recurring tasks, CSV export, bulk actions |
| 7 | 🔲 | Polish: mobile, empty states, skeletons, error toasts |
