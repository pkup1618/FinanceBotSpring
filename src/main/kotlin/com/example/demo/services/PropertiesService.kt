package com.example.demo.services

import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


@Service
class PropertiesService {
    fun loadProperties(propsDir: String): Properties {
        val properties = Properties()

        try {
            properties.load(FileInputStream(propsDir))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return properties
    }

    fun loadDbConnectionProperties(): Properties {
        return loadProperties("db_connection_props.properties")
    }

    fun loadTgBotProperties(): Properties {
        return loadProperties("tg_bot_props.properties")
    }
}
