package smarthome.canp;

import org.xlightweb.WebSocketMessage;

import java.io.IOException;


public interface WebSocketCodec {
    byte[] decodeMessage(final WebSocketMessage message) throws IOException;

    WebSocketMessage encodeMessage(final byte[] value);
}
