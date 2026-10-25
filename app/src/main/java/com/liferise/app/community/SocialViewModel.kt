package com.liferise.app.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.core.util.Resource
import com.liferise.app.data.repository.SocialRepository
import com.liferise.app.domain.model.Comment
import com.liferise.app.domain.model.Post
import com.liferise.app.domain.model.PostVisibility
import com.liferise.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SocialViewModel(
    private val repository: SocialRepository
) : ViewModel() {

    val isDemoMode: Boolean = repository.isDemoMode

    val posts: StateFlow<List<Post>> = repository.getPostsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _postCreationState = MutableStateFlow<Resource<Post>?>(null)
    val postCreationState: StateFlow<Resource<Post>?> = _postCreationState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = repository.getCurrentUserProfile()
        }
    }

    fun createPost(
        content: String,
        caption: String,
        imageUrl: String?,
        hashtags: List<String>,
        visibility: PostVisibility,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _postCreationState.value = Resource.Loading
            val result = repository.createPost(content, caption, imageUrl, hashtags, visibility)
            _postCreationState.value = result
            if (result is Resource.Success) {
                onSuccess()
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(postId, text)
        }
    }

    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            repository.reportPost(postId, reason)
        }
    }

    companion object {
        fun provideFactory(repository: SocialRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SocialViewModel(repository) as T
                }
            }
    }
}
