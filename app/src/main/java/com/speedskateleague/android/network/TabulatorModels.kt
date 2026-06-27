package com.speedskateleague.android.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscussionReplyDto(
    val id: String,
    @SerialName("discussion_id") val discussionId: String? = null,
    val body: String? = null,
    @SerialName("author_user_id") val authorUserId: String? = null,
    @SerialName("author_ssl_id") val authorSslId: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_role") val authorRole: String? = null,
    @SerialName("author_league") val authorLeague: String? = null,
    @SerialName("author_team") val authorTeam: String? = null,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class DiscussionDto(
    val id: String,
    val title: String? = null,
    val body: String? = null,
    val category: String? = null,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    @SerialName("author_user_id") val authorUserId: String? = null,
    @SerialName("author_ssl_id") val authorSslId: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_role") val authorRole: String? = null,
    @SerialName("author_league") val authorLeague: String? = null,
    @SerialName("author_team") val authorTeam: String? = null,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    val replies: List<DiscussionReplyDto> = emptyList(),
)

@Serializable
data class DiscussionsResponse(val discussions: List<DiscussionDto> = emptyList())

@Serializable
data class DiscussionDetailResponse(val discussion: DiscussionDto)

@Serializable
data class CreateDiscussionRequest(val title: String, val body: String)

@Serializable
data class CreateReplyRequest(val body: String)
