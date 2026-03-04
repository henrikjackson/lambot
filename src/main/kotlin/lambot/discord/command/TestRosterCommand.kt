package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import lambot.config.DiscordProperties
import lambot.raidhelper.RosterService
import lambot.raidhelper.SignUp
import org.springframework.stereotype.Component

@Component
class TestRosterCommand(
    private val rosterService: RosterService,
    private val discordProperties: DiscordProperties
) : SlashCommand {
    override val name = "test-roster"
    override val description = "Test rosteret med dummy-data (35 påmeldinger)"
    override val requiredRoleIds: Set<String> get() = discordProperties.allowedRoleIds.toSet()

    override suspend fun register(kord: Kord) {}

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        val callerUserId = event.interaction.user.id.toString()

        val signUps = listOf(
            signup(1,  "Jackson(Iamnorsk)",  "111111111111111111", "Tank",     "Protection", position = 1),
            signup(2,  "TankTwo",            "222222222222222222", "Tank",     "Blood",       position = 2),
            signup(3,  "TankThree",          "333333333333333333", "Tank",     "Brewmaster",  position = 3),
            signup(4,  "HealerOne",          "444444444444444444", "Healer",   "Discipline",  position = 4),
            signup(5,  "HealerTwo",          "555555555555555555", "Healer",   "Holy",        position = 5),
            signup(6,  "HealerThree",        "666666666666666666", "Healer",   "Mistweaver",  position = 6),
            signup(7,  "HealerFour",         "777777777777777777", "Healer",   "Restoration", position = 7),
            signup(8,  "HealerFive",         "888888888888888888", "Healer",   "Holy",        position = 8),
            signup(9,  "HealerSix",          "999999999999999999", "Healer",   "Preservation",position = 9),
            signup(10, "HealerSeven",        "101010101010101010", "Healer",   "Discipline",  position = 10),
            signup(11, "MeleeOne",           "111111111111111112", "Melee",    "Arms",        position = 11),
            signup(12, "MeleeTwo",           "121212121212121212", "Melee",    "Fury",        position = 12),
            signup(13, "MeleeThree",         "131313131313131313", "Melee",    "Havoc",       position = 13),
            signup(14, "MeleeFour",          "141414141414141414", "Melee",    "Retribution", position = 14),
            signup(15, "MeleeFive",          "151515151515151515", "Melee",    "Feral",       position = 15),
            signup(16, "RangedOne",          "161616161616161616", "Ranged",   "Fire",        position = 16),
            signup(17, "RangedTwo",          "171717171717171717", "Ranged",   "Balance",     position = 17),
            signup(18, "RangedThree",        "181818181818181818", "Ranged",   "Shadow",      position = 18),
            signup(19, "RangedFour",         "191919191919191919", "Ranged",   "Affliction",  position = 19),
            signup(20, "RangedFive",         "202020202020202020", "Ranged",   "Elemental",   position = 20),
            signup(21, "MeleeSix",           "212121212121212121", "Melee",    "Windwalker",  position = 21),
            signup(22, "RangedSix",          "222222222222222223", "Ranged",   "Marksmanship",position = 22),
            signup(23, "MeleeSeven",         "232323232323232323", "Melee",    "Subtlety",    position = 23),
            signup(24, "RangedSeven",        "242424242424242424", "Ranged",   "Destruction", position = 24),
            signup(25, "MeleeEight",         "252525252525252525", "Melee",    "Enhancement", position = 25),
            signup(26, "RangedEight",        "262626262626262626", "Ranged",   "Frost",       position = 26),
            signup(27, "MeleeNine",          "272727272727272727", "Melee",    "Outlaw",      position = 27),
            signup(28, "RangedNine",         "282828282828282828", "Ranged",   "Arcane",      position = 28),
            signup(29, "TentativeOne",       "292929292929292929", "Tentative",null,           position = 29),
            signup(30, "TentativeTwo",       "303030303030303030", "Tentative",null,           position = 30),
            // Bench
            signup(31, "BenchedOne",         callerUserId,         "Ranged",   "Shadow",      position = 31),
            signup(32, "BenchedTwo",         "323232323232323232", "Melee",    "Arms",        position = 32),
            signup(33, "LateOne",            "333333333333333334", "Late",     null,           position = 33),
            // Absence (filtered out)
            signup(34, "AbsentOne",          "343434343434343434", "Absence",  null,           position = 34),
            signup(35, "AbsentTwo",          "353535353535353535", "Absence",  null,           position = 35),
        )

        val (roster, bench) = rosterService.buildRoster(signUps)

        fun formatEntry(index: Int, signup: SignUp): String {
            val num = index + 1
            val label = if (signup.userId == callerUserId)
                "**${signup.name}** (${signup.className}) ← deg"
            else
                "${signup.name} (${signup.className})"
            return "$num. $label"
        }

        val sb = StringBuilder()
        sb.appendLine("**[TEST] Dummy Raid**")
        sb.appendLine()
        sb.appendLine("**I raidet (${roster.size}):**")
        roster.forEachIndexed { i, s -> sb.appendLine(formatEntry(i, s)) }

        if (bench.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("**Benk (${bench.size}):**")
            bench.forEachIndexed { i, s -> sb.appendLine(formatEntry(i, s)) }
        }

        event.interaction.respondEphemeral { content = sb.toString().trimEnd() }
    }

    private fun signup(id: Long, name: String, userId: String, className: String, specName: String?, position: Int) =
        SignUp(name = name, id = id, userId = userId, className = className, specName = specName, entryTime = id * 1000, position = position)
}
