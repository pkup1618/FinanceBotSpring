package com.example.demo.entity.old

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id


@Entity(name = "chat_member")
class ChatMember() {

    constructor(id: Long, locale: String) : this() {
        this.id = id
        this.locale = locale
    }

    @Id
    @Column(name = "id", nullable = false)
    var id: Long = 0

    @Column(name = "locale")
    var locale: String = "ru"
}