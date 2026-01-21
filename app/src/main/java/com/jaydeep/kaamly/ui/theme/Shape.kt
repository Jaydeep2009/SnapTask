package com.jaydeep.kaamly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Shape System
 * Defines corner radius for different component sizes
 */
val Shapes = Shapes(
    // Extra Small - chips, small buttons
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - buttons, text fields
    small = RoundedCornerShape(8.dp),
    
    // Medium - cards, dialogs
    medium = RoundedCornerShape(12.dp),
    
    // Large - bottom sheets, large cards
    large = RoundedCornerShape(16.dp),
    
    // Extra Large - full screen dialogs
    extraLarge = RoundedCornerShape(28.dp)
)
