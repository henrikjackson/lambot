package lambot.raid

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RaidProfileService {

    private val profiles =
        ConcurrentHashMap<Long, MutableMap<Role, WowClass>>()

    fun getClassForRole(userId: Long, role: Role): WowClass? =
        profiles[userId]?.get(role)

    fun saveClass(userId: Long, role: Role, wowClass: WowClass) {
        val profile = profiles.computeIfAbsent(userId) { mutableMapOf() }

        profile[role] = wowClass
    }
}