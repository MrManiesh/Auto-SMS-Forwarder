package com.example.smsforwarder.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.smsforwarder.service.ForwardingService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Settings", "Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto SMS Forwarder") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            if (selectedTab == 0) {
                SettingsScreen(viewModel)
            } else {
                LogsScreen(viewModel)
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val targetPhone by viewModel.targetPhone.collectAsState()
    val targetEmail by viewModel.targetEmail.collectAsState()
    val senderEmail by viewModel.senderEmail.collectAsState()
    val appPassword by viewModel.appPassword.collectAsState()
    val forwardingEnabled by viewModel.forwardingEnabled.collectAsState()
    val emailForwardingEnabled by viewModel.emailForwardingEnabled.collectAsState()

    var tempPhone by remember { mutableStateOf("") }
    var tempEmail by remember { mutableStateOf("") }
    var tempSenderEmail by remember { mutableStateOf("") }
    var tempAppPassword by remember { mutableStateOf("") }
    var localForwardingEnabled by remember { mutableStateOf(false) }
    var localEmailForwardingEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(targetPhone) { if (tempPhone.isEmpty() || tempPhone == targetPhone) tempPhone = targetPhone }
    LaunchedEffect(targetEmail) { if (tempEmail.isEmpty() || tempEmail == targetEmail) tempEmail = targetEmail }
    LaunchedEffect(senderEmail) { if (tempSenderEmail.isEmpty() || tempSenderEmail == senderEmail) tempSenderEmail = senderEmail }
    LaunchedEffect(appPassword) { if (tempAppPassword.isEmpty() || tempAppPassword == appPassword) tempAppPassword = appPassword }
    LaunchedEffect(forwardingEnabled) { localForwardingEnabled = forwardingEnabled }
    LaunchedEffect(emailForwardingEnabled) { localEmailForwardingEnabled = emailForwardingEnabled }

    var hasPermissions by remember { mutableStateOf(checkPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        hasPermissions = map.values.all { it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasPermissions) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permissions Required", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("The app requires SMS and notification permissions to function properly.", color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        permissionLauncher.launch(getRequiredPermissions())
                    }) {
                        Text("Grant Permissions")
                    }
                }
            }
        }

        OutlinedTextField(
            value = tempPhone,
            onValueChange = {
                tempPhone = it
                viewModel.updateTargetPhone(it)
            },
            label = { Text("Target Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tempEmail,
            onValueChange = {
                tempEmail = it
                viewModel.updateTargetEmail(it)
            },
            label = { Text("Target Email Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Email Forwarding", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = localEmailForwardingEnabled, 
                onCheckedChange = { 
                    localEmailForwardingEnabled = it
                    viewModel.setEmailForwardingEnabled(it) 
                }
            )
        }

        if (emailForwardingEnabled) {
            OutlinedTextField(
                value = tempSenderEmail,
                onValueChange = { tempSenderEmail = it; viewModel.updateSenderEmail(it) },
                label = { Text("Sender Email Address (e.g. your Gmail)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tempAppPassword,
                onValueChange = { tempAppPassword = it; viewModel.updateAppPassword(it) },
                label = { Text("App Password (16 chars)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Enable Forwarding",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = localForwardingEnabled,
                onCheckedChange = { isEnabled ->
                    if (hasPermissions) {
                        localForwardingEnabled = isEnabled
                        viewModel.setForwardingEnabled(isEnabled)
                        try {
                            if (isEnabled) {
                                val intent = Intent(context, ForwardingService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            } else {
                                val intent = Intent(context, ForwardingService::class.java)
                                context.stopService(intent)
                            }
                        } catch (e: Exception) {
                            localForwardingEnabled = !isEnabled
                            viewModel.setForwardingEnabled(!isEnabled)
                            Toast.makeText(context, "Service Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Missing Permissions! Check top of screen.", Toast.LENGTH_LONG).show()
                        permissionLauncher.launch(getRequiredPermissions())
                    }
                }
            )
        }

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disable Battery Optimization (Recommended)")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("</> Open source", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Text("Feedback & Support", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Text("Email: manishmatwacs@gmail.com", style = MaterialTheme.typography.bodyMedium)
            Text("Telegram: @zeetron", style = MaterialTheme.typography.bodyMedium)
            Text("Instagram: expert.py", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Crafted with ♥ by Manish Matwa Choudhary", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LogsScreen(viewModel: MainViewModel) {
    val logs by viewModel.recentLogs.collectAsState()

    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No forwarded messages yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sender: ${log.sender}", fontWeight = FontWeight.Bold)
                        Text(log.messageBody, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status: ${log.forwardStatus}", color = MaterialTheme.colorScheme.primary)
                            Text(java.util.Date(log.timestamp).toString())
                        }
                    }
                }
            }
        }
    }
}

private fun getRequiredPermissions(): Array<String> {
    val list = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        list.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return list.toTypedArray()
}

private fun checkPermissions(context: android.content.Context): Boolean {
    return getRequiredPermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
