package mmm.transactions.metrostation;

import jtps.jTPS_Transaction;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;

public class RemoveStationFromLineTransaction implements jTPS_Transaction {
    private MetroStation station;
    private MetroLine line;

    public RemoveStationFromLineTransaction(MetroStation station, MetroLine line) {
        this.station = station;
        this.line = line;
    }

    @Override
    public void doTransaction() {
        line.removeStation(station);
        station.removeLine(line);
    }

    @Override
    public void undoTransaction() {
        line.addStation(station);
    }
}
