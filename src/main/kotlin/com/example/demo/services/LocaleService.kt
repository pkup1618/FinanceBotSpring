package com.example.demo.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.*

/**
 * Предоставляет все используемые в ресурсах строковые данные на нужном языке
 * Для их добавления в папку resources нужно добавить соответствующий файл локализации
 */
@Component
class LocaleService @Autowired constructor(private val messageSource: MessageSource) {

    /**
     * Получить локализованное сообщение
     * @param message свойство из .properties
     * @param locale локализация - ru, en и другие
     * @return локализованное сообщение
     */
    fun getMessage(message: String?, locale: String?): String {
        return messageSource!!.getMessage(message, null, Locale.forLanguageTag(locale))
    }
}
