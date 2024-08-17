package com.example.demo.services

import com.example.demo.entity.ChatMember
import com.example.demo.entity.Habit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.expression.ExpressionException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
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

        val prettySql: String =
            """
            UPDATE chat_member
            SET locale = '$locale'
            WHERE id = $id
            """

        jdbcTemplate.update(prettySql)
    }

    @Transactional
    fun exist(id: Long): Boolean {

        val prettySql: String =
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
        /*
        if (!chatMemberRepository.existsById(chatMember.id)) {
        chatMember.locale = "ru"
        chatMemberRepository.save(chatMember)
        }
        */

        val prettySql: String =
            """
            INSERT INTO chat_member VALUES (${chatMember.id}, 'ru') 
            """ // todo bad

        jdbcTemplate.update(prettySql)
    }

    fun getChatMemberHabits(id: Long): List<Habit> {

        val prettySql: String =
            """
            "SELECT * FROM habit 
            WHERE chatmember_id = ?"
            """

        val namedParameters = MapSqlParameterSource("id", id)

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

        val prettySql: String =
            """
            SELECT * FROM chat_member
            WHERE id = $id
            """

        val chatMember: ChatMember? = jdbcTemplate.queryForObject(prettySql)
        { rs, _ ->
            ChatMember(rs.getLong("id"), rs.getString("locale"))
        }

        return chatMember ?: throw ExpressionException("Такая запись отсутствует в базе данных")
        // TODO handle exception
    }
}
