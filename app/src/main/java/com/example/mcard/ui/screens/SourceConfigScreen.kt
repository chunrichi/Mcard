package com.example.mcard.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mcard.ui.components.RectCornerShape
import com.example.mcard.ui.data.local.SourcesPreferences
import com.example.mcard.ui.data.local.SyncPreferences
import com.example.mcard.ui.data.model.AuthType
import com.example.mcard.ui.data.model.Source
import com.example.mcard.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceConfigScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    syncPreferences: SyncPreferences? = null
) {
    val context = LocalContext.current
    val sourcesPrefs = remember { SourcesPreferences(context) }

    var sources by remember { mutableStateOf<List<Source>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var editingSourceId by remember { mutableStateOf<String?>(null) }
    var syncTimeVersion by remember { mutableStateOf(0L) }

    // Load sources from local storage
    sources = sourcesPrefs.getSources()

    if (showAddDialog) {
        AddSourceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, url, authType, authKey, authValue ->
                val newSource = Source(
                    id = "source_${System.currentTimeMillis()}",
                    name = name,
                    url = url,
                    isEnabled = true,
                    authType = authType,
                    authKey = authKey,
                    authValue = authValue
                )
                sourcesPrefs.addSource(newSource)
                sources = sourcesPrefs.getSources()
                showAddDialog = false
            }
        )
    }

    // Sync time edit dialog
    if (editingSourceId != null) {
        val source = sources.find { it.id == editingSourceId }
        if (source != null && syncPreferences != null) {
            SyncTimeDialog(
                sourceName = source.name,
                currentTimestamp = syncPreferences.getLastSyncTimestamp(source.id),
                onDismiss = { editingSourceId = null },
                onSave = { newTimestamp ->
                    syncPreferences.setLastSyncTimestamp(source.id, newTimestamp)
                    syncTimeVersion = System.currentTimeMillis()
                    editingSourceId = null
                },
                onReset = {
                    syncPreferences.clearSourceTimestamp(source.id)
                    syncTimeVersion = System.currentTimeMillis()
                    editingSourceId = null
                }
            )
        }
    }

    if (showClearDialog) {
        ClearDataDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                sourcesPrefs.clearAll()
                sources = emptyList()
                showClearDialog = false
            }
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
                        text = "信息源配置",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空数据",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Add source button at top
            AddSourceButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(16.dp)
            )

            if (sources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无信息源",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sources,
                        key = { "${it.id}_$syncTimeVersion" }
                    ) { source ->
                        SourceCard(
                            source = source,
                            syncTime = syncPreferences?.getLastSyncTimestamp(source.id) ?: 0L,
                            onToggleEnabled = { enabled ->
                                sourcesPrefs.updateSource(source.copy(isEnabled = enabled))
                                sources = sourcesPrefs.getSources()
                            },
                            onDelete = {
                                sourcesPrefs.deleteSource(source.id)
                                sources = sourcesPrefs.getSources()
                            },
                            onEditSyncTime = { editingSourceId = source.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSourceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RectCornerShape)
            .clickable { onClick() },
        shape = RectCornerShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LightGray),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "添加信息源",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SourceCard(
    source: Source,
    syncTime: Long,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEditSyncTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectCornerShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LightGray),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (source.isEnabled) "已启用" else "已禁用",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    modifier = Modifier
                        .clip(RectCornerShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDelete() },
                    color = if (source.isEnabled) LightGray else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, LightGray)
                ) {
                    Text(
                        text = "删除",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = if (source.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = source.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // URL
            if (source.url.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = source.url,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Auth info
            if (source.authType != AuthType.NONE) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${getAuthTypeLabel(source.authType)}: ${source.authKey}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sync time
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "同步时间: ${if (syncTime > 0) {
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        date.format(java.util.Date(syncTime))
                    } else "未同步"}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "调整",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onEditSyncTime() }
                )
            }
        }
    }
}

@Composable
private fun AddSourceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, AuthType, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedAuthType by remember { mutableStateOf(AuthType.NONE) }
    var authKey by remember { mutableStateOf("") }
    var authValue by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RectCornerShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, LightGray),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加信息源",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(thickness = 1.dp, color = LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // Name input
                Text(
                    text = "名称",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("输入信息源名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectCornerShape
                )

                Spacer(modifier = Modifier.height(16.dp))

                // URL input
                Text(
                    text = "URL",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://api.example.com/api/messages") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectCornerShape
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auth type selection
                Text(
                    text = "认证方式",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthType.entries.forEach { authType ->
                    AuthTypeSelectItem(
                        label = getAuthTypeLabel(authType),
                        isSelected = selectedAuthType == authType,
                        onClick = { selectedAuthType = authType }
                    )
                    if (authType != AuthType.entries.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Auth fields
                if (selectedAuthType != AuthType.NONE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "认证信息",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedAuthType) {
                        AuthType.NONE -> {}
                        AuthType.API_KEY, AuthType.CUSTOM_HEADER -> {
                            OutlinedTextField(
                                value = authKey,
                                onValueChange = { authKey = it },
                                label = { Text("Header 名称") },
                                placeholder = { Text("X-API-Key") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectCornerShape
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = authValue,
                                onValueChange = { authValue = it },
                                label = { Text("密钥值") },
                                placeholder = { Text("your_api_key") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectCornerShape
                            )
                        }
                        AuthType.BEARER_TOKEN -> {
                            OutlinedTextField(
                                value = authValue,
                                onValueChange = { authValue = it },
                                label = { Text("Token") },
                                placeholder = { Text("your_bearer_token") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectCornerShape
                            )
                        }
                        AuthType.BASIC_AUTH -> {
                            OutlinedTextField(
                                value = authKey,
                                onValueChange = { authKey = it },
                                label = { Text("用户名") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectCornerShape
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = authValue,
                                onValueChange = { authValue = it },
                                label = { Text("密码") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectCornerShape
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() },
                        shape = RectCornerShape,
                        color = LightGray,
                        border = BorderStroke(1.dp, LightGray)
                    ) {
                        Text(
                            text = "取消",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = name.isNotBlank()) {
                                onAdd(name, url, selectedAuthType, authKey, authValue)
                            },
                        shape = RectCornerShape,
                        color = if (name.isNotBlank()) MaterialTheme.colorScheme.onSurface else LightGray,
                        border = BorderStroke(1.dp, if (name.isNotBlank()) MaterialTheme.colorScheme.onSurface else LightGray)
                    ) {
                        Text(
                            text = "添加",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (name.isNotBlank()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RectCornerShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, LightGray),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "清空数据",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "确定要清空所有信息源吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() },
                        shape = RectCornerShape,
                        color = LightGray,
                        border = BorderStroke(1.dp, LightGray)
                    ) {
                        Text(
                            text = "取消",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onConfirm() },
                        shape = RectCornerShape,
                        color = MaterialTheme.colorScheme.error,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = "清空",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTypeSelectItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectCornerShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RectCornerShape,
        color = when {
            isSelected -> MaterialTheme.colorScheme.onSurface
            isPressed -> LightGray
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
private fun SyncTimeDialog(
    sourceName: String,
    currentTimestamp: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
    onReset: () -> Unit
) {
    var timestampText by remember {
        mutableStateOf(
            if (currentTimestamp > 0) {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                date.format(java.util.Date(currentTimestamp))
            } else ""
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RectCornerShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, LightGray),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "调整同步时间",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "当前: ${if (currentTimestamp > 0) {
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        date.format(java.util.Date(currentTimestamp))
                    } else "未同步"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = timestampText,
                    onValueChange = { timestampText = it },
                    label = { Text("时间戳（毫秒）") },
                    placeholder = { Text("输入时间戳") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectCornerShape
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "留空表示从头同步",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onReset() },
                        shape = RectCornerShape,
                        color = MaterialTheme.colorScheme.error,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = "重置",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val timestamp = timestampText.trim().toLongOrNull() ?: 0L
                                onSave(timestamp)
                            },
                        shape = RectCornerShape,
                        color = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(
                            text = "保存",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun getAuthTypeLabel(type: AuthType): String {
    return when (type) {
        AuthType.NONE -> "无"
        AuthType.API_KEY -> "API Key"
        AuthType.BEARER_TOKEN -> "Bearer Token"
        AuthType.BASIC_AUTH -> "Basic Auth"
        AuthType.CUSTOM_HEADER -> "自定义 Header"
    }
}
