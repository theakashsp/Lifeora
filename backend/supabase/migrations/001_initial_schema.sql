-- ============================================================================
-- LifeRise Supabase PostgreSQL Schema & Security Policies
-- Migration: 001_initial_schema.sql
-- ============================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ----------------------------------------------------------------------------
-- 1. PROFILES TABLE
-- Linked to auth.users (Supabase Auth). Auto-created on user sign-up.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    bio TEXT DEFAULT '',
    interests TEXT[] DEFAULT '{}',
    is_private BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT username_length_check CHECK (char_length(username) >= 3 AND char_length(username) <= 30)
);

CREATE INDEX IF NOT EXISTS idx_profiles_username ON public.profiles(username);

-- ----------------------------------------------------------------------------
-- 2. USER SETTINGS TABLE (Cloud-synced preferences, distinct from local diary)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_settings (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    theme_preference TEXT DEFAULT 'SYSTEM' CHECK (theme_preference IN ('LIGHT', 'DARK', 'SYSTEM')),
    preferred_language TEXT DEFAULT 'en' CHECK (preferred_language IN ('en', 'kn')),
    ai_processing_consented BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ----------------------------------------------------------------------------
-- 3. FOLLOWS / CONNECTIONS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.follows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'ACCEPTED' CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT unique_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT cannot_follow_self CHECK (follower_id <> following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower ON public.follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON public.follows(following_id);

-- ----------------------------------------------------------------------------
-- 4. POSTS TABLE (Social status updates - NEVER DIARY ENTRIES)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    caption TEXT DEFAULT '',
    image_url TEXT,
    image_storage_path TEXT,
    hashtags TEXT[] DEFAULT '{}',
    visibility TEXT NOT NULL DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC', 'FOLLOWERS_ONLY', 'PRIVATE')),
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT content_length_check CHECK (char_length(content) > 0 AND char_length(content) <= 2000)
);

CREATE INDEX IF NOT EXISTS idx_posts_author ON public.posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON public.posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_visibility ON public.posts(visibility);

-- ----------------------------------------------------------------------------
-- 5. POST LIKES TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.post_likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT unique_post_user_like UNIQUE (post_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_post_likes_post ON public.post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_user ON public.post_likes(user_id);

-- ----------------------------------------------------------------------------
-- 6. COMMENTS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT comment_length_check CHECK (char_length(content) > 0 AND char_length(content) <= 500)
);

CREATE INDEX IF NOT EXISTS idx_comments_post ON public.comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_author ON public.comments(author_id);

-- ----------------------------------------------------------------------------
-- 7. REPORTS TABLE (Community safety, moderation & blocking)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    reported_user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    reported_post_id UUID REFERENCES public.posts(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED')),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ----------------------------------------------------------------------------
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ----------------------------------------------------------------------------

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.post_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;

-- 1. Profiles Policies
-- Any authenticated user can read public profiles
CREATE POLICY "Public profiles are readable by authenticated users"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (TRUE);

-- Users can only insert their own profile
CREATE POLICY "Users can create their own profile"
    ON public.profiles FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = id);

-- Users can only update their own profile
CREATE POLICY "Users can update their own profile"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- 2. User Settings Policies
CREATE POLICY "Users can view own settings"
    ON public.user_settings FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can manage own settings"
    ON public.user_settings FOR ALL
    TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- 3. Follows Policies
CREATE POLICY "Users can see follows they are part of"
    ON public.follows FOR SELECT
    TO authenticated
    USING (auth.uid() = follower_id OR auth.uid() = following_id);

CREATE POLICY "Users can follow others"
    ON public.follows FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = follower_id);

CREATE POLICY "Users can unfollow or modify follow status"
    ON public.follows FOR DELETE
    TO authenticated
    USING (auth.uid() = follower_id);

-- 4. Posts Policies (Enforcing Visibility Rules)
-- Authors can always see their own posts
-- Public posts can be viewed by anyone authenticated
-- Followers-only posts can only be viewed if user is accepted follower
CREATE POLICY "Posts visibility policy"
    ON public.posts FOR SELECT
    TO authenticated
    USING (
        author_id = auth.uid()
        OR visibility = 'PUBLIC'
        OR (
            visibility = 'FOLLOWERS_ONLY'
            AND EXISTS (
                SELECT 1 FROM public.follows
                WHERE follower_id = auth.uid()
                  AND following_id = posts.author_id
                  AND status = 'ACCEPTED'
            )
        )
    );

CREATE POLICY "Users can insert their own posts"
    ON public.posts FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = author_id);

CREATE POLICY "Users can update their own posts"
    ON public.posts FOR UPDATE
    TO authenticated
    USING (auth.uid() = author_id)
    WITH CHECK (auth.uid() = author_id);

CREATE POLICY "Users can delete their own posts"
    ON public.posts FOR DELETE
    TO authenticated
    USING (auth.uid() = author_id);

-- 5. Post Likes Policies
CREATE POLICY "Likes are readable if post is visible"
    ON public.post_likes FOR SELECT
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.posts
            WHERE posts.id = post_likes.post_id
        )
    );

CREATE POLICY "Users can like posts"
    ON public.post_likes FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can remove their own likes"
    ON public.post_likes FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- 6. Comments Policies
CREATE POLICY "Comments are readable if post is visible"
    ON public.comments FOR SELECT
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.posts
            WHERE posts.id = comments.post_id
        )
    );

CREATE POLICY "Users can post comments on visible posts"
    ON public.comments FOR INSERT
    TO authenticated
    WITH CHECK (
        auth.uid() = author_id
        AND EXISTS (
            SELECT 1 FROM public.posts
            WHERE posts.id = comments.post_id
        )
    );

CREATE POLICY "Users can delete their own comments"
    ON public.comments FOR DELETE
    TO authenticated
    USING (auth.uid() = author_id);

-- 7. Reports Policies
CREATE POLICY "Users can submit reports"
    ON public.reports FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = reporter_id);

-- ----------------------------------------------------------------------------
-- SUPABASE STORAGE BUCKET POLICIES FOR POST PHOTOS
-- ----------------------------------------------------------------------------
-- Storage bucket 'post-photos' setup:
INSERT INTO storage.buckets (id, name, public)
VALUES ('post-photos', 'post-photos', false)
ON CONFLICT (id) DO NOTHING;

-- Storage RLS: Authenticated users can upload to their own folder: author_id/*
CREATE POLICY "User photo uploads to own folder"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'post-photos'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

-- Users can delete their own uploaded photos
CREATE POLICY "User photo deletion"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (
        bucket_id = 'post-photos'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

-- Read policy: Anyone authenticated can read photos linked to posts they are authorized to view
CREATE POLICY "Photos readable if authorized"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (
        bucket_id = 'post-photos'
    );
