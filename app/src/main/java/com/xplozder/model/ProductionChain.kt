package com.xplozder.model

data class RecipeInput(
    val matId: Int,
    val name: String,
    val quantity: Int
)

data class ProductionRecipe(
    val id: String,
    val outputMatId: Int,
    val outputName: String,
    val outputQuantity: Int,
    val outputUnit: String,
    val facilityRequired: String,
    val cycleTimeMinutes: Int,
    val energyCostDollars: Double,
    val inputs: List<RecipeInput>
) {
    fun calculateInputCost(priceMap: Map<Int, Double>): Double {
        val ingredientCost = inputs.sumOf { inp ->
            val unitPrice = priceMap[inp.matId] ?: 0.0
            unitPrice * inp.quantity
        }
        return ingredientCost + energyCostDollars
    }

    fun calculateOutputValue(priceMap: Map<Int, Double>): Double {
        val unitPrice = priceMap[outputMatId] ?: 0.0
        return unitPrice * outputQuantity
    }

    fun calculateLiveProfit(priceMap: Map<Int, Double>): Double {
        val cost = calculateInputCost(priceMap)
        val revenue = calculateOutputValue(priceMap)
        return revenue - cost
    }

    fun calculateLiveMarginPct(priceMap: Map<Int, Double>): Double {
        val cost = calculateInputCost(priceMap)
        if (cost <= 0.0) return 0.0
        return (calculateLiveProfit(priceMap) / cost) * 100.0
    }
}

object GalacticProductionData {
    fun getRecipes(): List<ProductionRecipe> {
        return listOf(
            ProductionRecipe(
                id = "rec_iron_smelting",
                outputMatId = 2,
                outputName = "Iron",
                outputQuantity = 1,
                outputUnit = "unit",
                facilityRequired = "Smelter",
                cycleTimeMinutes = 60,
                energyCostDollars = 15.00,
                inputs = listOf(RecipeInput(1, "Iron Ore", 10))
            ),
            ProductionRecipe(
                id = "rec_copper_smelting",
                outputMatId = 6,
                outputName = "Copper",
                outputQuantity = 1,
                outputUnit = "unit",
                facilityRequired = "Smelter",
                cycleTimeMinutes = 90,
                energyCostDollars = 20.00,
                inputs = listOf(RecipeInput(5, "Copper Ore", 10))
            ),
            ProductionRecipe(
                id = "rec_steel_foundry",
                outputMatId = 35,
                outputName = "Steel",
                outputQuantity = 1,
                outputUnit = "unit",
                facilityRequired = "Foundry",
                cycleTimeMinutes = 75,
                energyCostDollars = 25.00,
                inputs = listOf(
                    RecipeInput(2, "Iron", 2),
                    RecipeInput(31, "Carbon", 1)
                )
            ),
            ProductionRecipe(
                id = "rec_concrete_mixing",
                outputMatId = 3,
                outputName = "Concrete",
                outputQuantity = 5,
                outputUnit = "units",
                facilityRequired = "Chemical Plant",
                cycleTimeMinutes = 45,
                energyCostDollars = 12.00,
                inputs = listOf(
                    RecipeInput(8, "Silica", 8),
                    RecipeInput(11, "Water", 4)
                )
            ),
            ProductionRecipe(
                id = "rec_rations_prep",
                outputMatId = 12,
                outputName = "Basic Rations",
                outputQuantity = 4,
                outputUnit = "packs",
                facilityRequired = "Food Processing Plant",
                cycleTimeMinutes = 30,
                energyCostDollars = 8.00,
                inputs = listOf(
                    RecipeInput(4, "Grain", 4),
                    RecipeInput(11, "Water", 2)
                )
            ),
            ProductionRecipe(
                id = "rec_hydrogen_electrolysis",
                outputMatId = 24,
                outputName = "Hydrogen Fuel",
                outputQuantity = 2,
                outputUnit = "cylinders",
                facilityRequired = "Electrolysis Plant",
                cycleTimeMinutes = 40,
                energyCostDollars = 10.00,
                inputs = listOf(RecipeInput(11, "Water", 2))
            ),
            ProductionRecipe(
                id = "rec_robot_assembly",
                outputMatId = 20,
                outputName = "Autonomous Robot",
                outputQuantity = 1,
                outputUnit = "unit",
                facilityRequired = "Robotics Factory",
                cycleTimeMinutes = 120,
                energyCostDollars = 60.00,
                inputs = listOf(
                    RecipeInput(35, "Steel", 2),
                    RecipeInput(59, "Electronic Circuit", 2),
                    RecipeInput(56, "Battery", 1)
                )
            )
        )
    }
}
