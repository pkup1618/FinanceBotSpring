package com.example.demo.config

import com.example.demo.services.PropertiesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.*
import javax.sql.DataSource


@Configuration
open class PersistenceConfig @Autowired constructor(private val propertiesService: PropertiesService) {

    @Bean
    @Primary
    open fun modelDataSource(): DataSource {
        val dbConnectionInfo = propertiesService.loadDbConnectionProperties()

        return DataSourceBuilder
            .create()
            .username(dbConnectionInfo.getProperty("username"))
            .password(dbConnectionInfo.getProperty("password"))
            .url(dbConnectionInfo.getProperty("db_url"))
            .driverClassName(dbConnectionInfo.getProperty("driver"))
            .build()
    }
}