package com.example.xr_demo.museum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.Session
import com.example.xr_demo.ui.theme.XR_DemoTheme
import java.util.concurrent.Executors
import androidx.xr.runtime.math.Pose
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import com.example.xr_demo.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.xr_demo.EnvironmentController

class MuseumPOCActivity : ComponentActivity() {

    companion object {
        const val ART_GLB_FILE_NAME = "models/museumSculpture.glb"
//        const val ART_GLB_FILE_NAME = "https://github.com/alexqinone/glbStorage/raw/refs/heads/main/Houseplant.glb"
        const val PEDESTAL_FILE_NAME = "models/columnPedestal.glb"
    }

    private var modelAngel: Float = 225f

    private val thumbnailImages = listOf(
        R.drawable.hannibalthumb_1,
        R.drawable.hannibalthumb_2,
        R.drawable.hannibalthumb_3,
        R.drawable.hannibalthumb_4
    )

    private var environmentController: EnvironmentController? = null
    private val environmentModelName = "models/museumAlley.glb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XR_DemoTheme {
                LocalSession.current?.let { xrSession ->
                    ShowModels(xrSession)
                }
            }
        }
    }

    @Composable
    fun ShowModels(xrSession: Session) {
        val modelRoot: Entity = xrSession.activitySpaceRoot
        val executor by lazy { Executors.newSingleThreadExecutor() }
        val focusRequesters = remember { List(thumbnailImages.size) { FocusRequester() } }
        var focusIndex = remember { mutableStateOf(0) }



        Subspace {
            LaunchedEffect(Unit) {

//              //create art/sculpture model
                val artGlbModel = GltfModel.create(xrSession, ART_GLB_FILE_NAME).await()
                val glbEntity = GltfModelEntity.create(xrSession, artGlbModel)

                val modelTranslation = Vector3(0f, 0f, 0f)
                val modelOrientation = Quaternion.fromEulerAngles(0f, modelAngel, 0f)
                val modelPose = Pose(modelTranslation, modelOrientation)


                glbEntity.setPose(modelPose)
                glbEntity.setScale(3.0f)

                //create pedestal model
                val pedGlbModel = GltfModel.create(xrSession, PEDESTAL_FILE_NAME).await()
                val pedEntity = GltfModelEntity.create(xrSession, pedGlbModel)

                val pedTranslation = Vector3(0f, -0.42f, 0f)
                val pedOrientation = Quaternion.fromEulerAngles(0f, 0f, 0f)
                val pedPose = Pose(pedTranslation, pedOrientation)

                pedEntity.setPose(pedPose)
                pedEntity.setScale(0.22f)

                val modelInteractable = InteractableComponent.create(xrSession, executor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {

                        }

                        InputEvent.ACTION_UP -> {

                        }

                        InputEvent.ACTION_MOVE -> {
                            val modelTranslation = Vector3(0f, 0f, 0f)
                            modelAngel = modelAngel + 5f
                            val modelOrientation = Quaternion.fromEulerAngles(0f, modelAngel, 0f)
                            val modelPose = Pose(modelTranslation, modelOrientation)
                            glbEntity.setPose(modelPose)

                            //update thumbnails focus
                            focusIndex.value = ((modelAngel % 360) / 90).toInt()
                            focusRequesters[focusIndex.value].requestFocus()
                        }
                    }
                }

                glbEntity.addComponent(modelInteractable)
                glbEntity.setParent(modelRoot)

            }

            SpatialPanel(
                SubspaceModifier
                    .height(400.dp)
                    .width(500.dp)
                    .offset(x = 400.dp, y = 100.dp)
            ) {
                ShowInfoPanel()
            }

            SpatialPanel(
                SubspaceModifier
                    .height(400.dp)
                    .width(100.dp)
                    .offset(x = -220.dp, y = 0.dp)
            ) {
                ShowThumbNailPanel(focusRequesters, focusIndex)
            }

            SpatialPanel(
                SubspaceModifier
                    .height(60.dp)
                    .width(150.dp)
                    .offset(x = 0.dp, y = -500.dp)
            ) {
                Button(
                    onClick = {
                        environmentController?.requestCustomEnvironment(
                            environmentModelName
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Set Env")
                }
            }
        }

        LocalActivity.current?.let { activity ->
            environmentController = remember(activity) {
                val session = Session.create(activity)
                EnvironmentController(session, (activity as ComponentActivity).lifecycleScope)
            }
            // load the model early so it's in memory for when we need it
            environmentController?.loadModelAsset(environmentModelName)
        }

    }

    @Composable
    fun ShowInfoPanel() {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
                .clip(RoundedCornerShape(10)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start,
            )
            {
                Text(text = stringResource(R.string.annibal), fontSize = 20.sp)
                Spacer(Modifier.weight(0.2f))
                Text(
                    text = stringResource(R.string.annibal_description),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.weight(0.2f))
            }
        }
    }

    @Composable
    fun ShowThumbNailPanel(
        focusRequesters: List<FocusRequester>,
        focusedIndex: MutableState<Int>
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = Color.Transparent
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(0.dp, 0.dp)
                    .padding(10.dp)
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(thumbnailImages.size) {
                    val imageUrl = thumbnailImages[it]
                    ThumbNailCard(imageUrl, focusRequesters[it], focusedIndex)
                }
            }
        }
    }

    @Composable
    fun ThumbNailCard(
        imageUrlId: Int,
        focusRequester: FocusRequester,
        focusedIndex: MutableState<Int>
    ) {

        val borderColor =
            if (focusedIndex.value == thumbnailImages.indexOf(imageUrlId)) Color.White else Color.Transparent

        Card(
            modifier = Modifier
                .width(75.dp)
                .height(75.dp)
                .focusable()
                .focusRequester(focusRequester)
                .onFocusEvent {
//                    isFocused = it.isFocused
                }
                .clickable {
                    focusedIndex.value = thumbnailImages.indexOf(imageUrlId)
                    focusRequester.requestFocus()
                }
                .background(Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            AsyncImage(
                model = imageUrlId,
                contentDescription = "Images",
                modifier = Modifier.border(4.dp, borderColor)
            )
        }
    }
}