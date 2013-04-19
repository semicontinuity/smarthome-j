package gearbox.io;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface ByteChannelDemultiplexer {

    /**
     * Read a tagged datagram from this object.
     * @param buffer the buffer, where to place the datagram
     * @return the tag of the datagram
     * @throws IOException
     */
    public int read(final ByteBuffer buffer) throws IOException;
}
