package com.example.demo.config

import com.example.demo.services.PropertiesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
open class BotConfig @Autowired constructor(private val propertiesService: PropertiesService) {

    @Bean
    open fun botOptions(): DefaultBotOptions {
        return DefaultBotOptions()
    }

    @Bean
    open fun botToken(): String {
        return propertiesService.loadTgBotProperties().getProperty("token")
    }

    @Bean
    open fun defaultBotSession(): TelegramBotsApi {
        return TelegramBotsApi(DefaultBotSession::class.java)
    }
}
