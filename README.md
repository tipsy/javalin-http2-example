A fully working HTTP2 example using Jetty and Javalin

Javalin will run on any Jetty server you provide it, and Jetty supports HTTP2 out of box. Here is how to set it up:

First you need to generate the keystore using the following command

    keytool -genkey -alias jetty -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
    
The example uses Conscrypt as the SSL provider, which is the [recommended by Jetty](https://webtide.com/conscrypting-native-ssl-for-jetty/).

The ALPN API version you need to use is specific to your Java version (see [Jetty docs](https://www.eclipse.org/jetty/documentation/9.4.x/alpn-chapter.html#alpn-versions))

When you run the example there will be a HTTP1.1 server on port 8080, and HTTP2 server on 8443 ()with fall back to SSL HTTP 1.1)

Open Chrome and go to https://localhost:8443 you'll see in the Developer tools under network tab, you are connecting using "h2", which means HTTP2.

If you go to `/static-files-test.html`, open the Network tab and set your emulated network to 3G, you'll notice the difference:

