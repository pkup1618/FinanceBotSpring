package com.example.demo.services

import com.example.demo.bot.Bot
import com.example.demo.entity.Habit
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage


@Component
class NotificationSender @Autowired constructor(
    private val threadPoolTaskScheduler: ThreadPoolTaskScheduler,
    private val chatMemberService: ChatMemberService,
    private val bot: Bot
) {
    @PostConstruct
    fun ex() {
        val habitsForSchedule: List<Habit> = chatMemberService.getAllTrackingHabits()

        for (habit in habitsForSchedule) {
            threadPoolTaskScheduler.schedule(
                MessagePrinterTask(bot, habit),
                CronTrigger(habit.notifictaionCron!!)
            )
        }
    }
}

class MessagePrinterTask(val bot: Bot, val habit: Habit) : Runnable {
    override fun run() {
        val notification = SendMessage()
        notification.setChatId(habit.chatMemberId)
        notification.text = habit.toString()
        bot.execute(notification)
    }
}