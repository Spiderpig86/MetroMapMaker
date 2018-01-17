package mmm.transactions.metroline;

import djf.AppTemplate;
import javafx.geometry.Point2D;
import jtps.jTPS_Transaction;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;
import mmm.data.MetroData;

public class CircularTransaction implements jTPS_Transaction {
    private boolean isCircular = false;
    private MetroLine line;
    private AppTemplate app;
    private Point2D startLocation;
    private Point2D endLocation;

    public CircularTransaction(MetroLine line, boolean isCircular,
                               AppTemplate app, Point2D startLocation,
                               Point2D endLocation) {
        this.isCircular = isCircular;
        this.line = line;
        this.app = app;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    @Override
    public void doTransaction() {
        if (isCircular) {
            makeCircular();
        } else {
            makeRegular();
        }
    }

    @Override
    public void undoTransaction() {
        if (isCircular) {
            makeRegular();
        } else {
            makeCircular();
        }
    }

    /**
     * Makes the line cyclic by connecting the last station to the first.
     */
    private void makeCircular() {
        // Otherwise, we can just make the line circular by attaching the
        // start and the end
        line.setCircular(true);
        // Add the first station back into the station
        //line.addStation(line.getLineStart().getNextByLine(line)); //
        // Circular property handled by the addStation() method

        line.getLineStart().getNextByLine(line).addPrev(line, line
                .getLineEnd().getPrevByLine(line));
        line.getLineEnd().getPrevByLine(line).addNext(line, line
                .getLineStart().getNextByLine(line));
        line.reloadSegments(line.getLineStart().getNextByLine(line),
                true);

        line.updateStationSequenceLoop(line.getLineStart().getNextByLine
                (line));
    }

    /**
     * Adds line ends back to the line.
     */
    private void makeRegular() {
        MetroStation prev = line.getLineStart().getNextByLine(line);
        MetroStation next = line.getLineEnd().getPrevByLine(line);
//        System.out.println(prev.toString());
//        System.out.println(next.toString());
        // The second station
        double stationRadius = line.getLineStart().getRadiusX() * 2;
        line.getLineStart().setLocationAndSize(startLocation.getX(), startLocation.getY(),
                stationRadius, stationRadius);
        line.getLineEnd().setLocationAndSize(endLocation.getX(), endLocation.getY(),
                stationRadius, stationRadius);
        line.getLineStart().getAssociatedLabel().setLocationAndSize
                (line.getLineStart().getX
                        (), line.getLineStart().getY());
        line.getLineEnd().getAssociatedLabel().setLocationAndSize(line.getLineEnd().getX
                (), line.getLineEnd().getY());
        prev.addPrev(line, line.getLineStart());
        next.addNext(line, line.getLineEnd()); // Add the line end again
        line.setCircular(false); // Reload this as a regular line

        // Add the ends and labels back
        line.getLineStart().addLabel();
        line.getLineEnd().addLabel();
        MetroData data = (MetroData) app.getDataComponent();
        data.getShapes().addAll(line.getLineStart(), line.getLineEnd());

        line.reloadSegments();
        line.updateStationSequence();
    }

}
