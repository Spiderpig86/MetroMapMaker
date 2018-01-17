package mmm.transactions.metroline;

import djf.AppTemplate;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import jtps.jTPS_Transaction;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;
import mmm.data.StationReference;

public class RemoveLineTransaction implements jTPS_Transaction {

    private MetroLine line;
    private ObservableList<MetroLine> metroLines;
    private ObservableList<Node> shapes;
    private AppTemplate app;

    public RemoveLineTransaction(MetroLine line, ObservableList<MetroLine>
            metroLines, ObservableList<Node> shapes, AppTemplate app) {
        this.line = line;
        this.metroLines = metroLines;
        this.shapes = shapes;
        this.app = app;
    }

    @Override
    public void doTransaction() {
        // We just want to remove the line but keep the remaining stations
        metroLines.remove(line);

        // Iterate through the stations and remove the associations
        for (StationReference station: line.getLineStations()) {
            station.getStation().removeLine(line); // Also removes the
            // references to next and prev stations
        }

        // Actually remove the line from the canvas
        shapes.remove(line.getPath());
        shapes.remove(line.getLineStart());
        line.getLineStart().removeLabel();
        shapes.remove(line.getLineEnd());
        line.getLineEnd().removeLabel();
    }

    @Override
    public void undoTransaction() {
        metroLines.add(line);

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

        // Add all the references to the station back
        // Note that the list of stations is already in order
        // Also, the line ends are automatically considered to be here, so
        // there will always be at least 2 "stations" (stored in the front)
        if (!line.isCircular()) {
            for (int i = 0; i < line.getLineStations().size() - 1; i++) {
                // Add the next station
                line.getLineStations().get(i).getStation().addNext(line, line
                        .getLineStations().get(i + 1).getStation());
                line.getLineStations().get(i + 1).getStation().addPrev(line, line
                        .getLineStations().get(i).getStation());
            }
            line.reloadSegments();
            line.updateStationSequence();
        } else {
            // It is a circular line
            for (int i = 0; i < line.getLineStations().size() - 1; i++) {
                // Add the next station
                line.getLineStations().get(i).getStation().addNext(line, line
                        .getLineStations().get(i + 1).getStation());
                line.getLineStations().get(i + 1).getStation().addPrev(line, line
                        .getLineStations().get(i).getStation());
            }
            // Bind the last station to the first
            line.getLineStations().get(0).getStation().addPrev(line, line
                    .getLineStations().get(line.getLineStations().size() - 1)
                    .getStation());
            line.getLineStations().get(line.getLineStations().size() - 1)
                    .getStation().addNext(line, line.getLineStations().get(0)
                    .getStation());

            line.reloadSegments(line.getLineStart().getNextByLine(line), false);
            line.updateStationSequenceLoop(line.getLineStart().getNextByLine
                    (line));
        }
    }
}