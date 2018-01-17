package mmm.transactions;

import djf.AppTemplate;
import javafx.scene.shape.Shape;
import jtps.jTPS_Transaction;
import mmm.controls.LinePath;
import mmm.controls.MetroStation;
import mmm.gui.MetroWorkspace;

public class StrokeWidthTransaction implements jTPS_Transaction {

    private Shape shape;
    private double oldWidth;
    private double newWidth;
    private AppTemplate app;

    public StrokeWidthTransaction(Shape s, double oldWidth, double newWidth, AppTemplate app) {
        this.shape = s;
        this.oldWidth = oldWidth;
        this.newWidth = newWidth;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        if (shape instanceof LinePath) {
            this.shape.setStrokeWidth(newWidth);
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateLineEditToolbar(((LinePath) shape).getAssociatedLine());
        } else {
            MetroStation station = (MetroStation) shape;
            station.setRadiusX(newWidth);
            station.setRadiusY(newWidth);
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateStationEditToolbar(((MetroStation) shape));
        }
    }

    @Override
    public void undoTransaction() {
        if (shape instanceof LinePath) {
            this.shape.setStrokeWidth(oldWidth);
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateLineEditToolbar(((LinePath) shape).getAssociatedLine());
        } else {
            MetroStation station = (MetroStation) shape;
            station.setRadiusX(oldWidth);
            station.setRadiusY(oldWidth);
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateStationEditToolbar(((MetroStation) shape));
        }
    }

}