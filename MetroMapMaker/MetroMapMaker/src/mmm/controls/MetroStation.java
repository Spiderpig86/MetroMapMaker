package mmm.controls;

import djf.AppTemplate;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import mmm.data.MetroLabelLocation;
import mmm.data.MetroLabelOrientation;
import mmm.data.StationReference;
import mmm.gui.MetroWorkspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetroStation extends DraggableEllipse implements Draggable {

    private String stationName;
    private Color stationColor = Color.WHITE;
    private double stationRadius = 20.0;
    private double strokeWidth = 3.0;
    protected List<MetroLine> associatedLines; // Store all the lines
    // this station serves
    protected MetroStationLabel associatedLabel;
    private ObservableList<Node> shapes;
    private StationReference stationReference;

    // Temp storage when loading station data
    private List<String> prevStations = new ArrayList<>();
    private List<String> nextStations = new ArrayList<>();
    private List<String> associatedLinesStrings = new ArrayList<>();

    // References
    protected HashMap<MetroLine, MetroStation> prev;
    protected HashMap<MetroLine, MetroStation> next;
    protected HashMap<MetroLine, PathElement> bindedSegmentPrev;
    protected HashMap<MetroLine, PathElement> bindedSegmentNext;

    protected AppTemplate app;

    public MetroStation(String name, ObservableList<Node> shapes, boolean
            hasLabel, AppTemplate app) {
        super();
        this.stationName = name;
        // Use default values for rest
        this.shapes = shapes;
        this.stationReference = new StationReference(this);
        this.app = app;

        // Set up our maps
        this.prev = new HashMap<>();
        this.next = new HashMap<>();
        this.bindedSegmentPrev = new HashMap<>();
        this.bindedSegmentNext = new HashMap<>();

        this.setCenterX(100);
        this.setCenterY(100);

        this.setLocationAndSize(this.getX(), this.getY(), stationRadius, stationRadius);

        // Set the clipping property
        Rectangle clip = ((MetroWorkspace) app.getWorkspaceComponent())
                .cloneCanvasClip();
        this.setClip(clip);

        // Set up the label
        if (hasLabel) {
            this.associatedLabel = new MetroStationLabel(name, this, false,
                    app);
            this.associatedLabel.setLocationAndSize(this.getCenterX(), this
                    .getCenterY());
            shapes.add(this.associatedLabel); // Add the label to
            // the canvas
        }

        // Default station look
        this.setFill(Color.WHITE);
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(strokeWidth);

        this.associatedLines = new ArrayList<>();
    }

    @Override
    public void drag(int x, int y) {
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

    public void addLabel() { shapes.add(this.associatedLabel); }
    public void removeLabel() {
        shapes.remove(this.associatedLabel);
    }

    // Helper methods
    public MetroStationLabel getAssociatedLabel() {
        return associatedLabel;
    }

    public Map<MetroLine, MetroStation> getPrev() { return this.prev; }
    public Map<MetroLine, MetroStation> getNext() { return this.next; }

    public void addPrev(MetroLine line, MetroStation prev) { this.prev.put
            (line, prev); }
    public void addNext(MetroLine line, MetroStation next) { this.next.put
            (line, next); }

    public void removePrev(MetroStation prev) { this.prev.values().remove
            (prev); }
    public void removeNext(MetroStation next) { this.next.values().remove
            (next); }

    // Get next stations and lines given lines
    public MetroStation getPrevByLine(MetroLine line) {
        MetroStation previousStation = null;
        previousStation = this.prev.get(line);

        // Return null if there are no associated stations
        return previousStation;
    }

    public MetroStation getNextByLine(MetroLine line) {
        MetroStation nextStation = null;
        nextStation = this.next.get(line);

        // Return null if there are no associated stations
        return nextStation;
    }

    // Line Segments
    public PathElement getPrevSegmentByLine(MetroLine line) { return this
            .bindedSegmentPrev.get(line); }
    public PathElement getNextSegmentByLine(MetroLine line) { return this
            .bindedSegmentNext.get(line); }

    public Map<MetroLine, PathElement> getPrevSegments() { return this
            .bindedSegmentPrev; }
    public Map<MetroLine, PathElement> getNextSegments() { return this
            .bindedSegmentNext; }

    public void addPrevSegment(MetroLine line, PathElement segment) {
        this.bindedSegmentPrev.put(line, segment);
    }
    public void addNextSegment(MetroLine line, PathElement segment) {
        this.bindedSegmentNext.put(line, segment);
    }

    public Point2D getPoint() { return new Point2D(this.getX(), this.getY()); }

    public List<MetroLine> getAssociatedLines() { return this
            .associatedLines; }

    public void addLine(MetroLine line) { this.associatedLines.add(line); }
    public void removeLine(MetroLine line) {
        this.associatedLines.remove(line); // Remove the line
        this.prev.remove(line); // Remove previous station associated with line
        this.next.remove(line); // Remove next station associated with line
    }

    public List<String> getPrevStations() { return this.prevStations; }
    public List<String> getNextStations() { return this.nextStations; }
    public List<String> getAssociatedLinesStrings() { return this
            .associatedLinesStrings; }


    public StationReference getStationReference() {
        return stationReference;
    }

    public void setName(String s) {
        this.stationName = s;
    }

    @Override
    public String getShapeType() {
        return STATION;
    }

    @Override
    public String toString() {
        return this.stationName;
    }
}
