package mariadbcdc.connector.packet.binlog.des;

import mariadbcdc.connector.io.PacketIO;
import mariadbcdc.connector.packet.binlog.BinLogData;
import mariadbcdc.connector.packet.binlog.BinLogHeader;
import mariadbcdc.connector.packet.binlog.BinLogStatus;

public interface BinLogDataDeserializer {
    BinLogData deserialize(PacketIO packetIO, BinLogStatus binLogStatus, BinLogHeader header);
}