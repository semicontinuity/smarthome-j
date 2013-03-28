package gearbox.io;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface ByteChannelDemultiplexer {

    public int read(final ByteBuffer buffer) throws IOException;
}
