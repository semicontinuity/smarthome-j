package smarthome.canp;

import gearbox.io.ByteChannelMultiplexer;
import gearbox.io.MultiplexedByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Abstraction of a segment of CAN network, with devices talking CANP protocol.
 * Each segment corresponds to different a physical CAN network.
 * The communication is done with the help of {@linkplain #canpNetworkAdapter}.
 */
public class CanpNetworkSegment implements ByteChannelMultiplexer {

    CanpNetwork network;
    private int id;
    private MultiplexedByteChannel canpNetworkAdapter;
    private transient Thread thread;


    public void setNetwork(final CanpNetwork network) { this.network = network; }

    public int getId() { return id; }

    public void setId(final int id) { this.id = id; }


    public void setCanpNetworkAdapter(final MultiplexedByteChannel canpNetworkAdapter) {
        this.canpNetworkAdapter = canpNetworkAdapter;
    }


    public void start() throws IOException {
        thread = new Thread(loop());
        thread.start();
    }


    public void stop() throws IOException {
        thread.interrupt();
        thread = null;
    }


    Runnable loop() {
        return new Runnable() {
            @Override
            public void run() {
                final ByteBuffer buffer = ByteBuffer.allocate(8);
                try {
                    while (!thread.isInterrupted()) {
                        final int id = canpNetworkAdapter.read(buffer);
                        System.out.println("Received, id = " + id);
                        buffer.flip();
                        network.write(id, buffer);
                        buffer.clear();
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    /**
     * Send a frame to this network segment.
     * @param eid
     * @param buffer
     * @throws IOException
     */
    public void write(final int eid, final ByteBuffer buffer) throws IOException {
//        network.write(eid, buffer);
        System.out.printf("eid=%08x\n", eid);
        canpNetworkAdapter.write(eid, buffer);
    }
}
