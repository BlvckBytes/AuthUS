package me.blvckbytes.authus.rest.config

import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.datetime.joda.DateTimeFormatterFactory
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class AppConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(PageCursorParamResolver) // Register regex request parameter processor
    }

    @Bean
    open fun corsConfigurer(): WebMvcConfigurer? {
        return object : WebMvcConfigurer {

            // Allow all known HTTP-Methods on every path existing, regarding CORS-policy
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
            }
        }
    }

    @Bean
    open fun jacksonJodaModule(): JodaModule? {
        val module = JodaModule()
        val formatterFactory = DateTimeFormatterFactory()

        // Set the date format to ISO-datetime
        formatterFactory.setIso(DateTimeFormat.ISO.DATE_TIME)

        // Register the serializer for DateTime data-types
        module.addSerializer(
            DateTime::class.java, DateTimeSerializer(
                JacksonJodaDateFormat(
                    formatterFactory.createDateTimeFormatter().withZoneUTC()
                )
            )
        )

        // Default zone is always UTC, especially important for deserialize
        DateTimeZone.setDefault(DateTimeZone.UTC)
        return module
    }
}