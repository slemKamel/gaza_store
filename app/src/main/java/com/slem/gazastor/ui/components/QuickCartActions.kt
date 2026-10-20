package com.slem.gazastor.ui.components

import com.slem.gazastor.ui.model.ItemUi

/**
 * The quick-cart slice the catalog grids hand to their [ItemGridCard]s: the
 * current quantity per product plus the three cart actions, all backed by the
 * shared cart repository.
 *
 * Defaults keep stateless previews and screens that don't wire the cart light —
 * [ItemGridCard] draws the quick-add control only when [onAdd] is provided.
 */
data class QuickCartActions(
    val quantities: Map<String, Int> = emptyMap(),
    val onAdd: (ItemUi) -> Unit = {},
    val onIncrease: (String) -> Unit = {},
    val onDecrease: (String) -> Unit = {}
)
