package smarthome;

import gearbox.server.http.xlightweb.FileRequestHandler;
import gearbox.server.http.xlightweb.Server;
import org.apache.log4j.BasicConfigurator;

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
        httpServer.setHost(InetAddress.getByName("127.0.0.1"));
        httpServer.setPort(80);

        final FileRequestHandler staticContentHandler = new FileRequestHandler();
        staticContentHandler.setPath(new File(System.getProperty("app.www.dir")));
        httpServer.put("/view/*", staticContentHandler);

        final CanNetworkAdapterEmu canNetworkAdapter0 = new CanNetworkAdapterEmu();
//        final CanNetworkAdapter canNetworkAdapter = new CanNetworkAdapter();
//        canNetworkAdapter.setCanInterface(System.getProperty("can.iface"));

        final CanNetworkSegment canNetworkSegment0 = new CanNetworkSegment();
        canNetworkSegment0.setId(0);
        canNetworkSegment0.setCanNetworkAdapter(canNetworkAdapter0);


        final CanNetwork canNetwork = new CanNetwork();
        canNetwork.add(canNetworkSegment0);


        final CanNetworkMessageHandler canNetworkMessageHandler = new CanNetworkBinaryMessageHandler();
        canNetworkMessageHandler.setCanNetwork(canNetwork);
        httpServer.put("/canp/*", canNetworkMessageHandler);


//        canNetworkAdapter.start();
        canNetworkSegment0.start();
        httpServer.start();


        shutdownLatch.await();  // will not block if shutdown has been initiated


        httpServer.stop();
        canNetworkSegment0.stop();
//        canNetworkAdapter.stop();
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
