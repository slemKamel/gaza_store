package com.slem.gazastor.core.config

object Constants {
    const val APP_NAME = "Gaza Stor"

    /**
     * Flat delivery fee added to every non-empty cart, in shekels.
     * Kept here (instead of the UI) so it can be changed or made remote later.
     */
    const val DELIVERY_FEE = 10.0
}
