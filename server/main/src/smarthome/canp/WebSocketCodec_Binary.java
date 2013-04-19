package smarthome.canp;

import org.xlightweb.BinaryMessage;
import org.xlightweb.WebSocketMessage;

import java.io.IOException;


public class WebSocketCodec_Binary implements WebSocketCodec {

    @Override
    public WebSocketMessage encodeMessage(byte[] value) {
        return new BinaryMessage(value);
    }


    @Override
    public byte[] decodeMessage(final WebSocketMessage message) throws IOException {
        return message.toBytes();
    }
}
