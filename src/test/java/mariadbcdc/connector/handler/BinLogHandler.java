package mariadbcdc.connector.handler;

import mariadbcdc.connector.io.Either;
import mariadbcdc.connector.io.PacketIO;
import mariadbcdc.connector.packet.ErrPacket;
import mariadbcdc.connector.packet.binlog.BinLogData;
import mariadbcdc.connector.packet.binlog.BinLogEvent;
import mariadbcdc.connector.packet.binlog.BinLogHeader;
import mariadbcdc.connector.packet.binlog.BinLogStatus;
import mariadbcdc.connector.packet.binlog.des.BinLogDataDeserializers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinLogHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final PacketIO packetIO;
    private final String checksum;

    public BinLogHandler(PacketIO packetIO, String checksum) {
        this.packetIO = packetIO;
        this.checksum = checksum;
    }

    public Either<ErrPacket, BinLogEvent> readBinLogEvent() {
        BinLogStatus binLogStatus = readBinLogStatus();
        logger.trace("binLogStatus: {}", binLogStatus);
        int status = binLogStatus.getStatus();
        if (status == 0x00) { // OK
            BinLogEvent binLogEvent = readBinLogEvent(binLogStatus);
            return binLogEvent == null ?
                    Either.right(BinLogEvent.EOF) :
                    Either.right(binLogEvent);
        } else if (status == 0xFF) { // ERR
            return Either.left(ErrPacket.from(binLogStatus, packetIO));
        } else if (status == 0xFE) { // EOF
            return Either.right(BinLogEvent.EOF);
        } else {
            return Either.right(BinLogEvent.UNKNOWN);
        }
    }

    @NotNull
    private BinLogStatus readBinLogStatus() {
        BinLogStatus binLogStatus = new BinLogStatus(
                packetIO.readInt(3), packetIO.readInt(1), packetIO.readInt(1)
        );
        return binLogStatus;
    }

    private BinLogEvent readBinLogEvent(BinLogStatus binLogStatus) {
        BinLogHeader header = readBinLogHeader();
        logger.trace("binlog header: {}", header);
        packetIO.startBlock((int) header.getEventDataLength() - checksumSize());
        BinLogData data = BinLogDataDeserializers.getDeserializer(header.getEventType()).deserialize(packetIO, binLogStatus, header);
        packetIO.skip(checksumSize());
        if (data != null) {
            logger.trace("binlog data: {}", data);
        }
        return new BinLogEvent(header, data);
    }

    private int checksumSize() {
        return 4; // "CRC32".equalsIgnoreCase(checksum) ? 4 : 0;
    }

    private BinLogHeader readBinLogHeader() {
        long timestamp = packetIO.readLong(4) * 1000L;
        int code = packetIO.readInt(1);
        long serverId = packetIO.readLong(4);
        long eventLength = packetIO.readLong(4);
        long nextPosition = packetIO.readLong(4);
        int flags = packetIO.readInt(2);
        return new BinLogHeader(
                timestamp,
                code,
                serverId,
                eventLength,
                nextPosition,
                flags
        );
    }

}
