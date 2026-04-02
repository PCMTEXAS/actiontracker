# Deploying the Frontend to Vercel

## Prerequisites

- GitHub repo pushed (already done on `claude/action-item-extractor-jWD6w`)
- Vercel account at [vercel.com](https://vercel.com)
- Backend already deployed and accessible via HTTPS (Railway / Cloud Run)

---

## Step 1 — Import the Repo into Vercel

1. Go to [vercel.com/new](https://vercel.com/new)
2. Click **Import Git Repository**
3. Connect your GitHub account if not already connected
4. Find **pcmtexas/actiontracker** and click **Import**

---

## Step 2 — Configure the Project

On the "Configure Project" screen, set:

| Setting | Value |
|---------|-------|
| **Root Directory** | `frontend` |
| **Framework Preset** | `Other` (or leave auto-detect) |
| **Build Command** | `npm run build:prod` |
| **Output Directory** | `dist/pcm-texas-action-tracker/browser` |
| **Install Command** | `npm install` |

> The `vercel.json` in `frontend/` handles SPA rewrites automatically — all routes return `index.html`.

---

## Step 3 — Set Environment Variables

In the Vercel dashboard, go to **Settings → Environment Variables** and add:

| Variable | Example Value | Notes |
|----------|--------------|-------|
| `VITE_API_URL` | `https://api.yourapp.com` | Your backend URL (Railway/Cloud Run) |

> **Note:** The Angular app proxies API calls via a relative `/api/` path in production. Configure your backend's CORS `allowedOrigins` to include your Vercel deployment URL.

---

## Step 4 — Deploy

Click **Deploy**. Vercel will:

1. Install dependencies (`npm install`)
2. Build the app (`ng build --configuration production`)
3. Deploy to a global CDN
4. Assign a URL like `https://pcm-texas-action-tracker.vercel.app`

Subsequent pushes to the `main` branch (or whichever branch you configure) will trigger automatic re-deployments.

---

## Step 5 — Update Backend CORS

Once you have your Vercel URL, update the backend's `FRONTEND_URL` environment variable:

```
FRONTEND_URL=https://pcm-texas-action-tracker.vercel.app
```

And update the Google OAuth **Authorized JavaScript origins** and **Authorized redirect URIs** in [Google Cloud Console](https://console.cloud.google.com/apis/credentials):

```
Authorized JavaScript origins:
  https://pcm-texas-action-tracker.vercel.app

Authorized redirect URIs:
  https://your-backend.railway.app/login/oauth2/code/google
```

---

## Step 6 — Update Google OAuth Redirect URI

Since OAuth redirects go through the **backend** (not the frontend), make sure the backend `GOOGLE_REDIRECT_URI` / Spring OAuth2 redirect is set correctly. The backend's `application.yml` reads:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            redirect-uri: "{baseUrl}/login/oauth2/code/google"
```

This resolves automatically to your backend's deployed URL.

---

## Vercel Preview Deployments

Every pull request automatically gets a preview deployment URL (e.g., `https://actiontracker-git-feature-branch.vercel.app`). This is useful for testing before merging to main.

---

## Troubleshooting

### Blank page after deploy
- Check **Output Directory** is set to `dist/pcm-texas-action-tracker/browser` (not `dist/pcm-texas-action-tracker`)
- Confirm `vercel.json` rewrites are present

### 404 on page refresh
- Confirm the rewrite rule in `vercel.json` is routing all paths to `/index.html`

### API calls fail (CORS error)
- Update `FRONTEND_URL` on the backend to match your exact Vercel URL (no trailing slash)
- Redeploy the backend

### Google OAuth fails
- Update Authorized redirect URIs in Google Cloud Console to include your new backend URL
- Ensure the backend `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` match the credentials in Google Cloud Console
