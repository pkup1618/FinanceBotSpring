package com.example.demo

import com.example.demo.services.MessageHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
//@EnableJpaRepositories(
//    entityManagerFactoryRef = "modelEntityManagerFactory",
//    transactionManagerRef = "modelTransactionManager",
//)
open class FinanceBotApplication

fun main(args: Array<String>) {
    val ctx: ConfigurableApplicationContext = runApplication<FinanceBotApplication>(*args)
    val messageHandler: MessageHandler = ctx.getBean("messageHandler", MessageHandler::class.java)
    messageHandler.start()
}