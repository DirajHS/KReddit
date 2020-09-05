package com.diraj.kreddit.presentation.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.HomeNavigationHeaderBinding
import com.diraj.kreddit.databinding.LayoutActivityHomeBinding
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.UserData
import com.diraj.kreddit.presentation.home.viewmodel.HomeActivityViewModel
import com.diraj.kreddit.presentation.login.AuthenticationActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class HomeActivity : AppCompatActivity(), HasAndroidInjector {

    @field:Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @field:Inject
    lateinit var viewModelFactory: ViewModelFactory<HomeActivityViewModel>

    private lateinit var homeActivityViewModel: HomeActivityViewModel

    private lateinit var layoutActivityHomeBinding: LayoutActivityHomeBinding

    private val navController: NavController
        get() = findNavController(R.id.home_nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutActivityHomeBinding = LayoutActivityHomeBinding.inflate(layoutInflater)
        setContentView(layoutActivityHomeBinding.root)

        homeActivityViewModel = ViewModelProvider(this, viewModelFactory).get(HomeActivityViewModel::class.java)
        setUpNavigationComponent()
    }

    override fun onResume() {
        super.onResume()
        fetchProfileData()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onSupportNavigateUp() = navController.navigateUp(layoutActivityHomeBinding.homeDrawerLayout)

    override fun onBackPressed() {
        if(layoutActivityHomeBinding.homeDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            layoutActivityHomeBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setUpNavigationComponent() {
        setSupportActionBar(layoutActivityHomeBinding.homeToolbar)
        setupWithNavController(layoutActivityHomeBinding.homeToolbar, navController, layoutActivityHomeBinding.homeDrawerLayout)
        layoutActivityHomeBinding.nvUserControls.setupWithNavController(navController)
        layoutActivityHomeBinding.nvUserControls.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.sign_out -> logout()
            }
            true
        }
    }

    private fun fetchProfileData() {
        homeActivityViewModel.fetchProfileInfo().observe(this, { redditResponse ->
            when(redditResponse) {
                is RedditResponse.Loading -> {
                    Timber.d("loading profile")
                }
                is RedditResponse.Success<*> -> {
                    Timber.d("successfully fetched profile")
                    renderNavigationDrawerItems(redditResponse.successData as UserData)
                }
                is RedditResponse.Error -> {
                    Timber.e("error during profile fetch ${redditResponse.ex.message}")
                }
            }
        })
    }

    private fun renderNavigationDrawerItems(userData: UserData) {
        if(layoutActivityHomeBinding.nvUserControls.headerCount > 0) {
            val homeNavigationHeaderBinding = HomeNavigationHeaderBinding
                .bind(layoutActivityHomeBinding.nvUserControls.getHeaderView(0))
            Glide.with(this)
                .load(userData.icon_img)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.1f)
                .into(homeNavigationHeaderBinding.rvProfile)
            homeNavigationHeaderBinding.tvUserName.text = userData.name
            homeNavigationHeaderBinding.tvUserDuration.text = PrettyTime(Locale.getDefault())
                .format(userData.created_utc.toFloat().toLong().times(1000L).let { Date(it) })
        }
    }

    private fun logout() {
        homeActivityViewModel.doLogout().observe(this, { redditResponse ->
            when(redditResponse) {
                is RedditResponse.Loading -> {
                    Timber.d("trying logout")
                }
                is RedditResponse.Success<*> -> {
                    Timber.d("logout successfully done")
                    finishHomeActivity()
                }
                is RedditResponse.Error -> {
                    Timber.e("error during profile logout ${redditResponse.ex.message}")
                }
            }

        })
    }

    private fun finishHomeActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}