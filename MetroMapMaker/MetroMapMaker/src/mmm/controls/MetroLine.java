package mmm.controls;

import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import mmm.data.StationReference;
import mmm.data.WeightedLineTo;
import mmm.transactions.metroline.SetLineColorTransaction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MetroLine {

    private String lineName;
    private MetroLineEnd lineStart, lineEnd;
    private List<StationReference> lineStations;
    private List<String> lineStationStrings = new ArrayList<>(); // Temp storage when
    // loading the project to store station sequentially
    private ObservableList<PathElement> lineSegments;
    private Color lineColor = Color.WHITE;
    private ObservableList<Node> shapes;
    private LinePath linePath = new LinePath(this);
    private boolean isCircular; // To denote that this line is circular
    // If a user connects a line to another station in the line and that
    // station does not have another station that is connected to it, then
    // the line is circular.
    // The second case is when the user deletes a station from the line and
    // it is circular, it cannot be circular anymore.
    private AppTemplate app;

    public MetroLine(String name, Color color, ObservableList<Node> shapes,
                     AppTemplate app, boolean isCircular) {
        this.lineStations = new ArrayList<>();
        this.lineName = name;
        this.lineColor = color;
        this.shapes = shapes;
        this.app = app;
        this.isCircular = isCircular;

        // Create the station ends
        this.lineStart = new MetroLineEnd(this, shapes, 20.0, new Point2D
                (100, 100), true, app);
        this.lineEnd = new MetroLineEnd(this, shapes, 20.0, new Point2D
                (100, 200), false, app);

        // Set their relations
        this.lineStart.addNext(this, lineEnd);
        this.lineEnd.addPrev(this, lineStart);

        // Add these stations to the polyline collection
        lineStations = FXCollections.observableArrayList();
        if (!isCircular) {
            lineStations.add(lineStart.getStationReference());
            lineStations.add(lineEnd.getStationReference());
        }

        // Create the start segment
        lineSegments = FXCollections.observableArrayList();
        MoveTo startPosition = new MoveTo(this.lineStart.getX(), this
                .lineStart.getY());
        WeightedLineTo startSegment = new WeightedLineTo(this.lineEnd.getX(), this.lineEnd
                .getY(), lineStart, lineEnd, this);
        // Do not set fill or else it will create a shape
        linePath.setStroke(this.lineColor);
        linePath.setStrokeWidth(3.0);

        // Bind the line to the start and end coordinates (only 2 stops so far)
        bindSegment(startPosition, null, this.lineStart);
        bindSegment(startSegment, this.lineStart, this.lineEnd);

        this.lineStart.addNextSegment(this, startSegment);
        this.lineEnd.addPrevSegment(this, startSegment);
        lineSegments.add(startSegment);

        // Now add the stations to the actual canvas
        this.linePath.getElements().addAll(startPosition, startSegment);
        shapes.add(linePath);
        //shapes.addAll(lineStations);
        lineStations.forEach(s -> shapes.add(s.getStation()));
    }

    public MetroLine(String name, ObservableList<Node> shapes,
                     AppTemplate app, Point2D start, Point2D end, boolean isCircular) {
        this.lineStations = new ArrayList<>();
        this.lineName = name;
        this.shapes = shapes;
        this.app = app;

        // Create the station ends
        this.lineStart = new MetroLineEnd(this, shapes, 20.0, start, true,
                app);
        this.lineEnd = new MetroLineEnd(this, shapes, 20.0, end, false, app);

        // Set their relations
        this.lineStart.addNext(this, lineEnd);
        this.lineEnd.addPrev(this, lineStart);

        // Add these stations to the polyline collection
        lineStations = FXCollections.observableArrayList();
        if (!isCircular) {
            lineStations.add(lineStart.getStationReference());
            lineStations.add(lineEnd.getStationReference());
        }


        // Create the start segment
        lineSegments = FXCollections.observableArrayList();
        MoveTo startPosition = new MoveTo(this.lineStart.getX(), this
                .lineStart.getY());
        WeightedLineTo startSegment = new WeightedLineTo(this.lineEnd.getX(), this.lineEnd
                .getY(), lineStart, lineEnd, this);
        // Do not set fill or else it will create a shape
        linePath.setStroke(this.lineColor);
        linePath.setStrokeWidth(3.0);

        // Bind the line to the start and end coordinates (only 2 stops so far)
        bindSegment(startPosition, null, this.lineStart);
        bindSegment(startSegment, this.lineStart, this.lineEnd);

        this.lineStart.addNextSegment(this, startSegment);
        this.lineEnd.addPrevSegment(this, startSegment);
        lineSegments.add(startSegment);

        // Now add the stations to the actual canvas
        this.linePath.getElements().addAll(startPosition, startSegment);
        //shapes.add(linePath);
        //shapes.addAll(lineStations);
        lineStations.forEach(s -> shapes.add(s.getStation()));
    }

    public void addStation(MetroStation station) {
        // See if we could create a circular line or not
        // Check if the station we are adding is a loop and there are more
        // than 3 stations (lineStations excludes the line ends)

        int stations = 0;
        // Make sure that we are really not counting the line ends
        for (StationReference s: lineStations)
            if (!s.getStation().getAssociatedLabel().isLineEnd())
                stations++;

        if ((lineStations.contains(station.getStationReference()))) {
            if (stations >= 3) {

                // Now we will handle the case of creating a circular line if
                // allowed. There are 2 criteria:
                // 1) The station is on the same line
                // 2a) The station has no previous station that belongs in the
                // same line
                // 2b) The station has no next station that belongs to the same line

                // First we need to get the line end (the station just before the
                // final line end) and connect it to the station we want to
                // connect to (making sure it meets the criteria above)

                MetroStation endStation = this.lineEnd.getPrevByLine(this);

                if (station == null || endStation == null) // Just in case
                    return;

                // Check if the station has a null neighbor for the same line
                boolean prevNull = station.getPrevByLine(this) == this.lineStart;
                boolean nextNull = endStation.getNextByLine(this) == this.lineEnd;
                if (!prevNull || !nextNull)
                    return; // Return since there is no way we can create a
                // circular line

                // TODO: Pretty sure it is only valid when the previous of the
                // station is null
                // nextNull checks to see if the station is also the last station

                if (prevNull) {
                    // Previous reference of the station is null for the station
                    // in the line
                    station.addPrev(this, endStation);
                    endStation.addNext(this, station);
                }

                this.isCircular = true; // Mark it as circular

                // Now reconstruct the line segments
                reloadSegments(station, false);
                updateStationSequenceLoop(this.lineStart.getNextByLine(this));
            }
            return;
        }

        station.addLine(this); // Add the line to the station

        // Get the station on the line closest to the one we want to add
        MetroStation closestStation;
        if (isCircular)
            closestStation = getClosestStationLoop(station);
        else
            closestStation = getClosestStation(station);
        //System.out.println("Closest Station: " + closestStation.toString());

        // Now we need to check if it is closer to the previous or the next
        // from the closest station
        double distPrev = getDistance(closestStation.getPrevByLine(this),
                station);
        double distNext = getDistance(station, closestStation.getNextByLine
                (this));

        MetroStation oldPrev;
        MetroStation oldNext;

        // Update the pointers like in a linked list
        if (distPrev < distNext) {
            // If the distance to the previous of the closest station is
            // closer, insert between previous and closest station
            oldPrev = closestStation.getPrevByLine(this);
            oldNext = closestStation;
            //System.out.println("2nd Prev: " + oldPrev.toString());
        } else {
            // Else, insert between closest station and next station
            oldPrev = closestStation;
            oldNext = closestStation.getNextByLine(this);
            //System.out.println("2nd Next: " + oldNext.toString());
        }

        station.addNext(this, oldNext);
        station.addPrev(this, oldPrev);
        oldPrev.addNext(this, station);
        oldNext.addPrev(this, station);

        if (!isCircular) {
            reloadSegments();
            updateStationSequence();
        } else {
            reloadSegments(lineStart.getNextByLine(this), false);
            updateStationSequenceLoop(this.lineStart.getNextByLine(this));
        }
    }

    public void removeStation(MetroStation station) {
        // If the station is a line end, do not remove
        if (station instanceof MetroLineEnd)
            return;

        // Update pointers to reflect the change
        MetroStation prev = station.getPrevByLine(this);
        MetroStation next = station.getNextByLine(this);

        if (prev == null || next == null) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Error " +
                    "deleting station.");
            return;
        }

        lineStations.remove(station.getStationReference());

        // Check if the line has less than 3 stations and is circular
        // Note that the size includes the 2 line ends
        if (lineStations.size() >= 3 || !isCircular) {
            prev.addNext(this, next);
            next.addPrev(this, prev);
        } else {
            // Line is no longer circular with only 2 stations left
            // prev. Note that it is possible that we are left with
            // non-original stations, so we must update the line ends to
            // match them
            prev = this.lineStations.get(0).getStation(); // The first
            // station
            next = this.lineStations.get(lineStations.size() - 1).getStation();
            // The second station
            double stationRadius = lineStart.getRadiusX() * 2;
            lineStart.setLocationAndSize(prev.getX(), prev.getY(),
                    stationRadius, stationRadius);
            lineEnd.setLocationAndSize(next.getX(), next.getY(),
                    stationRadius, stationRadius);
            lineStart.getAssociatedLabel().setLocationAndSize(lineStart.getX
                    (), lineStart.getY());
            lineEnd.getAssociatedLabel().setLocationAndSize(lineEnd.getX
                    (), lineEnd.getY());

            // Update the pointers
            lineStart.addNext(this, prev);
            lineEnd.addPrev(this, next);
            prev.addPrev(this, this.lineStart);
            // Add the line start again
            prev.addNext(this, next);
            next.addPrev(this, prev);
            next.addNext(this, this.lineEnd); // Add the line end again
            this.isCircular = false; // Reload this as a regular line

            // Add the ends and labels back
            lineStart.addLabel();
            lineEnd.addLabel();
            shapes.addAll(lineStart, lineEnd);
        }

        // Reload the line segments for the selected line
        if (!isCircular) {
            reloadSegments();
            updateStationSequence();
        } else {
            reloadSegments(lineStart.getNextByLine(this), false);
            updateStationSequenceLoop(station.getNextByLine(this));
        }

    }


    // Helper methods

    public void bindSegment(PathElement e, MetroStation prev, MetroStation next) {
        if (e instanceof MoveTo) {
            MoveTo m = (MoveTo) e;
            m.xProperty().bind(next.centerXProperty());
            m.yProperty().bind(next.centerYProperty());
        } else if (e instanceof LineTo) {
            LineTo l = (LineTo) e;
            l.xProperty().bind(next.centerXProperty());
            l.yProperty().bind(next.centerYProperty());
        } else if (e instanceof QuadCurveTo) {
            QuadCurveTo q = (QuadCurveTo) e;
            q.xProperty().bind(next.centerXProperty());
            q.yProperty().bind(next.centerYProperty());
//            NumberBinding controlBindX = Bindings.divide(prev.centerXProperty()
//                    .add(next.centerXProperty()), 2);
//            NumberBinding controlBindY = Bindings.divide(prev.centerYProperty()
//                    .add(next.centerYProperty()), 2);
//
//            q.controlXProperty().set(next.getCenterX());q.controlXProperty().bind(controlBindX);
//            q.controlYProperty().bind(controlBindY);
//            q.controlYProperty().set(next.getCenterY());
        }

        // No need to check for where to insert
        if (prev == null)
            return;

        // Insert the segment
        int i = 1;
        for (PathElement element: linePath.getElements()) {
            if (element == prev.getNextSegmentByLine(this)) {
                linePath.getElements().add(i, e);
                break;
            }
            i++;
        }
    }

    public List<StationReference> getLineStations() {
        return lineStations;
    }

    public String getLineName() { return lineName; }
    public void setLineName(String name) {
        this.lineName = name;
        this.lineStart.getAssociatedLabel().setText(name);
        this.lineEnd.getAssociatedLabel().setText(name);
    }

    public Color getLineColor() { return this.lineColor; }
    public void setLineColor(Color c, boolean addTransaction) {
        // Only add the transaction if we need it and if the colors are
        // different
        if (addTransaction && !c.equals(this.lineColor)) {
            SetLineColorTransaction transaction = new SetLineColorTransaction
                    (this, this.lineColor, c, app);
            app.getTPS().addTransaction(transaction);
        } else {
            this.getPath().setStroke(c);
            this.getLineStart().getAssociatedLabel().setFill(c); // Update
            // line label colors
            this.getLineEnd().getAssociatedLabel().setFill(c);
        }
        this.lineColor = c;
    }

    private MetroStation getClosestStation(MetroStation station) {
        MetroStation cursor = this.lineStart;
        MetroStation closestStation = null;
        double closestDistance = Double.MAX_VALUE;

        // Iterate over all the stations and find the closest one
        while (cursor != null) {
            double dist = getDistance(cursor, station);
            if (dist < closestDistance) {
                closestDistance = dist;
                closestStation = cursor;
            }
            cursor = cursor.getNextByLine(this);
        }
        return closestStation;
    }

    private MetroStation getClosestStationLoop(MetroStation station) {
        MetroStation cursor = this.lineStart.getNextByLine(this); // Gets the
        // first station of the circular line
        MetroStation closestStation = null;
        double closestDistance = Double.MAX_VALUE;

        // Iterate over all the stations and find the closest one
        while (cursor != null) {
            double dist = getDistance(cursor, station);
            if (dist < closestDistance) {
                closestDistance = dist;
                closestStation = cursor;
            }
            cursor = cursor.getNextByLine(this);

            // If we have traversed all the other stations, break out of the
            // loop
            if (cursor == this.lineStart.getNextByLine(this))
                break;
        }
        return closestStation;
    }

    private double getDistance(MetroStation source, MetroStation dest) {
        if (source == null || dest == null)
            return Double.MAX_VALUE;

        return Point.distance(source.getX(), source.getY(), dest.getX(),
                dest.getY());
    }

    private void addSegment(PathElement line) {
        lineSegments.add(line);
        linePath.getElements().add(line);
        shapes.remove(linePath);
        shapes.add(linePath);
        linePath.toBack();
    }

    private void removeSegment(PathElement line) {
        lineSegments.remove(line);
        linePath.getElements().remove(line);
        shapes.remove(linePath);
        shapes.add(linePath);
        linePath.toBack();
    }

    public void reloadSegments() {
        // Remove the old line segments
        this.linePath.getElements().clear();
        MoveTo startPosition = new MoveTo(this.lineStart.getX(), this
                .lineStart.getY());
        bindSegment(startPosition, null, this.lineStart);
        this.linePath.getElements().add(startPosition);

        // Loop through from beginning and add new lines
        MetroStation cursor = this.lineStart;
        while (cursor.getNextByLine(this) != null) {

            WeightedLineTo line = new WeightedLineTo(cursor, cursor
                    .getNextByLine(this), this);
            cursor.addNextSegment(this, line);
            cursor.getNextByLine(this).addPrevSegment(this, line);
            bindSegment(line, cursor, cursor.getNextByLine(this));
            addSegment(line); // Add to canvas
            cursor = cursor.getNextByLine(this);
        }
    }

    public void reloadSegments(MetroStation endStation, boolean keepStart) {
        // Remove the old line segments
        this.linePath.getElements().clear();
        boolean seenEndStation = false;

        // Loop through from beginning (first non line-end stop) and add new
        // lines
        MetroStation cursor = this.lineStart.getNextByLine(this);
        if (cursor == endStation) {
            // Update the next station of the linestart
            if (keepStart)
                this.lineStart.addNext(this, endStation);
            endStation = endStation.getNextByLine(this);
            if (!keepStart)
                this.lineStart.addNext(this, endStation);
        }

        // New start/end point of line is specified by the parameter
        MoveTo startPosition = new MoveTo(endStation.getX(), endStation.getY());
        bindSegment(startPosition, null, endStation);
        this.linePath.getElements().add(startPosition);

        while (cursor != null) {
            WeightedLineTo line = new WeightedLineTo(cursor, cursor
                    .getNextByLine(this), this);
            cursor.addNextSegment(this, line);
            cursor.getNextByLine(this).addPrevSegment(this, line);
            bindSegment(line, cursor, cursor.getNextByLine(this));
            addSegment(line); // Add to canvas
            cursor = cursor.getNextByLine(this);

            // TODO: check for the first stop (not line end) and terminate
            // the loop (this is for circular lines)

            if (cursor == endStation && seenEndStation) {
                // Complete the loop by constructing a new segment
                line = new WeightedLineTo(cursor, cursor.getNextByLine(this),
                        this);
                bindSegment(line, cursor, cursor.getNextByLine(this));
                addSegment(line); // Add to canvas

                // Delete all the line end stuff
                this.lineStart.removeLabel();
                shapes.remove(this.lineStart);
                this.lineEnd.removeLabel();
                shapes.remove(this.lineEnd);
                return;
            } else if (cursor == endStation)
                seenEndStation = true;
        }
    }

    /**
     * Updates the line stations list in order where the first and last
     * entries are metro line ends and everything in between is a station
     */
    public void updateStationSequence() {
        this.lineStations.clear();
        MetroStation cursor = this.lineStart;
        while (cursor != null) {
            this.lineStations.add(cursor.getStationReference());
            cursor = cursor.getNextByLine(this);
        }
    }

    public void updateStationSequenceLoop(MetroStation endStation) {
        this.lineStations.clear();
        MetroStation cursor = endStation;
        while (cursor != null) {
            this.lineStations.add(cursor.getStationReference());
            cursor = cursor.getNextByLine(this);

            // Exit the method if station we hit the first station again
            // (after the line start)
            if (cursor == endStation) {
                return;
            }
        }
    }

    public Path getPath() {
        return this.linePath;
    }

    public MetroStation getLineStart() {
        return lineStart;
    }

    public MetroStation getLineEnd() {
        return lineEnd;
    }

    public AppTemplate getApp() {
        return app;
    }

    public List<String> getLineStationStrings() {
        return lineStationStrings;
    }
    public boolean isCircular() { return this.isCircular; }
    public void setCircular(boolean circular) { this.isCircular = circular; }

    @Override
    public String toString() { return this.lineName; }
}
