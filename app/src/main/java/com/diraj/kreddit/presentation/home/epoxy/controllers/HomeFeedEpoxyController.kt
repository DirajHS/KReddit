package com.diraj.kreddit.presentation.home.epoxy.controllers

import android.os.Handler
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.presentation.home.epoxy.models.ErrorEpoxyViewModel_
import com.diraj.kreddit.presentation.home.epoxy.models.FeedEpoxyViewModel_
import com.diraj.kreddit.presentation.home.epoxy.models.LoadingEpoxyViewModel_

class HomeFeedEpoxyController(
    private val retryClickListener: View.OnClickListener,
    asyncDiffHandler: Handler): PagedListEpoxyController<RedditObject>(
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

    override fun buildItemModel(currentPosition: Int, item: RedditObject?): EpoxyModel<*> {
        return item?.let {
            FeedEpoxyViewModel_()
                .id("ItemBindingModel_$currentPosition")
                .redditObject(item)
        } ?: run {
            LoadingEpoxyViewModel_()
                .id("LoadingEpoxyViewModel_$currentPosition")
        }
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
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