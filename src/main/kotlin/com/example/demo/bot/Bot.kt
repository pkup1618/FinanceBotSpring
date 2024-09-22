package com.example.demo.bot

import com.example.demo.services.PropertiesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


@Component
class Bot @Autowired internal constructor(
    botOptions: DefaultBotOptions,
    botToken: String,
    val propertiesService: PropertiesService,

) : TelegramLongPollingBot(botOptions, botToken) {

    val receivedMessages: Queue<Update> = ConcurrentLinkedQueue()

    /**
     * Метод для приема сообщений.
     * Все сообщения отправляются в хранилище.
     * @param update Содержит сообщение от пользователя.
     */
    override fun onUpdateReceived(update: Update) {
        receivedMessages.add(update)
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     * @return имя бота
     */
    override fun getBotUsername(): String {
        return "Презентационный бот"
        //todo вынести в файл свойств
    }
}