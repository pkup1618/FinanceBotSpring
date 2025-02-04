package com.example.demo.services

import com.example.demo.entity.ChatMember
import com.example.demo.entity.Habit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class ChatMemberService @Autowired constructor(
    private val jdbcTemplate: JdbcTemplate
) {
    @Transactional
    fun exist(id: Long): Boolean {
        val prettySql = """
            SELECT * FROM chat_member WHERE id = $id
        """

        val chatMember: List<ChatMember> = jdbcTemplate.query(prettySql) { rs, _ ->
            ChatMember(rs.getLong("id"))
        }

        return chatMember.isEmpty()
    }

    @Transactional
    fun save(chatMember: ChatMember) {
        val prettySql = """
            INSERT INTO chat_member VALUES (${chatMember.id}) 
        """

        jdbcTemplate.update(prettySql)
    }

    fun getChatMemberHabits(id: Long): List<Habit> {
        val prettySql = """
            SELECT * FROM habit 
            WHERE chat_member_id = $id
        """

        val habits: List<Habit> = jdbcTemplate.query(prettySql)
        { rs, _ ->
            Habit(
                rs.getLong("id"),
                rs.getLong("chat_member_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("notification_cron")
            )
        }

        return habits
    }

    fun getAllTrackingHabits(): List<Habit> {
        val prettySql = """
            SELECT * FROM habit 
            WHERE notification_cron IS NOT NULL
        """

        val habits: List<Habit> = jdbcTemplate.query(prettySql)
        { rs, _ ->
            Habit(
                rs.getLong("id"),
                rs.getLong("chat_member_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("notification_cron")
            )
        }

        return habits
    }

    fun addHabit(userId: Long, userStateContainer: UserStateContainer?) {
        val prettySql = """
            INSERT INTO habit (description, name, chat_member_id, notification_cron) 
            VALUES (
                '${userStateContainer?.habitDescription}',
                '${userStateContainer?.habitName}',
                '${userId}',
                '${userStateContainer?.notifictaionCron}'
            )
        """

        jdbcTemplate.update(prettySql)
    }

    fun deleteHabitByName(id: Long?, name: String?) {
        val prettySql = """
            DELETE FROM habit
            WHERE chat_member_id = $id AND name = '$name'
        """

        jdbcTemplate.update(prettySql)
    }
}
