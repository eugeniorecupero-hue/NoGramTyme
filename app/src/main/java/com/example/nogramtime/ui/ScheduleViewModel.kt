package com.example.nogramtime.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nogramtime.data.AppDatabase
import com.example.nogramtime.data.BlockRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel exposing blocking rules to the schedule UI. It delegates
 * persistence operations to the DAO and launches coroutines on the view model
 * scope for writes.
 */
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).blockRuleDao()
    val rules: Flow<List<BlockRule>> = dao.getRulesFlow()

    fun insertRule(rule: BlockRule) {
        viewModelScope.launch {
            dao.insert(rule)
        }
    }

    fun updateRule(rule: BlockRule) {
        viewModelScope.launch {
            dao.update(rule)
        }
    }

    fun deleteRule(rule: BlockRule) {
        viewModelScope.launch {
            dao.delete(rule)
        }
    }
}