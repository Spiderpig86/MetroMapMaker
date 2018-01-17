package mmm.transactions.metroline;

import djf.AppTemplate;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jtps.jTPS_Transaction;
import mmm.controls.*;
import mmm.gui.MetroWorkspace;

public class SetLineColorTransaction implements jTPS_Transaction {
    private MetroLine line;
    private Color oldColor;
    private Color newColor;
    private AppTemplate app;

    public SetLineColorTransaction(MetroLine line, Color oldColor, Color
            newColor, AppTemplate app) {
        this.line = line;
        this.oldColor = oldColor;
        this.newColor = newColor;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        this.line.getPath().setStroke(newColor);
        this.line.getLineStart().getAssociatedLabel().setFill(newColor); // Update
        // line label colors
        this.line.getLineEnd().getAssociatedLabel().setFill(newColor);
    }

    @Override
    public void undoTransaction() {
        this.line.getPath().setStroke(oldColor);
        this.line.getLineStart().getAssociatedLabel().setFill(oldColor); // Update
        // line label colors
        this.line.getLineEnd().getAssociatedLabel().setFill(oldColor);
    }
}
