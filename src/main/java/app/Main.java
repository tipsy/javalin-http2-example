package app;

import io.javalin.Javalin;
import io.javalin.embeddedserver.jetty.EmbeddedJettyFactory;
import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Main {

    public static void main(String[] args) {

        Javalin app = Javalin.create()
            .embeddedServer(createHttp2Server())
            .enableStaticFiles("/public")
            .start();

        app.get("/", ctx -> ctx.result("Hello World"));

    }

    private static EmbeddedJettyFactory createHttp2Server() {
        return new EmbeddedJettyFactory(() -> {
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(8080);
            server.addConnector(connector);

            // HTTP Configuration
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSendServerVersion(false);
            httpConfig.setSecureScheme("https");
            httpConfig.setSecurePort(8443);

            // SSL Context Factory for HTTPS and HTTP/2
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(Main.class.getResource("/keystore.jks").toExternalForm()); // switch out this with your real keystore
            sslContextFactory.setKeyStorePassword("password"); // switch out this with your real keystore
            sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
            sslContextFactory.setProvider("Conscrypt");

            // HTTPS Configuration
            HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            // HTTP/2 Connection Factory
            HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConfig);
            ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
            alpn.setDefaultProtocol("h2");

            // SSL Connection Factory
            SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

            // HTTP/2 Connector
            ServerConnector http2Connector = new ServerConnector(server, ssl, alpn, h2, new HttpConnectionFactory(httpsConfig));
            http2Connector.setPort(8443);
            server.addConnector(http2Connector);

            ALPN.debug = false;

            return server;
        });
    }
}
