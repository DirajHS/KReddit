package com.diraj.kreddit.data.models

import android.os.Parcelable
import com.diraj.kreddit.data.utils.RedditObjectDataSerializer
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable(with = RedditObjectDataSerializer::class)
sealed class RedditObjectData: Parcelable {

    @Parcelize
    data class WithReplies(val redditObjectDataWithReplies: RedditObjectDataWithReplies)
        : RedditObjectData(), Parcelable

    @Parcelize
    data class WithoutReplies(val redditObjectDataWithoutReplies: RedditObjectDataWithoutReplies)
        : RedditObjectData(), Parcelable
}
