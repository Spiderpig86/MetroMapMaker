package mmm.transactions.background;

import djf.AppTemplate;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jtps.jTPS_Transaction;
import mmm.data.MetroData;
import mmm.gui.MetroWorkspace;

public class BackgroundColorTransaction implements jTPS_Transaction {
    private Pane canvas;
    private Color oldColor;
    private Color newColor;
    private AppTemplate app;

    public BackgroundColorTransaction(Pane canvas, Color oldColor, Color newColor, AppTemplate app) {
        this.canvas = canvas;
        this.oldColor = oldColor;
        this.newColor = newColor;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        MetroWorkspace workspace = ((MetroWorkspace) app
                .getWorkspaceComponent());
        BackgroundFill fill = new BackgroundFill(newColor, null, null);
        Background background = new Background(fill);
        canvas.setBackground(background);
        ((MetroData) app.getDataComponent()).setBackgroundColor(newColor);//
        // Just update the value
        workspace.getBackgroundColorPicker()
                .setValue(newColor);
        // TODO might need to redraw gridlines
        if (workspace.getShowGridChecked().isSelected())
            workspace.getController().constructGridLines(workspace);
    }

    @Override
    public void undoTransaction() {
        MetroWorkspace workspace = ((MetroWorkspace) app
                .getWorkspaceComponent());
        BackgroundFill fill = new BackgroundFill(oldColor, null, null);
        Background background = new Background(fill);
        canvas.setBackground(background);
        ((MetroData) app.getDataComponent()).setBackgroundColor(oldColor);//
        // Just update the value
        workspace.getBackgroundColorPicker().setValue(oldColor);
        if (workspace.getShowGridChecked().isSelected())
            workspace.getController().constructGridLines(workspace);
    }
}
