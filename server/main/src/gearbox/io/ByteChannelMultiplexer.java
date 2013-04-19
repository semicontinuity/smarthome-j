package gearbox.io;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface ByteChannelMultiplexer {

    /**
     * Write a tagged datagram to this object.
     * @param buffer the buffer with the datagram
     * @param id the tag of the datagram
     * @throws IOException
     */
    public void write(int id, final ByteBuffer buffer) throws IOException;
}
