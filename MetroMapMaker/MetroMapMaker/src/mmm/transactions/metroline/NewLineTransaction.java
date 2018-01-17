package mmm.transactions.metroline;

import djf.AppTemplate;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import jtps.jTPS_Transaction;
import mmm.controls.MetroLine;

public class NewLineTransaction implements jTPS_Transaction {

    private MetroLine line;
    private ObservableList<MetroLine> metroLines;
    private ObservableList<Node> shapes;
    private AppTemplate app;

    public NewLineTransaction(MetroLine line, ObservableList<MetroLine>
            metroLines, ObservableList<Node>shapes, AppTemplate app) {
        this.line = line;
        this.metroLines = metroLines;
        this.shapes = shapes;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        metroLines.add(line);

        // Put this inside if statement below
        //line = new MetroLine(line.getLineName(), line.getLineColor(),
//                    shapes, app, false);

        // Add the controls back to the canvas if it doesn't exist (when
        // redoing)
        if (!shapes.contains(line.getPath()))
            shapes.add(line.getPath());
        if (!shapes.contains(line.getLineStart())) {
            shapes.add(line.getLineStart());
            line.getLineStart().addLabel();
            line.getLineStart().toFront(); // Appear above label
        }
        if (!shapes.contains(line.getLineEnd())) {
            shapes.add(line.getLineEnd());
            line.getLineEnd().addLabel();
            line.getLineEnd().toFront();
        }

    }

    @Override
    public void undoTransaction() {
        metroLines.remove(line);

        // Remove the metroline from the canvas
        shapes.remove(line.getPath());
        shapes.remove(line.getLineStart());
        line.getLineStart().removeLabel();
        shapes.remove(line.getLineEnd());
        line.getLineEnd().removeLabel();
    }
}
