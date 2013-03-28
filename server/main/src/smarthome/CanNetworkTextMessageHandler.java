package smarthome;

import org.xlightweb.*;

import java.io.IOException;


public class CanNetworkTextMessageHandler extends CanNetworkMessageHandler {

    @Override
    protected WebSocketMessage encodeMessage(byte[] value) {
        final StringBuilder builder = new StringBuilder();
        //noinspection ForLoopReplaceableByForEach
        for (int j = 0; j < value.length; j++) {
            builder.append(String.format("%02X", value[j]));
        }
        final String msg = builder.toString();
        return new TextMessage(msg);
    }


    @Override
    protected byte[] readMessage(final IWebSocketConnection connection) throws IOException {
        return hexStringToBytes(connection.readTextMessage().toString());
    }


    static byte[] hexStringToBytes(final String text) {
        final byte[] data = new byte[text.length()>>1];
        for (int i = 0; i < data.length; i++) {
            final String substring = text.substring(i << 1, (i << 1) + 2);
            data[i] = Byte.parseByte(substring, 16);
        }
        return data;
    }
}
