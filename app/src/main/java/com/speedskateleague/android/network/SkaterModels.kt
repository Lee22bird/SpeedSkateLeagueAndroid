package com.speedskateleague.android.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkaterPostDto(
    val id: String,
    val body: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("author_ssl_id") val authorSslId: String? = null,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
    val replies: List<SkaterReplyDto>? = null,
)

@Serializable
data class SkaterReplyDto(
    val id: String,
    val body: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
)

@Serializable
data class SkaterUserDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("ssl_skater_id") val sslSkaterId: String? = null,
    val team: String? = null,
)

@Serializable
data class SkaterFeedResponse(
    val posts: List<SkaterPostDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class SkaterPostResponse(
    val post: SkaterPostDto,
)

@Serializable
data class SkaterFollowingResponse(
    val following: List<SkaterUserDto> = emptyList(),
)

@Serializable
data class SkaterUserPostsResponse(
    val profile: SkaterUserDto? = null,
    val posts: List<SkaterPostDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("is_following") val isFollowing: Boolean = false,
)

@Serializable
data class SkaterUploadUrlResponse(
    @SerialName("signed_url") val signedUrl: String,
    val path: String,
    @SerialName("public_url") val publicUrl: String,
)

@Serializable
data class SkaterCreatePostRequest(
    val body: String,
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class SkaterReplyRequest(
    val body: String,
)

@Serializable
data class SkaterReportRequest(
    val reason: String,
)

@Serializable
data class SkaterUploadUrlRequest(
    @SerialName("content_type") val contentType: String,
)
