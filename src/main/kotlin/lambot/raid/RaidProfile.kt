package lambot.raid

data class RaidProfile(
    val userId: Long,
    val classByRole: MutableMap<Role, WowClass>
)
