package smarthome;

import gearbox.io.MultiplexedByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;


public class CanNetworkAdapterEmu implements MultiplexedByteChannel {

    byte[] data;

    @Override
    public void write(int id, final ByteBuffer buffer) {
        synchronized (this) {
            data = new byte[buffer.remaining()];
            buffer.get(data);

            notifyAll();
        }
    }


    @Override
    public int read(final ByteBuffer buffer) throws IOException {
        synchronized (this) {
            try {
                if (data == null) wait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            buffer.put(data);
            data = null;
        }
        return 0x0600;
    }
}
