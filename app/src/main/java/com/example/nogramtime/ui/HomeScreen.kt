package com.example.nogramtime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nogramtime.R
import com.example.nogramtime.data.AppDatabase
import com.example.nogramtime.data.ScheduleRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Home screen displaying whether Instagram is currently blocked and when the
 * next blocking window will occur. A button is provided to open the system
 * accessibility settings if the service hasn’t been enabled.
 */
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val statusText = remember { mutableStateOf<String?>(null) }
    val nextBlock = remember { mutableStateOf<String?>(null) }

    // Get string resources in composable context before launching coroutine
    val blockedText = stringResource(id = R.string.home_status_blocked)
    val unblockedText = stringResource(id = R.string.home_status_unblocked)

    // Compute the current state once on composition. In a real app you might
    // observe a flow to update the UI in near real‑time. This simplified
    // implementation recalculates when the home screen becomes visible.
    remember(Unit) {
        scope.launch {
            val dao = AppDatabase.getInstance(context).blockRuleDao()
            val repo = ScheduleRepository(dao)
            val now = LocalDateTime.now()
            val blocked = repo.isInstagramBlockedNow(now)
            statusText.value = if (blocked) {
                blockedText
            } else {
                unblockedText
            }
            // Compute next upcoming block time by iterating future minutes up to 7 days
            val rules = dao.getRules()
            val formatter = DateTimeFormatter.ofPattern("EEE HH:mm")
            var upcoming: LocalDateTime? = null
            for (dayOffset in 0..7) {
                val date = now.plusDays(dayOffset.toLong())
                val dayOfWeek = date.dayOfWeek.value
                for (rule in rules) {
                    if (!rule.days.contains(dayOfWeek)) continue
                    val blockStart = date.withHour(rule.startHour).withMinute(rule.startMinute).withSecond(0).withNano(0)
                    if (blockStart.isAfter(now) && (upcoming == null || blockStart.isBefore(upcoming))) {
                        upcoming = blockStart
                    }
                }
            }
            nextBlock.value = upcoming?.format(formatter)
        }
    }

    // Get next block format string in composable context
    val nextBlockFormatted = nextBlock.value?.let { 
        stringResource(id = R.string.home_next_block, it) 
    } ?: "Nessun blocco programmato"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statusText.value ?: "", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = nextBlockFormatted)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            // Navigate to schedule screen so the user can adjust rules
            navController.navigate("schedule")
        }) {
            Text(text = "Gestisci pianificazioni")
        }
    }
}