package com.example.nogramtime.pin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel used by the Settings screen to handle PIN creation and verification.
 */
class PinViewModel(application: Application) : AndroidViewModel(application) {
    private val _pinExists = MutableStateFlow(PinManager.hasPin(application))
    val pinExists: StateFlow<Boolean> = _pinExists

    /**
     * Creates a new PIN. After calling this method the [pinExists] flow will
     * emit true. Existing pins are overwritten.
     */
    fun createPin(pin: String) {
        val ctx = getApplication<Application>()
        PinManager.setPin(ctx, pin)
        _pinExists.value = true
    }

    /**
     * Checks whether the provided [pin] matches the stored PIN. If there is
     * no stored PIN this always returns false.
     */
    fun verifyPin(pin: String, onResult: (Boolean) -> Unit) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            val result = PinManager.checkPin(ctx, pin)
            onResult(result)
        }
    }
}