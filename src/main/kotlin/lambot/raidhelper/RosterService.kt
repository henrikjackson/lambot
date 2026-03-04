package lambot.raidhelper

import lambot.config.RaidHelperProperties
import org.springframework.stereotype.Service

sealed class RosterResult {
    data class InRoster(val position: Int) : RosterResult()
    data class Benched(val queuePosition: Int) : RosterResult()
    object NotSignedUp : RosterResult()
}

data class FullRoster(
    val roster: List<SignUp>,
    val bench: List<SignUp>
)

@Service
class RosterService(private val properties: RaidHelperProperties) {

    fun buildRoster(signUps: List<SignUp>): FullRoster {
        val active = signUps.filter { it.className != "Absence" }
        val sorted = active.sortedWith(compareBy({ it.position ?: Int.MAX_VALUE }, { it.entryTime }))

        val leader = sorted.firstOrNull { it.userId == properties.leaderId }
        val withoutLeader = sorted.filter { it.userId != properties.leaderId }

        val roster = mutableListOf<SignUp>()
        val placedIds = mutableSetOf<String>()

        if (leader != null) {
            roster.add(leader)
            placedIds.add(leader.userId)
        }

        withoutLeader.filter { it.className == "Tank" }.take(2).forEach {
            if (placedIds.add(it.userId)) roster.add(it)
        }

        withoutLeader.filter { it.className == "Healer" }.take(6).forEach {
            if (placedIds.add(it.userId)) roster.add(it)
        }

        for (signup in withoutLeader) {
            if (roster.size >= 30) break
            if (placedIds.add(signup.userId)) roster.add(signup)
        }

        val bench = sorted.filter { it.userId !in placedIds }
        return FullRoster(roster, bench)
    }

    fun evaluate(signUps: List<SignUp>, userId: String): RosterResult {
        val (roster, bench) = buildRoster(signUps)

        val rosterIndex = roster.indexOfFirst { it.userId == userId }
        if (rosterIndex >= 0) return RosterResult.InRoster(rosterIndex + 1)

        val benchIndex = bench.indexOfFirst { it.userId == userId }
        if (benchIndex >= 0) return RosterResult.Benched(benchIndex + 1)

        return RosterResult.NotSignedUp
    }
}
