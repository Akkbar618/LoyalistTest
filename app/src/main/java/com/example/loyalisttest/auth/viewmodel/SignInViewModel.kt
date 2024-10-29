package com.example.loyalisttest.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInViewModel : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onPasswordVisibilityChange() {
        _state.value = _state.value.copy(
            isPasswordVisible = !_state.value.isPasswordVisible
        )
    }

    fun signIn(auth: FirebaseAuth?, onSuccess: () -> Unit) {
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            _state.value = _state.value.copy(error = "Заполните все поля")
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        auth?.signInWithEmailAndPassword(_state.value.email, _state.value.password)
            ?.addOnCompleteListener { task ->
                _state.value = _state.value.copy(isLoading = false)
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _state.value = _state.value.copy(
                        error = task.exception?.message ?: "Ошибка авторизации"
                    )
                }
            }
    }
}