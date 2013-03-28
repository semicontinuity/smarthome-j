package smarthome;

import de.entropia.can.CanSocket;
import gearbox.io.MultiplexedByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;


public class CanNetworkAdapter implements MultiplexedByteChannel {

    private String canInterface;
    private transient CanSocket.CanInterface canIface;
    private transient CanSocket socket;


    public void setCanInterface(final String canInterface) throws IOException {
        this.canInterface = canInterface;
    }


    public void start() throws IOException {
        socket = new CanSocket(CanSocket.Mode.RAW);
        canIface = new CanSocket.CanInterface(socket, canInterface);
        socket.bind(canIface);
    }


    public void stop() throws IOException {
        socket.close();
        socket = null;
    }


    @Override
    public void write(int id, final ByteBuffer buffer) throws IOException {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        final CanSocket.CanId canId = new CanSocket.CanId(id);
        canId.setEFFSFF();
        if (data.length == 0) canId.setRTR();

        socket.send(
            new CanSocket.CanFrame(
                canIface,
                canId,
                data
            )
        );
    }


    @Override
    public int read(final ByteBuffer buffer) throws IOException {
        final CanSocket.CanFrame frame = socket.recv();
        buffer.put(frame.getData());
        return frame.getCanId().getCanId_EFF();
    }
}
