# LifeRise Supabase Backend Setup Guide

This document describes how to configure the Supabase PostgreSQL backend, Row Level Security (RLS) policies, and storage bucket for the LifeRise Android application.

---

## 1. Create a Supabase Project

1. Go to [https://supabase.com](https://supabase.com) and create or open your organization.
2. Click **New Project** and choose a project name (e.g., `liferise-backend`).
3. Set a strong database password and select a region closest to your primary users.

---

## 2. Apply Database Migrations

1. In your Supabase Dashboard, navigate to the **SQL Editor** on the left menu.
2. Open the file:
   `backend/supabase/migrations/001_initial_schema.sql`
3. Copy the entire contents, paste it into the SQL Editor, and click **Run**.
4. Verify that the following tables are created:
   - `public.profiles`
   - `public.user_settings`
   - `public.follows`
   - `public.posts`
   - `public.post_likes`
   - `public.comments`
   - `public.reports`

---

## 3. Verify Row Level Security (RLS) Policies

All tables have RLS enabled to guarantee data privacy:
- **Private Diary Entries**: Kept strictly offline in local Room SQLite database on the user's device. Diary entries are **never** synced to public posts.
- **Posts Visibility**:
  - `PUBLIC`: Readable by any authenticated member.
  - `FOLLOWERS_ONLY`: Readable only by accepted followers via `public.follows`.
  - `PRIVATE`: Readable only by the author.
- **Storage Protection**: The `post-photos` storage bucket only allows uploads into paths matching the authenticated user's ID (`author_id/*`).

---

## 4. Configure Android Client

1. In Supabase Dashboard, go to **Project Settings** > **API**.
2. Copy:
   - **Project URL**
   - **Project API Keys -> `anon` / `public`** (NEVER use `service_role` in Android!)
3. Open `local.properties` in your Android project root (or copy from `local.properties.example`):
   ```properties
   SUPABASE_URL=https://<your-project-id>.supabase.co
   SUPABASE_ANON_KEY=<your-anon-key>
   ```
4. LifeRise automatically detects if credentials are present. If empty, the app runs in **Demo Mode**, displaying mock community feed items and enabling full local offline functionality.
