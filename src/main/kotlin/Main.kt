package app

import io.javalin.Javalin
import io.javalin.embeddedserver.jetty.EmbeddedJettyFactory
import org.eclipse.jetty.alpn.ALPN
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.HTTP2Cipher
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.util.ssl.SslContextFactory

fun main(args: Array<String>) {

    val app = Javalin.create()
            .embeddedServer(createHttp2Server())
            .enableStaticFiles("/public")
            .start()

    app.get("/", { ctx -> ctx.result("Hello World") })

}

private fun createHttp2Server() = EmbeddedJettyFactory {

    val alpn = ALPNServerConnectionFactory().apply {
        defaultProtocol = "h2"
    }

    val sslContextFactory = SslContextFactory().apply {
        keyStorePath = Main::class.java.getResource("/keystore.jks").toExternalForm() // switch out this with your real keystore
        setKeyStorePassword("password") // switch out this with your real keystore
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

    Server().apply {
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
