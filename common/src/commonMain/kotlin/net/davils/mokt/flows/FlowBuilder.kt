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

package net.davils.mokt.flows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

public class FlowBuilder<T : FlowData, R : FlowProgress> internal constructor(maxParallelism: Int) {
    private val dispatcher = Dispatchers.Default.limitedParallelism(maxParallelism)
    private val steps = mutableListOf<FlowStep<T, R>>()
//    private val parallelProgressList = atomic(mutableListOf<Int>())

    public fun step(flowStep: FlowStep<T, R>): FlowStep<T, R> = flowStep.also { steps.add(it) }

    public fun conditionalStep(flowStep: FlowStep<T, R>, condition: (T) -> Boolean): FlowStep<T, R> {
        val step = object : FlowStep<T, R> {
            override suspend fun execute(flowData: T): Flow<R> {
                if (condition(flowData)) {
                    return flowStep.execute(flowData)
                }
                return channelFlow { }
            }
        }
        return step.also { steps.add(it) }
    }

//    public fun parallelStep(vararg flowSteps: FlowStep<T, R>): FlowStep<T, R> {
//        val step = object : FlowStep<T, R> {
//            override suspend fun execute(flowData: T): Flow<R> = channelFlow {
//
//                val stepJobs = flowSteps.map { flowStep ->
//                    CoroutineScope(dispatcher).launch {
//                        flowStep.execute(flowData).collect {
//                            parallelProgressList.value.add(it.progress)
//                            val progress = parallelProgressList.value.sumOf { num -> num } / flowSteps.size
//
//                            it.incrementParallelProgress(progress)
//                            send(it)
//                        }
//                    }
//                }
//                stepJobs.joinAll()
//            }
//        }
//        return step.also { steps.add(it) }
//    }

//    public fun conditionalParallelStep(vararg flowSteps: FlowStep<T, R>, condition: (T) -> Boolean): FlowStep<T, R> {
//        val step = object : FlowStep<T, R> {
//            override suspend fun execute(flowData: T): Flow<R> {
//                if (condition(flowData)) {
//                    return parallelStep(*flowSteps).execute(flowData)
//                }
//                return channelFlow { }
//            }
//        }
//        return step.also { steps.add(it) }
//    }

    internal fun build(data: T, onCancel: suspend () -> Unit, onError: suspend () -> Unit) = channelFlow {
        steps.forEach { step ->
            if (data.isCanceled) {
                onCancel()
                return@channelFlow
            }

            try {
                val flow = step.execute(data)
                flow.collect { progress ->
                    send(progress)
                }
            } catch (e: Exception) {
                onError()
                return@channelFlow
            }
        }
    }
}

public suspend fun <T : FlowData, R : FlowProgress> flow(
    data: T,
    maxParallelism: Int = 16,
    onCancel: suspend () -> Unit = {},
    onError: suspend () -> Unit = {},
    builder: suspend FlowBuilder<T, R>.() -> Unit,
): Flow<R> {
    val flowBuilder = FlowBuilder<T, R>(maxParallelism).apply { builder() }
    return flowBuilder.build(data, onCancel, onError)
}
