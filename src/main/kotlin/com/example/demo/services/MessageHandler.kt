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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

@Component
class MessageHandler @Autowired constructor(
    private val chatMemberService: ChatMemberService,
    private val bot: Bot,
    private val localeService: LocaleService
) : Thread() {

    private val userStates: MutableMap<Long, UserStateContainer> = ConcurrentHashMap()


    //TODO change on timer task
    /**
     * Запускает цикл обработки сообщений, каждые 0.5 секунды
     * выделяется 1 поток на группу сообщений, пришедших за это время
     */
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

    /**
     * Основной метод обработки сообщений (корень всей обработки)
     * Распознаёт, является ли сообщение командой или зарезервированным
     * и в соответствии с этим вызывает нужный метод обработки
     * @param update - сообщение
     */
    private fun processMessage(update: Update) {
        if (!update.hasMessage()) {
            return
        }

        val message = update.message
        val chatId = message.chatId
        val userId = message.from.id

        log.info("RECEIVED MESSAGE!")
        log.info("chatId : {}", chatId)
        log.info("userId : {}", userId)
        log.info("\n")

        if (chatMemberService.exist(chatId)) {
            saveUserInfo(userId)
        }

        userStates.putIfAbsent(userId, UserStateContainer())
        val locale = chatMemberService.findById(userId).locale


        if (message.isCommand) {
            when (parseCommand(update)) {
                BotCommands.START -> {
                    sendMessage(userId, locale, "startMessage")
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.HELP -> {
                    sendMessage(userId, locale, "helpMessage")
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.LOCALE -> {
                    sendChangeLocaleMessage(userId, locale)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.SET_RU_LOCALE -> {
                    changeLocale(userId, "ru")
                    var newLocale = "ru"
                    sendMessage(userId, newLocale, "locale.changeSuccess")
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.SET_EN_LOCALE -> {
                    changeLocale(userId, "en")
                    var newLocale = "en"
                    sendMessage(userId, locale, "locale.changeSuccess")
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.MENU -> {
                    sendMenuMessage(userId, locale)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.MY_HABITS -> {
                    sendHabits(userId, locale)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                BotCommands.ADD_HABIT -> {
                    sendHabitAdditionForm(userId, locale)
                    userStates[userId]?.changeState(ADDING_HABIT_HEADER)
                }

                BotCommands.REMOVE_HABIT -> {
                    sendHabitRemovementForm(message)
                    userStates[userId]?.changeState(DELETING_HABIT)
                }

                BotCommands.REMOVE_HABIT -> TODO()
                BotCommands.EDIT_HABIT -> TODO()
                BotCommands.STATS_TODAY -> TODO()
                BotCommands.STATS_YESTERDAY -> TODO()
                BotCommands.NOTIFICATIONS -> TODO()
                BotCommands.NOTIFICATIONS_ENABLE -> TODO()
                BotCommands.NOTIFICATIONS_DISABLE -> TODO()
                BotCommands.UNKNOWN -> TODO()
            }
        } else {
            when (userStates[userId]?.userState) {
                UNNECESSARY -> {
                    handleLikeEcho(message)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                ADDING_HABIT_HEADER -> {
                    parseHabitHeader(message)
                    userStates[userId]?.changeState(ADDING_HABIT_BODY)
                }

                ADDING_HABIT_BODY -> {
                    parseHabitDescription(message)
                    userStates[userId]?.changeState(ADDING_HABIT_NOTIFICATION_CRON)
                }

                ADDING_HABIT_NOTIFICATION_CRON -> {
                    parseHabitNotificationCron(message)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                DELETING_HABIT -> {
                    parseHabitName(message)
                    userStates[userId]?.changeState(UNNECESSARY)
                }

                null -> TODO()

            }
        }
    }




    private fun sendHabitRemovementForm(message: Message) {
        val text =
"""
Для того, чтобы удалить привычку, введите название привычки.  

Привычка с данным названием будет удалена
"""
        val answer = SendMessage()
        answer.setChatId(message.chatId)
        answer.text = text

        bot.execute(answer)

        sendHabits(message.chatId, locale = "ru")
    }

    private fun parseHabitHeader(message: Message) {
        userStates[message.chatId]?.habitHeader = message.text
        userStates[message.chatId]?.userState = ADDING_HABIT_BODY

        val text =
            """
            Теперь отправьте только описание привычки
            """

        val answer = SendMessage()
        answer.setChatId(message.chatId)
        answer.text = text

        bot.execute(answer)
    }

    private fun parseHabitDescription(message: Message) {
        userStates[message.chatId]?.habitDescription = message.text
        userStates[message.chatId]?.userState = ADDING_HABIT_BODY

        val text =
            """
            Теперь отправьте время отправки привычки в формате CRON
            
            СЕК МИН ЧАС МЕС ГОД ДЕНЬНЕ_ДЕЛИ
            """

        val answer = SendMessage()
        answer.setChatId(message.chatId)
        answer.text = text

        bot.execute(answer)
    }

    private fun parseHabitNotificationCron(message: Message) {

        userStates[message.chatId]?.notifictaionCron = message.text
        userStates[message.chatId]?.userState = ADDING_HABIT_BODY

        chatMemberService.addHabit(message.chatId, userStates[message.chatId])

        val text = "привычка сохранена"

        val answer = SendMessage()
        answer.setChatId(message.chatId)
        answer.text = text

        bot.execute(answer)
    }

    private fun parseHabitName(message: Message) {
        val habitName = message.text
        chatMemberService.deleteHabitByName(message.chatId, habitName)

        val answer = SendMessage()
        answer.setChatId(message.chatId)
        answer.text = """
            Привычка удалена
        """.trimIndent()

        bot.execute(answer)
    }

    private fun saveUserInfo(id: Long) {
        val chatMember = ChatMember(id, "ru") //todo make it changeable
        chatMemberService.save(chatMember)
    }

    /**
     * Метод установки локализации пользователю
     * @param userId id пользователя
     * @param locale локализация (ru / en)
     */
    private fun changeLocale(userId: Long, locale: String) {
        chatMemberService.changeLocale(userId, locale)
    }

    /**
     * Метод отправки простого текстового сообщения
     * @param chatId чат, в который отправляем сообщение
     * @param locale на каком языке
     * @param message текст сообщения
     */
    private fun sendMessage(chatId: Long, locale: String, message: String) {
        val answer = SendMessage()
        answer.text = localeService.getMessage(message, locale)
        answer.setChatId(chatId)

        try {
            bot.execute(answer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Отправляет текстовое сообщение с просьбой сменить язык
     * и 2 кнопки, с помощью которых можно выбрать язык
     * @param chatId идентификатор пользователя
     * @param locale на каком языке отправлять сообщение
     */
    private fun sendChangeLocaleMessage(chatId: Long, locale: String) {
        val keyboardRows = ArrayList<KeyboardRow>()
        val buttons = ArrayList<KeyboardButton>()

        val ruLocaleButton = KeyboardButton.builder().text("RU \uD83C\uDDF7\uD83C\uDDFA").build()
        val enLocaleButton = KeyboardButton.builder().text("EN \uD83C\uDDFA\uD83C\uDDF8").build()

        buttons.add(ruLocaleButton)
        buttons.add(enLocaleButton)

        keyboardRows.add(KeyboardRow(buttons))

        val replyKeyboardMarkup = ReplyKeyboardMarkup.builder().keyboard(keyboardRows).build()

        val message = SendMessage()

        message.replyMarkup = replyKeyboardMarkup
        message.setChatId(chatId)
        message.text = localeService.getMessage("locale.chooseLanguage", locale)

        try {
            bot.execute(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMenuMessage(chatId: Long, locale: String) {
        val keyboard = ArrayList<KeyboardRow>()

        val row1 = ArrayList<KeyboardButton>()
        val myHabitsButton = KeyboardButton.builder().text("Мои привычки").build()
        row1.add(myHabitsButton)

        val row2 = ArrayList<KeyboardButton>()
        val statsTodayButton = KeyboardButton.builder().text("Статистика за сегодня").build()
        row2.add(statsTodayButton)
        val statsYesterdayButton = KeyboardButton.builder().text("Статистика за вчера").build()
        row2.add(statsYesterdayButton)

        val row3 = ArrayList<KeyboardButton>()
        val alertsSettingsButton = KeyboardButton.builder().text("Настройки уведомлений").build()
        row3.add(alertsSettingsButton)

        keyboard.add(KeyboardRow(row1))
        keyboard.add(KeyboardRow(row2))
        keyboard.add(KeyboardRow(row3))

        val replyKeyboardMarkup = ReplyKeyboardMarkup.builder().keyboard(keyboard).build()

        val message = SendMessage()
        message.replyMarkup = replyKeyboardMarkup
        message.setChatId(chatId)
        message.text = localeService.getMessage("locale.chooseLanguage", locale)

        try {
            bot.execute(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun sendHabits(userId: Long, locale: String) {
        val habits = chatMemberService.getChatMemberHabits(userId)
        val result = habits.stream()
            .map { obj: Habit -> obj.toString() }
            .collect(Collectors.joining("\n"))

        val message = SendMessage()
        message.setChatId(userId)
        message.text = result

        try {
            bot.execute(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendHabitAdditionForm(userId: Long, locale: String) {
        val text =
"""
Для того, чтобы добавить новую привычку, поочерёдно введите её название и описание

В данном сообщении отправтье только название привычки
"""
        val message = SendMessage()
        message.setChatId(userId)
        message.text = text

        bot.execute(message)
    }

    private fun parseCommand(update: Update): BotCommands {
        val message = update.message
        val text = message.text

        return when (text) {
            "/start" -> {
                BotCommands.START
            }

            "/help" -> {
                BotCommands.HELP
            }

            "/locale" -> {
                BotCommands.LOCALE
            }

            "/set_ru_locale \uD83C\uDDF7\uD83C\uDDFA" -> {
                BotCommands.SET_RU_LOCALE
            }

            "/set_en_locale \uD83C\uDDFA\uD83C\uDDF8" -> {
                BotCommands.SET_EN_LOCALE
            }

            "/menu" -> {
                BotCommands.MENU
            }

            "/my_habits" -> {
                BotCommands.MY_HABITS
            }

            "/add_habit" -> {
                BotCommands.ADD_HABIT
            }

            "/remove_habit" -> {
                BotCommands.REMOVE_HABIT
            }

            "/edit_habit" -> {
                BotCommands.EDIT_HABIT
            }

            "/stats_today" -> {
                BotCommands.STATS_TODAY
            }

            "/stats_yesterday" -> {
                BotCommands.STATS_YESTERDAY
            }

            "/notifications" -> {
                BotCommands.NOTIFICATIONS
            }

            "/notifications_enable" -> {
                BotCommands.NOTIFICATIONS_ENABLE
            }

            "/notifications_disable" -> {
                BotCommands.NOTIFICATIONS_DISABLE
            }

            else -> {
                BotCommands.UNKNOWN
            }
        }
    }

    /**
     * Отвечает на сообщение тем же текстом
     * @param message сообщение
     */
    private fun handleLikeEcho(message: Message) {
        val answer = SendMessage()
        answer.text = message.text
        answer.setChatId(message.chatId)

        try {
            bot.execute(answer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MessageHandler::class.java)
    }
}