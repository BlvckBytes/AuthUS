package me.blvckbytes.authus.application.config

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component

@Component
class TomcatConfig : WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    override fun customize(factory: TomcatServletWebServerFactory?) {
        factory?.addConnectorCustomizers(TomcatConnectorCustomizer { connector ->
            connector.setAttribute("relaxedQueryChars", "<>[\\]^`{|}")
        })
    }
}