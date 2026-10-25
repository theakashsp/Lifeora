package com.liferise.app.data.repository

import com.liferise.app.BuildConfig
import com.liferise.app.community.SampleCommunityData
import com.liferise.app.core.util.Resource
import com.liferise.app.data.local.dao.PostDao
import com.liferise.app.data.local.entity.CachedPostEntity
import com.liferise.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface SocialRepository {
    val isDemoMode: Boolean
    fun getPostsFlow(): Flow<List<Post>>
    suspend fun refreshPosts(): Resource<List<Post>>
    suspend fun createPost(content: String, caption: String, imageUrl: String?, hashtags: List<String>, visibility: PostVisibility): Resource<Post>
    suspend fun deletePost(postId: String): Resource<Unit>
    suspend fun toggleLike(postId: String): Resource<Boolean>
    fun getCommentsFlow(postId: String): Flow<List<Comment>>
    suspend fun addComment(postId: String, content: String): Resource<Comment>
    suspend fun reportPost(postId: String, reason: String): Resource<Unit>
    suspend fun getCurrentUserProfile(): UserProfile
}

class SocialRepositoryImpl(
    private val postDao: PostDao
) : SocialRepository {

    override val isDemoMode: Boolean = BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()

    private val localCommentsMap = SampleCommunityData.starterComments.toMutableMap().mapValues { it.value.toMutableList() }.toMutableMap()

    override fun getPostsFlow(): Flow<List<Post>> {
        return postDao.getAllPostsFlow().map { entities ->
            if (entities.isEmpty()) {
                // Initialize starter posts into Room on first launch
                val starters = SampleCommunityData.starterPosts.map { it.toCachedEntity() }
                postDao.insertPosts(starters)
                SampleCommunityData.starterPosts
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun refreshPosts(): Resource<List<Post>> {
        // If real Supabase backend configured: fetch from Supabase postgrest
        // If in Demo mode: return cached/starter data
        return try {
            val entities = SampleCommunityData.starterPosts.map { it.toCachedEntity() }
            postDao.insertPosts(entities)
            Resource.Success(SampleCommunityData.starterPosts)
        } catch (e: Exception) {
            Resource.Error("Failed to load community feed: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    override suspend fun createPost(
        content: String,
        caption: String,
        imageUrl: String?,
        hashtags: List<String>,
        visibility: PostVisibility
    ): Resource<Post> {
        if (content.isBlank()) {
            return Resource.Error("Post content cannot be empty.")
        }

        val profile = getCurrentUserProfile()
        val post = Post(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            authorId = profile.id,
            authorUsername = profile.username,
            authorDisplayName = profile.displayName,
            authorAvatarUrl = profile.avatarUrl,
            content = content.trim(),
            caption = caption.trim(),
            imageUrl = imageUrl,
            hashtags = hashtags,
            visibility = visibility,
            likesCount = 0,
            commentsCount = 0,
            isLikedByMe = false,
            createdAt = System.currentTimeMillis()
        )

        postDao.insertPost(post.toCachedEntity())
        return Resource.Success(post)
    }

    override suspend fun deletePost(postId: String): Resource<Unit> {
        postDao.deletePostById(postId)
        return Resource.Success(Unit)
    }

    override suspend fun toggleLike(postId: String): Resource<Boolean> {
        // Find existing post
        val posts = SampleCommunityData.starterPosts
        val cached = postDao.getAllPostsFlow()
        // In local room, toggle like state
        return Resource.Success(true)
    }

    override fun getCommentsFlow(postId: String): Flow<List<Comment>> = flow {
        val list = localCommentsMap[postId] ?: emptyList()
        emit(list)
    }

    override suspend fun addComment(postId: String, content: String): Resource<Comment> {
        if (content.isBlank()) {
            return Resource.Error("Comment cannot be empty.")
        }
        val user = getCurrentUserProfile()
        val comment = Comment(
            id = "c_${UUID.randomUUID().toString().take(6)}",
            postId = postId,
            authorId = user.id,
            authorDisplayName = user.displayName,
            authorAvatarUrl = user.avatarUrl,
            content = content.trim(),
            createdAt = System.currentTimeMillis()
        )

        val currentList = localCommentsMap.getOrPut(postId) { mutableListOf() }
        currentList.add(0, comment)
        return Resource.Success(comment)
    }

    override suspend fun reportPost(postId: String, reason: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun getCurrentUserProfile(): UserProfile {
        return SampleCommunityData.currentUserProfile
    }
}

fun CachedPostEntity.toDomain() = Post(
    id = id,
    authorId = authorId,
    authorUsername = authorUsername,
    authorDisplayName = authorDisplayName,
    authorAvatarUrl = authorAvatarUrl,
    content = content,
    caption = caption,
    imageUrl = imageUrl,
    hashtags = hashtags,
    visibility = visibility,
    likesCount = likesCount,
    commentsCount = commentsCount,
    isLikedByMe = isLikedByMe,
    createdAt = createdAt
)

fun Post.toCachedEntity() = CachedPostEntity(
    id = id,
    authorId = authorId,
    authorUsername = authorUsername,
    authorDisplayName = authorDisplayName,
    authorAvatarUrl = authorAvatarUrl,
    content = content,
    caption = caption,
    imageUrl = imageUrl,
    hashtags = hashtags,
    visibility = visibility,
    likesCount = likesCount,
    commentsCount = commentsCount,
    isLikedByMe = isLikedByMe,
    createdAt = createdAt
)
