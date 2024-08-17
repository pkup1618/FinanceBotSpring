package com.example.demo.services

import com.example.demo.entity.ChatMember
import com.example.demo.entity.Habit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.expression.ExpressionException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


/**
 * Сервис (обёртка над репозиторием) для всех взаимодействий с базой данных
 */
@Service
class ChatMemberService @Autowired constructor(
    private val jdbcTemplate: JdbcTemplate
) {

    /**
     * Метод установки локализации
     * @param id - id пользователя
     * @param locale - локаль, которую надо установить
     */
    @Transactional
    fun changeLocale(id: Long, locale: String) {

        val prettySql =
            """
            UPDATE chat_member
            SET locale = '$locale'
            WHERE id = $id
            """

        jdbcTemplate.update(prettySql)
    }

    @Transactional
    fun exist(id: Long): Boolean {

        val prettySql =
            """
            SELECT * FROM chat_member WHERE id = $id
            """

        val chatMember: List<ChatMember> = jdbcTemplate.query(prettySql) { rs, _ ->
            ChatMember(rs.getLong("id"), rs.getString("locale"))
        }

        return chatMember.isEmpty()
    }

    /**
     * Метод для добавления пользователя в базу данных
     * (По умолчанию добавляются пользователи с русской локализацией)
     * @param chatMember - пользователь, id нужно установить самостоятельно, взяв в telegram
     */
    @Transactional
    fun save(chatMember: ChatMember) {
        val prettySql =
            """
            INSERT INTO chat_member VALUES (${chatMember.id}, 'ru') 
            """

        jdbcTemplate.update(prettySql)
    }

    fun getChatMemberHabits(id: Long): List<Habit> {

        val prettySql =
            """
            SELECT * FROM habit 
            WHERE chatmember_id = $id
            """

        val habits: List<Habit> = jdbcTemplate.query(prettySql)
        { rs, _ ->
            Habit(
                rs.getLong("id"),
                rs.getLong("chatmember_id"),
                rs.getString("name"),
                rs.getString("description")
            )
        }

        return habits
    }

    /**
     * Метод для поиска пользователя в базе данных
     * @param id - id пользователя
     */
    fun findById(id: Long): ChatMember {

        val prettySql =
            """
            SELECT * FROM chat_member
            WHERE id = $id
            """

        val chatMember: ChatMember? = jdbcTemplate.queryForObject(prettySql)
        { rs, _ ->
            ChatMember(rs.getLong("id"), rs.getString("locale"))
        }

        return chatMember ?: throw ExpressionException("Такая запись отсутствует в базе данных")
    }

    fun addHabit(id: Long, userStateContainer: UserStateContainer?) {

        val prettySql =
            """
            INSERT INTO habit (chatmember_id, days, description, name) 
            VALUES ($id, NULL, '${userStateContainer?.habitDescription}', '${userStateContainer?.habitHeader}')    
            """

        jdbcTemplate.update(prettySql)
    }

    fun deleteHabitByName(id: Long?, name: String?) {

        val prettySql =
            """
            DELETE FROM habit
            WHERE chatmember_id = $id AND name = '$name'
            """

        jdbcTemplate.update(prettySql)
    }
}
