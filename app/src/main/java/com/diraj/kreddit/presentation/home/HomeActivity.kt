package com.diraj.kreddit.presentation.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diraj.kreddit.R
import com.diraj.kreddit.data.models.UserData
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.databinding.HomeNavigationHeaderBinding
import com.diraj.kreddit.databinding.HomeNavigationHeaderBinding
import com.diraj.kreddit.databinding.LayoutActivityHomeBinding // Will remove if fully Compose
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.presentation.home.compose.HomeFeedScreen
import com.diraj.kreddit.presentation.home.viewmodel.HomeActivityViewModel
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.presentation.login.AuthenticationActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeActivity : AppCompatActivity(), HasAndroidInjector {

    @field:Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @field:Inject
    lateinit var viewModelFactory: ViewModelFactory<HomeActivityViewModel>

    private lateinit var homeActivityViewModel: HomeActivityViewModel

    // Retain binding for drawer and header for now, will see if it can be replaced
    private lateinit var layoutActivityHomeBinding: LayoutActivityHomeBinding
    private lateinit var homeNavigationHeaderBinding: HomeNavigationHeaderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeActivityViewModel = ViewModelProvider(this, viewModelFactory).get(HomeActivityViewModel::class.java)

        // Still inflate the activity layout to get the drawer and toolbar
        layoutActivityHomeBinding = LayoutActivityHomeBinding.inflate(layoutInflater)
        setContentView(layoutActivityHomeBinding.root) // Keep this for DrawerLayout and Toolbar

        setSupportActionBar(layoutActivityHomeBinding.homeToolbar)

        // Original NavController setup for XML Fragments (might be needed for details screen)
        // We need to decide if the Compose NavController can also navigate the fragment container
        // or if we need two NavControllers or a hybrid approach.
        // For now, let's assume the Compose NavHost takes over the main content area.

        setContent {
            // Assuming a Theme is defined, e.g., AppTheme { ... }
            // For now, using a basic MaterialTheme wrapper if no app specific theme is set up for compose
            MaterialTheme { // Replace with your app's Compose theme if available
                val homeFeedViewModel: HomeFeedViewModel = hiltViewModel()
                val sharedViewModel: SharedViewModel = hiltViewModel()
                // val scope = rememberCoroutineScope() // For drawer actions if needed

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.home_fragment_title)) }, // Or dynamic title based on xmlNavController.currentDestination
                            navigationIcon = {
                                IconButton(onClick = {
                                    layoutActivityHomeBinding.homeDrawerLayout.openDrawer(GravityCompat.START)
                                }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    // HomeFeedScreen is the main content, pass xmlNavController for navigation to XML fragments
                    HomeFeedScreen(
                        navController = xmlNavController, // Pass the XML NavController
                        homeFeedViewModel = homeFeedViewModel,
                        sharedViewModel = sharedViewModel,
                        // modifier = Modifier.padding(paddingValues) // Apply padding from Scaffold
                    )
                }
            }
        }

        // Setup drawer item clicks (sign out)
        layoutActivityHomeBinding.nvUserControls.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.sign_out -> {
                    logout()
                    layoutActivityHomeBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    // Allow XML NavController to handle other menu items if they exist
                    // and are part of the home_navigation.xml and should navigate in the home_nav_host_fragment
                    // This requires home_nav_host_fragment to still be part of the layout if used for other destinations.
                    // For now, we assume sign_out is the main action.
                    // NavigationUI.onNavDestinationSelected(menuItem, xmlNavController) || super.onOptionsItemSelected(menuItem)
                    // Let's simplify: only handle sign_out, close drawer.
                    layoutActivityHomeBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }

        fetchProfileData() // For drawer header
    }

    override fun onResume() {
        super.onResume()
        // If HomeFeedScreen is the main content, ensure profile data is fetched for the drawer.
        fetchProfileData()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onSupportNavigateUp() = xmlNavController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


    override fun onBackPressed() {
        if(layoutActivityHomeBinding.homeDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            layoutActivityHomeBinding.homeDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // If Compose NavHost can go back, it should. Otherwise, default behavior.
            // val composeNavController = ? // Need access to it here
            // if (composeNavController.previousBackStackEntry != null) {
            //     composeNavController.popBackStack()
            // } else {
            super.onBackPressed()
            // }
        }
    }

    // setUpNavigationComponent is replaced by setContent and NavHost setup

    private fun fetchProfileData() { // For Drawer
        homeActivityViewModel.userInfoLiveData.observe(this, { redditResponse ->
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
        // This part remains the same as it updates the XML navigation header
        if(layoutActivityHomeBinding.nvUserControls.headerCount > 0) {
            homeNavigationHeaderBinding = HomeNavigationHeaderBinding
                .bind(layoutActivityHomeBinding.nvUserControls.getHeaderView(0))
            Glide.with(this)
                .load(userData.iconImg)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.1f)
                .into(homeNavigationHeaderBinding.rvProfile)
            homeNavigationHeaderBinding.tvUserName.text = userData.name
            homeNavigationHeaderBinding.tvUserDuration.text = PrettyTime(Locale.getDefault())
                .format(userData.createdUtc.toFloat().toLong().times(1000L).let { Date(it) })
        }
    }

    private fun logout() {
        homeActivityViewModel.doLogout().observe(this) { redditResponse ->
            when (redditResponse) {
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

        }
    }

    private fun finishHomeActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}