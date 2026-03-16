package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.PaneStateImpl.Companion.Saver


/** Possible values of [PaneState]. */
enum class PaneValue {
    /** The state of the rail when it is collapsed. */
    Collapsed,

    /** The state of the rail when it is expanded. */
    Expanded,
}

interface PaneState {

    /** Whether the state is currently animating */
    val isAnimating: Boolean

    /** Whether the rail is going to be expanded or not. */
    val targetValue: PaneValue

    /** Whether the rail is currently expanded or not. */
    val currentValue: PaneValue

    /** Expand the rail with animation and suspend until it fully expands. */
    suspend fun expand()

    /** Collapse the rail with animation and suspend until it fully collapses. */
    suspend fun collapse()

    /**
     * Collapse the rail with animation if it's expanded, or expand it if it's collapsed, and
     * suspend until it's set to its new state.
     */
    suspend fun toggle()

    /**
     * Set the state without any animation and suspend until it's set.
     *
     * @param targetValue the expanded boolean to set to
     */
    suspend fun snapTo(targetValue: PaneValue)

    companion object {
        operator fun invoke(
            initialValue: PaneValue = PaneValue.Collapsed,
            animationSpec: AnimationSpec<Float> = spring(),
        ): PaneState = PaneStateImpl(initialValue, animationSpec)
    }
}

/** Create and [remember] a [PaneState]. */
@Composable
fun rememberPaneState(
    initialValue: PaneValue = PaneValue.Collapsed,
): PaneState {
    val animationSpec = spring<Float>()
    return rememberSaveable(saver = PaneStateImpl.Companion.Saver(animationSpec)) {
        PaneStateImpl(
            initialValue = initialValue,
            animationSpec = animationSpec,
        )
    }
}

internal val PaneValue.isExpanded
    get() = this == PaneValue.Expanded

internal operator fun PaneValue.not(): PaneValue {
    return if (this == PaneValue.Collapsed) {
        PaneValue.Expanded
    } else {
        PaneValue.Collapsed
    }
}

internal class PaneStateImpl(
    var initialValue: PaneValue,
    private val animationSpec: AnimationSpec<Float>,
) : PaneState {

    private val internalValue = if (initialValue.isExpanded) Expanded else Collapsed
    private val internalState = Animatable(internalValue, Float.VectorConverter)
    private val _currentVal = derivedStateOf {
        if (internalState.value == Expanded) {
            PaneValue.Expanded
        } else {
            PaneValue.Collapsed
        }
    }

    override val isAnimating: Boolean
        get() = internalState.isRunning

    override val targetValue: PaneValue
        get() =
            if (internalState.targetValue == Expanded) {
                PaneValue.Expanded
            } else {
                PaneValue.Collapsed
            }

    override val currentValue: PaneValue
        get() = _currentVal.value

    override suspend fun expand() {
        internalState.animateTo(targetValue = Expanded, animationSpec = animationSpec)
    }

    override suspend fun collapse() {
        internalState.animateTo(targetValue = Collapsed, animationSpec = animationSpec)
    }

    override suspend fun toggle() {
        internalState.animateTo(
            targetValue = if (targetValue.isExpanded) Collapsed else Expanded,
            animationSpec = animationSpec,
        )
    }

    override suspend fun snapTo(targetValue: PaneValue) {
        val target = if (targetValue.isExpanded) Expanded else Collapsed
        internalState.snapTo(target)
    }

    companion object {
        private const val Collapsed = 0f
        private const val Expanded = 1f

        /** The default [Saver] implementation for [PaneState]. */
        fun Saver(animationSpec: AnimationSpec<Float>) =
            Saver<PaneState, PaneValue>(
                save = { it.targetValue },
                restore = {
                    PaneStateImpl(
                        initialValue = it,
                        animationSpec = animationSpec,
                    )
                },
            )
    }
}

//internal class ModalSidebarState(
//    state: PaneState,
//    density: Density,
//    val animationSpec: AnimationSpec<Float>,
//) : PaneState by state {
//    internal val anchoredDraggableState: AnchoredDraggableState<PaneValue> =
//        AnchoredDraggableState(
//            initialValue = state.targetValue,
//            positionalThreshold = { distance -> distance * 0.5f },
//            velocityThreshold = { with(density) { 400.dp.toPx() } },
//            animationSpec = { animationSpec },
//        )
//
//    /**
//     * The current value of the state.
//     *
//     * If no swipe or animation is in progress, this corresponds to the value the dismissible modal
//     * wide navigation rail is currently in. If a swipe or an animation is in progress, this
//     * corresponds to the value the rail was in before the swipe or animation started.
//     */
//    override val currentValue: PaneValue
//        get() = anchoredDraggableState.currentValue
//
//    /**
//     * The target value of the dismissible modal wide navigation rail state.
//     *
//     * If a swipe is in progress, this is the value that the modal rail will animate to if the swipe
//     * finishes. If an animation is running, this is the target value of that animation. Finally, if
//     * no swipe or animation is in progress, this is the same as the [currentValue].
//     */
//    override val targetValue: PaneValue
//        get() = anchoredDraggableState.targetValue
//
//    override val isAnimating: Boolean
//        get() = anchoredDraggableState.isAnimationRunning
//
//    override suspend fun expand() = animateTo(PaneValue.Expanded)
//
//    override suspend fun collapse() = animateTo(PaneValue.Collapsed)
//
//    override suspend fun toggle() {
//        animateTo(!targetValue)
//    }
//
//    override suspend fun snapTo(targetValue: PaneValue) {
//        anchoredDraggableState.snapTo(targetValue)
//    }
//
//    /**
//     * Find the closest anchor taking into account the velocity and settle at it with an animation.
//     */
//    internal suspend fun settle(velocity: Float) {
//        anchoredDraggableState.settle(velocity)
//    }
//
//    /**
//     * The current position (in pixels) of the rail, or Float.NaN before the offset is initialized.
//     *
//     * @see [AnchoredDraggableState.offset] for more information.
//     */
//    val currentOffset: Float
//        get() = anchoredDraggableState.offset
//
//    private suspend fun animateTo(
//        targetValue: PaneValue,
//        animationSpec: AnimationSpec<Float> = this.animationSpec,
//        velocity: Float = anchoredDraggableState.lastVelocity,
//    ) {
//        anchoredDraggableState.anchoredDrag(targetValue = targetValue) { anchors, latestTarget ->
//            val targetOffset = anchors.positionOf(latestTarget)
//            if (!targetOffset.isNaN()) {
//                var prev = if (currentOffset.isNaN()) 0f else currentOffset
//                animate(prev, targetOffset, velocity, animationSpec) { value, velocity ->
//                    // Our onDrag coerces the value within the bounds, but an animation may
//                    // overshoot, for example a spring animation or an overshooting interpolator.
//                    // We respect the user's intention and allow the overshoot, but still use
//                    // DraggableState's drag for its mutex.
//                    dragTo(value, velocity)
//                    prev = value
//                }
//            }
//        }
//    }
//}