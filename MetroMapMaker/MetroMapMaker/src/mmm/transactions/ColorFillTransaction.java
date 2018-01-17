package mmm.transactions;

import djf.AppTemplate;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jtps.jTPS_Transaction;
import mmm.controls.*;
import mmm.gui.MetroWorkspace;

public class ColorFillTransaction implements jTPS_Transaction {
    private Shape currentShape;
    private Color oldColor;
    private Color newColor;
    private AppTemplate app;

    public ColorFillTransaction(Shape s, Color oldColor, Color newColor, AppTemplate app) {
        this.currentShape = s;
        this.oldColor = oldColor;
        this.newColor = newColor;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        currentShape.setFill(newColor);
    }

    @Override
    public void undoTransaction() {
        currentShape.setFill(oldColor);
        updateEditToolbar();
    }

    private void updateEditToolbar() {
        if (currentShape instanceof LinePath) {
            LinePath metroLine = (LinePath) currentShape;
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateLineEditToolbar(metroLine.getAssociatedLine());
        } else if (currentShape instanceof MetroStationLabel) {
            // For selecting a label or station, we will update both
            // properties for completion
            MetroStationLabel label = (MetroStationLabel) currentShape;
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateStationEditToolbar(label.getAssociatedStation());
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateFontToolbar(label);
        } else if (currentShape instanceof MetroStation) {
            MetroStation station = (MetroStation) currentShape;
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateStationEditToolbar(station);
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateFontToolbar(station.getAssociatedLabel());
        } else if (currentShape instanceof DraggableText) {
            DraggableText label = (DraggableText) currentShape;
            ((MetroWorkspace) app.getWorkspaceComponent())
                    .updateFontToolbar(label);
        }
    }
}
