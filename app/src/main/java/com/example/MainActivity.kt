package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.ChatMessage
import com.example.data.ChatSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBlack
                ) {
                    MainAppScreen()
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel = viewModel()) {
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentMessages by viewModel.currentMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    
    var showAuthDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<ChatSession?>(null) }
    var renameInput by remember { mutableStateOf(TextFieldValue("")) }
    var messageInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMode by remember { mutableStateOf("Normal") }
    var isSidebarExpanded by remember { mutableStateOf(true) }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        if (isSidebarExpanded) {
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .border(1.dp, CyberBorder),
                color = CyberDarkCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    // New Chat Button
                    Button(
                        onClick = { viewModel.createNewSession(selectedMode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নতুন চ্যাট", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mode Selection
                    Text(
                        "মোড নির্বাচন করুন",
                        color = CyberLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    listOf("Normal", "Girlfriend", "Writer", "Debugger", "Summarizer").forEach { mode ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .border(
                                    1.dp,
                                    if (selectedMode == mode) CyberPurple else CyberBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMode = mode },
                            color = if (selectedMode == mode) CyberPurple.copy(alpha = 0.2f) else CyberRecentCard,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    mode,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // History Title
                    Text(
                        "সাম্প্রতিক কথোপকথন",
                        color = CyberLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Sessions List
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(chatSessions) { session ->
                            SessionItem(
                                session = session,
                                active = activeSessionId == session.id,
                                onClick = { viewModel.selectSession(session.id) },
                                onPinClick = { viewModel.togglePin(session.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(session.id) },
                                onRenameClick = {
                                    selectedSession = session
                                    renameInput = TextFieldValue(session.title)
                                    showRenameDialog = true
                                },
                                onDeleteClick = {
                                    selectedSession = session
                                    showDeleteDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Support Button
                    Button(
                        onClick = { showSupportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Help, null, modifier = Modifier.size(16.dp), tint = CyberLightGray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সাপোর্ট", fontSize = 12.sp, color = CyberLightGray)
                    }
                }
            }
        }
        
        // Main Content
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(CyberBlack)
        ) {
            // Top Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(1.dp, CyberBorder),
                color = CyberDarkCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { isSidebarExpanded = !isSidebarExpanded },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Toggle sidebar",
                            tint = CyberBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Text(
                        "PS AI Assistant",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    IconButton(
                        onClick = { showAuthDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Account",
                            tint = CyberPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // User Profile Card
            if (activeSessionId == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UserProfileCard(
                        userEmail = userEmail,
                        userDisplayName = userDisplayName,
                        onLoginClick = { showAuthDialog = true },
                        onLogoutClick = { viewModel.logout() }
                    )
                    
                    // Feature Cards
                    Text(
                        "ফিচার হাব",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FeatureCard(
                                title = "দ্রুত জবাব",
                                icon = Icons.Filled.Bolt,
                                description = "তাৎক্ষণিক AI প্রতিক্রিয়া",
                                color = CyberBlue,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.createNewSession("Normal") }
                            )
                            FeatureCard(
                                title = "ভিশন",
                                icon = Icons.Filled.Image,
                                description = "ছবি বিশ্লেষণ",
                                color = CyberPink,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.createNewSession("Normal") }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FeatureCard(
                                title = "লেখক মোড",
                                icon = Icons.Filled.EditNote,
                                description = "সৃজনশীল লেখা সহায়তা",
                                color = CyberPurple,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.createNewSession("Writer") }
                            )
                            FeatureCard(
                                title = "ডিবাগার",
                                icon = Icons.Filled.Code,
                                description = "কোড সমস্যা সমাধান",
                                color = CyberOrange,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.createNewSession("Debugger") }
                            )
                        }
                    }
                    
                    // Suggestion Chips
                    Text(
                        "দ্রুত পরামর্শ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "একটি গল্প লেখো",
                            "কোড ডিবাগ করো",
                            "আমার সাথে কথা বলো",
                            "পাঠ্যবস্তু সংক্ষিপ্ত করো"
                        ).forEach { suggestion ->
                            SuggestionChip(
                                label = suggestion,
                                onClick = { viewModel.sendMessage(suggestion) }
                            )
                        }
                    }
                }
            } else {
                // Chat Interface
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = false
                    ) {
                        if (currentMessages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "কোনো বার্তা নেই। শুরু করতে কিছু লিখুন!",
                                        color = CyberLightGray
                                    )
                                }
                            }
                        } else {
                            items(currentMessages) { message ->
                                ChatBubble(message)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        if (isLoading) {
                            item {
                                GlowingLoadingIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                StreamingBubble("চিন্তা করছি...")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Input Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
                            .background(CyberDarkCard, RoundedCornerShape(24.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Transparent),
                            placeholder = { Text("বার্তা লিখুন...", color = CyberLightGray) },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            ),
                            singleLine = false
                        )
                        
                        IconButton(
                            onClick = {
                                if (messageInput.text.isNotEmpty()) {
                                    viewModel.sendMessage(messageInput.text)
                                    messageInput = TextFieldValue("")
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = CyberBlue
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAuthDialog) {
        AuthDialog(
            userEmail = userEmail,
            userDisplayName = userDisplayName,
            onLogin = { email, name ->
                viewModel.login(email, name)
                showAuthDialog = false
            },
            onLogout = {
                viewModel.logout()
                showAuthDialog = false
            },
            onDismiss = { showAuthDialog = false }
        )
    }
    
    if (showRenameDialog && selectedSession != null) {
        RenameDialog(
            currentTitle = renameInput.text,
            onRename = { newTitle ->
                viewModel.renameSession(selectedSession!!.id, newTitle)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
            onTitleChange = { renameInput = TextFieldValue(it) }
        )
    }
    
    if (showDeleteDialog && selectedSession != null) {
        DeleteConfirmDialog(
            sessionTitle = selectedSession!!.title,
            onConfirm = {
                viewModel.deleteSession(selectedSession!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }
}

// Dialog Composables
@Composable
fun AuthDialog(
    userEmail: String?,
    userDisplayName: String?,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf(TextFieldValue(userEmail ?: "")) }
    var displayName by remember { mutableStateOf(TextFieldValue(userDisplayName ?: "")) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (userEmail != null) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink)
                ) {
                    Text("লগ আউট করুন")
                }
            } else {
                Button(
                    onClick = { onLogin(email.text, displayName.text) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                ) {
                    Text("লগ ইন করুন")
                }
            }
        },
        title = { Text(if (userEmail != null) "আপনার অ্যাকাউন্ট" else "লগ ইন করুন", color = Color.White) },
        text = {
            if (userEmail != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ইমেল: $userEmail", color = Color.White)
                    Text("নাম: $userDisplayName", color = Color.White)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ইমেল", color = CyberLightGray) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = CyberDarkCard,
                            focusedContainerColor = CyberDarkCard
                        )
                    )
                    TextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("নাম", color = CyberLightGray) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = CyberDarkCard,
                            focusedContainerColor = CyberDarkCard
                        )
                    )
                }
            }
        },
        containerColor = CyberDarkCard,
        textContentColor = Color.White
    )
}

@Composable
fun RenameDialog(
    currentTitle: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(TextFieldValue(currentTitle)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onRename(newTitle.text) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
            ) {
                Text("পরিবর্তন করুন")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGray)
            ) {
                Text("বাতিল করুন")
            }
        },
        title = { Text("চ্যাট নাম পরিবর্তন করুন", color = Color.White) },
        text = {
            TextField(
                value = newTitle,
                onValueChange = {
                    newTitle = it
                    onTitleChange(it.text)
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = CyberDarkCard,
                    focusedContainerColor = CyberDarkCard,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
        },
        containerColor = CyberDarkCard,
        textContentColor = Color.White
    )
}

@Composable
fun DeleteConfirmDialog(
    sessionTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CyberPink)
            ) {
                Text("মুছুন")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGray)
            ) {
                Text("বাতিল করুন")
            }
        },
        title = { Text("চ্যাট মুছুন?", color = Color.White) },
        text = { Text("\"$sessionTitle\" মুছে ফেলতে চান? এটি পুনরুদ্ধার করা যাবে না।", color = Color.White) },
        containerColor = CyberDarkCard,
        textContentColor = Color.White
    )
}

@Composable
fun SupportDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ঠিক আছে", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        title = { Text("সাহায্যের জন্য আমাদের সাথে যোগাযোগ করুন", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "আপনার কোনো প্রশ্ন বা সমস্যা থাকলে আমাদের সাথে যোগাযোগ করুন।",
                    color = Color.White
                )
                Text(
                    "ইমেল: support@psai.com\nফোন: +880 1700-000000",
                    color = CyberBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = CyberDarkCard,
        textContentColor = Color.White
    )
}

// User Info Widget card
@Composable
fun UserProfileCard(
    userEmail: String?,
    userDisplayName: String?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
            .clickable { onLoginClick() },
        colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(CyberPurple, CyberPink)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Avatar icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                if (userEmail != null) {
                    Text(
                        text = "Hello, $userDisplayName!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Cloud backup sync is active",
                        color = CyberBlue,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "PS AI Premium Cloud Sync",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Tap to login and secure your history",
                        color = CyberLightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = if (userEmail != null) Icons.Filled.CloudSync else Icons.Filled.Person,
                contentDescription = "sync status icon",
                tint = if (userEmail != null) CyberBlue else CyberLightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Feature Hub shortcut cards
@Composable
fun FeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(135.dp)
            .border(1.dp, CyberBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = CyberLightGray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Quick chip prompt suggestions
@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CyberGray)
            .clickable { onClick() }
            .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// History session item component
@Composable
fun SessionItem(
    session: ChatSession,
    active: Boolean,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val accentColor = when (session.mode) {
        "Girlfriend" -> CyberPink
        "Writer" -> CyberPurple
        "Debugger" -> CyberBlue
        "Summarizer" -> CyberOrange
        else -> CyberLightGray
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (active) accentColor.copy(alpha = 0.5f) else CyberBorder,
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = CyberRecentCard
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sidebar-accent vertical line indicator (border-l-4 equivalent)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(68.dp)
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (session.mode) {
                            "Girlfriend" -> Icons.Filled.Favorite
                            "Writer" -> Icons.Filled.EditNote
                            "Debugger" -> Icons.Filled.Code
                            "Summarizer" -> Icons.Filled.Summarize
                            else -> Icons.Filled.History
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = session.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when (session.mode) {
                                "Girlfriend" -> "PS-GF Mode"
                                "Writer" -> "Content Writer"
                                "Debugger" -> "Debugger"
                                "Summarizer" -> "Summarizer"
                                else -> "Standard Assistant"
                            },
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (session.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = CyberPurple,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        if (session.isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Starred",
                                tint = CyberGold,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Quick actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onPinClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pin",
                            tint = if (session.isPinned) CyberPurple else CyberLightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onRenameClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Rename",
                            tint = CyberLightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = CyberPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Empty state placeholder
@Composable
fun EmptyHistoryPlaceholder(onNewChat: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = CyberGray,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No saved conversations found",
            color = CyberLightGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple.copy(alpha = 0.3f))
        ) {
            Text("Create New Chat", color = Color.White)
        }
    }
}

// Chat bubbles
@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Image preview if present in message history
        if (message.imageBase64 != null) {
            val bitmap = remember(message.imageBase64) {
                try {
                    val bytes = android.util.Base64.decode(message.imageBase64, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Card(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    // Decoded bitmap rendered cleanly via Coil AsyncImage
                    AsyncImage(
                        model = bitmap,
                        contentDescription = "Vision Attachment",
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        Surface(
            color = if (isUser) CyberPurple else CyberDarkCard,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.Transparent else CyberBorder,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Live stream bubble
@Composable
fun StreamingBubble(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            color = CyberDarkCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .border(1.dp, CyberBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

// Glowing progress loader
@Composable
fun GlowingLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val glowWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(CyberBlue, CyberPurple, CyberPink),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f)
                    )
                )
            }
    )
}
