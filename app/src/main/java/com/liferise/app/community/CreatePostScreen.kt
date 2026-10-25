package com.liferise.app.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.liferise.app.R
import com.liferise.app.core.designsystem.LifeRiseCard
import com.liferise.app.core.designsystem.LifeRiseTopAppBar
import com.liferise.app.domain.model.PostVisibility
import com.liferise.app.photos.PhotoCompressor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: SocialViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var content by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var hashtagsInput by remember { mutableStateOf("#LifeRise, #Growth") }
    var selectedVisibility by remember { mutableStateOf(PostVisibility.PUBLIC) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val compressResult = PhotoCompressor.compressImage(context, uri)
                if (compressResult.isSuccess) {
                    selectedPhotoUri = uri
                    errorMessage = null
                } else {
                    errorMessage = compressResult.exceptionOrNull()?.message ?: "Failed to process photo."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = "Create Social Post",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (content.isBlank()) {
                                errorMessage = "Post content cannot be empty."
                                return@Button
                            }
                            isUploading = true
                            val tags = hashtagsInput.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .map { if (!it.startsWith("#")) "#$it" else it }

                            viewModel.createPost(
                                content = content,
                                caption = caption,
                                imageUrl = selectedPhotoUri?.toString(),
                                hashtags = tags,
                                visibility = selectedVisibility
                            ) {
                                isUploading = false
                                onNavigateBack()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isUploading && content.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.action_share), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Privacy Boundary Note
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Shield: Private diary entries and personal scores are NEVER shared to the community. Only what you write in this post is visible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Post Text
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    errorMessage = null
                },
                label = { Text("What's on your mind? *") },
                placeholder = { Text("Share an achievement, reflection, or encouragement (English or ಕನ್ನಡ)...") },
                minLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Caption
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Caption (Optional)") },
                placeholder = { Text("e.g. Day 10 completed! 🚀") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Photo Attachment Section
            LifeRiseCard(elevation = 1.dp) {
                Text("Attach Photo (Max 5 MB)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedPhotoUri != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Selected photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        IconButton(
                            onClick = { selectedPhotoUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            }

            // Visibility Selector
            Text("Post Audience", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostVisibility.values().forEach { v ->
                    FilterChip(
                        selected = selectedVisibility == v,
                        onClick = { selectedVisibility = v },
                        label = { Text(v.label) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Hashtags
            OutlinedTextField(
                value = hashtagsInput,
                onValueChange = { hashtagsInput = it },
                label = { Text("Hashtags") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
