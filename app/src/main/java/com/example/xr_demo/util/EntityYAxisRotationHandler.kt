package com.example.xr_demo.util

import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import kotlin.math.abs

// Implements a grab and rotate behavior, intended to be used as an input event listener passed to an InteractableComponent
// Currently assumes that you want to rotate about an object's Y axis, but could be generalized to support any axis.
class EntityYAxisRotationHandler(
    // Entity to rotate
    val gltfEntity: Entity,
    // Rate at which to rotate the entity in degrees / meter
    val linearToAngularMovementScalar: Float = 135.0f,
    // Optional callback to perform some action when the rotation changes
    val onEntityRotated: ((entity: Entity, yRotation: Float) -> Unit)? = null) : InputEventListener {

    private val EPSILON = 0.001f

    data class PlaneInteractionData(
        val initialRotationY: Float,
        val interactionPlaneP: Vector3,
        val interactionPlaneN: Vector3,
        val interactionDirection: Vector3
    )

    private var currentInteraction: PlaneInteractionData? = null

    private fun intersectRayWithPlane(rayOrigin: Vector3, rayDirection: Vector3, planeNormal: Vector3, planePoint: Vector3): Vector3? {
        val dirDotN = rayDirection dot planeNormal
        if (abs(dirDotN) < EPSILON) {
            return null
        }
        val t = ((planePoint - rayOrigin) dot planeNormal) / dirDotN
        if (t <= 0.0f) {
            return null
        }
        return (rayDirection * t) + rayOrigin
    }

    override fun onInputEvent(inputEvent: InputEvent) {
        if (inputEvent.action == InputEvent.ACTION_DOWN) {
            // todo- leveraging hitInfo would be a little better than just using the object's translation
            // to construct the plane, but it seems to not be hooked up yet.

            // when action begins, establish a plane to ray cast onto
            val interactionPlaneP = gltfEntity.getPose().translation // inputEvent.hitInfo?.hitPosition
            val interactionPlaneN = -inputEvent.direction.toNormalized()

            // the probably won't grab the object exactly at the origin, so set interactionPlaneP to the initial ray intersection
            val interactionPlanePAdjusted = intersectRayWithPlane(inputEvent.origin, inputEvent.direction, interactionPlaneN, interactionPlaneP)
            if (interactionPlanePAdjusted == null) {
                return
            }

            // project the object's up vector onto the plane so we can determine which direction the user moves
            val entityUpProjOnPlane = Vector3.projectOnPlane(gltfEntity.getPose().up, interactionPlaneN)
            if (entityUpProjOnPlane.lengthSquared < EPSILON) {
                return
            }

            // compute a line that we can project future hits onto to determine how far the user moves
            val interactionDirection = interactionPlaneN.cross(entityUpProjOnPlane.toNormalized())

            // store the initial rotation of the entity
            val initialRotationY = gltfEntity.getPose().rotation.eulerAngles.y

            currentInteraction = PlaneInteractionData(initialRotationY, interactionPlanePAdjusted, interactionPlaneN, interactionDirection)

        } else if (inputEvent.action == InputEvent.ACTION_UP) {

            currentInteraction = null

        } else if (inputEvent.action == InputEvent.ACTION_MOVE) {

            val ci = currentInteraction
            if (ci == null) {
                return
            }

            // find the intersection on the interaction plane
            val p = intersectRayWithPlane(inputEvent.origin, inputEvent.direction, ci.interactionPlaneN, ci.interactionPlaneP)
            if (p == null) {
                return
            }

            // compute a vector to represent movement across the plane
            val mv = p - ci.interactionPlaneP

            // project mv onto interactionDirection to determine how far the user moved
            // (assume interactionDirection is normalized)
            val distance = (mv dot ci.interactionDirection) / ci.interactionDirection.lengthSquared

            // update the entity's rotation accordingly
            val currentRotationEuler: Vector3 = gltfEntity.getPose().rotation.eulerAngles
            val newYrotation = ci.initialRotationY - (distance * linearToAngularMovementScalar)
            gltfEntity.setPose(Pose(
                gltfEntity.getPose().translation,
                Quaternion.fromEulerAngles(currentRotationEuler.x, newYrotation, currentRotationEuler.z)
            ))

            this.onEntityRotated?.invoke(gltfEntity, newYrotation)
        }
    }
}