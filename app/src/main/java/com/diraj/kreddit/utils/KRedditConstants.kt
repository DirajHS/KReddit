package com.diraj.kreddit.utils

import com.diraj.kreddit.BuildConfig

object KRedditConstants {

    //Feed constants
    const val REDDIT_OBJECT_PARCELABLE_KEY = "redditObject"
    const val FEED_DETAILS_MOTION_PROGRESS_KEY = "feed_details_motion_progress_key"
    const val FEED_THUMBNAIL_URL_REPLACEMENT_KEY = "amp;"
    const val FEED_COMMENT_KIND = "t1"
    
    //Feed Vote constants
    const val DIR = "dir"
    const val ID = "id"
    
    //Authentication constants
    const val ACCESS_TOKEN_KEY = "access_token"
    const val REFRESH_TOKEN_KEY = "refresh_token"
    const val USER_AGENT_KEY = "User-Agent"
    const val USER_AGENT_VALUE = "KReddit"
    const val STATE = "KREDDIT_LOGIN"
    const val AUTH_URL = "https://www.reddit.com/api/v1/authorize.compact?client_id=${BuildConfig.REDDIT_CLIENT_ID}" +
            "&response_type=code&state=$STATE&redirect_uri=${BuildConfig.REDDIT_REDIRECT_URI}&" +
            "duration=permanent&scope=${BuildConfig.REDDIT_LOGIN_SCOPES}"
    const val AUTHORIZATION = "Authorization"
    const val AUTHORIZATION_HEADER_PREFIX_BEARER="Bearer"
    const val ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX = "Basic"
    const val MEDIA_TYPE = "application/x-www-form-urlencoded"

    const val CLICKED_LIKE = "clicked_like"
    const val CLICKED_DISLIKE = "clicked_dislike"
}