# A fully working HTTP2 example with Jetty and Javalin

Javalin will run on any embedded Jetty server, and Jetty supports HTTP2 (with a few additional dependencies). This guide explains how to set it all up.

The example includes a generated keystore, but you'll need to [generate your own](https://support.globalsign.com/customer/en/portal/articles/2121490-java-keytool---create-keystore) if you intended to use this for anything: 

* Open a command prompt in the same directory as Java keytool and run:
* `keytool -genkey -alias mydomain -keyalg RSA -keystore keystore.jks -keysize 2048`
    
The example uses Conscrypt as the SSL provider, which is [recommended by Jetty](https://webtide.com/conscrypting-native-ssl-for-jetty/).

The ALPN API version you need to use is specific to your JDK version. Find your correct version in the [Jetty docs](https://www.eclipse.org/jetty/documentation/9.4.x/alpn-chapter.html#alpn-versions)

When you run the example there will be a HTTP1.1 server on port 8080 and HTTP2 server on 8443 (with fallback to SSL HTTP 1.1).

To verify that everything is working, open Chrome (or your browser of choice), go to https://localhost:8443, and open the network tab in the devtols. You should see "h2" in the "Protocol" column.

If you go to `/static-files-test.html`, open the Network tab and set your emulated network to 3G, you'll notice the difference:

### HTTP1.1 
![http1](https://github.com/tipsy/javalin-http2-example/blob/master/readme/http1.PNG)

### HTTP2
![http2](https://github.com/tipsy/javalin-http2-example/blob/master/readme/http2.PNG)
