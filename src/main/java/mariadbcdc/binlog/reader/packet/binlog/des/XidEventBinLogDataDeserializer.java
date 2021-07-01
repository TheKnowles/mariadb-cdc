package mariadbcdc.binlog.reader.packet.binlog.des;

import mariadbcdc.binlog.reader.io.ReadPacketData;
import mariadbcdc.binlog.reader.packet.binlog.BinLogData;
import mariadbcdc.binlog.reader.packet.binlog.BinLogHeader;
import mariadbcdc.binlog.reader.packet.binlog.BinLogStatus;
import mariadbcdc.binlog.reader.packet.binlog.data.TableMapEvent;
import mariadbcdc.binlog.reader.packet.binlog.data.XidEvent;

import java.util.Map;

public class XidEventBinLogDataDeserializer implements BinLogDataDeserializer {
    @Override
    public BinLogData deserialize(ReadPacketData readPacketData, BinLogStatus binLogStatus, BinLogHeader header, Map<Long, TableMapEvent> tableMap) {
        return new XidEvent(readPacketData.readLong(8));
    }
}
