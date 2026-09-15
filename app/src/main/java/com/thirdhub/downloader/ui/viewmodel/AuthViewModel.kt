package com.thirdhub.downloader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thirdhub.downloader.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("请输入邮箱和密码")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            repository.login(email, password)
                .onSuccess { user ->
                    _loginState.value = LoginState.Success(user)
                }
                .onFailure { e ->
                    _loginState.value = LoginState.Error(e.message ?: "登录失败")
                }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: com.thirdhub.downloader.data.model.User) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
