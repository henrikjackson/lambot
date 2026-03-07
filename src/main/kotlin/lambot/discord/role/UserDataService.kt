package lambot.discord.role

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Service
class UserDataService(
    @Value("\${features.user-data-file:user-data.json}") private val filePath: String
) {
    private val logger = LoggerFactory.getLogger(UserDataService::class.java)
    private val mapper = jacksonObjectMapper()
    private val data = ConcurrentHashMap<String, UserRoleData>()

    @PostConstruct
    fun load() {
        val file = File(filePath)
        if (!file.exists()) return
        try {
            val loaded: Map<String, UserRoleData> = mapper.readValue(file)
            data.putAll(loaded)
            logger.info("Loaded ${data.size} user role entries from $filePath")
        } catch (e: Exception) {
            logger.error("Failed to load user data from $filePath: ${e.message}", e)
        }
    }

    fun save(userId: String, wowClass: String, wowSpec: String) {
        data[userId] = UserRoleData(userId, wowClass, wowSpec)
        persist()
    }

    fun remove(userId: String) {
        if (data.remove(userId) != null) persist()
    }

    fun get(userId: String): UserRoleData? = data[userId]

    fun getAll(): Map<String, UserRoleData> = data.toMap()

    private fun persist() {
        try {
            mapper.writeValue(File(filePath), data)
        } catch (e: Exception) {
            logger.error("Failed to persist user data to $filePath: ${e.message}", e)
        }
    }
}
