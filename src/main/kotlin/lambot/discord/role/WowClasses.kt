package lambot.discord.role

enum class WowRole(val label: String) {
    TANK("Tanks"),
    HEALER("Healers"),
    MELEE("Melee DPS"),
    RANGED("Ranged DPS")
}

object WowClasses {
    val specs: Map<String, List<String>> = linkedMapOf(
        "Death Knight" to listOf("Blood", "Frost", "Unholy"),
        "Demon Hunter" to listOf("Havoc", "Vengeance"),
        "Druid" to listOf("Balance", "Feral", "Guardian", "Restoration"),
        "Evoker" to listOf("Augmentation", "Devastation", "Preservation"),
        "Hunter" to listOf("Beast Mastery", "Marksmanship", "Survival"),
        "Mage" to listOf("Arcane", "Fire", "Frost"),
        "Monk" to listOf("Brewmaster", "Mistweaver", "Windwalker"),
        "Paladin" to listOf("Holy", "Protection", "Retribution"),
        "Priest" to listOf("Discipline", "Holy", "Shadow"),
        "Rogue" to listOf("Assassination", "Outlaw", "Subtlety"),
        "Shaman" to listOf("Elemental", "Enhancement", "Restoration"),
        "Warlock" to listOf("Affliction", "Demonology", "Destruction"),
        "Warrior" to listOf("Arms", "Fury", "Protection")
    )

    private val roles: Map<Pair<String, String>, WowRole> = mapOf(
        // Tanks
        ("Death Knight" to "Blood")      to WowRole.TANK,
        ("Demon Hunter" to "Vengeance")  to WowRole.TANK,
        ("Druid"        to "Guardian")   to WowRole.TANK,
        ("Monk"         to "Brewmaster") to WowRole.TANK,
        ("Paladin"      to "Protection") to WowRole.TANK,
        ("Warrior"      to "Protection") to WowRole.TANK,
        // Healers
        ("Druid"   to "Restoration") to WowRole.HEALER,
        ("Evoker"  to "Preservation") to WowRole.HEALER,
        ("Monk"    to "Mistweaver")  to WowRole.HEALER,
        ("Paladin" to "Holy")        to WowRole.HEALER,
        ("Priest"  to "Discipline")  to WowRole.HEALER,
        ("Priest"  to "Holy")        to WowRole.HEALER,
        ("Shaman"  to "Restoration") to WowRole.HEALER,
        // Melee DPS
        ("Death Knight" to "Frost")        to WowRole.MELEE,
        ("Death Knight" to "Unholy")       to WowRole.MELEE,
        ("Demon Hunter" to "Havoc")        to WowRole.MELEE,
        ("Druid"        to "Feral")        to WowRole.MELEE,
        ("Hunter"       to "Survival")     to WowRole.MELEE,
        ("Monk"         to "Windwalker")   to WowRole.MELEE,
        ("Paladin"      to "Retribution")  to WowRole.MELEE,
        ("Rogue"        to "Assassination") to WowRole.MELEE,
        ("Rogue"        to "Outlaw")       to WowRole.MELEE,
        ("Rogue"        to "Subtlety")     to WowRole.MELEE,
        ("Shaman"       to "Enhancement")  to WowRole.MELEE,
        ("Warrior"      to "Arms")         to WowRole.MELEE,
        ("Warrior"      to "Fury")         to WowRole.MELEE,
        // Ranged DPS
        ("Druid"   to "Balance")      to WowRole.RANGED,
        ("Evoker"  to "Augmentation") to WowRole.RANGED,
        ("Evoker"  to "Devastation")  to WowRole.RANGED,
        ("Hunter"  to "Beast Mastery") to WowRole.RANGED,
        ("Hunter"  to "Marksmanship") to WowRole.RANGED,
        ("Mage"    to "Arcane")       to WowRole.RANGED,
        ("Mage"    to "Fire")         to WowRole.RANGED,
        ("Mage"    to "Frost")        to WowRole.RANGED,
        ("Priest"  to "Shadow")       to WowRole.RANGED,
        ("Shaman"  to "Elemental")    to WowRole.RANGED,
        ("Warlock" to "Affliction")   to WowRole.RANGED,
        ("Warlock" to "Demonology")   to WowRole.RANGED,
        ("Warlock" to "Destruction")  to WowRole.RANGED,
    )

    fun roleFor(className: String, specName: String): WowRole? = roles[className to specName]

    fun keyFor(className: String) = className.lowercase().replace(" ", "-")
    fun fromKey(key: String) = specs.keys.firstOrNull { keyFor(it) == key }
    fun specsFor(classKey: String) = specs[fromKey(classKey)].orEmpty()
}
