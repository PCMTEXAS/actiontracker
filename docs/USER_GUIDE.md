# PCM Texas Action Tracker — User Guide

> **Version:** 1.0 · **Audience:** DigitalChalk team members and owners

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Dashboard — My Day](#2-dashboard--my-day)
3. [Task List View](#3-task-list-view)
4. [Kanban Board View](#4-kanban-board-view)
5. [Creating a Task](#5-creating-a-task)
6. [AI Paste-to-Tasks](#6-ai-paste-to-tasks)
7. [Task Detail & Comments](#7-task-detail--comments)
8. [Bulk Actions](#8-bulk-actions)
9. [CSV Export](#9-csv-export)
10. [Recurring Tasks](#10-recurring-tasks)
11. [Roles: Owner vs Member](#11-roles-owner-vs-member)
12. [Notifications](#12-notifications)

---

## 1. Getting Started

### Login

Open the app URL in your browser. You will see the login screen:

```
┌─────────────────────────────────────────┐
│                                         │
│      PCM Texas Action Tracker           │
│   Team action item tracker for          │
│           DigitalChalk                  │
│                                         │
│   ┌─────────────────────────────────┐   │
│   │   G   Sign in with Google       │   │
│   └─────────────────────────────────┘   │
│                                         │
│   Login restricted to                   │
│   @digitalchalk.com accounts            │
│                                         │
└─────────────────────────────────────────┘
```

Click **Sign in with Google** — you will be redirected to Google's OAuth screen. Only `@digitalchalk.com` accounts are permitted.

---

## 2. Dashboard — My Day

After login you land on the **Dashboard**, your daily command center.

```
PCM Texas Action Tracker        [3 overdue]  👤 Sarah Johnson [OWNER] [Logout]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

My Day                                           Thursday, April 2, 2026

┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│  My Open Tasks   │ │    ⚠ Overdue      │ │ Waiting On Others│ │  Assigned By Me  │
│                  │ │                  │ │                  │ │                  │
│       12         │ │        3         │ │        5         │ │       18         │
│  (due this week) │ │   past due date  │ │  WAITING_ON / OD │ │  I delegated     │
└──────────────────┘ └──────────────────┘ └──────────────────┘ └──────────────────┘
  [primary border]     [danger border]       [warning border]      [info border]

MY OPEN TASKS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 Title                    Due Date     Priority      Status
─────────────────────────────────────────────────────────────
 Update API docs          Apr 4        [MEDIUM]      IN PROGRESS
 Deploy v2.1 to staging   Apr 3 ⚠      [HIGH]        NOT STARTED
 Review Mike's PR         Apr 5        [LOW]         NOT STARTED

OVERDUE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 🔴 Write sprint retro notes     Mar 30   [HIGH]   WAITING ON
 🔴 Send invoice to client       Mar 28   [HIGH]   IN PROGRESS

RECENT ACTIVITY (last 10)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 Mike Chen    marked "Fix login bug" COMPLETE         2 min ago
 Sarah J.     @mentioned you in "Deploy v2.1"         1 hr ago
 Tom Davis    created "Update billing API"            3 hr ago
```

### Summary Cards

| Card | Color | Meaning |
|------|-------|---------|
| My Open Tasks | Blue border | Tasks assigned **to you** that aren't COMPLETE |
| Overdue | Red border + text | Tasks past their due date (not COMPLETE) |
| Waiting On Others | Yellow border | Tasks you assigned that are in WAITING_ON status |
| Assigned By Me | Cyan border | All tasks you delegated to teammates |

> **Tip:** Click any task title to open the full Task Detail view.

---

## 3. Task List View

Navigate to **Tasks** in the navbar to see the full task table.

```
Tasks                    [📋 Paste Notes]  [+ New Task]   [≡ List] [⊞ Kanban]  [↓ Export CSV]

Filter:  [All Assignees ▼]  [All Statuses ▼]  [All Priorities ▼]  [Tag: ______]  [Clear]

┌──────────────────────────────────────────────────────────────────────────────────────────┐
│ ☐ │  Task ↕          │ Assignee ↕    │ Due Date ↕  │ Priority │ Status       │ Source  │⋯│
├──────────────────────────────────────────────────────────────────────────────────────────┤
│ ☐ │ Update API docs  │ 🔵 SJ Sarah J │ Apr 4, 2026 │ [MEDIUM] │ [IN PROG  ▼] │ Standup │✏│
│ ☐ │ Deploy v2.1 ⟳   │ 🔵 TC Tom C.  │ Apr 3 ⚠     │  [HIGH]  │ [NOT STR  ▼] │ Zoom    │✏│
│ ☐ │ Review Mike's PR │ 🟢 MK Mike K. │ Apr 5, 2026 │  [LOW]   │ [NOT STR  ▼] │ Slack   │✏│
├──────────────────────────────────────────────────────────────────────────────────────────┤
│ ⟳ = recurring task · ⚠ = overdue (red row) · ↕ = click to sort                         │
└──────────────────────────────────────────────────────────────────────────────────────────┘
                                               « 1 2 3 … »  (showing 1–20 of 47)
```

### Filter Bar

| Filter | How it works |
|--------|--------------|
| **Assignee** | Dropdown of all team members — select one to see only their tasks |
| **Status** | Filter by NOT_STARTED, IN_PROGRESS, WAITING_ON, BLOCKED, COMPLETE |
| **Priority** | Filter by HIGH, MEDIUM, or LOW |
| **Project Tag** | Type any tag (partial match) |
| **Recurring Only** | Checkbox — show only repeating tasks |
| **Clear** | Reset all filters |

### Status Badges

| Badge | Color | Meaning |
|-------|-------|---------|
| `NOT STARTED` | Grey | Task not yet started |
| `IN PROGRESS` | Blue | Actively being worked on |
| `WAITING ON` | Cyan | Blocked waiting for someone else |
| `BLOCKED` | Dark | Hard blockers (tech, dependency, etc.) |
| `COMPLETE` | Green | Done ✓ |

### Inline Status Update

Click the dropdown in the **Status** column of any row and select a new status — it saves immediately without a page reload.

### Sort

Click any column header (Task, Assignee, Due Date, Priority, Status) to sort ascending. Click again to sort descending. The active sort column shows `↑` or `↓`.

---

## 4. Kanban Board View

Click the **⊞ Kanban** toggle button to switch to the board view.

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ NOT STARTED  │  │ IN PROGRESS  │  │ WAITING ON   │  │   BLOCKED    │  │   COMPLETE   │
│  [grey]  3  │  │  [blue]  5  │  │  [cyan]  2  │  │  [dark]  1  │  │  [green] 12  │
├──────────────┤  ├──────────────┤  ├──────────────┤  ├──────────────┤  ├──────────────┤
│ 🔲           │  │ 🔲           │  │ 🔲           │  │ 🔲           │  │ ✅           │
│ Update docs  │  │ Deploy v2.1  │  │ Client call  │  │ DB migration │  │ Fix login    │
│ 👤 SJ        │  │ 👤 TC   ⚠   │  │ 👤 MK        │  │ 👤 TD        │  │ 👤 MK        │
│ Apr 4        │  │ Apr 3 [HIGH] │  │ Apr 8        │  │ TBD  [HIGH]  │  │ Apr 1 [LOW]  │
│─────────────│  │─────────────│  │              │  │              │  │              │
│ 🔲           │  │ 🔲           │  │              │  │              │  │ ✅           │
│ Review PR    │  │ Write tests  │  │              │  │              │  │ Sprint plan  │
│ 👤 MK        │  │ 👤 SJ        │  │              │  │              │  │ 👤 SJ        │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
     ↑ Drag cards between columns to change status
```

### Drag and Drop

- **Drag** any task card and **drop it into another column** to instantly update its status.
- Overdue cards have a **red border** `border-danger`.
- Click a card title to open the full Task Detail.

---

## 5. Creating a Task

Click **+ New Task** in the task list toolbar. A modal form opens:

```
┌──────────────────────────────────────────────────────────┐
│  New Task                                            [✕]  │
├──────────────────────────────────────────────────────────┤
│  Title *          [___________________________________]  │
│                                                          │
│  Description      [___________________________________]  │
│                   [___________________________________]  │
│                                                          │
│  Assignee *       [Select team member           ▼]      │
│                                                          │
│  Due Date         [Apr 09, 2026     ]                   │
│                                                          │
│  Priority         [MEDIUM ▼]   Status  [NOT STARTED ▼]  │
│                                                          │
│  Source           [e.g. Weekly Sync - Zoom ___________]  │
│  Source URL       [https://zoom.us/rec/... ___________]  │
│  Project Tag      [e.g. Q2-Mobile __________________]   │
│                                                          │
│  Repeat this task [ ] (toggle)                          │
│  ─── if toggled on ────────────────────────────────     │
│  Frequency        [WEEKLY ▼]                            │
│                                                          │
├──────────────────────────────────────────────────────────┤
│                           [Cancel]  [Save Task]          │
└──────────────────────────────────────────────────────────┘
```

### Required Fields

- **Title** — Brief, actionable description
- **Assignee** — Select from your Google Workspace team roster

### Optional Fields

| Field | Notes |
|-------|-------|
| Due Date | ISO date picker |
| Priority | HIGH / MEDIUM (default) / LOW |
| Status | Defaults to NOT STARTED |
| Source | E.g. "Weekly Sync – Zoom" |
| Source URL | Link to Zoom recording, Plaud transcript, Fireflies, etc. |
| Project Tag | Free-text label for filtering (e.g. "Q2-Mobile") |
| Recurring | Toggle on to set WEEKLY or MONTHLY auto-reset |

---

## 6. AI Paste-to-Tasks

This is the killer feature. Paste raw meeting notes and let Claude extract action items automatically.

### Step 1 — Paste your notes

Click **📋 Paste Notes** button. A large modal opens:

```
┌────────────────────────────────────────────────────────────────┐
│  Paste Meeting Notes                                      [✕]  │
├────────────────────────────────────────────────────────────────┤
│  Paste your meeting transcript, Zoom summary, Plaud notes,     │
│  Fireflies AI notes, or any text with action items below.      │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Weekly sync Apr 2 — attendees: Sarah, Mike, Tom         │  │
│  │                                                          │  │
│  │ - Mike to fix the login bug before Thursday             │  │
│  │ - Sarah will send the Q2 report to the board by Apr 10  │  │
│  │ - Tom needs to update the billing API — HIGH priority   │  │
│  │ - Follow up: Sarah to schedule retro next week          │  │
│  │                                                          │  │
│  │ Recording: https://zoom.us/rec/abcd1234                 │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                │
├────────────────────────────────────────────────────────────────┤
│                    [Cancel]  [⚡ Extract Action Items ●●●]     │
└────────────────────────────────────────────────────────────────┘
```

Click **Extract Action Items** — the app sends your notes to Claude AI and waits ~3–5 seconds.

### Step 2 — Review and edit

Claude returns a pre-filled table of extracted tasks. Review each row, edit anything that needs adjusting, then save:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  Review Extracted Tasks (4)                                              ← Back  [✕]  │
├────────────────────────────────────────────────────────────────────────────────────────┤
│  Title              │ Assignee              │ Due Date   │ Priority │ Source      │ ✕  │
│─────────────────────┼───────────────────────┼────────────┼──────────┼─────────────┼────│
│ [Fix login bug    ] │ [Mike Chen       ▼]   │ [Apr 3   ] │ [MED ▼]  │ [Wkly Sync] │ ✕  │
│ [Send Q2 report   ] │ [Sarah Johnson   ▼]   │ [Apr 10  ] │ [MED ▼]  │ [Wkly Sync] │ ✕  │
│ [Update billing   ] │ [Tom Davis       ▼]   │ [        ] │ [HIGH▼]  │ [Wkly Sync] │ ✕  │
│ [Schedule retro   ] │ [Sarah Johnson   ▼]   │ [        ] │ [LOW ▼]  │ [Wkly Sync] │ ✕  │
│─────────────────────┴───────────────────────┴────────────┴──────────┴─────────────┴────│
│ [+ Add Row]                                                                            │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                              [Back]  [✅ Save 4 Tasks]                 │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

- **Edit any cell inline** before saving
- **Assignee** auto-matched from your team roster using fuzzy name matching
- **Source URL** auto-detected if a Zoom/Meet/Plaud link was in the notes
- **Remove** any row you don't need
- **Add Row** to add a manually typed task

Click **Save X Tasks** — all tasks are bulk-created and assigned immediately.

---

## 7. Task Detail & Comments

Click any task title (in list view, kanban, or dashboard) to open the full detail page.

```
← Tasks › Fix login bug

┌─────────────────────────────────────────────────────────────────────────┐
│  Fix login bug                          [MEDIUM]  [IN PROGRESS]  [Edit] │
├─────────────────────────┬───────────────────────────────────────────────┤
│  DETAILS                │  COMMENTS (3)                                 │
│                         │                                               │
│  Description:           │  ┌──────────────────────────────────────────┐│
│  Login fails after      │  │ 👤 Sarah J. · 2 hours ago                ││
│  OAuth redirect when    │  │ Mike, can you check the redirect URI     ││
│  using Safari.          │  │ config in Google Cloud Console?          ││
│                         │  └──────────────────────────────────────────┘│
│  Assignee:  Mike Chen   │  ┌──────────────────────────────────────────┐│
│  Assigned by: Sarah J.  │  │ 👤 Mike C. · 1 hour ago                  ││
│  Due Date:  Apr 3, 2026 │  │ On it. @Tom can you check the proxy?    ││
│  Source:    Weekly Sync │  └──────────────────────────────────────────┘│
│  Source URL: zoom.us/.. │                                               │
│  Project:   Q2-Mobile   │  ┌──────────────────────────────────────────┐│
│  Recurring: No          │  │ Add a comment...                         ││
│                         │  │ Use @ to mention teammates               ││
│ ─ ACTIVITY LOG ──────── │  └──────────────────────────────────────────┘│
│ Status → IN PROGRESS    │            [Add Comment]                      │
│   Mike C · 1 hour ago   │                                               │
│ Task created            │                                               │
│   Sarah J · 3 hours ago │                                               │
└─────────────────────────┴───────────────────────────────────────────────┘
```

### @Mention in Comments

Type `@` followed by a name in the comment box. A dropdown appears:

```
  Add a comment...
  @mi|
  ┌───────────────────┐
  │ 👤 Mike Chen      │  ← click to insert @MikeChen
  │ 👤 Miranda Lopez  │
  └───────────────────┘
```

When you @mention a teammate, they receive an **email notification** immediately.

### Activity Log

Every status change, reassignment, and due-date update is automatically recorded in the activity log below the details, in chronological order.

---

## 8. Bulk Actions

When you check one or more rows in the task list, a bulk action toolbar appears at the top:

```
┌──────────────────────────────────────────────────────────────────┐
│  ✓ 3 tasks selected                                              │
│  [🗑 Delete]   [Status ▼ Change to...]   [👤 Reassign to...]    │
└──────────────────────────────────────────────────────────────────┘
  [Change to]
  ┌────────────────────┐
  │ NOT STARTED        │
  │ IN PROGRESS        │
  │ WAITING ON         │
  │ BLOCKED            │
  │ COMPLETE           │
  └────────────────────┘
```

| Action | What it does |
|--------|-------------|
| **Delete** | Prompts confirmation, then permanently deletes all selected tasks |
| **Change Status** | Sets all selected tasks to the chosen status in one API call |

Select all rows with the **checkbox in the header row**.

---

## 9. CSV Export

Click **↓ Export CSV** in the task list toolbar. The current **filtered view** is exported — whatever filters are active will be reflected in the CSV.

Columns exported:

| Column |
|--------|
| Title |
| Description |
| Assignee |
| Assigned By |
| Due Date |
| Priority |
| Status |
| Source |
| Project Tag |
| Created At |

The file downloads as `tasks.csv` immediately.

---

## 10. Recurring Tasks

Some tasks need to repeat on a regular cadence — like weekly standup prep or monthly billing review.

### Creating a Recurring Task

In the task form, toggle **"Repeat this task"** ON, then select **WEEKLY** or **MONTHLY**.

### How It Works

A nightly automated job (runs at 2:00 AM) scans for recurring tasks that have been marked **COMPLETE**:

- **WEEKLY tasks** completed ≥ 7 days ago → cloned with status reset to `NOT STARTED`
- **MONTHLY tasks** completed ≥ 30 days ago → cloned with status reset to `NOT STARTED`

The new task keeps all original fields (title, description, assignee, priority) but gets a fresh `createdAt` timestamp.

Recurring tasks display a **⟳** repeat icon in the task list.

---

## 11. Roles: Owner vs Member

| Feature | Owner | Member |
|---------|-------|--------|
| See all tasks in the org | ✅ | ❌ |
| See own tasks (assigned to me) | ✅ | ✅ |
| See tasks I assigned to others | ✅ | ✅ |
| Create tasks for anyone | ✅ | ✅ |
| Delete any task | ✅ | Own only |
| View full dashboard with all team metrics | ✅ | Own metrics |
| Overdue badge count in navbar | All team tasks | Own tasks only |

The **Owner role** is assigned to the email configured in `OWNER_EMAIL` environment variable. All other `@digitalchalk.com` users get the **Member** role automatically on first login.

---

## 12. Notifications

The app sends Gmail notifications automatically for these events:

### Instant Notifications

| Event | Who Gets It |
|-------|------------|
| Task assigned to you | Assignee |
| You were @mentioned in a comment | Mentioned user |
| A task you created is marked COMPLETE | Task creator |

### Scheduled Notifications

| Event | Time | Who Gets It |
|-------|------|------------|
| Task due tomorrow | 8:00 AM | Assignee |
| Task is overdue | 8:00 AM daily | Assignee + Owner |
| Daily digest (opt-in) | 8:00 AM daily | Any user with digest enabled |

### Daily Digest

The digest email includes:
- All your open tasks sorted by due date
- All overdue tasks you assigned to others

Toggle the daily digest **on or off** in your profile settings (click your avatar in the navbar → Profile).

### Email Format

Each notification email includes:
- Task title and description
- Due date and priority
- Current status
- Deep link back to the task in the app
- Name of who triggered the event (e.g., "Sarah Johnson assigned you this task")

---

## Quick Reference — Keyboard & UI Shortcuts

| Action | How |
|--------|-----|
| Open Paste Notes modal | Click **📋 Paste Notes** button |
| Create new task | Click **+ New Task** button |
| Inline status change | Click the status dropdown in any table row |
| Sort by column | Click any column header |
| Select all tasks | Check header checkbox |
| Clear all filters | Click **Clear** button in filter bar |
| Toggle list/kanban | List / Kanban buttons (top right of task list) |
| Open task detail | Click task title |

---

## Supported Note Formats for AI Extraction

The **Paste Meeting Notes** feature works with any of these:

- **Zoom** — AI Summary, transcript, or manual notes
- **Google Meet** — Auto-generated meeting notes
- **Plaud** — AI transcript output
- **Fireflies.ai** — Meeting notes export
- **Attention.io** — CRM call notes
- **Slack threads** — Copy-paste any message thread
- **Email threads** — Copy-paste action items from emails
- **Manual notes** — Any free-form text mentioning who needs to do what

> **Tip:** The more clearly the notes state *who* is doing *what* by *when*, the better Claude's extraction will be.

---

*PCM Texas Action Tracker · Built for DigitalChalk · Powered by Claude AI*
