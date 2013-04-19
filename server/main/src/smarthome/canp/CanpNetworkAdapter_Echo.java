package smarthome.canp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * CanpNetworkAdapter that echoes all frames sent to it back.
 */
@SuppressWarnings("UnusedDeclaration")
public class CanpNetworkAdapter_Echo extends CanpNetworkAdapter {

    BlockingQueue<Integer> qId = new ArrayBlockingQueue<>(16);
    BlockingQueue<byte[]> qData = new ArrayBlockingQueue<>(16);


    @Override
    public void write(int id, final ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        qId.add(id);
        qData.add(data);

        notifyAll();
    }


    @Override
    public int read(final ByteBuffer buffer) throws IOException {
        try {
            buffer.put(qData.take());
            return qId.take();
        }
        catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
