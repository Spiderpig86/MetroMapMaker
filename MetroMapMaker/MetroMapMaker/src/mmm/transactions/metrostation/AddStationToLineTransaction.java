package mmm.transactions.metrostation;

import jtps.jTPS_Transaction;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;

public class AddStationToLineTransaction implements jTPS_Transaction {

    private MetroStation station;
    private MetroLine line;

    public AddStationToLineTransaction(MetroStation station, MetroLine line) {
        this.station = station;
        this.line = line;
    }

    // Note that addStation takes care of adding the line to the station
    @Override
    public void doTransaction() {
        line.addStation(station);
    }

    @Override
    public void undoTransaction() {
        line.removeStation(station);
        station.removeLine(line);
    }
}
