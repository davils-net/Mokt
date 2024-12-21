/*
 * MIT License
 * Copyright 2024 Davils
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.davils.mokt.flows

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow

public abstract class FlowData : SynchronizedObject() {
    public var isCanceled: Boolean = false
        private set

    public fun cancel(): Unit = synchronized(this) {
        isCanceled = true
    }
}

public abstract class FlowProgress {
    public abstract val currentStep: Int
    public abstract val totalSteps: Int
    public var stepProgress: Double? = null

//    private val parallelProgress = atomic(0)

    public val progress: Int
        get() {
            require(value = totalSteps > 0 && currentStep > 0) { "Total steps and current step must be greater than 0" }
//            if (parallelProgress.value != 0) return parallelProgress.value

            if (stepProgress == null) {
                return (currentStep.toDouble() / totalSteps.toDouble() * 100).toInt()
            }

            val progress = stepProgress!! + (currentStep.toDouble() / totalSteps.toDouble() * 100)
            return progress.toInt()
        }

//    internal fun incrementParallelProgress(progress: Int) {
//        parallelProgress.value = progress
//    }
}

public interface FlowStep<T : FlowData, R : FlowProgress> {
    public suspend fun execute(flowData: T): Flow<R>
}
