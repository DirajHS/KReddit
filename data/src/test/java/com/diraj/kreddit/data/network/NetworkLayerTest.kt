package com.diraj.kreddit.data.network

import android.util.Base64
import com.diraj.kreddit.data.BuildConfig
import com.diraj.kreddit.data.di.DaggerTestComponent
import com.diraj.kreddit.data.di.TestNetworkModule
import com.diraj.kreddit.data.models.AccessTokenModel
import com.diraj.kreddit.data.repo.auth.api.AuthAPIService
import com.diraj.kreddit.data.repo.details.api.DetailsAPIService
import com.diraj.kreddit.data.repo.home.api.HomeAPIService
import com.diraj.kreddit.data.repo.profile.api.ProfileAPIService
import com.diraj.kreddit.data.user.UserSession
import com.diraj.kreddit.data.utils.DataLayerConstants.ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION_HEADER_PREFIX_BEARER
import com.diraj.kreddit.data.utils.DataLayerConstants.MEDIA_TYPE
import com.diraj.kreddit.data.utils.DataLayerConstants.USER_AGENT_KEY
import com.diraj.kreddit.data.utils.DataLayerConstants.USER_AGENT_VALUE
import com.tencent.mmkv.MMKV
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var kredditRetrofit: Retrofit

    @field:[Inject Named("Authenticator")]
    lateinit var authenticatorRetrofit: Retrofit

    private lateinit var homeAPIService: HomeAPIService

    private lateinit var detailsAPIService: DetailsAPIService

    private lateinit var profileAPIService: ProfileAPIService

    private lateinit var authAPIService: AuthAPIService

    private lateinit var userProfileResponse: String

    private lateinit var homeFeedResponse: String

    private lateinit var mockedMMKVObject: MockedStatic<MMKV>

    private lateinit var json: Json

    @Before
    fun setUp() {
        DaggerTestComponent.builder().appModuleForTest(TestNetworkModule()).build().inject(this)
        homeAPIService = kredditRetrofit.create(HomeAPIService::class.java)
        detailsAPIService = kredditRetrofit.create(DetailsAPIService::class.java)
        profileAPIService = kredditRetrofit.create(ProfileAPIService::class.java)
        authAPIService = authenticatorRetrofit.create(AuthAPIService::class.java)

        val mockedMMKV = mock(MMKV::class.java)
        mockedMMKVObject = mockStatic(MMKV::class.java)
        `when`(MMKV.defaultMMKV()).thenReturn(mockedMMKV)

        mockkObject(UserSession)
        every { UserSession.accessToken } returns "AccessToken"
        every { UserSession.refreshToken } returns "RefreshToken"

        json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
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

        val homeFeedAPIResponse = homeAPIService.getHomeFeed(after = null, limit = 25)
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

        profileAPIService.getCurrentUserInfo()
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
            accessToken = "NewAccessToken",
            expiresIn = 3600,
            scope = "identity vote",
            tokenType = "bearer",
            refreshToken = null
        )

        val responseBody = json.encodeToString(authResponse)
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

        val response = homeAPIService.getHomeFeed(after = null, limit = 25)

        mockWebServer.takeRequest()
        mockWebServer.takeRequest()
        val retryRequest = mockWebServer.takeRequest()
        val header = retryRequest.getHeader(AUTHORIZATION)
        assertThat(UserSession.accessToken, `is`(authResponse.accessToken))
        assertThat(header, `is`("$AUTHORIZATION_HEADER_PREFIX_BEARER ${authResponse.accessToken}"))
        assert(response.data.children.size == 25)
    }

    @Test
    fun `Logout POST request is correctly sent`() = runBlocking {
        val authString = BuildConfig.REDDIT_CLIENT_ID + ":"
        mockkStatic(Base64::class)
        every { Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP) } returns "Base64EncodedString"

        mockWebServer.enqueue(MockResponse())

        val postInfo = "token=${UserSession.refreshToken}&token_type_hint=refresh_token"
        val postBody = postInfo.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())

        authAPIService.logout(postBody)
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.method, `is`("POST"))
        assertThat(recordedRequest.getHeader(USER_AGENT_KEY), `is`(USER_AGENT_VALUE))
        assertThat(recordedRequest.getHeader(AUTHORIZATION), `is`("$ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX Base64EncodedString"))
    }

    @Test
    fun `Comments data is parsed correctly for feed response`() = runBlocking {
        val homeFeedDetailsResponseBuilder = StringBuilder()
        File("../app/src/main/res/raw/comments_response.json")
            .readLines()
            .forEach {
                homeFeedDetailsResponseBuilder.append(it)
            }
        val homeFeedDetailsResponse = homeFeedDetailsResponseBuilder.toString()

        val mockedHomeFeedAPIResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(homeFeedDetailsResponse)
        mockWebServer.enqueue(mockedHomeFeedAPIResponse)

        val feedDetails = detailsAPIService.fetchCommentsFromPermalink("/r/science/comments/iqdiie/researchers_put_people_aged_over_65_with_some/.json")
        val parsedComments = com.diraj.kreddit.data.utils.CommentsParser(feedDetails).parseComments()

        val firstComment = "I work in an assisted living facility in the US and can say I knew this without the use of a study.. while I work with people generally over age 80 and each one has a diagnosis of dementia already, anytime speech, occupational or physical therapy is invoked there decline slows or they even have improvement. While this is expected of therapy, this is more noticeable in families that are more interactive with those who are affected.. or put differently the more attention the person gets the “better” the dementia or more specifically the behaviors associated with- improves. Nice to have something published tho as dementia is still a very nuanced thing in the medical world... it takes a village"
        val firstCommentReply = "When my mom was living on her own, she pretty much stared at the walls all day. A few phone calls each day to her friends, but not much more stimulation. She was really starting to lose it. Now she lives with me and is constantly exposed to other people around as well as YouTube videos about history and archaeology and travel and the places  she has been and the things that she has done and cherished all her life. She is a different person now. Her cognitive  abilities and general joy in life are much improved.  I think this change has added many years to her life."
        mockWebServer.takeRequest()

        val parseFirstComment = parsedComments.first().body
        assert(parseFirstComment == firstComment)
        val parsedFirstCommentFirstReply = parsedComments.first().children?.first()?.body
        assert(parsedFirstCommentFirstReply == firstCommentReply)
    }
}