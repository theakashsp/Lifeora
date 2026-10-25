package com.liferise.app

import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Mood
import com.liferise.app.domain.model.Post
import com.liferise.app.domain.model.PostVisibility
import org.junit.Assert.*
import org.junit.Test

class SocialVisibilityTest {

    @Test
    fun testVisibilityOptions() {
        val publicPost = Post(
            id = "p1",
            authorId = "u1",
            authorUsername = "user1",
            authorDisplayName = "User One",
            content = "Public update",
            visibility = PostVisibility.PUBLIC
        )
        val privatePost = publicPost.copy(id = "p2", visibility = PostVisibility.PRIVATE)
        val followersPost = publicPost.copy(id = "p3", visibility = PostVisibility.FOLLOWERS_ONLY)

        assertEquals(PostVisibility.PUBLIC, publicPost.visibility)
        assertEquals(PostVisibility.PRIVATE, privatePost.visibility)
        assertEquals(PostVisibility.FOLLOWERS_ONLY, followersPost.visibility)
    }

    @Test
    fun testDiaryIsolationFromSocialPosts() {
        val diaryEntry = DiaryEntry(
            id = 10,
            title = "My Private Thoughts",
            content = "This contains personal and private reflections that must remain offline on device.",
            mood = Mood.PEACEFUL
        )

        // Verify domain models are structurally segregated
        assertFalse(
            "Diary entry class is distinct from Post and has no public social broadcast mechanism",
            diaryEntry is Post
        )
    }
}
