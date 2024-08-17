package com.example.demo.bot

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Component
class BotInitializer @Autowired constructor(
    private val bot: Bot,
    private val telegramBotsApi: TelegramBotsApi
) {

    @PostConstruct
    @Throws(TelegramApiException::class)
    fun initBot() {
        telegramBotsApi.registerBot(bot) // Starting LongPolling bot
    }
}
