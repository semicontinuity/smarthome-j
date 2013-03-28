package smarthome;

import org.xlightweb.BinaryMessage;
import org.xlightweb.IWebSocketConnection;
import org.xlightweb.WebSocketMessage;

import java.io.IOException;


public class CanNetworkBinaryMessageHandler extends CanNetworkMessageHandler {

    @Override
    protected WebSocketMessage encodeMessage(byte[] value) {
        return new BinaryMessage(value);
    }


    @Override
    protected byte[] readMessage(final IWebSocketConnection connection) throws IOException {
        return connection.readMessage().toBytes();
    }
}
