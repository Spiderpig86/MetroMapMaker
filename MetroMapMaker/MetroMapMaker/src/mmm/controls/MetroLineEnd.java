package mmm.controls;

import djf.AppTemplate;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

import javafx.scene.paint.Color;
import mmm.data.MetroData;

public class MetroLineEnd extends MetroStation implements Draggable {

    private MetroLine associatedLine;
    private boolean isLineStart;
    private boolean isSelected;

    public MetroLineEnd(MetroLine line, ObservableList<Node> shapes, double radius, Point2D
            location, boolean isStart, AppTemplate app) {
        super(line.getLineName(), shapes, false, app);
        this.associatedLine = line;
        this.associatedLines.add(line);
        this.isLineStart = isStart;
        this.app = app;

        // Set location of the line end
        this.setLocationAndSize(location.getX(), location.getY(), radius,
                radius);

        this.associatedLabel = new MetroStationLabel(line.getLineName(),
                this, true, app);

        this.associatedLabel.setLocationAndSize(this.getCenterX(), this
                .getCenterY());
        shapes.add(this.associatedLabel);

        // Hide the node
        this.setSelected(false);
    }

    // Helper method
    public boolean isLineStart() { return this.isLineStart; }

    public MetroLine getAssociatedLine() { return this.associatedLine; }

    public boolean isSelected() { return this.isSelected; }
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.associatedLabel.setVisible(!selected);
        if (selected) {
            this.setVisible(true);
            // Highlight this shape
            MetroData data = (MetroData) this.associatedLine.getApp()
                    .getDataComponent();
            data.highlightShape(this);
            data.selectTopShape(new Double(this.getCenterX()).intValue(), new
                    Double(this
                    .getCenterY()).intValue());
        } else
            this.setVisible(false);
    }

    @Override
    public void drag(int x, int y) {
        if (this.isSelected) {
            double diffX = x - startCenterX;
            double diffY = y - startCenterY;
            double newX = getCenterX() + diffX;
            double newY = getCenterY() + diffY;
            setCenterX(newX);
            setCenterY(newY);
            startCenterX = x;
            startCenterY = y;

            // Update the label location also
            this.associatedLabel.setLocationAndSize(newX, newY);
        }
    }

    @Override
    public String getShapeType() {
        return LINE_END;
    }
}
