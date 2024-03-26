package cn.nukkit.network.protocol;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.network.Network;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.SnappyCompression;
import cn.nukkit.utils.Zlib;
import com.nukkitx.network.raknet.RakNetReliability;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public abstract class DataPacket extends BinaryStream implements Cloneable {

    public volatile boolean isEncoded = false;

    @Deprecated
    private int channel = Network.CHANNEL_NONE;

    @Deprecated
    public RakNetReliability reliability = RakNetReliability.RELIABLE_ORDERED;

    public abstract byte pid();

    public abstract void decode();

    public abstract void encode();

    @Override
    public DataPacket reset() {
        super.reset();
        byte packetId = this.pid();
        if (packetId < 0 && packetId >= -56) { // Hack: (byte) 200+ --> (int) 300+
            this.putUnsignedVarInt(packetId + 356);
        } else {
            this.putUnsignedVarInt(packetId & 0xff);
        }
        return this;
    }

    @Deprecated
    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Deprecated
    public int getChannel() {
        return channel;
    }

    public DataPacket clean() {
        this.setBuffer(null);
        this.setOffset(0);
        this.isEncoded = false;
        return this;
    }

    @Override
    public DataPacket clone() {
        try {
            DataPacket packet = (DataPacket) super.clone();
            packet.setBuffer(this.count < 0 ? null : this.getBuffer()); // prevent reflecting same buffer instance
            packet.offset = this.offset;
            packet.count = this.count;
            return packet;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public BatchPacket compress() {
        return this.compress(Server.getInstance().networkCompressionLevel);
    }

    public BatchPacket compress(int level) {
        byte[] buf = this.getBuffer();
        BinaryStream stream = new BinaryStream(new byte[5 + buf.length]).reset();
        stream.putUnsignedVarInt(buf.length);
        stream.put(buf);
        try {
            byte[] bytes = stream.getBuffer();
            BatchPacket batched = new BatchPacket();
            if (Server.getInstance().useSnappy) {
                batched.payload = SnappyCompression.compress(bytes);
            } else {
                batched.payload = Zlib.deflateRaw(bytes, level);
            }
            return batched;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final void tryEncode() {
        if (!this.isEncoded) {
            this.isEncoded = true;
            this.encode();
        }
    }

    void decodeUnsupported() {
        if (Nukkit.DEBUG > 1) {
            Server.getInstance().getLogger().debug("Warning: decode() not implemented for " + this.getClass().getName());
        }
    }

    void encodeUnsupported() {
        if (Nukkit.DEBUG > 1) {
            Server.getInstance().getLogger().debug("Warning: encode() not implemented for " + this.getClass().getName());
            Thread.dumpStack();
        }
    }
}
