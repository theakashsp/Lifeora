package com.liferise.app.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liferise.app.R
import com.liferise.app.core.designsystem.*
import com.liferise.app.domain.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: SocialViewModel,
    onNavigateToCreatePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            LifeRiseTopAppBar(
                title = stringResource(R.string.community_title),
                subtitle = "Empowering growth together"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePost,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create post")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Demo Mode Notice Banner
            if (viewModel.isDemoMode) {
                Surface(
                    color = WarningAmberLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.community_demo_badge),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "Running in offline demo mode. To connect Supabase, provide credentials in local.properties. Full SQL schema provided in backend/supabase/migrations/.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }

            if (posts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "No Posts Yet",
                    description = "Be the first to share an inspiring milestone or positive encouragement with the community.",
                    actionLabel = "Share an Update",
                    onActionClick = onNavigateToCreatePost,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostItemView(
                            post = post,
                            isAuthorMe = post.authorId == currentUser?.id,
                            onLikeToggle = { viewModel.toggleLike(post.id) },
                            onDelete = { viewModel.deletePost(post.id) },
                            onReport = { reason -> viewModel.reportPost(post.id, reason) },
                            onAddComment = { text -> viewModel.addComment(post.id, text) }
                        )
                    }
                }
            }
        }
    }
}
