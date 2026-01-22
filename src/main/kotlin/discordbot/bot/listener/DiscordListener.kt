package discordbot.bot.listener

import dev.kord.core.Kord

interface DiscordListener {
    fun register(kord: Kord)
}