package com.example.xr_demo.museum

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.xr_demo.museum.MuseumPOCActivity.Companion.ART_GLB_FILE_NAME
import com.example.xr_demo.ui.theme.XR_DemoTheme
import java.nio.file.WatchEvent

class ModelLoadActivity: ComponentActivity() {

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
        var textValue by remember { mutableStateOf("") }
        var sliderValue by remember { mutableStateOf(50f) }
        var showModel by remember { mutableStateOf(false) }
        Subspace {
            SpatialPanel(
                SubspaceModifier
                    .height(280.dp)
                    .width(350.dp)
                    .offset(x = 0.dp, y = -300.dp)
                    .rotate(-20f, 0f, 0f),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            label = { Text("Model URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Model Scale: ${sliderValue.toInt()} / 100")

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 1f..100f,
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        Row (
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            Button(
                                onClick = { showModel = true }) {
                                Text(text = "Load Model")
                            }

                            Button(onClick = { /* Handle button click */ }) {
                                Text(text = "Remove Model")
                            }

                            if (showModel) {
                                LoadModel(xrSession, textValue, sliderValue / 100)
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun LoadModel(xrSession: Session, modelUrl: String, modelScale: Float) {

        Subspace {
            LaunchedEffect(Unit) {
                val modelRoot: Entity = xrSession.activitySpaceRoot
                val startTime = System.currentTimeMillis()
                val glbModel = GltfModel.create(xrSession, modelUrl).await()
                val glbEntity = GltfModelEntity.create(xrSession, glbModel)
                Toast.makeText(baseContext, "Model load time: ${System.currentTimeMillis() - startTime}", Toast.LENGTH_LONG).show()

                val modelTranslation = Vector3(0f, 0f, 0f)
                val modelOrientation = Quaternion.fromEulerAngles(0f, 0f, 0f)
                val modelPose = Pose(modelTranslation, modelOrientation)
                glbEntity.setPose(modelPose)
                glbEntity.setScale(modelScale)
                glbEntity.setParent(modelRoot)
            }
        }

    }
}

