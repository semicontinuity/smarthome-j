package smarthome;

import gearbox.io.ByteChannelMultiplexer;
import gearbox.io.MultiplexedByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;


public class CanNetworkSegment implements ByteChannelMultiplexer {

    CanNetwork network;
    private int id;
    private MultiplexedByteChannel canNetworkAdapter;
    private transient Thread thread;


    public void setNetwork(final CanNetwork network) { this.network = network; }

    public int getId() { return id; }

    public void setId(final int id) { this.id = id; }


    public void setCanNetworkAdapter(final MultiplexedByteChannel canNetworkAdapter) {
        this.canNetworkAdapter = canNetworkAdapter;
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
                        final int id = canNetworkAdapter.read(buffer);
                        buffer.flip();
                        write(id, buffer);
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
     * Frame received from this segment.
     * @param eid the EID part of frame
     * @param buffer the data of frame
     * @throws IOException
     */
    public void write(final int eid, final ByteBuffer buffer) throws IOException {
        network.write(eid, buffer);
    }
}
