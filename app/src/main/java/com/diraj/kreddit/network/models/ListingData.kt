package com.diraj.kreddit.network.models

import android.os.Parcelable
import androidx.room.TypeConverters
import com.diraj.kreddit.presentation.home.db.typeconverters.RedditObjectConverter
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@TypeConverters(RedditObjectConverter::class)
@Serializable
data class ListingData(
    val after: String?,
    val before: String?,
    val children: List<RedditObject>
) : Parcelable
