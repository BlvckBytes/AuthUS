package me.blvckbytes.authus.rest.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.datatype.joda.JodaModule
import me.blvckbytes.authus.rest.exception.InvalidDateTimeException
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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

        // Register the serializer for DateTime data-types
        module.addSerializer(DateTime::class.java, object : StdScalarSerializer<DateTime>(DateTime::class.java) {
            override fun serialize(value: DateTime?, gen: JsonGenerator?, provider: SerializerProvider?) {
                gen?.writeString(
                    // Keep null values, as print would fallback to now()
                    if (value != null ) ISODateTimeFormat.dateTime().withZoneUTC().print(value) else null
                )
            }
        })

        // Register the deserializer for DateTime data-types
        module.addDeserializer(DateTime::class.java, object : StdScalarDeserializer<DateTime>(DateTime::class.java) {
            override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): DateTime {
                // Needs to be a string value
                if (p?.currentToken == JsonToken.VALUE_STRING)
                    // Try to parse from ISO format
                    try {
                        return ISODateTimeFormat.dateTime().withZoneUTC().parseDateTime(p.text.trim())

                    // Not parsable, continue execution
                    } catch (e: Exception) {}

                // Throw error
                throw InvalidDateTimeException(p?.text?.trim() ?: "")
            }
        })

        return module
    }
}