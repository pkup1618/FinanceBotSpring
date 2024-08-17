package com.example.demo.entity.old

import jakarta.persistence.*
import java.time.DayOfWeek

@Entity(name = "habit")
class Habit() {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne
    @JoinColumn(name = "chatmember_id")
    var chatMember: ChatMember? = null

    var name: String? = null

    var description: String? = null

    var days: List<DayOfWeek>? = null

    override fun toString(): String {
        return """
                Название: $name
                Описание: $description
                
                """.trimIndent()
    }
}


