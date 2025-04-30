package com.example.xr_demo.museum

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.Session
import com.example.xr_demo.ui.theme.XR_DemoTheme

class DisposeTestActivity: ComponentActivity() {

    private var gltfModelEntity: GltfModelEntity? = null
    private val ModelName_range = "models/armor_for_man_and_horse.glb"
    private val ModelName_ranbo = "models/breastplate_from_hussars_cuirass.glb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XR_DemoTheme {
                LocalSession.current?.let {
                    MainContent(it)
                }
            }
        }
    }

    @Composable
    fun MainContent(xrSession: Session) {
        var reloadModel by remember { mutableStateOf(false) }
        var loadNewModel by remember { mutableStateOf(false) }

        ModelContent(xrSession, ModelName_range, 0.5f)

        Subspace {
            SpatialPanel(
                SubspaceModifier
                    .height(100.dp)
                    .width(450.dp)
                    .offset(x = 0.dp, y = -225.dp)
                    .rotate(-10f, 0f, 0f),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10)),
                ) {

                    Row (
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Button(
                            onClick = { reloadModel = true }) {
                            Text(text = "Re-Load Model")
                        }

                        Button(
                            onClick = { loadNewModel = true }) {
                            Text(text = "load New Model")
                        }

                        if (reloadModel) {
                            ModelContent(xrSession, ModelName_range, 0.5f)
                            reloadModel = false
                        }

                        if (loadNewModel) {
                            ModelContent(xrSession, ModelName_ranbo, 1.0f)
                            loadNewModel = false
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ModelContent(xrSession: Session, modelUrl: String, modelScale: Float) {
        Subspace {
            LaunchedEffect(Unit) {
                gltfModelEntity?.removeAllComponents()
                gltfModelEntity?.dispose()
                gltfModelEntity?.setHidden(true)
                val modelRoot: Entity = xrSession.activitySpaceRoot
                val startTime = System.currentTimeMillis()
                val glbModel = GltfModel.create(xrSession, modelUrl).await()
                gltfModelEntity?.setHidden(false)
                gltfModelEntity = GltfModelEntity.create(xrSession, glbModel)
                Toast.makeText(baseContext, "Model load time: ${System.currentTimeMillis() - startTime}", Toast.LENGTH_LONG).show()

                val modelTranslation = Vector3(0f, 0f, -1f)
                val modelOrientation = Quaternion.fromEulerAngles(0f, -90f, 0f)
                val modelPose = Pose(modelTranslation, modelOrientation)
                gltfModelEntity?.setPose(modelPose)
                gltfModelEntity?.setScale(modelScale)
                gltfModelEntity?.setParent(modelRoot)
            }
        }
    }

    @Composable
    fun disposeEntity() {
        Subspace {
            LaunchedEffect(Unit) {
                gltfModelEntity?.removeAllComponents()
                gltfModelEntity?.dispose()
                gltfModelEntity?.setHidden(true)
            }
        }
    }
}