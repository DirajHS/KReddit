package com.diraj.kreddit.network

import android.util.Base64
import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.di.DaggerTestComponent
import com.diraj.kreddit.di.TestNetworkModule
import com.diraj.kreddit.network.models.AccessTokenModel
import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION
import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION_HEADER_PREFIX_BEARER
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_KEY
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_VALUE
import com.diraj.kreddit.utils.UserSession
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import retrofit2.Retrofit
import java.io.File
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named


class NetworkLayerTest {

    @field:Inject
    lateinit var mockWebServer: MockWebServer

    @field:Inject
    lateinit var kredditRetrofit: Retrofit

    @field:[Inject Named("Authenticator")]
    lateinit var authenticatorRetrofit: Retrofit

    private lateinit var redditAPIService: RedditAPIService

    private lateinit var userProfileResponse: String

    private lateinit var homeFeedResponse: String

    private lateinit var mockedMMKVObject: MockedStatic<MMKV>

    @Before
    fun setUp() {
        DaggerTestComponent.builder().appModuleForTest(TestNetworkModule()).build().inject(this)
        redditAPIService = kredditRetrofit.create(RedditAPIService::class.java)

        val mockedMMKV = mock(MMKV::class.java)
        mockedMMKVObject = mockStatic(MMKV::class.java)
        `when`(MMKV.defaultMMKV()).thenReturn(mockedMMKV)

        mockkObject(UserSession)
        every { UserSession.accessToken } returns "AccessToken"
        every { UserSession.refreshToken } returns "RefreshToken"
    }

    @After
    fun tearDown() {
        unmockkObject(UserSession)
        mockedMMKVObject.close()
    }

    @Test
    fun `When succeed with valid data, Then response is parsed`() = runBlocking {
        val homeFeedResponseBuilder = StringBuilder()
        File("../app/src/main/res/raw/home_feed_response.json")
            .readLines()
            .forEach {
                homeFeedResponseBuilder.append(it)
            }
        homeFeedResponse = homeFeedResponseBuilder.toString()

        val mockedHomeFeedAPIResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(homeFeedResponse)
        mockWebServer.enqueue(mockedHomeFeedAPIResponse)

        val homeFeedAPIResponse = redditAPIService.getHomeFeed(after = null, limit = 25)
        mockWebServer.takeRequest()

        assert(homeFeedAPIResponse.data.children.size == 25)
    }

    @Test
    fun `When a call is done, Then auth header is added`() = runBlocking {
        val userProfileResponseBuilder = StringBuilder()
        File("../app/src/main/res/raw/user_profile_response.json")
            .readLines()
            .forEach {
                userProfileResponseBuilder.append(it)
            }
        userProfileResponse = userProfileResponseBuilder.toString()

        val mockedUserResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(userProfileResponse)

        mockWebServer.enqueue(mockedUserResponse)

        redditAPIService.getCurrentUserInfo()
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(USER_AGENT_KEY), `is`(USER_AGENT_VALUE))
        assertThat(
            recordedRequest.getHeader(AUTHORIZATION),
            `is`("$AUTHORIZATION_HEADER_PREFIX_BEARER ${UserSession.accessToken}")
        )
        return@runBlocking
    }

    @Test
    fun `When request fails with 401, Then authenticator refreshes token`() = runBlocking {

        val authString = BuildConfig.REDDIT_CLIENT_ID + ":"

        mockkStatic(Base64::class)
        every { Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP) } returns "Base64EncodedString"
        every { UserSession.accessToken } returns "NewAccessToken"

        val invalidTokenResponse = MockResponse().setResponseCode(401)
        val authResponse = AccessTokenModel(
            access_token = "NewAccessToken",
            expires_in = 3600,
            scope = "identity vote",
            token_type = "bearer",
            refresh_token = null
        )

        val responseBody = Gson().toJson(authResponse)
        val refreshResponse = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody)

        val homeFeedResponseBuilder = StringBuilder()
        File("../app/src/main/res/raw/home_feed_response.json")
            .readLines()
            .forEach {
                homeFeedResponseBuilder.append(it)
            }
        homeFeedResponse = homeFeedResponseBuilder.toString()

        val mockedHomeFeedAPIResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(homeFeedResponse)

        // Enqueue 401 response
        mockWebServer.enqueue(invalidTokenResponse)
        // Enqueue 200 refresh response
        mockWebServer.enqueue(refreshResponse)
        // Enqueue 200 original response
        mockWebServer.enqueue(mockedHomeFeedAPIResponse)

        val response = redditAPIService.getHomeFeed(after = null, limit = 25)

        mockWebServer.takeRequest()
        mockWebServer.takeRequest()
        val retryRequest = mockWebServer.takeRequest()
        val header = retryRequest.getHeader(AUTHORIZATION)
        assertThat(UserSession.accessToken, `is`(authResponse.access_token))
        assertThat(header, `is`("$AUTHORIZATION_HEADER_PREFIX_BEARER ${authResponse.access_token}"))
        assert(response.data.children.size == 25)
    }
}