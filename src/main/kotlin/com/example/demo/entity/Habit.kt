package com.example.demo.entity

import jakarta.persistence.*
import java.time.DayOfWeek


data class Habit(
    val id: Long,
    val chatMemberId: Long,
    val name: String,
    val description: String
) {

    override fun toString(): String {
        return """
                Название: $name
                Описание: $description
                """.trimIndent()
    }

}


