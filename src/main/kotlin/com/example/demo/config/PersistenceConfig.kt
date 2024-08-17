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

//    @Bean
//    open fun modelTransactionManager(): PlatformTransactionManager {
//        return JpaTransactionManager(Objects.requireNonNull(modelEntityManagerFactory().getObject()))
//    }
//
//    @Bean
//    open fun modelEntityManagerFactory(): LocalContainerEntityManagerFactoryBean {
//        val vendorAdapter : HibernateJpaVendorAdapter = HibernateJpaVendorAdapter()
//        vendorAdapter.setGenerateDdl(true)
//
//        val factoryBean = LocalContainerEntityManagerFactoryBean()
//        val prop = Properties()
//        prop.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
//        prop.setProperty("hibernate.ddl-auto", "update") //create on first start, update on second
//        factoryBean.setJpaProperties(prop)
//
//        factoryBean.dataSource = modelDataSource()
//        factoryBean.jpaVendorAdapter = vendorAdapter
//        factoryBean.setPackagesToScan("com.example.demo.data")
//
//        return factoryBean
//    }

    @Bean
    @Primary
    open fun modelDataSource(): DataSource {
        val dbConnectionInfo = propertiesService.loadDbConnectionProperties()

        return DataSourceBuilder
            .create()
            .username(dbConnectionInfo.getProperty("username"))
            .password(dbConnectionInfo.getProperty("password"))
            .url(dbConnectionInfo.getProperty("db_url"))
            .driverClassName("org.postgresql.Driver")
            .build()


        //todo это работает неправильно. Мне нужно загружать данные во время работы, а не компиляции
    }
}