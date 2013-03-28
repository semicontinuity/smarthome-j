package gearbox.server.http.xlightweb;

import org.xlightweb.Context;
import org.xlightweb.IWebHandler;
import org.xlightweb.server.HttpServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;

@Resource
public class Server {
    InetAddress host;
    int port;

    transient Context context = new Context("");    // contextPath not supported yet
    transient HttpServer server;

    // @Inject
    @SuppressWarnings({"UnusedDeclaration"})
    public void setHost(final InetAddress host) { this.host = host; }

    // @Inject
    @SuppressWarnings({"UnusedDeclaration"})
    public void setPort(final int port) { this.port = port; }

    // @Inject
    @SuppressWarnings({"UnusedDeclaration"})
    public void add(final IWebHandler webHandler) {
        context.addHandler(webHandler);
    }

    // @Inject
    @SuppressWarnings({"UnusedDeclaration"})
    public void put(final String pattern, final IWebHandler webHandler) {
        context.addHandler(pattern, webHandler);
    }


    @PostConstruct
    public void start() throws IOException {
        server = new HttpServer(host, port, context);
        server.start();
    }


    @PreDestroy
    public void stop() {
        server.close();
    }
}
