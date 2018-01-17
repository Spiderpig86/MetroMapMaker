package mmm.transactions.metrolabel;

import jtps.jTPS_Transaction;
import mmm.controls.MetroStationLabel;
import mmm.data.MetroLabelLocation;

public class LabelLocationTransaction implements jTPS_Transaction {

    private MetroStationLabel label;
    private MetroLabelLocation oldLocation;
    private MetroLabelLocation newLocation;

    public LabelLocationTransaction(MetroStationLabel label,
                                    MetroLabelLocation oldLocation,
                                    MetroLabelLocation newLocation) {
        this.label = label;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
    }

    @Override
    public void doTransaction() {
        this.label.setLocation(newLocation);
    }

    @Override
    public void undoTransaction() {
        this.label.setLocation(oldLocation);
    }
}
