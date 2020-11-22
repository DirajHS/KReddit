package com.diraj.kreddit.presentation.home.fragment

import android.view.View
import com.diraj.kreddit.data.models.RedditObjectData

interface IFeedClickListener {

    fun onFeedItemClicked(view: View, redditObject: RedditObjectData.RedditObjectDataWithoutReplies)
}