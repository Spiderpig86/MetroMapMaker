package mmm.transactions.background;

import djf.AppTemplate;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import jtps.jTPS_Transaction;

public class BackgroundSizeTransaction implements jTPS_Transaction {
    private Pane canvas;
    private Point2D oldSize;
    private Point2D newSize;
    private AppTemplate app;
    private Line line = new Line(); // Trigger canvas to stay at right location

    public BackgroundSizeTransaction(Pane canvas, Point2D oldSize, Point2D
            newSize, AppTemplate app) {
        this.canvas = canvas;
        this.oldSize = oldSize;
        this.newSize = newSize;
        this.app = app;
        this.line.setFill(Color.TRANSPARENT);
    }

    @Override
    public void doTransaction() {
        this.canvas.setPrefWidth(newSize.getX());
        this.canvas.setPrefHeight(newSize.getY());
        // Trick to get canvas to stick at corner
        this.canvas.getChildren().add(this.line);
        this.canvas.getChildren().remove(this.line);
    }

    @Override
    public void undoTransaction() {
        this.canvas.setPrefWidth(oldSize.getX());
        this.canvas.setPrefHeight(oldSize.getY());
        this.canvas.getChildren().add(this.line);
        this.canvas.getChildren().remove(this.line);
    }
}
