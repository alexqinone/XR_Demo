package com.example.xr_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.scenecore.Session
import com.example.xr_demo.navigation.ActivityInfo
import com.example.xr_demo.navigation.NavManager
import com.example.xr_demo.ui.env.EnvironmentController
import com.example.xr_demo.ui.theme.LocalSpacing
import com.example.xr_demo.ui.theme.XR_DemoTheme
import kotlin.collections.get

class DashActivity : ComponentActivity() {

    companion object {
        const val TAG = "DashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XR_DemoTheme {
                generateMainContent()
            }
        }
    }

    @Composable
    fun generateMainContent() {
        Scaffold(
            topBar = { XRTopAppBar() }
        ) { innerPadding ->

            val modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()

            Box(Modifier.padding(innerPadding)) {
                ActivityListScreen(
                    modifier = modifier
                )
                if (LocalHasXrSpatialFeature.current && LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    LocalActivity.current?.let { activity ->
                        val environmentController = remember(activity) {
                            val session = Session.create(activity)
                            EnvironmentController(session, (activity as ComponentActivity).lifecycleScope)
                        }
                        // load the model early so it's in memory for when we need it
                        val environmentModelName = "env/green_hills_ktx2_mipmap.glb"
                        environmentController.loadModelAsset(environmentModelName)

                        val showSecondOrbiter = remember { mutableStateOf(false) }

                        Orbiter(
                            position = OrbiterEdge.Vertical.End,
                            alignment = Alignment.Top,
                            offset = LocalSpacing.current.xxxl
                        ) {
                            Surface(modifier = Modifier.clip(CircleShape)) {
                                Column {
                                    RequestHomeSpaceButton { environmentController.requestHomeSpaceMode() }
                                    SetVirtualEnvironmentButton(
                                        modifier = Modifier
                                            .padding(LocalSpacing.current.m)
                                            .background(
                                                if (showSecondOrbiter.value)
                                                    MaterialTheme.colorScheme.inversePrimary
                                                else
                                                    MaterialTheme.colorScheme.onSecondary,
                                                CircleShape
                                            )
                                    ) {
                                        showSecondOrbiter.value = !showSecondOrbiter.value
                                    }
                                }
                            }
                        }

                        if (showSecondOrbiter.value) {
                            Orbiter(
                                position = OrbiterEdge.Vertical.End,
                                alignment = Alignment.Top,
                                offset = LocalSpacing.current.xxxxl + LocalSpacing.current.xxxxl
                            ) {
                                Surface(modifier = Modifier.clip(CircleShape)) {
                                    Column {
                                        SetPassthroughButton(
                                            iconResId = R.drawable.visibility_on,
                                            stringResId = R.string.enable_passthrough
                                        ) { environmentController.requestPassthrough(1f) }
                                        SetPassthroughButton(
                                            iconResId = R.drawable.visibility_off,
                                            stringResId = R.string.disable_passthrough
                                        ) { environmentController.requestPassthrough(0f) }
                                        SetVirtualEnvironmentButton(
                                            iconResId = R.drawable.ic_custom_env,
                                            stringResId = R.string.use_custom_environment
                                        ) { environmentController.requestCustomEnvironment(environmentModelName) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun XRTopAppBar() {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                // Only show the mode toggle if the device supports spatial UI
                if (LocalHasXrSpatialFeature.current && !LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    // If we aren't able to access the session, these buttons wouldn't work and shouldn't be shown
                    val activity = LocalActivity.current
                    val isJXRSessionAvailable = LocalSession.current != null
                    if (activity is ComponentActivity && isJXRSessionAvailable) {
                        val environmentController = remember(activity) {
                            val session = Session.create(activity)
                            EnvironmentController(session, activity.lifecycleScope)
                        }
                        RequestFullSpaceButton { environmentController.requestFullSpaceMode() }
                    }
                }
            }
        )
    }

    @Composable
    fun RequestFullSpaceButton(onclick: () -> Unit) {
        IconButton(
            onClick = onclick, modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
                contentDescription = stringResource(R.string.switch_to_full_space_mode)
            )
        }
    }

    @Composable
    fun RequestHomeSpaceButton(onclick: () -> Unit) {
        IconButton(
            onClick = onclick,
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
                contentDescription = stringResource(R.string.switch_to_home_space_mode)
            )
        }
    }

    @Composable
    fun SetVirtualEnvironmentButton(
        modifier: Modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.onSecondary, CircleShape),
        @DrawableRes iconResId: Int = R.drawable.ic_show_env,
        @StringRes stringResId: Int = R.string.set_virtual_environment,
        onclick: () -> Unit
    ) {
        IconButton(
            onClick = onclick,
            modifier = modifier
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = stringResource(stringResId),
            )
        }
    }

    @Composable
    fun SetPassthroughButton(
        modifier: Modifier = Modifier,
        @DrawableRes iconResId: Int = R.drawable.ic_hide_env,
        @StringRes stringResId: Int = R.string.set_passthrough,
        onclick: () -> Unit
    ) {
        IconButton(
            onClick = onclick,
            modifier = modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = stringResource(stringResId),
            )
        }
    }

    @Composable
    fun ActivityListScreen(modifier : Modifier) {
        val context = LocalContext.current
        val activities = NavManager.getActivities()
        val session = LocalSession.current
        LazyColumn(
            modifier = modifier
        ) {
            items(activities.size) {
                val expActivityInfo = activities[it]
                ActivityItem(expActivityInfo, onClick = {
                    NavManager.start(context, expActivityInfo, session)
                })
            }
        }
    }

    @Composable
    fun ActivityItem(activityInfo: ActivityInfo, onClick: () -> Unit) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = activityInfo.title)
                Text(text = activityInfo.description)
            }
        }
    }

}