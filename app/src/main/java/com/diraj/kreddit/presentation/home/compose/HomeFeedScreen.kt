package com.diraj.kreddit.presentation.home.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.paging.PagedList
import com.diraj.kreddit.R
import com.diraj.kreddit.data.models.RedditObjectDataWithoutReplies
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.utils.DataLayerConstants
import com.diraj.kreddit.presentation.home.fragment.HomeFeedFragmentDirections
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import retrofit2.HttpException

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeFeedScreen(
    navController: NavController,
    homeFeedViewModel: HomeFeedViewModel,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    // Use LocalConfiguration to get screen width for more dynamic adaptation if needed,
    // but R.bool.isTablet is fine for distinct phone/tablet layouts.
    val isTablet = context.resources.getBoolean(R.bool.isTablet)

    val pagedFeedListState = homeFeedViewModel.pagedFeedList.observeAsState()
    val feedApiState by homeFeedViewModel.getFeedApiState().observeAsState()

    val isRefreshing = feedApiState is RedditResponse.Loading && !homeFeedViewModel.listIsEmpty()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { homeFeedViewModel.refresh() }
    )

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.35f) // Left pane for the list
                    .pullRefresh(pullRefreshState)
            ) {
                FeedListContent(
                    homeFeedViewModel = homeFeedViewModel,
                    pagedFeedListState = pagedFeedListState,
                    feedApiState = feedApiState,
                    sharedViewModel = sharedViewModel,
                    navController = navController,
                    isTablet = true, // Pass isTablet for navigation logic if needed inside
                    modifier = Modifier.fillMaxSize()
                )
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.65f) // Right pane for details
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), // Placeholder background
                contentAlignment = Alignment.Center
            ) {
                Text("Details View Placeholder", style = MaterialTheme.typography.headlineSmall)
            }
        }
    } else {
        // Phone layout (existing logic)
        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            FeedListContent(
                homeFeedViewModel = homeFeedViewModel,
                pagedFeedListState = pagedFeedListState,
                feedApiState = feedApiState,
                sharedViewModel = sharedViewModel,
                navController = navController,
                isTablet = false,
                modifier = Modifier.fillMaxSize()
            )
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FeedListContent(
    homeFeedViewModel: HomeFeedViewModel,
    pagedFeedListState: State<PagedList<RedditObjectDataWithoutReplies>?>,
    feedApiState: RedditResponse?,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // Get context for resources

    Column(modifier = modifier) {
        val currentFeedApiState = feedApiState
        when {
            homeFeedViewModel.listIsEmpty() && currentFeedApiState is RedditResponse.Loading -> {
                FullScreenLoadingIndicator()
            }
            homeFeedViewModel.listIsEmpty() && currentFeedApiState is RedditResponse.Error -> {
                FullScreenErrorView(
                    errorText = getErrorText(currentFeedApiState, context.resources),
                    onRetry = { homeFeedViewModel.retry() }
                )
            }
            else -> {
                val pagedList = pagedFeedListState.value
                if (pagedList != null && pagedList.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(), // Fill the allocated space
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(pagedList.size) { index ->
                            val feedItem = pagedList[index]
                            if (feedItem != null) {
                                FeedListItem(
                                    data = mapRedditObjectToFeedItemData(
                                        feedItem,
                                        onUpvoteClicked = {
                                            sharedViewModel.vote(
                                                DataLayerConstants.CLICKED_LIKE,
                                                feedItem
                                            )
                                        },
                                        onDownvoteClicked = {
                                            sharedViewModel.vote(
                                                DataLayerConstants.CLICKED_DISLIKE,
                                                feedItem
                                            )
                                        },
                                        onCommentsClicked = { /* Not directly handled in FeedListItem, item click below */ },
                                        onItemClicked = {
                                            // Navigate to details
                                            if (isTablet) {
                                                // For tablet, the navigation action might be different
                                                // (e.g., updating a different NavHost or a SharedViewModel state)
                                                // For now, let's assume it might still use the same action
                                                // but the NavController might be for a different NavHost if using nested NavHosts.
                                                // This part depends heavily on the tablet navigation strategy.
                                                // If the same NavController handles both panes, this is fine.
                                                // If HomeFeedDetailsFragment is shown in the right pane,
                                                // this click should trigger that update.
                                                val action = HomeFeedFragmentDirections
                                                    .actionHomeFeedFragmentToHomeFeedDetailsFragment(feedItem)
                                                navController.navigate(action)
                                            } else {
                                                val action = HomeFeedFragmentDirections
                                                    .actionHomeFeedFragmentToHomeFeedDetailsFragment(feedItem)
                                                navController.navigate(action)
                                            }
                                        }
                                    )
                                )
                            }
                        }
                        // Loading/Error state at the bottom of the list
                        if (currentFeedApiState is RedditResponse.Loading && !homeFeedViewModel.listIsEmpty()) {
                            item { ListItemLoadingIndicator() }
                        } else if (currentFeedApiState is RedditResponse.Error && !homeFeedViewModel.listIsEmpty()) {
                            item {
                                ListItemErrorView(
                                    errorText = getErrorText(currentFeedApiState, context.resources),
                                    onRetry = { homeFeedViewModel.retry() }
                                )
                            }
                        }
                    }
                } else if (pagedList != null && pagedList.isEmpty() && currentFeedApiState is RedditResponse.Success<*>) {
                     FullScreenMessage(message = stringResource(id = R.string.no_feed_data))
                }
            }
        }
    }
}

fun mapRedditObjectToFeedItemData(
    redditObject: RedditObjectDataWithoutReplies,
    onUpvoteClicked: () -> Unit,
    onDownvoteClicked: () -> Unit,
    onCommentsClicked: () -> Unit, // Retained for consistency, though item click handles navigation
    onItemClicked: () -> Unit
): FeedItemData {
    return FeedItemData(
        author = redditObject.author ?: "Unknown",
        subreddit = redditObject.subreddit ?: "Unknown",
        title = redditObject.title ?: "",
        imageUrl = redditObject.thumbnail.takeIf { it != "self" && it != "default" && it != "image" && it != "spoiler"},
        upvotes = redditObject.ups?.toString() ?: "0",
        commentsCount = redditObject.num_comments?.toString() ?: "0",
        time = redditObject.created_utc?.let { com.diraj.kreddit.utils.Utils.getElapsedTime(it) } ?: "",
        onUpvoteClicked = onUpvoteClicked,
        onDownvoteClicked = onDownvoteClicked,
        onCommentsClicked = onItemClicked, // Map item click to comments click for FeedListItem
        // Add onItemClicked to FeedItemData if you want a separate click area
        // For now, the entire card click is handled by onCommentsClicked in FeedListItem
        // which we are repurposing here for overall item click.
    )
}

@Composable
fun FullScreenLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ListItemLoadingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun FullScreenErrorView(errorText: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = errorText ?: stringResource(id = R.string.generic_error_string), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry_btn_text))
        }
    }
}

@Composable
fun ListItemErrorView(errorText: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = errorText ?: stringResource(id = R.string.generic_error_string), textAlign = TextAlign.Center, color = Color.Red)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry_btn_text))
        }
    }
}

@Composable
fun FullScreenMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    }
}


fun getErrorText(redditResponse: RedditResponse.Error, resources: android.content.res.Resources): String {
    return when (val ex = redditResponse.ex) {
        is HttpException -> {
            "${ex.code()}: ${ex.message()}" // ex.message is nullable, use message()
        }
        else -> {
            resources.getString(R.string.generic_error_string)
        }
    }
}

// --- Preview ---
class MockHomeFeedViewModel : HomeFeedViewModel(
    homeFeedBoundaryCallback = MockHomeFeedBoundaryCallback(),
    homeFeedRepo = MockHomeFeedRepo(),
    kredditDB = MockKredditDB() // You'll need to define these mock classes
) {
    override val pagedFeedList: LiveData<PagedList<RedditObjectDataWithoutReplies>>
        get() = MutableLiveData() // Provide a mock PagedList if needed for preview

    override fun getFeedApiState(): LiveData<RedditResponse> {
        return MutableLiveData(RedditResponse.SuccessNoData) // Or other states for preview
    }
    override fun listIsEmpty(): Boolean = true
    override fun retry() {}
    override fun refresh() {}
}

class MockSharedViewModel : SharedViewModel(voteRepo = MockVoteRepo()) {
    override fun vote(clickedBtnType: String, redditObjectDataWithoutReplies: RedditObjectDataWithoutReplies) {}
}

// Define Mock implementations for dependencies of ViewModels if not already available
// For brevity, these are simplified. You'd need to mock their methods as required.
class MockHomeFeedBoundaryCallback : com.diraj.kreddit.data.repo.home.HomeFeedBoundaryCallback(
    appExecutors = com.diraj.kreddit.data.repo.AppExecutors(),
    kredditDB = MockKredditDB(),
    homeFeedAPIService = MockHomeFeedAPIService()
) {
    override val feedApiStateLiveData: MutableLiveData<RedditResponse> = MutableLiveData(RedditResponse.SuccessNoData)
    override fun retry() {}
}
class MockHomeFeedRepo(private val kredditDB: com.diraj.kreddit.data.db.KRedditDB = MockKredditDB()) : com.diraj.kreddit.data.repo.home.HomeFeedRepo(kredditDB, MockHomeFeedAPIService()) {
    suspend override fun refresh(feedApiStateLiveData: MutableLiveData<RedditResponse>) {}
}
class MockVoteRepo : com.diraj.kreddit.data.repo.vote.VoteRepo(MockKredditDB(), MockVoteAPIService()) {
    suspend override fun doVote(voteModel: com.diraj.kreddit.data.models.VoteModel?): RedditResponse { return RedditResponse.SuccessNoData }
    suspend override fun updateDBAfterVote(redditObjectDataWithoutReplies: RedditObjectDataWithoutReplies) {}
}

// Mock KRedditDB and its DAO (very simplified)
class MockKredditDB : com.diraj.kreddit.data.db.KRedditDB() {
    override fun kredditPostsDAO(): com.diraj.kreddit.data.db.dao.KRedditPostsDAO = MockKredditPostsDAO()
    override fun kredditUserDAO(): com.diraj.kreddit.data.db.dao.KRedditUserDAO = MockKredditUserDAO()
    override fun kredditRemoteKeysDAO(): com.diraj.kreddit.data.db.dao.KRedditRemoteKeysDAO = MockKredditRemoteKeysDAO()
    override fun createOpenHelper(config: androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration?): androidx.sqlite.db.SupportSQLiteOpenHelper { TODO("Not yet implemented") }
    override fun createInvalidationTracker(): androidx.room.InvalidationTracker { TODO("Not yet implemented") }
    override fun clearAllTables() {}
}
class MockKredditPostsDAO : com.diraj.kreddit.data.db.dao.KRedditPostsDAO {
    override fun insertPosts(posts: List<RedditObjectDataWithoutReplies>) {}
    override fun posts(): androidx.paging.DataSource.Factory<Int, RedditObjectDataWithoutReplies> { TODO("Not yet implemented for preview") }
    override fun getPost(name: String): LiveData<RedditObjectDataWithoutReplies> { TODO("Not yet implemented for preview") }
    override fun updatePost(redditObjectDataWithoutReplies: RedditObjectDataWithoutReplies) {}
    override fun clearPosts() {}
}
class MockKredditUserDAO : com.diraj.kreddit.data.db.dao.KRedditUserDAO {
    override fun insertUser(user: com.diraj.kreddit.data.models.account.KRedditUserAccount): Long { return 0L }
    override fun getUser(): com.diraj.kreddit.data.models.account.KRedditUserAccount? { return null }
    override fun deleteUser() {}
}
class MockKredditRemoteKeysDAO : com.diraj.kreddit.data.db.dao.KRedditRemoteKeysDAO {
    override suspend fun insertAll(remoteKey: List<com.diraj.kreddit.data.models.db.KRedditRemoteKey>) {}
    override suspend fun remoteKeysPostId(postId: String): com.diraj.kreddit.data.models.db.KRedditRemoteKey? { return null }
    override suspend fun clearRemoteKeys() {}
}

// Mock API Services (very simplified)
class MockHomeFeedAPIService : com.diraj.kreddit.data.network.HomeFeedAPIService {
    override suspend fun getFeed(subreddit: String, after: String?, limit: Int): com.diraj.kreddit.data.models.RedditListing {
        return com.diraj.kreddit.data.models.RedditListing(com.diraj.kreddit.data.models.RedditListingData("", null, null, emptyList(), null))
    }
    override suspend fun getPopularFeed(after: String?, limit: Int): com.diraj.kreddit.data.models.RedditListing {
        return com.diraj.kreddit.data.models.RedditListing(com.diraj.kreddit.data.models.RedditListingData("", null, null, emptyList(), null))
    }
    override suspend fun getNextFeedPage(subreddit: String, after: String?, limit: Int): com.diraj.kreddit.data.models.RedditListing {
        return com.diraj.kreddit.data.models.RedditListing(com.diraj.kreddit.data.models.RedditListingData("", null, null, emptyList(), null))
    }
}
class MockVoteAPIService : com.diraj.kreddit.data.network.VoteAPIService {
    override suspend fun voteAsync(dir: Int, id: String, rank: Int): retrofit2.Response<Unit> { return retrofit2.Response.success(Unit) }
}


@Preview(showBackground = true, name = "HomeFeedScreen Empty/Loading")
@Composable
fun HomeFeedScreenPreview_Loading() {
    val mockNavController = NavController(LocalContext.current)
    val mockHomeFeedViewModel = MockHomeFeedViewModel()
    // Simulate loading state
    (mockHomeFeedViewModel.getFeedApiState() as MutableLiveData).postValue(RedditResponse.Loading)

    HomeFeedScreen(
        navController = mockNavController,
        homeFeedViewModel = mockHomeFeedViewModel,
        sharedViewModel = MockSharedViewModel()
    )
}

@Preview(showBackground = true, name = "HomeFeedScreen Error")
@Composable
fun HomeFeedScreenPreview_Error() {
    val mockNavController = NavController(LocalContext.current)
    val mockHomeFeedViewModel = MockHomeFeedViewModel()
    // Simulate error state
    (mockHomeFeedViewModel.getFeedApiState() as MutableLiveData).postValue(RedditResponse.Error(Exception("Preview Error")))

    HomeFeedScreen(
        navController = mockNavController,
        homeFeedViewModel = mockHomeFeedViewModel,
        sharedViewModel = MockSharedViewModel()
    )
}

// For a preview with data, you would need to make MockKredditPostsDAO().posts() return a mock DataSource.Factory
// and HomeFeedViewModel.pagedFeedList to be populated with a mock PagedList. This is more involved.
// For now, previews focus on empty/loading/error states.

@Preview(showBackground = true, name = "HomeFeedScreen No Data")
@Composable
fun HomeFeedScreenPreview_NoData() {
    val mockNavController = NavController(LocalContext.current)
    val mockHomeFeedViewModel = MockHomeFeedViewModel()
    (mockHomeFeedViewModel.getFeedApiState() as MutableLiveData).postValue(RedditResponse.SuccessNoData)
     // Simulate empty list
    (mockHomeFeedViewModel.pagedFeedList as MutableLiveData).postValue(null)


    HomeFeedScreen(
        navController = mockNavController,
        homeFeedViewModel = mockHomeFeedViewModel,
        sharedViewModel = MockSharedViewModel()
    )
}
