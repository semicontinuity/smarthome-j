package smarthome.canp;

import gearbox.io.ByteChannelMultiplexer;
import openbox.patterns.Consumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Abstraction of a CAN network with devices talking CANP protocol.
 *
 * CANP protocol summary:
 *
 * The physical network can be composed of several physical segments.
 * Each physical segment is identified by segment_id.
 *
 * An addressable entity on the network is called {@linkplain Endpoint}.
 * Endpoint address (also called parameter_id) consists of host_id (8 bits) and slot_id (8 bits).
 * The field host_id is split into segment_id and host_address.
 * The number of bits devoted to segment_id is deployment-specific.
 *
 * Only Extended CAN frames are used.
 * EID layout:
 *
 *  28              20    17  15            8 7             0
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  Long Tag - 11 bits |   | endpoint_id (16 bits)         |
 * +-----+---------+-----+-+-+-----+---------+---------------+
 * |short|   routing     |o|a|    host_id    | slot          |
 * | tag |   tag         |w|u|               |               |
 * +-----+---------+-----+-+-+-----+---------+---------------+
 *
 * - Short tag (3 bits): used to tag the specific type of traffic;
 *   also specifies the priority of the current message. Currently not used.
 * - Routing tag, or Counter-party (8 bits):
 *   Holds bits identifying the counter-party of the current network transaction,
 *   e.g. sender address for requests and destination address for replies.
 *   When multiple physical segments are present, it may be used for routing between segments.
 * - Owner flag bit (ow): Set for a packet with the payload, transmitted by the owner of the parameter.
 *  -Auxiliary bit (au), for now used to distinguish SYSTEM (1) and USER (0) messages.
 *
 * The combination of Auxiliary, RTR and Owner bits defines the message type:
 *
 * - AUX=0, RTR=0, OWN=0:  PUT request, the client asks the node to set the certain parameter.
 * - AUX=0, RTR=0, OWN=1:  Parameter VALUE, sent by the parameter owner. Either the response to PUT or the owner-initiated notification.
 * - AUX=0, RTR=1, OWN=0:  NOT_OK response to PUT request.
 * - AUX=0, RTR=1, OWN=1:  GET request, the client asks the node to send out the value of a certain parameter.
 *
 *  CANP node copies tag fields from request frame to response frame,
 *  and inserts pre-set values of tag fields into notification frames.
 *
 * Each node has a virtual memory space, that can be read or written remotely.
 * As the maximum payload of CAN message is 8 bytes,
 * the addresses used in read and write operations are aligned to 8-byte boundaries.
 * The 8-byte piece of memory is called a Slot.
 * Memory operations are using slot numbers instead of memory addresses.
 *
 * In total, there are 9 bits in the message header for the slot number:
 * 1 bit of AUX + 8 bits of SLOT, so there are 512 slots per node.
 * The lower 256 slots (AUX=0) are dedicated for user memory, and the higher 256 (AUX=1) for system memory.
 *
 * This implementation is designed for network with 3 segment_id bits;
 * thus with max 8 segments and 32 nodes per segment.
 */
public class CanpNetwork implements ByteChannelMultiplexer {

    final CanpNetworkSegment[] segments = new CanpNetworkSegment[8];
    final transient Map<String, Endpoint> resources = new HashMap<>();



    public void add(final CanpNetworkSegment segment) {
        segments[segment.getId()] = segment;
        segment.setNetwork(this);
    }

    /**
     * Frame received from the network.
     * @param eid the EID part of frame
     * @param buffer the data of frame
     * @throws IOException
     */
    public void write(final int eid, final ByteBuffer buffer) throws IOException {
        final byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        endpoint(address(eid)).listeners.put(data);
    }

    public static String address(final int eid) {return address((eid >> 8) & 0xFF, eid & 0xFF);}

    public static String address(final int host, final int slot) {return String.format("%02X/%02X", host, slot);}



    public Endpoint endpoint(final String endpointAddress) {
        final Endpoint endpoint = resources.get(endpointAddress);
        if (endpoint != null) return endpoint;

        final int host = Integer.parseInt(endpointAddress.substring(0, 2), 16);
        final int slot = Integer.parseInt(endpointAddress.substring(3, 5), 16);
        final Endpoint newEndpoint = new Endpoint(host, slot);
        resources.put(endpointAddress, newEndpoint);
        return newEndpoint;
    }


    /**
     * An Endpoint is an addressable entity on the CANP network.
     */
    public class Endpoint implements Consumer<byte[], IOException> {
        final MulticastConsumer<byte[], IOException> listeners = new MulticastConsumer<>();

        final int host;
        final int slot;
        final CanpNetworkSegment segment;


        public Endpoint(final int host, final int slot) {
            this.host = host;
            this.slot = slot;
            this.segment = segments[host >> 5];
        }


        @Override
        public void put(final byte[] data) throws IOException {
            final int id = (host << 8) | slot;
            segment.write(id, ByteBuffer.wrap(data));
        }
    }


    static class MulticastConsumer<T, E extends Exception>
        extends ArrayList<Consumer<T, E>> implements Consumer<T, E> {

        @Override
        public void put(final T value) throws E {
            for (int i = 0; i < size(); i++) get(i).put(value);
        }
    }
}
