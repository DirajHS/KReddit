package com.diraj.kreddit.presentation.home.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.RedditObjectData
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException
import javax.inject.Inject

@ExperimentalPagingApi
class PostsRemoteMediator@Inject constructor(private val kRedditDB: KRedditDB,
                                             private val kredditRetrofit: Retrofit)
    : RemoteMediator<Int, RedditObjectData.RedditObjectDataWithoutReplies>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RedditObjectData.RedditObjectDataWithoutReplies>
    ): MediatorResult {
        try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = kRedditDB.withTransaction {
                        kRedditDB.kredditPostsDAO().getNextKey()
                    }
                    remoteKey?.name
                }
            }

            val homeAPIService = kredditRetrofit.create(RedditAPIService::class.java)
            val feedData = homeAPIService.getHomeFeed(
                after = loadKey,
                limit = when (loadType) {
                    LoadType.REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                }
            )

            val items = feedData.data.children
            kRedditDB.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    kRedditDB.kredditPostsDAO().deleteAllPosts()
                }
                insertFeed(feedData)
            }

            return MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private fun insertFeed(baseModel: BaseModel) {
        val redditFeedList = mutableListOf<RedditObjectData.RedditObjectDataWithoutReplies>()
        val start = kRedditDB.kredditPostsDAO().getNextIndexInReddit()
        baseModel.data.children.mapIndexed { index, redditObject ->
            val redditObjectData = (redditObject.data
                    as RedditObjectData.RedditObjectDataWithoutReplies).copy(indexInResponse = start + index)
            redditObjectData
        }.forEach {
            redditFeedList.add(it)
        }
        kRedditDB.kredditPostsDAO().insert(redditFeedList)
    }

}