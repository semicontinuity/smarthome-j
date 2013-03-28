package gearbox.io;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface ByteChannelMultiplexer {
    public void write(int id, final ByteBuffer buffer) throws IOException;
}
