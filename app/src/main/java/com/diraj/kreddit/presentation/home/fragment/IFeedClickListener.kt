package com.diraj.kreddit.presentation.home.fragment

import android.view.View
import com.diraj.kreddit.network.models.RedditObjectData

interface IFeedClickListener {

    fun onFeedItemClicked(view: View, redditObject: RedditObjectData.RedditObjectDataWithoutReplies)
}