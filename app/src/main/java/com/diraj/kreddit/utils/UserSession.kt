package com.diraj.kreddit.utils

import android.text.TextUtils
import com.diraj.kreddit.utils.KRedditConstants.ACCESS_TOKEN_KEY
import com.diraj.kreddit.utils.KRedditConstants.REFRESH_TOKEN_KEY
import com.tencent.mmkv.MMKV
import timber.log.Timber

object UserSession {

    private val encodedSharedPrefs = MMKV.defaultMMKV()

    var accessToken: String? = null
        @Synchronized
        get() {
            if (field == null) {
                field = encodedSharedPrefs.decodeString(ACCESS_TOKEN_KEY)
            }
            return field
        }
        @Synchronized
        private set(value) {
            Timber.d("setting value: $value")
            field = value
            encodedSharedPrefs.encode(ACCESS_TOKEN_KEY, value)
        }

    var refreshToken: String? = null
        @Synchronized
        get() {
            if (field == null) {
                field = encodedSharedPrefs.decodeString(REFRESH_TOKEN_KEY)
            }
            return field
        }
        @Synchronized
        private set(value) {
            field = value
            encodedSharedPrefs.encode(REFRESH_TOKEN_KEY, value)
        }


    val isOpen: Boolean
        get() = !TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(refreshToken)

    fun open(accessToken: String, refreshToken: String) {
        UserSession.accessToken = accessToken
        UserSession.refreshToken = refreshToken
    }

    fun close() {
        if (!isOpen) {
            return
        }
        accessToken = null
        refreshToken = null
    }
}