package smarthome;

import openbox.patterns.Consumer;
import org.apache.log4j.Logger;
import org.xlightweb.IWebSocketConnection;
import org.xlightweb.IWebSocketHandler;
import org.xlightweb.WebSocketMessage;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;


public abstract class CanNetworkMessageHandler implements IWebSocketHandler {
    static final Logger LOGGER = Logger.getLogger(CanNetworkMessageHandler.class);
    private final Map<IWebSocketConnection, Consumer<byte[], IOException>> proxies = new IdentityHashMap<>();
    private CanNetwork canNetwork;


    public void setCanNetwork(final CanNetwork canNetwork) {
        this.canNetwork = canNetwork;
    }


    @Override
    public void onConnect(final IWebSocketConnection connection) throws IOException {
        final CanNetwork.Endpoint endpoint = endpoint(connection);
        final Consumer<byte[], IOException> proxy = newProxy(connection);
        endpoint.listeners.add(proxy);
        proxies.put(connection, proxy);
    }


    @Override
    public void onMessage(final IWebSocketConnection connection) throws IOException {
        final CanNetwork.Endpoint endpoint = endpoint(connection);
        endpoint.put(readMessage(connection));
    }


    @Override
    public void onDisconnect(final IWebSocketConnection connection) throws IOException {
        final CanNetwork.Endpoint endpoint = endpoint(connection);
        endpoint.listeners.remove(proxies.remove(connection));
    }


    protected CanNetwork.Endpoint endpoint(final IWebSocketConnection connection) throws IOException {
        final String requestURI = connection.getUpgradeRequestHeader().getRequestURI();
        final int index = requestURI.indexOf('/', 1);
        if (index == -1) connection.close();

        final String endpointAddress = requestURI.substring(index + 1);
        return canNetwork.endpoint(endpointAddress);
    }


    protected Consumer<byte[], IOException> newProxy(final IWebSocketConnection connection) {
        return new Consumer<byte[], IOException>() {
            @Override
            public void put(byte[] value) throws IOException {
                final WebSocketMessage message = encodeMessage(value);
                LOGGER.debug("Writing message '" + message + "' to websocket connection " + connection.getId());
                connection.writeMessage(message);
            }
        };
    }

    protected abstract byte[] readMessage(IWebSocketConnection connection) throws IOException;

    protected abstract WebSocketMessage encodeMessage(byte[] value);
}
