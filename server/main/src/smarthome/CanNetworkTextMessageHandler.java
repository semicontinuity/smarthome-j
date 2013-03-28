package smarthome;

import openbox.patterns.Consumer;
import org.apache.log4j.Logger;
import org.xlightweb.*;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;


public class CanNetworkTextMessageHandler implements IWebSocketHandler {

    static final Logger LOGGER = Logger.getLogger(CanNetworkTextMessageHandler.class);

    private CanNetwork canNetwork;
    private final Map<IWebSocketConnection, Consumer<byte[], IOException>> proxies = new IdentityHashMap<>();


    public void setCanNetwork(final CanNetwork canNetwork) {
        this.canNetwork = canNetwork;
    }


    @Override
    public void onConnect(final IWebSocketConnection connection) throws IOException {
        final CanNetwork.Endpoint endpoint = endpoint(connection);
        final Consumer<byte[], IOException> proxy = new Consumer<byte[], IOException>() {
            @Override
            public void put(byte[] value) throws IOException {
                final StringBuilder builder = new StringBuilder();
                //noinspection ForLoopReplaceableByForEach
                for (int j = 0; j < value.length; j++) {
                    builder.append(String.format("%02X", value[j]));
                }
                final String msg = builder.toString();
                LOGGER.debug("Writing message '" + msg + "' to websocket connection " + connection.getId());
                connection.writeMessage(new TextMessage(msg));
            }
        };
        endpoint.listeners.add(proxy);
        proxies.put(connection, proxy);
    }


    @Override
    public void onMessage(final IWebSocketConnection connection) throws IOException {
        final CanNetwork.Endpoint endpoint = endpoint(connection);
        endpoint.put(hexStringToBytes(connection.readTextMessage().toString()));
    }


    @Override
    public void onDisconnect(final IWebSocketConnection connection) throws IOException {
        final CanNetwork.Endpoint endpoint = endpoint(connection);
        endpoint.listeners.remove(proxies.remove(connection));
    }


    CanNetwork.Endpoint endpoint(final IWebSocketConnection connection) throws IOException {
        final String requestURI = connection.getUpgradeRequestHeader().getRequestURI();
        final int index = requestURI.indexOf('/', 1);
        if (index == -1) connection.close();

        final String endpointAddress = requestURI.substring(index + 1);
        return canNetwork.endpoint(endpointAddress);
    }


    static byte[] hexStringToBytes(String text) {
        final byte[] data = new byte[text.length()>>1];
        for (int i = 0; i < data.length; i++) {
            final String substring = text.substring(i << 1, (i << 1) + 2);
            data[i] = Byte.parseByte(substring, 16);
        }
        return data;
    }
}
