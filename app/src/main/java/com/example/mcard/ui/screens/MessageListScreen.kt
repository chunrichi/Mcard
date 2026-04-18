package com.example.mcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mcard.ui.components.MessageCard
import com.example.mcard.ui.components.MessageDetailPager
import com.example.mcard.ui.data.local.MessagesPreferences
import com.example.mcard.ui.data.local.SourcesPreferences
import com.example.mcard.ui.data.local.SyncPreferences
import com.example.mcard.ui.data.model.Message
import com.example.mcard.ui.data.network.ApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    onNavigateToConfig: () -> Unit,
    modifier: Modifier = Modifier,
    syncPreferences: SyncPreferences? = null,
    sourcesPreferences: SourcesPreferences? = null,
    messagesPreferences: MessagesPreferences? = null
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var selectedMessageIndex by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val apiService = remember { ApiService() }
    val scope = rememberCoroutineScope()

    // Load messages on first composition - load from local first, then fetch from network
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        // First load from local storage
        val localMessages = messagesPreferences?.getMessages() ?: emptyList()
        if (localMessages.isNotEmpty()) {
            messages = localMessages.distinctBy { "${it.source}_${it.id}" }
        }

        // Then fetch from network
        val sources = sourcesPreferences?.getSources() ?: emptyList()
        val enabledSources = sources.filter { it.isEnabled }

        if (enabledSources.isEmpty()) {
            if (localMessages.isEmpty()) {
                messages = emptyList()
                errorMessage = if (sources.isEmpty()) "请先添加信息源" else "没有启用的信息源"
            }
        } else {
            val allMessages = mutableListOf<Message>()
            for (source in enabledSources) {
                val lastSync = syncPreferences?.getLastSyncTimestamp(source.id) ?: 0L
                val result = apiService.fetchMessagesFromSource(source, if (lastSync > 0) lastSync else null)
                result.onSuccess { msgs ->
                    allMessages.addAll(msgs)
                    if (msgs.isNotEmpty()) {
                        val maxTimestamp = msgs.maxOf { it.timestamp }
                        syncPreferences?.setLastSyncTimestamp(source.id, maxTimestamp)
                    }
                }.onFailure { e ->
                    // Don't overwrite local messages with error
                    if (messages.isEmpty()) {
                        errorMessage = "拉取失败: ${e::class.simpleName}"
                    }
                    android.util.Log.e("MessageList", "Fetch failed for ${source.name}: ${e.message}", e)
                }
            }
            if (allMessages.isNotEmpty()) {
                messages = allMessages.distinctBy { "${it.source}_${it.id}" }
                    .sortedByDescending { it.timestamp }
                messagesPreferences?.saveMessages(messages)
                syncPreferences?.lastMessageCount = messages.size
            } else if (messages.isEmpty()) {
                errorMessage = if (localMessages.isEmpty()) "拉取失败" else "已是最新数据"
            }
        }
        isLoading = false
    }

    if (selectedMessageIndex != null) {
        MessageDetailPager(
            messages = messages,
            initialIndex = selectedMessageIndex!!,
            onDismiss = { selectedMessageIndex = null }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "消息",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    if (isRefreshing) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (!isLoading) {
                                    isRefreshing = true
                                    errorMessage = null
                                    scope.launch {
                                        val sources = sourcesPreferences?.getSources() ?: emptyList()
                                        val enabledSources = sources.filter { it.isEnabled }

                                        if (enabledSources.isEmpty()) {
                                            errorMessage = "没有启用的信息源"
                                        } else {
                                            val allMessages = mutableListOf<Message>()
                                            for (source in enabledSources) {
                                                val lastSync = syncPreferences?.getLastSyncTimestamp(source.id) ?: 0L
                                                val result = apiService.fetchMessagesFromSource(source, if (lastSync > 0) lastSync else null)
                                                result.onSuccess { msgs ->
                                                    allMessages.addAll(msgs)
                                                    if (msgs.isNotEmpty()) {
                                                        val maxTimestamp = msgs.maxOf { it.timestamp }
                                                        syncPreferences?.setLastSyncTimestamp(source.id, maxTimestamp)
                                                    }
                                                }.onFailure { e ->
                                                    errorMessage = "拉取失败: ${e::class.simpleName}"
                                                    android.util.Log.e("MessageList", "Fetch failed: ${e.message}", e)
                                                }
                                            }
                                            if (allMessages.isNotEmpty()) {
                                                messages = allMessages.distinctBy { "${it.source}_${it.id}" }
                                                    .sortedByDescending { it.timestamp }
                                                syncPreferences?.lastMessageCount = messages.size
                                            } else if (messages.isEmpty()) {
                                                errorMessage = "拉取失败"
                                            }
                                        }
                                        isRefreshing = false
                                    }
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "刷新",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToConfig) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "配置",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when {
            isLoading && messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "暂无消息",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (errorMessage != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = messages,
                        key = { _, message -> "${message.source}_${message.id}" }
                    ) { index, message ->
                        MessageCard(
                            message = message,
                            onCardClick = { selectedMessageIndex = index }
                        )
                    }
                }
            }
        }
    }
}
