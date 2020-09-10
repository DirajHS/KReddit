package com.diraj.kreddit.presentation.home.epoxy.controllers

import android.os.Handler
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.bumptech.glide.RequestManager
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.epoxy.models.ErrorEpoxyViewModel_
import com.diraj.kreddit.presentation.home.epoxy.models.FeedEpoxyViewModel_
import com.diraj.kreddit.presentation.home.epoxy.models.LoadingEpoxyViewModel_
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener

class HomeFeedEpoxyController(
    private val retryClickListener: View.OnClickListener,
    asyncDiffHandler: Handler, private val feedClickListener: IFeedClickListener,
    private val glideRequestManager: RequestManager): PagedListEpoxyController<RedditObjectData>(
    defaultModelBuildingHandler, asyncDiffHandler) {

    private var isError: Boolean = false

    var error: String? = ""
        set(value) {
            field = value?.let {
                isError = true
                it
            } ?: run {
                isError = false
                null
            }
            if (isError) {
                requestModelBuild()
            }
        }

    var isLoading = false
        set(value) {
            field = value
            if (field) {
                requestModelBuild()
            }
        }

    override fun buildItemModel(currentPosition: Int, item: RedditObjectData?): EpoxyModel<*> {
        /*
        We can construct models based on position for different types from RedditObjectData here.
        For now we are showing only image feed, later when we integrate ExoPlayer to play GIF's and
        videos in feed, we can create a new model for that. OfCourse, ExoPlayer has to instantiated
        separately as a part of EpoxyRecyclerView to prevent creating multiple players and instead
        attach the player to the model using PlayerView, to keep the smooth scroll.
         */
        return item?.let {
            FeedEpoxyViewModel_()
                .id("ItemBindingModel_$currentPosition")
                .redditObject(item)
                .feedItemClickListener(feedClickListener)
                .glideRequestManager(glideRequestManager)
        } ?: run {
            LoadingEpoxyViewModel_()
                .id("LoadingEpoxyViewModel_$currentPosition")
        }
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        /*
        Once the models are built, we can add additional headers or footers at certain positions if required.
        For now we are showing only loader and error info, but assume if there is a group of feeds
        such as cute dogs, we can add a text header in between the given list with a text header and a puppy emoji :-)
        This kind of approach helps where these models are not part of actual list, but enhance the UI flow, thereby
        when list updates, Diff can happen independent of models built above and adding the required headers here to
        such models.
         */
        when {
            isError -> {
                super.addModels(
                    models.plus(
                        ErrorEpoxyViewModel_()
                            .id("ErrorEpoxyViewModel_")
                            .errorMessage(error ?: "")
                            .retryClickListener(retryClickListener)
                    ).filter { it !is LoadingEpoxyViewModel_ }
                )
            }
            isLoading -> {
                super.addModels(
                    models.plus(
                        LoadingEpoxyViewModel_()
                            .id("LoadingEpoxyViewModel_")
                    ).distinct()
                )
            }
            else -> {
                super.addModels(models.distinct())
            }
        }
    }
}
