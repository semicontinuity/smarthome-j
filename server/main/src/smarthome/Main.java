package smarthome;

import gearbox.server.http.xlightweb.FileRequestHandler;
import gearbox.server.http.xlightweb.Server;
import org.apache.log4j.BasicConfigurator;
import smarthome.canp.CanpNetwork;
import smarthome.canp.CanpNetworkAdapter_SocketCAN;
import smarthome.canp.CanpNetworkSegment;
import smarthome.canp.WebSocketCanpNetworkGateway;
import smarthome.canp.WebSocketCodec;
import smarthome.canp.WebSocketCodec_Binary;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;


public class Main {

    private static CountDownLatch shutdownLatch = new CountDownLatch(1);


    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        installShutdownHook();
        run();
    }


    static void run() throws IOException, InterruptedException {
        final Server httpServer = new Server();
        httpServer.setHost(InetAddress.getByName(System.getProperty("httpServer.host")));
        httpServer.setPort(Integer.parseInt(System.getProperty("httpServer.port")));

        final FileRequestHandler staticContentHandler = new FileRequestHandler();
        staticContentHandler.setPath(new File(System.getProperty("app.www.dir")));
        httpServer.put("/view/*", staticContentHandler);

        final CanpNetworkAdapter_SocketCAN canpNetworkAdapter = new CanpNetworkAdapter_SocketCAN();
        canpNetworkAdapter.setCanInterface(System.getProperty("can.iface"));

        final CanpNetworkSegment canpNetworkSegment0 = new CanpNetworkSegment();
        canpNetworkSegment0.setId(0);
        canpNetworkSegment0.setCanpNetworkAdapter(canpNetworkAdapter);


        final CanpNetwork canpNetwork = new CanpNetwork();
        canpNetwork.add(canpNetworkSegment0);


        final WebSocketCodec webSocketCodec = new WebSocketCodec_Binary();


        final WebSocketCanpNetworkGateway webSocketCanpNetworkGateway = new WebSocketCanpNetworkGateway();
        webSocketCanpNetworkGateway.setCanpNetwork(canpNetwork);
        webSocketCanpNetworkGateway.setWebSocketCodec(webSocketCodec);
        httpServer.put("/canp/*", webSocketCanpNetworkGateway);


        canpNetworkAdapter.start();
        canpNetworkSegment0.start();
        httpServer.start();


        shutdownLatch.await();  // will not block if shutdown has been initiated


        httpServer.stop();
        canpNetworkSegment0.stop();
        canpNetworkAdapter.stop();
    }


    static void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                public void run() {
                    shutdownLatch.countDown();  // wake up main thread, it will stop services
                }
            }
        );
    }
}
