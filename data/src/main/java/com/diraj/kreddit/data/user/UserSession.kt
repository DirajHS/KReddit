package com.diraj.kreddit.data.user

import android.text.TextUtils
import com.diraj.kreddit.data.utils.DataLayerConstants.ACCESS_TOKEN_KEY
import com.diraj.kreddit.data.utils.DataLayerConstants.REFRESH_TOKEN_KEY
import com.tencent.mmkv.MMKV
import timber.log.Timber

object UserSession {

    private val encodedSharedPrefs = MMKV.defaultMMKV()

    var accessToken: String? = null
        get() {
            if (field == null) {
                field = encodedSharedPrefs.decodeString(ACCESS_TOKEN_KEY)
            }
            return field
        }
        private set(value) {
            Timber.d("setting value: $value")
            field = value
            encodedSharedPrefs.encode(ACCESS_TOKEN_KEY, value)
        }

    var refreshToken: String? = null
        get() {
            if (field == null) {
                field = encodedSharedPrefs.decodeString(REFRESH_TOKEN_KEY)
            }
            return field
        }
        private set(value) {
            field = value
            encodedSharedPrefs.encode(REFRESH_TOKEN_KEY, value)
        }


    val isOpen: Boolean
        @Synchronized
        get() = !TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(refreshToken)

    @Synchronized
    fun open(accessToken: String, refreshToken: String) {
        UserSession.accessToken = accessToken
        UserSession.refreshToken = refreshToken
    }

    @Synchronized
    fun close() {
        if (!isOpen) {
            return
        }
        accessToken = null
        refreshToken = null
    }
}