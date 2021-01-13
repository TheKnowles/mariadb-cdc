package mariadbcdc.connector.packet.binlog.des;

import mariadbcdc.connector.io.ReadPacketData;
import mariadbcdc.connector.packet.binlog.BinLogData;
import mariadbcdc.connector.packet.binlog.BinLogHeader;
import mariadbcdc.connector.packet.binlog.BinLogStatus;
import mariadbcdc.connector.packet.binlog.data.RowsPair;
import mariadbcdc.connector.packet.binlog.data.TableMapEvent;
import mariadbcdc.connector.packet.binlog.data.UpdateRowsEvent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class UpdateRowsEventBinLogDataDeserializer extends BaseRowsEventBinLogDataDeserializer {
    @Override
    public BinLogData deserialize(ReadPacketData readPacketData, BinLogStatus binLogStatus, BinLogHeader header, Map<Long, TableMapEvent> tableMap) {
        long tableId = readPacketData.readLong(6);
        readPacketData.readInt(2); // skip flags
        int numberOfColumns = readPacketData.readLengthEncodedInt();
        byte[] bitMap = new byte[(numberOfColumns + 7) / 8];
        readPacketData.readBytes(bitMap);
        BitSet columnUsed = BitSet.valueOf(bitMap);

        readPacketData.readBytes(bitMap);
        BitSet updateColumnUsed = BitSet.valueOf(bitMap);
        List<RowsPair> pairs = new ArrayList<>();

        while (readPacketData.remaining() > 0) {
            readPacketData.readBytes(bitMap);
            BitSet nullBitmap = BitSet.valueOf(bitMap);
            Object[] columnValues = readColumnValues(columnUsed, nullBitmap, tableMap.get(tableId), readPacketData);

            readPacketData.readBytes(bitMap);
            BitSet updateNullBitmap = BitSet.valueOf(bitMap);
            Object[] updateColumnValues = readColumnValues(updateColumnUsed, updateNullBitmap, tableMap.get(tableId), readPacketData);
            pairs.add(new RowsPair(columnValues, updateColumnValues));
        }
        return new UpdateRowsEvent(
                tableId,
                numberOfColumns,
                columnUsed,
                updateColumnUsed,
                pairs
        );
    }

}
