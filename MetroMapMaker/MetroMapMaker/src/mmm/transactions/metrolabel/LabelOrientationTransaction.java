package mmm.transactions.metrolabel;

import jtps.jTPS_Transaction;
import mmm.controls.MetroStationLabel;
import mmm.data.MetroLabelOrientation;

public class LabelOrientationTransaction implements jTPS_Transaction {

    private MetroStationLabel label;
    private MetroLabelOrientation oldOrientation;
    private MetroLabelOrientation newOrientation;

    public LabelOrientationTransaction(MetroStationLabel label,
                                    MetroLabelOrientation oldOrientation,
                                    MetroLabelOrientation newOrientation) {
        this.label = label;
        this.oldOrientation = oldOrientation;
        this.newOrientation = newOrientation;
    }

    @Override
    public void doTransaction() {
        this.label.setRotation(newOrientation);
    }

    @Override
    public void undoTransaction() {
        this.label.setRotation(oldOrientation);
    }
}
