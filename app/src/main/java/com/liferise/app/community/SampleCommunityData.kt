package com.liferise.app.community

import com.liferise.app.domain.model.Comment
import com.liferise.app.domain.model.Post
import com.liferise.app.domain.model.PostVisibility
import com.liferise.app.domain.model.UserProfile

object SampleCommunityData {

    val currentUserProfile = UserProfile(
        id = "user_me_001",
        username = "raghav_k",
        displayName = "Raghavendra K",
        avatarUrl = null,
        bio = "Exploring mindfulness, daily discipline, and Kannada literature.",
        interests = listOf("Habits", "Kannada Literature", "Early Rising", "Fitness"),
        isPrivate = false,
        followersCount = 142,
        followingCount = 89
    )

    val starterPosts = listOf(
        Post(
            id = "post_001",
            authorId = "user_ananya_01",
            authorUsername = "ananya_sharma",
            authorDisplayName = "Ananya Sharma",
            authorAvatarUrl = null,
            content = "Completed a 6:00 AM wake-up with the Kannada Proverbs challenge! Starting the morning by reflecting on 'ಹಾಸಿಗೆ ಇದ್ದಷ್ಟು ಕಾಲು ಚಾಚು' keeps me grounded and focused today.",
            caption = "Early morning clarity 🌅",
            imageUrl = null,
            hashtags = listOf("#LifeRise", "#EarlyRiser", "#KannadaWisdom", "#Habits"),
            visibility = PostVisibility.PUBLIC,
            likesCount = 24,
            commentsCount = 5,
            isLikedByMe = false,
            createdAt = System.currentTimeMillis() - 7200000L
        ),
        Post(
            id = "post_002",
            authorId = "user_darshan_02",
            authorUsername = "darshan_b",
            authorDisplayName = "Darshan B. Gouda",
            authorAvatarUrl = null,
            content = "ಪ್ರತಿದಿನದ ಸಣ್ಣ ಪ್ರಯತ್ನಗಳು ಕೊನೆಗೆ ದೊಡ್ಡ ಯಶಸ್ಸಿಗೆ ಕಾರಣವಾಗುತ್ತವೆ. ಇಂದಿನ ೩ ಧ್ಯೇಯಗಳನ್ನು ಪೂರ್ಣಗೊಳಿಸಿದೆ! Keep striving everyone.",
            caption = "ದೈನಂದಿನ ಶಿಸ್ತು 💪",
            imageUrl = null,
            hashtags = listOf("#LifeRise", "#Motivation", "#SelfImprovement", "#Bengaluru"),
            visibility = PostVisibility.PUBLIC,
            likesCount = 42,
            commentsCount = 8,
            isLikedByMe = true,
            createdAt = System.currentTimeMillis() - 18000000L
        ),
        Post(
            id = "post_003",
            authorId = "user_priya_03",
            authorUsername = "priya_m",
            authorDisplayName = "Priya Murthy",
            authorAvatarUrl = null,
            content = "Just hit a 14-day continuous progress streak on LifeRise. The weighted scoring dashboard really helps you identify what needs attention between tasks and mindfulness.",
            caption = "Two weeks strong ✨",
            imageUrl = null,
            hashtags = listOf("#Streak", "#Productivity", "#MindfulGrowth"),
            visibility = PostVisibility.PUBLIC,
            likesCount = 37,
            commentsCount = 3,
            isLikedByMe = false,
            createdAt = System.currentTimeMillis() - 86400000L
        )
    )

    val starterComments = mapOf(
        "post_001" to listOf(
            Comment(
                id = "c_001",
                postId = "post_001",
                authorId = "user_darshan_02",
                authorDisplayName = "Darshan B. Gouda",
                content = "Great quote to start the day! Have a productive day ahead.",
                createdAt = System.currentTimeMillis() - 5400000L
            ),
            Comment(
                id = "c_002",
                postId = "post_001",
                authorId = "user_me_001",
                authorDisplayName = "Raghavendra K",
                content = "The Kannada proverbs challenge is by far my favorite alarm mode too!",
                createdAt = System.currentTimeMillis() - 3600000L
            )
        )
    )
}
