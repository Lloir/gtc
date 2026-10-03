package com.example.model

enum class CommodityCategory(val displayName: String) {
    ALL("All Sectors"),
    ORES("Raw Ores"),
    METALS("Metals & Alloys"),
    HIGH_TECH("High-Tech & Robotics"),
    GASES_ENERGY("Gases & Energy"),
    FOOD_COLONY("Food & Consumables"),
    CONSTRUCTION("Construction & Tools")
}

data class OrderBookEntry(
    val id: Long,
    val playerTycoon: String,
    val quantity: Long,
    val priceCents: Long,
    val location: String = "Galactic Exchange"
) {
    val priceDollars: Double
        get() = priceCents / 100.0
}

data class Commodity(
    val matId: Int,
    val name: String,
    val category: CommodityCategory,
    val currentPriceCents: Long, // e.g. 2300 = $23.00
    val avgPriceCents: Long, // e.g. 2233 = $22.33
    val totalQtyAvailable: Long = 0,
    val unit: String = "$",
    val description: String = "",
    val priceHistory: List<Double> = emptyList(),
    val buyOrders: List<OrderBookEntry> = emptyList(),
    val sellOrders: List<OrderBookEntry> = emptyList()
) {
    val id: String
        get() = matId.toString()

    val currentPrice: Double
        get() = currentPriceCents / 100.0

    val avgPrice: Double
        get() = avgPriceCents / 100.0

    val changePercent: Double
        get() = if (avgPriceCents > 0) {
            ((currentPriceCents - avgPriceCents).toDouble() / avgPriceCents.toDouble()) * 100.0
        } else 0.0

    val isTrendingUp: Boolean
        get() = currentPriceCents >= avgPriceCents
}

object GalacticMarketData {
    fun categorizeMaterial(name: String): CommodityCategory {
        val lower = name.lowercase()
        return when {
            lower.contains("ore") || lower.contains("silica") || lower.contains("tesserite") || lower.contains("lithium") || lower.contains("bauxite") ->
                CommodityCategory.ORES

            lower.contains("iron") || lower.contains("copper") || lower.contains("steel") || lower.contains("aluminium") || lower.contains("gold") || lower.contains("titanium") ->
                CommodityCategory.METALS

            lower.contains("robot") || lower.contains("tool") || lower.contains("fusion kit") || lower.contains("circuit") || lower.contains("computer") || lower.contains("electronics") || lower.contains("suit") || lower.contains("battery") ->
                CommodityCategory.HIGH_TECH

            lower.contains("oxygen") || lower.contains("hydrogen") || lower.contains("water") || lower.contains("bioxene") || lower.contains("gas") || lower.contains("fuel") || lower.contains("methane") ->
                CommodityCategory.GASES_ENERGY

            lower.contains("grain") || lower.contains("milk") || lower.contains("ale") || lower.contains("rations") || lower.contains("fruit") || lower.contains("vegetable") || lower.contains("coffee") ->
                CommodityCategory.FOOD_COLONY

            else -> CommodityCategory.CONSTRUCTION
        }
    }

    /**
     * Initial real baseline materials from https://wiki.galactictycoons.com/api/exchange
     * and https://api.g2.galactictycoons.com/public/exchange/mat-prices
     */
    fun getInitialCommodities(): List<Commodity> {
        return listOf(
            Commodity(
                matId = 1,
                name = "Iron Ore",
                category = CommodityCategory.ORES,
                currentPriceCents = 2300,
                avgPriceCents = 2233,
                totalQtyAvailable = 287951677,
                description = "Primary raw mineral aggregate mined from planetary crusts for iron smelting.",
                priceHistory = listOf(22.0, 22.2, 22.3, 22.5, 22.8, 23.0),
                sellOrders = listOf(
                    OrderBookEntry(16478030, "Sumran Consortium", 746605, 2300),
                    OrderBookEntry(16476063, "Awaitafar", 2875972, 2350),
                    OrderBookEntry(16471174, "Deutsche Metalle AG", 3419544, 2450)
                )
            ),
            Commodity(
                matId = 2,
                name = "Iron",
                category = CommodityCategory.METALS,
                currentPriceCents = 24500,
                avgPriceCents = 27347,
                totalQtyAvailable = 45120000,
                description = "Refined structural metal extracted from iron ore. Standard currency in heavy manufacturing.",
                priceHistory = listOf(27.8, 27.3, 26.5, 25.8, 25.0, 24.5)
            ),
            Commodity(
                matId = 3,
                name = "Concrete",
                category = CommodityCategory.CONSTRUCTION,
                currentPriceCents = 4150,
                avgPriceCents = 4215,
                totalQtyAvailable = 89000000,
                description = "Heavy building aggregate essential for laying planetary outpost foundations and building structures.",
                priceHistory = listOf(4.2, 4.25, 4.22, 4.18, 4.16, 4.15)
            ),
            Commodity(
                matId = 4,
                name = "Grain",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 1500,
                avgPriceCents = 1524,
                totalQtyAvailable = 64000000,
                description = "Staple crop grown in orbital hydroponics and fertile colony farms.",
                priceHistory = listOf(1.55, 1.54, 1.53, 1.51, 1.50, 1.50)
            ),
            Commodity(
                matId = 5,
                name = "Copper Ore",
                category = CommodityCategory.ORES,
                currentPriceCents = 5500,
                avgPriceCents = 5086,
                totalQtyAvailable = 32000000,
                description = "Raw conductive mineral required for advanced wiring, electronics, and power distribution.",
                priceHistory = listOf(4.9, 5.0, 5.1, 5.25, 5.4, 5.5)
            ),
            Commodity(
                matId = 6,
                name = "Copper",
                category = CommodityCategory.METALS,
                currentPriceCents = 78000,
                avgPriceCents = 79806,
                totalQtyAvailable = 18500000,
                description = "Smelted high-purity copper ingots used across electronics and aerospace manufacturing.",
                priceHistory = listOf(81.0, 80.5, 79.8, 79.0, 78.5, 78.0)
            ),
            Commodity(
                matId = 7,
                name = "Oxygen",
                category = CommodityCategory.GASES_ENERGY,
                currentPriceCents = 1150,
                avgPriceCents = 1138,
                totalQtyAvailable = 152000000,
                description = "Pressurized life support gas required for all off-world planetary colonies and worker habitats.",
                priceHistory = listOf(1.12, 1.13, 1.13, 1.14, 1.14, 1.15)
            ),
            Commodity(
                matId = 8,
                name = "Silica",
                category = CommodityCategory.ORES,
                currentPriceCents = 1350,
                avgPriceCents = 1367,
                totalQtyAvailable = 94000000,
                description = "Silicon dioxide crystals extracted from desert worlds for glass and microchip fabrication.",
                priceHistory = listOf(1.38, 1.37, 1.36, 1.37, 1.35, 1.35)
            ),
            Commodity(
                matId = 9,
                name = "Milk",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 7400,
                avgPriceCents = 6953,
                totalQtyAvailable = 12000000,
                description = "High-nutrition dairy product produced by specialized bio-domes.",
                priceHistory = listOf(6.8, 6.9, 7.0, 7.1, 7.3, 7.4)
            ),
            Commodity(
                matId = 10,
                name = "Ale",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 9200,
                avgPriceCents = 10095,
                totalQtyAvailable = 8500000,
                description = "Fermented beverage for colony crew happiness, productivity, and executive recreation.",
                priceHistory = listOf(10.5, 10.3, 10.1, 9.8, 9.5, 9.2)
            ),
            Commodity(
                matId = 11,
                name = "Water",
                category = CommodityCategory.GASES_ENERGY,
                currentPriceCents = 1950,
                avgPriceCents = 2338,
                totalQtyAvailable = 180000000,
                description = "Raw unpurified liquid water extracted from glacial ice caps and underground aquifers.",
                priceHistory = listOf(2.4, 2.35, 2.3, 2.2, 2.05, 1.95)
            ),
            Commodity(
                matId = 12,
                name = "Rations",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 5000,
                avgPriceCents = 4940,
                totalQtyAvailable = 45000000,
                description = "Standard nutrient packs consumed daily by mine workers and factory technicians.",
                priceHistory = listOf(4.9, 4.92, 4.94, 4.96, 4.98, 5.0)
            ),
            Commodity(
                matId = 13,
                name = "Fine Rations",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 9700,
                avgPriceCents = 10662,
                totalQtyAvailable = 21000000,
                description = "Gourmet rations prepared for engineers, executives, and high-tier specialist workers.",
                priceHistory = listOf(11.0, 10.8, 10.5, 10.2, 9.9, 9.7)
            ),
            Commodity(
                matId = 14,
                name = "Laboratory Suit",
                category = CommodityCategory.HIGH_TECH,
                currentPriceCents = 94000,
                avgPriceCents = 95671,
                totalQtyAvailable = 3200000,
                description = "Hazard-sealed environmental gear for high-tech cleanrooms and genetic research labs.",
                priceHistory = listOf(96.0, 95.8, 95.5, 95.0, 94.5, 94.0)
            ),
            Commodity(
                matId = 15,
                name = "Exosuit",
                category = CommodityCategory.HIGH_TECH,
                currentPriceCents = 78000,
                avgPriceCents = 71668,
                totalQtyAvailable = 4500000,
                description = "Powered hydraulic exoskeleton increasing miner and construction worker physical payload.",
                priceHistory = listOf(71.0, 72.5, 74.0, 75.5, 77.0, 78.0)
            ),
            Commodity(
                matId = 16,
                name = "Drinking Water",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 2700,
                avgPriceCents = 2694,
                totalQtyAvailable = 98000000,
                description = "Filtered, mineralized drinking water required for workforce upkeep.",
                priceHistory = listOf(2.68, 2.69, 2.70, 2.69, 2.70, 2.70)
            ),
            Commodity(
                matId = 17,
                name = "Tools",
                category = CommodityCategory.CONSTRUCTION,
                currentPriceCents = 8100,
                avgPriceCents = 8047,
                totalQtyAvailable = 34000000,
                description = "Standard hand tools and pneumatic equipment required for production upkeep.",
                priceHistory = listOf(8.0, 8.02, 8.04, 8.06, 8.08, 8.10)
            ),
            Commodity(
                matId = 18,
                name = "Advanced Tools",
                category = CommodityCategory.HIGH_TECH,
                currentPriceCents = 24000,
                avgPriceCents = 24934,
                totalQtyAvailable = 12000000,
                description = "Precision laser cutters and robotic calibration kits for advanced factories.",
                priceHistory = listOf(25.2, 25.0, 24.8, 24.5, 24.2, 24.0)
            ),
            Commodity(
                matId = 19,
                name = "Molecular Fusion Kit",
                category = CommodityCategory.HIGH_TECH,
                currentPriceCents = 145000,
                avgPriceCents = 162680,
                totalQtyAvailable = 1500000,
                description = "High-energy molecular bonding apparatus for advanced starship and reactor construction.",
                priceHistory = listOf(168.0, 164.0, 160.0, 155.0, 150.0, 145.0)
            ),
            Commodity(
                matId = 20,
                name = "Robot",
                category = CommodityCategory.HIGH_TECH,
                currentPriceCents = 295000,
                avgPriceCents = 283565,
                totalQtyAvailable = 950000,
                description = "Autonomous bipedal worker android capable of 24/7 unassisted industrial labor.",
                priceHistory = listOf(280.0, 283.0, 286.0, 289.0, 292.0, 295.0)
            ),
            Commodity(
                matId = 21,
                name = "Coffee",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 20500,
                avgPriceCents = 21116,
                totalQtyAvailable = 6200000,
                description = "Stimulating roast beans traded at premium prices among galactic corporations.",
                priceHistory = listOf(21.4, 21.2, 21.0, 20.8, 20.6, 20.5)
            ),
            Commodity(
                matId = 22,
                name = "Bioxene",
                category = CommodityCategory.GASES_ENERGY,
                currentPriceCents = 19000,
                avgPriceCents = 22460,
                totalQtyAvailable = 7800000,
                description = "Exotic organic solvent harvested from alien swamp worlds for chemical synthesis.",
                priceHistory = listOf(23.0, 22.4, 21.8, 21.0, 20.0, 19.0)
            ),
            Commodity(
                matId = 23,
                name = "Tesserite",
                category = CommodityCategory.ORES,
                currentPriceCents = 35000,
                avgPriceCents = 36359,
                totalQtyAvailable = 5400000,
                description = "Rare geometric crystalline ore with spatial distortion properties.",
                priceHistory = listOf(37.0, 36.6, 36.2, 35.8, 35.4, 35.0)
            ),
            Commodity(
                matId = 24,
                name = "Hydrogen",
                category = CommodityCategory.GASES_ENERGY,
                currentPriceCents = 1650,
                avgPriceCents = 1895,
                totalQtyAvailable = 210000000,
                description = "Ubiquitous combustible gas scooped from gas giants for fusion fuel and polymer synthesis.",
                priceHistory = listOf(1.95, 1.90, 1.85, 1.78, 1.72, 1.65)
            ),
            Commodity(
                matId = 25,
                name = "Polyethylene",
                category = CommodityCategory.CONSTRUCTION,
                currentPriceCents = 1850,
                avgPriceCents = 1867,
                totalQtyAvailable = 92000000,
                description = "Thermoplastic polymer resin used as casing, insulation, and packaging material.",
                priceHistory = listOf(1.88, 1.87, 1.86, 1.86, 1.85, 1.85)
            ),
            Commodity(
                matId = 26,
                name = "Construction Kit",
                category = CommodityCategory.CONSTRUCTION,
                currentPriceCents = 155000,
                avgPriceCents = 152647,
                totalQtyAvailable = 2400000,
                description = "Comprehensive modular building assembly kit for planetary facilities.",
                priceHistory = listOf(150.0, 151.0, 152.0, 153.0, 154.0, 155.0)
            ),
            Commodity(
                matId = 27,
                name = "Construction Tools",
                category = CommodityCategory.CONSTRUCTION,
                currentPriceCents = 18000,
                avgPriceCents = 18148,
                totalQtyAvailable = 16000000,
                description = "Heavy pneumatic rivets, drills, and laser welders for structural fabrication.",
                priceHistory = listOf(18.2, 18.2, 18.1, 18.1, 18.0, 18.0)
            ),
            Commodity(
                matId = 28,
                name = "Fruits",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 3850,
                avgPriceCents = 4911,
                totalQtyAvailable = 38000000,
                description = "Fresh perishable crops harvested from greenhouse biodomes.",
                priceHistory = listOf(5.1, 4.8, 4.5, 4.2, 4.0, 3.85)
            ),
            Commodity(
                matId = 29,
                name = "Vegetables",
                category = CommodityCategory.FOOD_COLONY,
                currentPriceCents = 4250,
                avgPriceCents = 3679,
                totalQtyAvailable = 41000000,
                description = "Nutritious produce grown on agricultural worlds for colony rations.",
                priceHistory = listOf(3.6, 3.7, 3.85, 4.0, 4.15, 4.25)
            ),
            Commodity(
                matId = 30,
                name = "Neoplast",
                category = CommodityCategory.CONSTRUCTION,
                currentPriceCents = 5400,
                avgPriceCents = 5349,
                totalQtyAvailable = 28000000,
                description = "Advanced carbon-reinforced composite polymer with steel-grade tensile strength.",
                priceHistory = listOf(5.3, 5.32, 5.35, 5.37, 5.38, 5.40)
            )
        )
    }
}
