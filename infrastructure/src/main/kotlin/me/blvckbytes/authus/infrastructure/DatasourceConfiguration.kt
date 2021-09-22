package me.blvckbytes.authus.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Profile("!test")
@Configuration
open class DatasourceConfiguration {

    // Use datasource from config file
    @Bean
    @ConfigurationProperties("app.datasource")
    open fun dataSource(): DataSource? {
        return DataSourceBuilder.create().build()
    }
}