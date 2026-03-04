package lambot.raidhelper

data class SignUp(
    val name: String,
    val id: Long,
    val userId: String,
    val className: String,
    val specName: String?,
    val entryTime: Long,
    val position: Int?
)

data class RaidEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val signUps: List<SignUp>? = null
)

data class EventsResponse(
    val postedEvents: List<RaidEvent>
)

data class AdvancedSettings(
    val image: String
)

data class CreateEventRequest(
    val leaderId: String,
    val templateId: String,
    val date: String,
    val time: String,
    val title: String,
    val description: String,
    val advancedSettings: AdvancedSettings
)
