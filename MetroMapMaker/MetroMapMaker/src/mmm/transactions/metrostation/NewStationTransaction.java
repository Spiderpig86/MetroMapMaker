package mmm.transactions.metrostation;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import jtps.jTPS_Transaction;
import mmm.controls.MetroStation;

public class NewStationTransaction implements jTPS_Transaction {
    private MetroStation station;
    private ObservableList<Node> shapes;

    public NewStationTransaction(MetroStation station, ObservableList<Node>
            shapes) {
        this.station = station; // Shape that we are inserting
        this.shapes = shapes;
    }

    @Override
    public void doTransaction() {
        this.shapes.add(this.station);
        // When redoing, just add the label back
        if (!shapes.contains(station.getAssociatedLabel()))
            station.addLabel();
    }

    @Override
    public void undoTransaction() {
        // When undoing the station add, remember to remove the label also
        this.shapes.remove(station);
        station.removeLabel();
    }
}
