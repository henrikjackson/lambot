package lambot.raidhelper

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
class RosterService {

    private val jacksonName = "Jackson(Iamnorsk)"

    fun buildRoster(signUps: List<SignUp>): FullRoster {
        val active = signUps.filter { it.className != "Absence" }
        val sorted = active.sortedWith(compareBy({ it.position ?: Int.MAX_VALUE }, { it.entryTime }))

        val jackson = sorted.firstOrNull { it.name == jacksonName }
        val withoutJackson = sorted.filter { it.name != jacksonName }

        val roster = mutableListOf<SignUp>()
        val placedIds = mutableSetOf<String>()

        if (jackson != null) {
            roster.add(jackson)
            placedIds.add(jackson.userId)
        }

        withoutJackson.filter { it.className == "Tank" }.take(2).forEach {
            if (placedIds.add(it.userId)) roster.add(it)
        }

        withoutJackson.filter { it.className == "Healer" }.take(6).forEach {
            if (placedIds.add(it.userId)) roster.add(it)
        }

        for (signup in withoutJackson) {
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
