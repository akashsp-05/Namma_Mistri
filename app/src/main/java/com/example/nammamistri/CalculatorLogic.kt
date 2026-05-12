package com.example.nammamistri

object CalculatorLogic {

    fun calculateMaterials(
        length: Double,
        width: Double,
        height: Double,
        wallType: String,
        doorCount: Int = 0,
        doorWidth: Double = 0.0,
        doorHeight: Double = 0.0,
        doorRate: Double = 0.0,
        windowCount: Int = 0,
        windowWidth: Double = 0.0,
        windowHeight: Double = 0.0,
        windowRate: Double = 0.0
    ): String {

        val grossVolume = length * width * height
        val openingVolume = ((doorCount * doorWidth * doorHeight) + (windowCount * windowWidth * windowHeight)) * width
        val volume = (grossVolume - openingVolume).coerceAtLeast(0.0)
        val factor = if (wallType == "4 inch") 1.0 else 1.5

        val bricks = volume * 8 * factor
        val cement = volume * 0.4 * factor
        val sand = volume * 0.3 * factor
        val doorCost = doorCount * doorRate
        val windowCost = windowCount * windowRate

        return """
--- Site Report ---

Wall Type: $wallType

Gross Wall Volume: ${"%.2f".format(grossVolume)}
Door/Window Deduction: ${"%.2f".format(openingVolume)}
Net Brickwork Volume: ${"%.2f".format(volume)}

Bricks Required: ${bricks.toInt()}
Cement Bags: ${cement.toInt()}
Sand Loads: ${sand.toInt()}
Doors: $doorCount (${doorWidth} ft x ${doorHeight} ft) - Rs ${"%.2f".format(doorCost)}
Windows: $windowCount (${windowWidth} ft x ${windowHeight} ft) - Rs ${"%.2f".format(windowCost)}
        """.trimIndent()
    }
}
