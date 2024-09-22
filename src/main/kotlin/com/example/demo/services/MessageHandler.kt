package com.example.demo.services

import com.example.demo.bot.Bot
import com.example.demo.entity.ChatMember
import com.example.demo.entity.Habit
import com.example.demo.services.UserState.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

@Component
class MessageHandler @Autowired constructor(
    private val chatMemberService: ChatMemberService,
    private val bot: Bot,
) : Thread() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MessageHandler::class.java)
    }

    private val userStates: MutableMap<Long, UserStateContainer> = ConcurrentHashMap()

    override fun run() {
        while (true) {
            if (!bot.receivedMessages.isEmpty()) {
                val thread = Thread {
                    while (!bot.receivedMessages.isEmpty()) {
                        processMessage(bot.receivedMessages.poll())
                    }
                }
                thread.start()

                try {
                    sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun processMessage(update: Update) {
        if (update.hasCallbackQuery())
            processInline(update)
        else if (update.hasMessage() && update.message.isCommand)
            processCommand(update)
        else (processMultiformTask(update))
    }

    private fun processInline(update: Update) {
        if (update.hasCallbackQuery()) {
            val callback = update.callbackQuery
            val userId = callback.from.id

            log.run {
                info("RECEIVED CALLBACK MESSAGE!")
                info("userId : {}", userId)
                info("\n")
            }

            if (chatMemberService.exist(userId)) {
                saveUserInfo(userId)
            }

            userStates.putIfAbsent(userId, UserStateContainer())
            val requestMessage = callback.message

            when (callback.data) {
                BotCommands.START.command -> {
                    sendStartMessage(requestMessage)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.HELP.command -> {
                    userStates[userId]?.changeState(UNNECESSARY)
                    sendHelpMessage(requestMessage)
                }

                BotCommands.MENU.command -> {
                    sendMenuMessage(requestMessage)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.MY_HABITS.command -> {
                    sendHabits(requestMessage)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.ADD_HABIT.command -> {
                    sendHabitAdditionForm(requestMessage)
                    userStates[userId]?.changeState(ADDING_HABIT_HEADER)
                }

                BotCommands.REMOVE_HABIT.command -> {
                    sendHabitRemovementForm(requestMessage)
                    userStates[userId]?.changeState(DELETING_HABIT)
                }

                BotCommands.EDIT_HABIT.command -> TODO()
                BotCommands.STATS_TODAY.command -> TODO()
                BotCommands.STATS_YESTERDAY.command -> TODO()
                BotCommands.NOTIFICATIONS.command -> TODO()
                BotCommands.NOTIFICATIONS_ENABLE.command -> TODO()
                BotCommands.NOTIFICATIONS_DISABLE.command -> TODO()
            }
        }
    }

    private fun processCommand(update: Update) {
        val requestMessage = update.message
        val userId = requestMessage.from.id

        log.run {
            info("RECEIVED SIMPLE MESSAGE")
            info("userId : {}", userId)
            info("\n")
        }

        if (chatMemberService.exist(userId)) {
            saveUserInfo(userId)
        }

        userStates.putIfAbsent(userId, UserStateContainer())

        if (requestMessage.isCommand) {
            when (requestMessage.text) {
                BotCommands.START.command -> {
                    sendStartMessage(requestMessage)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.HELP.command -> {
                    userStates[userId]?.changeState(UNNECESSARY)
                    sendHelpMessage(requestMessage)
                }

                BotCommands.MENU.command -> {
                    sendMenuMessage(requestMessage)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.MY_HABITS.command -> {
                    sendHabits(requestMessage)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.ADD_HABIT.command -> {
                    sendHabitAdditionForm(requestMessage)
                    userStates[userId]?.changeState(ADDING_HABIT_HEADER)
                }

                BotCommands.REMOVE_HABIT.command -> {
                    sendHabitRemovementForm(requestMessage)
                    userStates[userId]?.changeState(DELETING_HABIT)
                }

                BotCommands.REMOVE_HABIT.command -> TODO()
                BotCommands.STATS_TODAY.command -> TODO()
                BotCommands.STATS_YESTERDAY.command -> TODO()
                BotCommands.NOTIFICATIONS.command -> TODO()
                BotCommands.NOTIFICATIONS_ENABLE.command -> TODO()
                BotCommands.NOTIFICATIONS_DISABLE.command -> TODO()
            }
        }
    }

    private fun processMultiformTask(update: Update) {
        val requestMessage = update.message
        val userId = requestMessage.from.id

        log.run {
            info("RECEIVED MULTIFORM TASK PROCESS STEP")
            info("userId : {}", userId)
            info("\n")
        }

        if (chatMemberService.exist(userId)) {
            saveUserInfo(userId)
        }

        userStates.putIfAbsent(userId, UserStateContainer())

        when (userStates[userId]?.userState) {
            UNNECESSARY -> {
                handleLikeEcho(requestMessage)
                userStates[userId]?.changeState(UNNECESSARY)
            }

            ADDING_HABIT_HEADER -> {
                parseHabitHeader(requestMessage)
                userStates[userId]?.changeState(ADDING_HABIT_BODY)
            }

            ADDING_HABIT_BODY -> {
                parseHabitDescription(requestMessage)
                userStates[userId]?.changeState(ADDING_HABIT_NOTIFICATION_CRON)
            }

            ADDING_HABIT_NOTIFICATION_CRON -> {
                parseHabitNotificationCron(requestMessage)
                userStates[userId]?.changeState(UNNECESSARY)
            }

            DELETING_HABIT -> {
                parseHabitName(requestMessage)
                userStates[userId]?.changeState(UNNECESSARY)
            }

            null -> TODO()

        }
    }

    private fun sendHabitRemovementForm(requestMessage: Message) {
        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = """
            Для того, чтобы удалить привычку, введите название привычки.  
            Привычка с данным названием будет удалена
        """

        bot.execute(responseMessage)
        sendHabits(requestMessage)
    }

    private fun parseHabitHeader(requestMessage: Message) {
        userStates[requestMessage.chatId]?.habitName = requestMessage.text
        userStates[requestMessage.chatId]?.userState = ADDING_HABIT_BODY

        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = """
            Теперь отправьте описание привычки
        """

        bot.execute(responseMessage)
    }

    private fun parseHabitDescription(requestMessage: Message) {
        userStates[requestMessage.chatId]?.habitDescription = requestMessage.text
        userStates[requestMessage.chatId]?.userState = ADDING_HABIT_BODY

        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = """
            Теперь отправьте время отправки привычки в формате CRON
            
            СЕК МИН ЧАС МЕС ГОД ДЕНЬ_НЕДЕЛИ
        """

        bot.execute(responseMessage)
    }

    private fun parseHabitNotificationCron(requestMessage: Message) {
        userStates[requestMessage.chatId]?.notifictaionCron = requestMessage.text
        userStates[requestMessage.chatId]?.userState = UNNECESSARY

        chatMemberService.addHabit(requestMessage.chatId, userStates[requestMessage.chatId])

        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = "Привычка сохранена"

        bot.execute(responseMessage)
    }

    private fun parseHabitName(requestMessage: Message) {
        val habitName = requestMessage.text
        chatMemberService.deleteHabitByName(requestMessage.chatId, habitName)

        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = "Привычка удалена"

        bot.execute(responseMessage)
    }

    private fun saveUserInfo(id: Long) {
        val chatMember = ChatMember(id)
        chatMemberService.save(chatMember)
    }

    private fun sendMessage(userId: Long, messageText: String) {
        val responseMessage = SendMessage()
        responseMessage.setChatId(userId)
        responseMessage.text = messageText

        bot.execute(responseMessage)
    }



    private fun sendHabits(requestMessage: Message) {
        val habits = chatMemberService.getChatMemberHabits(requestMessage.chatId)
        val habitsTextView = habits.stream().map { obj: Habit -> obj.toString() }.collect(Collectors.joining("\n"))

        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = habitsTextView

        bot.execute(responseMessage)
    }

    private fun sendHabitAdditionForm(requestMessage: Message) {
        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = """
            Для того, чтобы добавить новую привычку, поочерёдно введите её название и описание
            
            В данном сообщении отправтье только название привычки
        """

        bot.execute(responseMessage)
    }

    private fun sendStartMessage(requestMessage: Message) {
        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = "Добрый день. Для большей информации перейдите во вкладку \"Помощь\""
        responseMessage.replyMarkup = InlineKeyboardMarkup.builder().keyboard(
            listOf(
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.builder()
                        .text("Помощь")
                        .callbackData("/help")
                        .build()
                ),
            )
        ).build()

        bot.execute(responseMessage)
    }

    private fun sendHelpMessage(requestMessage: Message) {
        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = """
            При помощи данного боты вы можете лучше управлять своей жизнью.
            На данный момент бот поддерживает возможности:
            
            1. Устанавливать напоминания (привычки)
            2... пока что всё
            
            Для начала работы с ботом нужно прописать команду /menu
            Эта команда - начало для любого действия. 
            Если в ходе работы что-то идёт не по плану, введите команду /menu
            
            Перейдите во вкладку МЕНЮ, чтобы управлять ботом
        """
        responseMessage.replyMarkup = InlineKeyboardMarkup.builder().keyboard(
            listOf(
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.builder()
                        .text("Меню")
                        .callbackData("/menu")
                        .build()
                ),
            )
        ).build()

        bot.execute(responseMessage)
    }

    private fun sendMenuMessage(requestMessage: Message) {
        val responseMessage = SendMessage()
        responseMessage.setChatId(requestMessage.chatId)
        responseMessage.text = "Выберите пункт меню"
        responseMessage.replyMarkup = InlineKeyboardMarkup.builder().keyboard(
            listOf(
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.builder()
                        .text("Мои привычки")
                        .callbackData("/my_habits")
                        .build()
                ),
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.builder()
                        .text("Добавить привычку")
                        .callbackData("/add_habit")
                        .build(),
                    InlineKeyboardButton.builder()
                        .text("Удалить привычку")
                        .callbackData("/remove_habit")
                        .build(),
                ),
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.builder()
                        .text("Статистика за сегодня")
                        .callbackData("/stats_today")
                        .build(),
                    InlineKeyboardButton.builder()
                        .text("Статистика за вчера")
                        .callbackData("/stats_yesterday")
                        .build(),
                ),
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.builder()
                        .text("Настройки уведомлений")
                        .callbackData("/notifications")
                        .build()
                ),
            )
        ).build()

        bot.execute(responseMessage)
    }

    private fun handleLikeEcho(requestMessage: Message) {
        val reponseMessage = SendMessage()
        reponseMessage.setChatId(requestMessage.chatId)
        reponseMessage.text = requestMessage.text

        bot.execute(reponseMessage)
    }
}