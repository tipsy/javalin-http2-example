package app

import io.javalin.Javalin
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.HTTP2Cipher
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory

fun main() {

    val app = Javalin.create {
        it.server { createHttp2Server() }
        it.addStaticFiles("/public")
    }.start()

    app.get("/") { it.result("Hello World") }

}

private fun createHttp2Server(): Server {

    val alpn = ALPNServerConnectionFactory().apply {
        defaultProtocol = "h2"
    }

    val sslContextFactory = SslContextFactory().apply {
        keyStorePath = Main::class.java.getResource("/keystore.jks").toExternalForm() // replace with your real keystore
        setKeyStorePassword("password") // replace with your real password
        cipherComparator = HTTP2Cipher.COMPARATOR
        provider = "Conscrypt"
    }

    val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

    val httpsConfig = HttpConfiguration().apply {
        sendServerVersion = false
        secureScheme = "https"
        securePort = 8443
        addCustomizer(SecureRequestCustomizer())
    }

    val http2 = HTTP2ServerConnectionFactory(httpsConfig)

    val fallback = HttpConnectionFactory(httpsConfig)

    return Server().apply {
        //HTTP/1.1 Connector
        addConnector(ServerConnector(server).apply {
            port = 8080
        })
        // HTTP/2 Connector
        addConnector(ServerConnector(server, ssl, alpn, http2, fallback).apply {
            port = 8443
        })
    }

}
