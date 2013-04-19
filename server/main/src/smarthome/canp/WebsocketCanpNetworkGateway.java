package smarthome.canp;

import openbox.patterns.Consumer;
import org.apache.log4j.Logger;
import org.xlightweb.IWebSocketConnection;
import org.xlightweb.IWebSocketHandler;
import org.xlightweb.WebSocketMessage;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;


/**
 * Websocket gateway to a CAN network with devices talking CANP protocol.
 * Translates websocket messages to CAN frames and back.
 * Websocket connections are established to CANP endpoints.
 * The URI scheme is "/.../{canp_host}/{canp_endpoint}".
 */
public class WebSocketCanpNetworkGateway implements IWebSocketHandler {
    static final Logger LOGGER = Logger.getLogger(WebSocketCanpNetworkGateway.class);
    private final Map<IWebSocketConnection, Consumer<byte[], IOException>> proxies = new IdentityHashMap<>();
    private CanpNetwork canpNetwork;
    private WebSocketCodec webSocketCodec;


    public void setCanpNetwork(final CanpNetwork canpNetwork) {
        this.canpNetwork = canpNetwork;
    }


    public void setWebSocketCodec(final WebSocketCodec webSocketCodec) {
        this.webSocketCodec = webSocketCodec;
    }


    @Override
    public void onConnect(final IWebSocketConnection connection) throws IOException {
        final CanpNetwork.Endpoint endpoint = endpoint(connection);
        final Consumer<byte[], IOException> proxy = newProxy(connection);
        endpoint.listeners.add(proxy);
        proxies.put(connection, proxy);
    }


    @Override
    public void onMessage(final IWebSocketConnection connection) throws IOException {
        final CanpNetwork.Endpoint endpoint = endpoint(connection);
        endpoint.put(webSocketCodec.decodeMessage(connection.readMessage()));
    }


    @Override
    public void onDisconnect(final IWebSocketConnection connection) throws IOException {
        final CanpNetwork.Endpoint endpoint = endpoint(connection);
        endpoint.listeners.remove(proxies.remove(connection));
    }


    protected CanpNetwork.Endpoint endpoint(final IWebSocketConnection connection) throws IOException {
        final String requestURI = connection.getUpgradeRequestHeader().getRequestURI();
        final int index = requestURI.indexOf('/', 1);
        if (index == -1) connection.close();

        final String endpointAddress = requestURI.substring(index + 1);
        return canpNetwork.endpoint(endpointAddress);
    }


    protected Consumer<byte[], IOException> newProxy(final IWebSocketConnection connection) {
        return new Consumer<byte[], IOException>() {
            @Override
            public void put(byte[] value) throws IOException {
                final WebSocketMessage message = webSocketCodec.encodeMessage(value);
                LOGGER.debug("Writing message '" + message + "' to websocket connection " + connection.getId());
                connection.writeMessage(message);
            }
        };
    }

}
