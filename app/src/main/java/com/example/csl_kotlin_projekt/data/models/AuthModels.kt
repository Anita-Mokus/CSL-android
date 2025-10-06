package com.example.csl_kotlin_projekt.data.models

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: UserDto?,
    @SerializedName("tokens")
    val tokens: TokensDto?
)

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?
)

data class TokensDto(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class SignInDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class SignUpDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
