package com.diraj.kreddit.presentation.home.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class FeedItemData(
    val author: String,
    val subreddit: String,
    val title: String,
    val imageUrl: String?, // Nullable if image is optional
    val upvotes: String,
    val commentsCount: String,
    val time: String,
    val onUpvoteClicked: () -> Unit,
    val onDownvoteClicked: () -> Unit,
    val onCommentsClicked: () -> Unit
)

@Composable
fun FeedListItem(data: FeedItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
        ) {
            // Feed Info (Subreddit and Author)
            Text(
                text = "r/${data.subreddit} u/${data.author}",
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                maxLines = 1
            )

            // Title
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            // Image
            data.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Feed image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.6f)
                        .padding(top = 2.dp),
                    contentScale = ContentScale.FitCenter
                )
            }

            // Feed Actions
            FeedActions(data = data)
        }
    }
}

@Composable
fun FeedActions(data: FeedItemData) {
    BoxWithConstraints {
        val screenWidth = maxWidth // from BoxWithConstraints
        if (screenWidth < 600.dp) {
            // Phone layout for actions
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = data.onUpvoteClicked) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Upvote", tint = Color(0xFF80000000))
                        }
                        Text(
                            text = data.upvotes,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        IconButton(onClick = data.onDownvoteClicked, modifier = Modifier.padding(start = 4.dp)) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Downvote", tint = Color(0xFF80000000))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = data.onCommentsClicked) {
                            Icon(Icons.Filled.Comment, contentDescription = "Comments", tint = Color(0xFFFF8717))
                        }
                        Text(
                            text = data.commentsCount,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = data.time,
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, end = 8.dp) // Align to end, similar to original implicit layout
                )
            }
        } else {
            // Tablet layout for actions (sw600dp)
            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Upvotes and Downvotes on the start
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = data.onUpvoteClicked) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Upvote", tint = Color(0xFF80000000))
                        }
                        Text(
                            text = data.upvotes,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        IconButton(onClick = data.onDownvoteClicked, modifier = Modifier.padding(start = 4.dp)) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Downvote", tint = Color(0xFF80000000))
                        }
                    }

                    // Comments at the end
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = data.onCommentsClicked) {
                            Icon(Icons.Filled.Comment, contentDescription = "Comments", tint = Color(0xFFFF8717))
                        }
                        Text(
                            text = data.commentsCount,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                // Time below, aligned to start
                Text(
                    text = data.time,
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.Start)
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Phone FeedListItem Preview")
@Composable
fun DefaultFeedListItemPreview() {
    val sampleData = FeedItemData(
        author = "Diraj",
        subreddit = "Science",
        title = "Sample reddit title which is quite long and should wrap or elide",
        imageUrl = "https://www.example.com/image.jpg", // Replace with a real image URL for preview if possible
        upvotes = "201k",
        commentsCount = "86",
        time = "2h",
        onUpvoteClicked = {},
        onDownvoteClicked = {},
        onCommentsClicked = {}
    )
    MaterialTheme { // Wrap in MaterialTheme for previews
        FeedListItem(data = sampleData)
    }
}

@Preview(showBackground = true, name = "Phone FeedListItem - No Image")
@Composable
fun FeedListItemPreviewWithoutImage() {
    val sampleData = FeedItemData(
        author = "AnotherUser",
        subreddit = "compose",
        title = "This is a post without an image",
        imageUrl = null,
        upvotes = "1.2k",
        commentsCount = "23",
        time = "10m",
        onUpvoteClicked = {},
        onDownvoteClicked = {},
        onCommentsClicked = {}
    )
    MaterialTheme {
        FeedListItem(data = sampleData)
    }
}

@Preview(showBackground = true, name = "Tablet FeedListItem Preview", widthDp = 700)
@Composable
fun TabletFeedListItemPreview() {
    val sampleData = FeedItemData(
        author = "TabletUser",
        subreddit = "androiddev",
        title = "Showing FeedListItem on a wider screen (tablet)",
        imageUrl = "https://www.example.com/image_tablet.jpg",
        upvotes = "500",
        commentsCount = "60",
        time = "1hr ago",
        onUpvoteClicked = {},
        onDownvoteClicked = {},
        onCommentsClicked = {}
    )
    MaterialTheme {
        FeedListItem(data = sampleData)
    }
}
