package com.demo.mdisplaytest

import de.proglove.sdk.IPgErrorCallback
import de.proglove.sdk.commands.IPgCommandData

interface IPgFeedbackCallback : IPgErrorCallback {

    /**
     * Called when the Feedback was successfully started
     */
    fun onSuccess()
}

/**
 * Some predefined FeedbackSequences that can be triggered on the Mark
 *
 * @property id Internally used to identify the Feedback
 */
enum class PgPredefinedFeedback(val id: Int = 1) : IPgCommandData<PgPredefinedFeedback> {

    /**
     * A positive Feedback
     */
    SUCCESS(1),

    /**
     * A negative Feedback
     */
    ERROR(2),

    /**
     * A neutral Feedback
     */
    SPECIAL_1(3)
}