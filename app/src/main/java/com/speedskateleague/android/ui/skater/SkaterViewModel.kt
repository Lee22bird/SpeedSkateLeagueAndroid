package com.speedskateleague.android.ui.skater

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.speedskateleague.android.SslApplication
import com.speedskateleague.android.network.SkaterCreatePostRequest
import com.speedskateleague.android.network.SkaterPostDto
import com.speedskateleague.android.network.SkaterReplyRequest
import com.speedskateleague.android.network.SkaterReportRequest
import com.speedskateleague.android.network.SkaterUserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SkaterFeedTab { PUBLIC, FOLLOWING }

data class SkaterUiState(
    val tab: SkaterFeedTab = SkaterFeedTab.PUBLIC,
    val publicPosts: List<SkaterPostDto> = emptyList(),
    val followingPosts: List<SkaterPostDto> = emptyList(),
    val following: List<SkaterUserDto> = emptyList(),
    val selectedUser: SkaterUserDto? = null,
    val userPosts: List<SkaterPostDto> = emptyList(),
    val selectedUserIsFollowing: Boolean = false,
    val expandedPost: SkaterPostDto? = null,
    val isLoadingFeed: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String? = null,
    // composer
    val composerText: String = "",
    val isPosting: Boolean = false,
    val postError: String? = null,
    // reply
    val replyText: String = "",
    val isReplying: Boolean = false,
)

class SkaterViewModel(app: Application) : AndroidViewModel(app) {
    private val api = (app as SslApplication).apiClient.api

    private val _state = MutableStateFlow(SkaterUiState())
    val state: StateFlow<SkaterUiState> = _state.asStateFlow()

    private var publicCursor: String? = null
    private var followingCursor: String? = null

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            runCatching { api.me() }.onSuccess { me -> _state.value = _state.value.copy(currentUserId = me.profileId) }
        }
        loadPublicFeed(refresh = true)
        loadFollowing()
    }

    fun setTab(tab: SkaterFeedTab) {
        _state.value = _state.value.copy(tab = tab)
        if (tab == SkaterFeedTab.FOLLOWING && _state.value.followingPosts.isEmpty()) {
            loadFollowingFeed(refresh = true)
        }
    }

    fun loadPublicFeed(refresh: Boolean = false) {
        if (refresh) publicCursor = null
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingFeed = true, errorMessage = null)
            runCatching { api.getSkaterPublicFeed(cursor = publicCursor) }.onSuccess { resp ->
                publicCursor = resp.nextCursor
                val posts = if (refresh) resp.posts else _state.value.publicPosts + resp.posts
                _state.value = _state.value.copy(publicPosts = posts, isLoadingFeed = false)
            }.onFailure { err ->
                _state.value = _state.value.copy(isLoadingFeed = false, errorMessage = err.message)
            }
        }
    }

    fun loadFollowingFeed(refresh: Boolean = false) {
        if (refresh) followingCursor = null
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingFeed = true)
            runCatching { api.getSkaterFollowingFeed(cursor = followingCursor) }.onSuccess { resp ->
                followingCursor = resp.nextCursor
                val posts = if (refresh) resp.posts else _state.value.followingPosts + resp.posts
                _state.value = _state.value.copy(followingPosts = posts, isLoadingFeed = false)
            }.onFailure {
                _state.value = _state.value.copy(isLoadingFeed = false)
            }
        }
    }

    fun loadFollowing() {
        viewModelScope.launch {
            runCatching { api.getSkaterFollowing() }.onSuccess { resp ->
                _state.value = _state.value.copy(following = resp.following)
            }
        }
    }

    fun expandPost(post: SkaterPostDto) {
        _state.value = _state.value.copy(expandedPost = post)
        viewModelScope.launch {
            runCatching { api.getSkaterPost(post.id) }.onSuccess { resp ->
                _state.value = _state.value.copy(expandedPost = resp.post)
            }
        }
    }

    fun collapsePost() { _state.value = _state.value.copy(expandedPost = null) }

    fun selectUser(user: SkaterUserDto) {
        _state.value = _state.value.copy(selectedUser = user)
        viewModelScope.launch {
            runCatching { api.getSkaterUserPosts(user.id) }.onSuccess { resp ->
                _state.value = _state.value.copy(
                    userPosts = resp.posts,
                    selectedUserIsFollowing = resp.isFollowing,
                    selectedUser = resp.profile ?: user,
                )
            }
        }
    }

    fun clearSelectedUser() { _state.value = _state.value.copy(selectedUser = null, userPosts = emptyList()) }

    fun toggleLike(post: SkaterPostDto) {
        val wasLiked = post.likedByMe
        updatePost(post.id) { it.copy(likedByMe = !wasLiked, likeCount = it.likeCount + if (wasLiked) -1 else 1) }
        viewModelScope.launch {
            runCatching {
                if (wasLiked) api.unlikeSkaterPost(post.id) else api.likeSkaterPost(post.id)
            }.onFailure {
                updatePost(post.id) { it.copy(likedByMe = wasLiked, likeCount = it.likeCount + if (wasLiked) 1 else -1) }
            }
        }
    }

    fun deletePost(post: SkaterPostDto) {
        viewModelScope.launch {
            runCatching { api.deleteSkaterPost(post.id) }.onSuccess {
                _state.value = _state.value.copy(
                    publicPosts = _state.value.publicPosts.filter { it.id != post.id },
                    followingPosts = _state.value.followingPosts.filter { it.id != post.id },
                    userPosts = _state.value.userPosts.filter { it.id != post.id },
                    expandedPost = if (_state.value.expandedPost?.id == post.id) null else _state.value.expandedPost,
                )
            }
        }
    }

    fun reportPost(post: SkaterPostDto, reason: String) {
        viewModelScope.launch { runCatching { api.reportSkaterPost(post.id, SkaterReportRequest(reason)) } }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            runCatching { api.blockSkaterUser(userId) }.onSuccess {
                _state.value = _state.value.copy(
                    publicPosts = _state.value.publicPosts.filter { it.authorId != userId },
                    followingPosts = _state.value.followingPosts.filter { it.authorId != userId },
                    following = _state.value.following.filter { it.id != userId },
                    selectedUser = null,
                )
            }
        }
    }

    fun toggleFollow(userId: String) {
        val wasFollowing = _state.value.selectedUserIsFollowing
        _state.value = _state.value.copy(selectedUserIsFollowing = !wasFollowing)
        viewModelScope.launch {
            runCatching {
                if (wasFollowing) api.unfollowSkaterUser(userId) else api.followSkaterUser(userId)
            }.onSuccess {
                loadFollowing()
            }.onFailure {
                _state.value = _state.value.copy(selectedUserIsFollowing = wasFollowing)
            }
        }
    }

    fun updateComposerText(text: String) {
        if (text.length <= 300) _state.value = _state.value.copy(composerText = text)
    }

    fun submitPost() {
        val text = _state.value.composerText.trim()
        if (text.isEmpty()) return
        _state.value = _state.value.copy(isPosting = true, postError = null)
        viewModelScope.launch {
            runCatching { api.createSkaterPost(SkaterCreatePostRequest(body = text)) }.onSuccess { resp ->
                _state.value = _state.value.copy(
                    publicPosts = listOf(resp.post) + _state.value.publicPosts,
                    composerText = "",
                    isPosting = false,
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(isPosting = false, postError = err.message ?: "Post failed")
            }
        }
    }

    fun updateReplyText(text: String) {
        if (text.length <= 300) _state.value = _state.value.copy(replyText = text)
    }

    fun submitReply(postId: String) {
        val text = _state.value.replyText.trim()
        if (text.isEmpty()) return
        _state.value = _state.value.copy(isReplying = true)
        viewModelScope.launch {
            runCatching { api.createSkaterReply(postId, SkaterReplyRequest(text)) }.onSuccess { resp ->
                _state.value = _state.value.copy(expandedPost = resp.post, replyText = "", isReplying = false)
            }.onFailure {
                _state.value = _state.value.copy(isReplying = false)
            }
        }
    }

    fun refresh() {
        if (_state.value.tab == SkaterFeedTab.PUBLIC) loadPublicFeed(refresh = true)
        else loadFollowingFeed(refresh = true)
        loadFollowing()
    }

    private fun updatePost(postId: String, transform: (SkaterPostDto) -> SkaterPostDto) {
        _state.value = _state.value.copy(
            publicPosts = _state.value.publicPosts.map { if (it.id == postId) transform(it) else it },
            followingPosts = _state.value.followingPosts.map { if (it.id == postId) transform(it) else it },
            userPosts = _state.value.userPosts.map { if (it.id == postId) transform(it) else it },
            expandedPost = _state.value.expandedPost?.let { if (it.id == postId) transform(it) else it },
        )
    }
}
