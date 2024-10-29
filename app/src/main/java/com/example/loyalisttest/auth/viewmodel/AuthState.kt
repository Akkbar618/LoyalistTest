package com.example.loyalisttest.auth.viewmodel

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)