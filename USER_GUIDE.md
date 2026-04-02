# PCM Texas Action Tracker — User Guide

> **For DigitalChalk team members.** Login restricted to `@digitalchalk.com` Google accounts.

---

## Table of Contents

1. [Logging In](#1-logging-in)
2. [Dashboard — My Day](#2-dashboard--my-day)
3. [Task List View](#3-task-list-view)
4. [Creating a Task](#4-creating-a-task)
5. [AI Paste-to-Tasks (Meeting Notes)](#5-ai-paste-to-tasks-meeting-notes)
6. [Kanban Board View](#6-kanban-board-view)
7. [Task Detail & Comments](#7-task-detail--comments)
8. [Filtering & Sorting](#8-filtering--sorting)
9. [Bulk Actions](#9-bulk-actions)
10. [Recurring Tasks](#10-recurring-tasks)
11. [CSV Export](#11-csv-export)
12. [Email Notifications](#12-email-notifications)
13. [Roles: Owner vs. Member](#13-roles-owner-vs-member)
14. [Keyboard & Tips](#14-keyboard--tips)

---

## 1. Logging In

Open the app URL in your browser. You will see the login screen:

```
┌─────────────────────────────────────────────┐
│                                             │
│       PCM Texas Action Tracker             │
│   Team action item tracker for DigitalChalk │
│                                             │
│         ┌─────────────────────────┐         │
│         │  G  Sign in with Google │         │
│         └─────────────────────────┘         │
│                                             │
│   Login restricted to @digitalchalk.com    │
│                                             │
└─────────────────────────────────────────────┘
```

1. Click **Sign in with Google**
2. Select your `@digitalchalk.com` Google account
3. You are redirected to the **Dashboard**

> **Note:** Non-`@digitalchalk.com` accounts are automatically rejected.

---

## 2. Dashboard — My Day

The Dashboard is the first screen after login. It gives you a complete picture of your workload at a glance.

```
PCM Texas Action Tracker          [3 overdue ⚠]  [AB ▾]
──────────────────────────────────────────────────────────

My Day — Thursday, April 2, 2026

┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ My Open Tasks│ │   Overdue    │ │Waiting on    │ │ Assigned     │
│      7       │ │      3       │ │  Others      │ │   By Me      │
│              │ │  ⚠ text-red  │ │      2       │ │      5       │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘

MY OPEN TASKS
┌──────────────────────┬──────────┬──────────┬────────────┐
│ Task                 │ Due Date │ Priority │ Status     │
├──────────────────────┼──────────┼──────────┼────────────┤
│ Update onboarding doc│ Apr 5    │ HIGH     │ In Progress│
│ Review Q2 OKRs       │ Apr 7    │ MEDIUM   │ Not Started│
│ Fix login redirect   │ Apr 3    │ HIGH     │ Blocked    │
└──────────────────────┴──────────┴──────────┴────────────┘

OVERDUE  (rows highlighted in red)
┌──────────────────────┬──────────┬──────────┬────────────┐
│ Sync API docs        │ Mar 28   │ MEDIUM   │ In Progress│
│ Write release notes  │ Mar 30   │ HIGH     │ Not Started│
└──────────────────────┴──────────┴──────────┴────────────┘

RECENT ACTIVITY
  • Alice updated "Fix login redirect" → Blocked  (2h ago)
  • Bob completed "Deploy staging env"            (5h ago)
  • You were @mentioned in "Q2 Planning"          (1d ago)
```

### Summary Cards

| Card | What it shows |
|------|--------------|
| **My Open Tasks** | All tasks assigned to you that aren't Complete |
| **Overdue** | Tasks past their due date — count shown in red navbar badge |
| **Waiting On Others** | Tasks you assigned to others that are overdue or WAITING_ON |
| **Assigned By Me** | All tasks you delegated, with completion percentage |

---

## 3. Task List View

Click **Tasks** in the navbar to open the full task table.

```
Tasks                        [Paste Notes] [+ New Task] [≡ List] [⊞ Kanban]
──────────────────────────────────────────────────────────────────────────────
Filter: [Assignee ▾] [Status ▾] [Priority ▾] [Due From] [Due To] [Tag] [✕]

[ 2 selected ]  [Reassign] [Change Status ▾] [🗑 Delete]

☐  Task ↑↓          Assignee     Due Date    Priority    Status        Actions
──────────────────────────────────────────────────────────────────────────────
☑  Update onboarding  Alice B.    Apr 5 🔴   ● HIGH      [In Progress▾]  ✏ 🗑
☐  Review Q2 OKRs     Bob C.      Apr 7      ● MEDIUM    [Not Started▾]  ✏ 🗑
☑  Fix login redirect  You        Apr 3 🔴   ● HIGH      [Blocked    ▾]  ✏ 🗑
☐  Weekly standup ↺   Alice B.    —          ● LOW       [Not Started▾]  ✏ 🗑

                                            « 1 2 3 »  Showing 1–20 of 47
```

### Column descriptions

| Column | Notes |
|--------|-------|
| **☐ checkbox** | Select rows for bulk actions |
| **Task** | Click the title to open Task Detail. ↺ icon = recurring task |
| **Assignee** | Initials circle + full name |
| **Due Date** | Shows in **red** if overdue. Row background is red for overdue tasks |
| **Priority** | `● HIGH` (red badge) `● MEDIUM` (yellow) `● LOW` (grey) |
| **Status** | Inline dropdown — change status without opening the task |
| **Actions** | ✏ opens edit form, 🗑 deletes with confirmation |

---

## 4. Creating a Task

Click **+ New Task** to open the task creation form in a modal:

```
┌────────────────────── New Task ──────────────────────────┐
│                                                          │
│  Title *          [                                  ]   │
│  Description      [                                  ]   │
│                   [                                  ]   │
│  Assignee *       [Select team member          ▾    ]   │
│  Due Date         [        ]   Priority [MEDIUM   ▾]   │
│  Status           [NOT STARTED                    ▾]   │
│  Source           [e.g. "Zoom call 4/2/26"           ]   │
│  Source URL       [https://...                       ]   │
│  Project Tag      [e.g. "Q2-Launch"                  ]   │
│                                                          │
│  ☐ Repeat this task                                      │
│                                                          │
│               [Cancel]  [Save Task]                      │
└──────────────────────────────────────────────────────────┘
```

### Field reference

| Field | Required | Notes |
|-------|----------|-------|
| **Title** | ✅ | Short, action-oriented — e.g. "Update API docs" |
| **Assignee** | ✅ | Dropdown lists all `@digitalchalk.com` team members |
| **Due Date** | ✅ | Date picker |
| **Priority** | ✅ | HIGH / MEDIUM / LOW |
| **Status** | ✅ | Default: NOT_STARTED |
| **Source** | — | Where this came from: "Weekly Sync 4/2", "Slack #dev" |
| **Source URL** | — | Link to Zoom recording, Plaud transcript, etc. |
| **Project Tag** | — | Groups tasks: "Q2-Launch", "Onboarding", etc. |
| **Repeat** | — | Enable for recurring tasks (see [§10](#10-recurring-tasks)) |

---

## 5. AI Paste-to-Tasks (Meeting Notes)

This is the most powerful feature. Paste raw meeting notes and the app automatically extracts every action item using Claude AI.

### Step 1 — Open the modal

Click **Paste Notes** in the task list toolbar.

```
┌──────────────── Paste Meeting Notes ─────────────────────┐
│                                                          │
│  Paste your transcript, AI notetaker output, or any      │
│  meeting notes below. Supported: Plaud, Zoom, Google     │
│  Meet, Fireflies, attention.io, or plain text.           │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │ Action items from today's sync:                    │  │
│  │ - John needs to finish the API migration by Fri    │  │
│  │ - Sarah should send the Q2 report ASAP, high pri   │  │
│  │ - Bob to schedule kickoff meeting next week        │  │
│  │ https://zoom.us/rec/share/abc123                   │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│              [Cancel]  [⚡ Extract Action Items]          │
└──────────────────────────────────────────────────────────┘
```

### Step 2 — Review extracted tasks

After clicking **Extract Action Items** (takes ~3–5 seconds), a review table appears:

```
┌──────── Review Extracted Tasks (3) ──────────────────────┐
│ ◀ Back                                           [✕]     │
│                                                          │
│ Title             │Assignee     │Due    │Priority│Source │
│───────────────────┼─────────────┼───────┼────────┼───────│
│[API migration   ] │[John S.  ▾] │[Apr 4]│[HIGH ▾]│[Zoom ]│
│[Send Q2 report  ] │[Sarah M. ▾] │[ASAP ]│[HIGH ▾]│[Zoom ]│
│[Schedule kickoff] │[Bob C.   ▾] │[     ]│[MED  ▾]│[Zoom ]│
│                                                          │
│                           [+ Add Row]                    │
│                                                          │
│  Source URL auto-detected: https://zoom.us/rec/share/abc │
│                                                          │
│         [◀ Back]   [✅ Save 3 Tasks]                      │
└──────────────────────────────────────────────────────────┘
```

### What you can do in the review table

- **Edit any field inline** — click any cell to change title, assignee, due date, or priority
- **Change assignee** — dropdown matches names to your Google Workspace team roster
- **Add a row** — click **+ Add Row** for action items Claude missed
- **Remove a row** — click ✕ on any row to exclude it
- **Source URL** — automatically detected if a Zoom/Meet/Plaud URL is in the notes

### Step 3 — Save

Click **Save N Tasks**. All tasks are created at once and appear in the task list immediately.

> **Tip:** The AI works best with structured notes. The more clearly an action item names a person and action ("John will X by Y"), the better the extraction.

---

## 6. Kanban Board View

Click the **⊞ Kanban** toggle button in the task list toolbar to switch to board view.

```
NOT STARTED (12)   IN PROGRESS (5)   WAITING ON (3)   BLOCKED (2)   COMPLETE (8)
──────────────     ─────────────     ─────────────     ──────────    ────────────
┌────────────┐     ┌────────────┐    ┌────────────┐
│Update docs │     │API migrate │    │Q2 report   │
│  AB  Apr5  │     │  JS  Apr4  │    │  SM  ASAP  │
│  ● HIGH    │     │  ● HIGH    │    │  ● HIGH    │
└────────────┘     └────────────┘    └────────────┘  ┌────────────┐
┌────────────┐     ┌────────────┐                    │Login redir │
│Review OKRs │     │Deploy stag │                    │  You Apr3  │
│  BC  Apr7  │     │  BC  Apr5  │                    │  ● HIGH 🔴 │ ← red border
│  ● MEDIUM  │     │  ● MEDIUM  │                    └────────────┘
└────────────┘     └────────────┘
```

### Drag and drop

Drag any card to a different column to update its status instantly. The status change is saved to the server automatically.

- Cards with a **red border** are overdue
- Click a card title to open the full Task Detail view

---

## 7. Task Detail & Comments

Click any task title (in the list or kanban) to open the Task Detail page.

```
< Tasks / Update onboarding documentation

┌── Task ──────────────────────────────────────────────────────────────────┐
│  Update onboarding documentation         ● HIGH   [In Progress]  [✏ Edit]│
├──────────────────────────────┬───────────────────────────────────────────┤
│  Description                 │  💬 Comments (2)                           │
│  Update the new-hire doc to  │  ─────────────────────────────────────────│
│  reflect the April platform  │  Alice B. · 2 hours ago                   │
│  changes. Include SSO setup. │  I've updated sections 1–3. @Bob can you  │
│                              │  review the SSO part?                     │
│  ─────────────────────────── │                                           │
│  Assignee:  Alice B.         │  Bob C. · 1 hour ago                      │
│  Due Date:  Apr 5, 2026      │  On it, will check tonight.               │
│  Source:    Weekly Sync      │  ─────────────────────────────────────────│
│  Source URL: zoom.us/rec/... │                                           │
│  Project:   Onboarding       │  ┌────────────────────────────────────┐   │
│  Recurring: No               │  │ Add a comment... Use @ to mention  │   │
│                              │  └────────────────────────────────────┘   │
│  Assigned by: You            │                    [Add Comment]           │
└──────────────────────────────┴───────────────────────────────────────────┘
```

### @Mention teammates in comments

Type `@` in the comment box to trigger an autocomplete dropdown:

```
  ┌─────────────────────────┐
  │ Add a comment... @b     │
  └─────────────────────────┘
        ┌──────────────────┐
        │ 🅱 Bob Chen       │  ← click to insert
        │ 🅱 Brittany Wade  │
        └──────────────────┘
```

When you mention someone, they receive an **email notification** automatically.

### Activity log

All changes to a task are logged below the comments:
- Status changed
- Task reassigned
- Due date updated
- Comments added

---

## 8. Filtering & Sorting

The filter bar appears at the top of the Task List:

```
[Assignee ▾] [Status ▾] [Priority ▾] [Due From ──] [Due To ──] [Tag ──] [✕ Clear]
```

| Filter | Options |
|--------|---------|
| **Assignee** | Dropdown of all team members |
| **Status** | NOT_STARTED / IN_PROGRESS / WAITING_ON / BLOCKED / COMPLETE |
| **Priority** | HIGH / MEDIUM / LOW |
| **Due From / To** | Date range picker |
| **Project Tag** | Free-text search on the tag field |
| **Recurring only** | Checkbox to show only recurring tasks |

Click **✕ Clear** to reset all filters.

### Sorting

Click any column header to sort. Click again to reverse. The current sort direction is shown with ↑ or ↓:

```
  Task ↑    Assignee    Due Date ↓    Priority    Status
```

---

## 9. Bulk Actions

Select multiple tasks using the checkboxes, then use the bulk action toolbar that appears:

```
[ 3 selected ]   [Reassign]   [Change Status ▾]   [🗑 Delete]
```

| Action | Behavior |
|--------|----------|
| **Reassign** | Opens a team member picker; updates assignee on all selected tasks |
| **Change Status** | Dropdown to set all selected tasks to the same status |
| **Delete** | Confirmation dialog, then permanently deletes all selected tasks |

> **Tip:** Use **Select All** (top checkbox in the header row) to select every task on the current page, then filter first to target a specific subset.

---

## 10. Recurring Tasks

When creating or editing a task, toggle **Repeat this task**:

```
  ☑ Repeat this task
  Frequency: [WEEKLY  ▾]    (options: WEEKLY / MONTHLY)
```

### How recurring tasks work

1. A team member completes the task (status → **COMPLETE**)
2. Every night at 2 AM, the system checks completed recurring tasks
3. If 7 days have passed (WEEKLY) or 30 days (MONTHLY), the task is automatically **reset to NOT_STARTED**
4. The assignee receives a notification that the task is active again

Recurring tasks show the ↺ icon in the task list.

---

## 11. CSV Export

Click **Export CSV** in the task list toolbar. The export respects your current filters — so filter first, then export.

The downloaded file (`tasks.csv`) contains:

| Column | Notes |
|--------|-------|
| Title | |
| Description | |
| Assignee | Full name |
| Assigned By | Full name |
| Due Date | YYYY-MM-DD |
| Priority | HIGH / MEDIUM / LOW |
| Status | Full status value |
| Source | |
| Source URL | |
| Project Tag | |
| Created At | ISO timestamp |

---

## 12. Email Notifications

The system sends Gmail notifications automatically for these events:

| Event | Who receives it |
|-------|----------------|
| Task assigned to you | You (the assignee) |
| Task due tomorrow | Assignee |
| Task overdue (daily 8 AM) | Assignee + Owner |
| @mention in a comment | The mentioned teammate |
| Task marked COMPLETE | The person who created the task |

### Daily Digest (optional)

Each morning at 8 AM you receive a digest email listing:
- All your open and overdue tasks
- All overdue tasks you assigned to others

To disable: go to your **Profile** (click your avatar → Profile) and toggle off **Daily Digest**.

Each notification email contains:
- Task title, description, due date, priority
- Current status
- Who triggered the event
- A deep link to the task in the app

---

## 13. Roles: Owner vs. Member

| Capability | Owner | Member |
|-----------|-------|--------|
| See **all** tasks across the team | ✅ | ✗ |
| See tasks assigned to me | ✅ | ✅ |
| See tasks I assigned to others | ✅ | ✅ |
| Create / edit / delete tasks | ✅ | ✅ |
| Bulk actions | ✅ | ✅ |
| Export CSV | ✅ | ✅ |
| Overdue count badge (all tasks) | ✅ | Own tasks only |

The **Owner** account is configured by the administrator (`OWNER_EMAIL` in server config). All other `@digitalchalk.com` accounts are **Members**.

Your role is shown as a badge next to your name in the top-right corner of the navbar.

---

## 14. Keyboard & Tips

### Status values quick reference

| Status | Meaning |
|--------|---------|
| **NOT_STARTED** | Task created, not yet begun |
| **IN_PROGRESS** | Actively being worked on |
| **WAITING_ON** | Waiting for something external (another person, approval, info) |
| **BLOCKED** | Cannot proceed due to a hard blocker |
| **COMPLETE** | Done ✅ |

### Priority color reference

| Badge | Priority | Use when |
|-------|----------|---------|
| 🔴 red | **HIGH** | Urgent, deadline-sensitive, or blocking others |
| 🟡 yellow | **MEDIUM** | Important but not on fire |
| ⚫ grey | **LOW** | Nice-to-have, no hard deadline |

### Tips for the AI extraction

- Include the person's **first and last name** in your notes for best matching
- Dates like "by Friday", "next week", "EOD" are understood
- Urgency words like "ASAP", "urgent", "critical" are mapped to HIGH priority
- Paste **entire meeting transcripts** — Claude will ignore irrelevant content
- You can paste from Plaud, Fireflies, Otter.ai, Zoom AI Summary, Google Meet transcripts, or plain text

### Inline status update

You don't need to open a task to change its status. Use the **status dropdown directly in the task list row** — changes save instantly.

---

*PCM Texas Action Tracker — Built for the DigitalChalk team*
*Questions or issues? Contact your system administrator.*
