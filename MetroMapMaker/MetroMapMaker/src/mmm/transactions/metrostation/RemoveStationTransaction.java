package mmm.transactions.metrostation;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import jtps.jTPS_Transaction;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;
import mmm.data.StationReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// TODO: Works for the most part, just not repeatedly undoing and redoing a
// station deletion that makes a line go from circular to not circular
public class RemoveStationTransaction implements jTPS_Transaction {
    private MetroStation station;
    private ObservableList<Node> shapes;
    private Map<MetroLine, MetroStation> prev, next;
    private Map<MetroLine, Integer> stationPosition = new HashMap<>(); // Keep track
    // of station position so it will re-add to the right position
    private Set<MetroLine> previouslyCircleLines = new HashSet<>();
    private ObservableList<StationReference> stations;

    public RemoveStationTransaction(MetroStation station, ObservableList<Node>
            shapes, ObservableList<StationReference> stations) {
        this.station = station; // Shape that we are inserting
        this.prev = station.getPrev();
        this.next = station.getNext();
        this.shapes = shapes;
        this.stations = stations;

        // Add station positions to map
        for (MetroLine line: station.getAssociatedLines()) {
            stationPosition.put(line, line.getLineStations().indexOf(station
                    .getStationReference()));
            if (line.isCircular())
                previouslyCircleLines.add(line); // Help with transition when
            // we undo remove to create a circular line again
        }

    }

    @Override
    public void doTransaction() {
        // Make sure to remove station from all associated lines
        for (MetroLine line: station.getAssociatedLines())
            line.removeStation(station);

        // When undoing the station add, remember to remove the label also
        station.getAssociatedLines().clear(); // Clear all the lines
        station.removeLabel(); // Remove the station label from canvas
        shapes.remove(station);
    }

    @Override
    public void undoTransaction() {
        // Add all the associated lines back
        for (MetroLine line: stationPosition.keySet()) {
            station.addLine(line);
            // Add station back to correct position in lines
            line.getLineStations().add(stationPosition.get(line), station
                    .getStationReference());

            // Add the connections back
            prev.get(line).addNext(line, station);
            next.get(line).addPrev(line, station);

            // Redraw the segment
            if (line.isCircular() || previouslyCircleLines.contains(line)) {
                line.reloadSegments(line.getLineStart().getNextByLine(line),
                        false);
                line.updateStationSequenceLoop(line.getLineStart().getNextByLine(line));
                line.setCircular(true);
            } else {
                line.reloadSegments();
                line.updateStationSequence();
            }
        }

        this.shapes.add(this.station);
        // When redoing, just add the label back
        if (!shapes.contains(station.getAssociatedLabel()))
            station.addLabel();

        // Add station reference back into list
        stations.add(station.getStationReference());
    }
}
