package lambot.raid

enum class WowClass(
    val displayName: String,
    val roles: Set<Role>,
    val emoji: String
) {
    WARRIOR("Warrior", setOf(Role.TANK, Role.DPS), WowClassEmoji.PALADIN.emoji),
    PALADIN("Paladin", setOf(Role.TANK, Role.HEALER, Role.DPS), WowClassEmoji.PALADIN.emoji),
    DRUID("Druid", setOf(Role.TANK, Role.HEALER, Role.DPS), WowClassEmoji.DRUID.emoji),
    DEATH_KNIGHT("Death Knight", setOf(Role.TANK, Role.DPS), WowClassEmoji.DEATH_KNIGHT.emoji),
    DEMON_HUNTER("Demon Hunter", setOf(Role.TANK, Role.DPS), WowClassEmoji.DEMON_HUNTER.emoji),

    PRIEST("Priest", setOf(Role.HEALER, Role.DPS), WowClassEmoji.PRIEST.emoji),
    SHAMAN("Shaman", setOf(Role.HEALER, Role.DPS), WowClassEmoji.SHAMAN.emoji),
    MONK("Monk", setOf(Role.TANK, Role.HEALER, Role.DPS), WowClassEmoji.MONK.emoji),
    EVOKER("Evoker", setOf(Role.HEALER, Role.DPS), WowClassEmoji.EVOKER.emoji),

    MAGE("Mage", setOf(Role.DPS), WowClassEmoji.MAGE.emoji),
    WARLOCK("Warlock", setOf(Role.DPS), WowClassEmoji.WARLOCK.emoji),
    HUNTER("Hunter", setOf(Role.DPS), WowClassEmoji.HUNTER.emoji),
    ROGUE("Rogue", setOf(Role.DPS), WowClassEmoji.ROGUE.emoji),;

    companion object {
        fun forRole(role: Role): List<WowClass> =
            entries.filter { it.roles.contains(role) }
    }
}

enum class WowClassEmoji(val emoji: String) {
    PALADIN("<:paladin:1474050440084590878>"),
    DRUID("<:druid:1474050297906069687>"),
    DEATH_KNIGHT("<:deathknight:1474050201718227025>"),
    DEMON_HUNTER("<:demonhunter:1474050263475159215>"),
    PRIEST("<:priest:1474050474457043045>"),
    SHAMAN("<:shaman:1474050551082782897>"),
    MONK("<:monk:1474050398980669634>"),
    EVOKER("<:evoker:1062327581493440522>"),
    MAGE("<:wowmage:1474051362160382033>"),
    WARLOCK("<:warlock:1474050587497726044>"),
    HUNTER("<:hunter:1474050331901165660>"),
    ROGUE("<:rogue:1474050509689323662>"),
}
