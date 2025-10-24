package com.example.nogramtime.ui

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nogramtime.R
import com.example.nogramtime.admin.AdminReceiver
import com.example.nogramtime.data.SettingsManager
import com.example.nogramtime.pin.PinManager
import com.example.nogramtime.pin.PinViewModel
import com.example.nogramtime.service.BlockingVpnService
import kotlinx.coroutines.launch

/**
 * Settings screen that is protected by a user defined PIN. When first opened
 * the user is prompted to enter their existing PIN or create a new one if
 * none exists. Once unlocked the screen presents toggles to suspend the
 * blocking, enable the optional VPN module, manage overlay and accessibility
 * permissions and manage device administrator activation. Users can also
 * update or remove the PIN from this screen.
 */
@Composable
fun SettingsScreen(pinViewModel: PinViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Whether a PIN has been defined at all
    val pinExists by pinViewModel.pinExists.collectAsState()
    // Whether the user has successfully entered the PIN and unlocked settings
    val (unlocked, setUnlocked) = remember { mutableStateOf(!pinExists) }
    // Local state for the PIN prompt
    val (pinInput, setPinInput) = remember { mutableStateOf("") }
    val (confirmPinInput, setConfirmPinInput) = remember { mutableStateOf("") }
    val (pinError, setPinError) = remember { mutableStateOf(false) }
    val (creatingPin, setCreatingPin) = remember { mutableStateOf(!pinExists) }
    // When the existence of a PIN changes reset the unlocked state
    LaunchedEffect(pinExists) {
        if (!pinExists) {
            setUnlocked(true)
            setCreatingPin(true)
        }
    }

    if (!unlocked) {
        // Prompt for PIN if the user hasn’t unlocked the settings yet
        AlertDialog(
            onDismissRequest = {
                // Do nothing; user must enter the PIN or back out via system controls
            },
            title = {
                Text(
                    text = if (creatingPin) stringResource(id = R.string.pin_setup)
                    else "Inserisci PIN"
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.Center) {
                    if (creatingPin) {
                        Text(text = "Scegli un PIN di 4–8 cifre")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 8 && it.all { ch -> ch.isDigit() }) setPinInput(it)
                            },
                            label = { Text(text = stringResource(id = R.string.pin_setup)) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = {
                                if (it.length <= 8 && it.all { ch -> ch.isDigit() }) setConfirmPinInput(it)
                            },
                            label = { Text(text = stringResource(id = R.string.pin_confirm)) },
                            singleLine = true
                        )
                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "I PIN non coincidono o sono troppo corti", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 8 && it.all { ch -> ch.isDigit() }) setPinInput(it)
                            },
                            label = { Text(text = "PIN") },
                            singleLine = true
                        )
                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(id = R.string.pin_incorrect), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (creatingPin) {
                        // Creating a new PIN
                        if (pinInput.length in 4..8 && pinInput == confirmPinInput) {
                            pinViewModel.createPin(pinInput)
                            setUnlocked(true)
                            setPinInput("")
                            setConfirmPinInput("")
                            setPinError(false)
                            setCreatingPin(false)
                        } else {
                            setPinError(true)
                        }
                    } else {
                        // Verifying existing PIN
                        pinViewModel.verifyPin(pinInput) { ok ->
                            if (ok) {
                                setUnlocked(true)
                                setPinInput("")
                                setPinError(false)
                            } else {
                                setPinError(true)
                            }
                        }
                    }
                }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                if (creatingPin) {
                    TextButton(onClick = {
                        // Cancel creation resets fields but leaves the dialog
                        setPinInput("")
                        setConfirmPinInput("")
                        setPinError(false)
                    }) {
                        Text(text = "Annulla")
                    }
                }
            }
        )
        return
    }

    // Once unlocked, show the actual settings UI
    val suspendBlock = remember { mutableStateOf(SettingsManager.isBlockingSuspended(context)) }
    val vpnEnabled = remember { mutableStateOf(SettingsManager.isVpnEnabled(context)) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(text = "Impostazioni", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        // Suspend block toggle
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Sospendi blocco", modifier = Modifier.weight(1f))
            Switch(
                checked = suspendBlock.value,
                onCheckedChange = { checked ->
                    suspendBlock.value = checked
                    SettingsManager.setBlockingSuspended(context, checked)
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // VPN toggle
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "VPN Instagram", modifier = Modifier.weight(1f))
            Switch(
                checked = vpnEnabled.value,
                onCheckedChange = { checked ->
                    vpnEnabled.value = checked
                    SettingsManager.setVpnEnabled(context, checked)
                    if (checked) {
                        // Start VPN service
                        val intent = Intent(context, BlockingVpnService::class.java)
                        context.startService(intent)
                    } else {
                        // Stop VPN service
                        val intent = Intent(context, BlockingVpnService::class.java)
                        context.stopService(intent)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Manage overlay permission
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Permesso overlay", modifier = Modifier.weight(1f))
            Button(onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName)
                )
                context.startActivity(intent)
            }) {
                Text(text = "Apri")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Manage accessibility service
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Servizio accessibilità", modifier = Modifier.weight(1f))
            Button(onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }) {
                Text(text = "Apri")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Manage device admin
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val compName = ComponentName(context, AdminReceiver::class.java)
            val isActive = dpm.isAdminActive(compName)
            Text(text = "Amministratore dispositivo", modifier = Modifier.weight(1f))
            Button(onClick = {
                if (!isActive) {
                    // Request activation
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "L’app utilizza l’amministratore per prevenire la disinstallazione accidentale.")
                    // Need to launch from an activity context; fallback if not an Activity
                    if (context is Activity) {
                        context.startActivityForResult(intent, 0)
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                } else {
                    dpm.removeActiveAdmin(compName)
                }
            }) {
                Text(text = if (isActive) "Disattiva" else "Attiva")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Change or remove PIN
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "PIN", modifier = Modifier.weight(1f))
            Button(onClick = {
                // Changing or removing the PIN clears the stored hash. When a PIN
                // exists we clear it and trigger the creation flow again.
                PinManager.clearPin(context)
                setUnlocked(false)
                setCreatingPin(true)
            }) {
                Text(text = if (pinExists) "Cambia" else "Imposta")
            }
        }
    }
}