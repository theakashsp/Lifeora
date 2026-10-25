package com.liferise.app.domain.model

enum class PostVisibility(val label: String) {
    PUBLIC("Public"),
    FOLLOWERS_ONLY("Followers Only"),
    PRIVATE("Only Me (Private)")
}

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String = "",
    val interests: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

data class Post(
    val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val caption: String = "",
    val imageUrl: String? = null,
    val hashtags: List<String> = emptyList(),
    val visibility: PostVisibility = PostVisibility.PUBLIC,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
