package mmm.gui;

import djf.AppPropertyType;
import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.shape.PathElement;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mmm.controls.MetroLineEnd;
import mmm.controls.MetroStation;
import mmm.css.MetroStyle;
import mmm.data.StationReference;
import mmm.data.WeightedLineTo;
import properties_manager.PropertiesManager;

import java.util.*;

import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;
import static mmm.MetroLanguageProperty.*;

public class RouteDialog {

    private static StationReference startStation;
    private static StationReference endStation;
    private static ObservableList<StationReference> metroStations;

    private static Map<StationReference, Double> stationDistances = new
            HashMap<>();
    private static Map<StationReference, StationReference> stationParent =
            new HashMap<>();
    private static Map<StationReference, Boolean> stationVisited = new
            HashMap<>();

    private static AppTemplate app;

    public static void showDialog(AppTemplate appTemplate,
                                  ObservableList<StationReference> stations,
            StationReference start, StationReference end) {
        startStation = start;
        endStation = end;
        metroStations = stations;
        app = appTemplate;

        PropertiesManager props = PropertiesManager.getPropertiesManager();
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();

        // Construct a new stage
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.WINDOW_MODAL);
        window.setTitle(props.getProperty(ROUTE_TITLE));

        // Create our base layout
        VBox vLayout = new VBox(10);
        vLayout.setAlignment(Pos.CENTER);
        vLayout.setPadding(new Insets(10, 50, 10, 50));

        // Create the control
        Label routeLabel = new Label(props.getProperty(ROUTE_LABEL)
                + startStation.toString() + " to " + endStation.toString());
        TextArea routeText = new TextArea();
        routeText.setEditable(false);
        Button okButton  = new Button(props.getProperty(BUTTON_OK));
        okButton.setOnAction(e -> {
            window.close();
        });

        // Add to layout
        vLayout.getChildren().addAll(routeLabel, routeText, okButton);

        calculateRoute();

        // Insert our string
        String routeInfo = routeInfo();
        if (routeInfo == null)
            return;
        routeText.setText(routeInfo);

        // Add layout to scene
        Scene scene = new Scene(vLayout, 600, 400);

        // Set the scene style
        MetroStyle.initStyle(app, scene);

        // SET THE APP ICON
        String appIcon = FILE_PROTOCOL + PATH_IMAGES + props.getProperty(AppPropertyType.APP_LOGO);
        window.getIcons().add(new Image(appIcon));

        window.setScene(scene);
        window.initModality(Modality.APPLICATION_MODAL);
        window.showAndWait();
    }

    private static void calculateRoute() {
        // Create a map holding all the stations with weights
        // TODO: make this a singleton and cache these values

        for (StationReference station: metroStations) {
            stationDistances.put(station, Double.MAX_VALUE);
            stationParent.put(station, null); // Points to shortest parent
            stationVisited.put(station, false);
        }

        // Distance from itself should be 0
        stationDistances.put(startStation, 0.0);
        StationReference nextStation = startStation; //
        // Holds the shortest distance station compared to all the adjacent
        // stations

        // Take the starting station and start checking its neighbors
        // Use the outer loop for going through every vertex
        // Solution is to change this to a while loop
        for (int i = 0; i < metroStations.size(); i++) {
            // First look at all the neighbors
            List<MetroStation> adjacentStations = new ArrayList<>();
            // Populate it with the previous and next stations
            adjacentStations.addAll(nextStation.getStation().getPrev().values());
            adjacentStations.addAll(nextStation.getStation().getNext().values());

            // Iterate over all of its neighbors
            for (MetroStation adjStation: adjacentStations) {
                // Get the distance from where we start and the weight of the
                // edge to go to adjStation
                WeightedLineTo associatedEdge = getAssociatedSegment(nextStation
                        .getStation(), adjStation);

                if (associatedEdge == null || adjStation instanceof
                        MetroLineEnd)
                    continue;

                if (!stationVisited.get(adjStation.getStationReference())) {
//                    double distance = stationDistances.get(nextStation) + associatedEdge
//                            .getWeight(); // THIS CALCULATES BASED ON DISTANCE
                    double distance = stationDistances.get(nextStation) + 1;

                    // Update the distance if it is less than original
                    if (distance < stationDistances.get(adjStation
                            .getStationReference())) {
                        stationDistances.put(adjStation.getStationReference(),
                                distance);

                        stationParent.put(adjStation.getStationReference(), nextStation); //
                        // Update parent of next station (since we have found
                        // the shortest path
                    }
                }
            }

            stationVisited.put(nextStation, true); // Mark this station as visited

            // The next node to visit is to get the one that is the shortest
            // distance away from the one we just processed
            nextStation = getShortestDistance();

        }
    }

    private static WeightedLineTo getAssociatedSegment(MetroStation source, MetroStation adjacent) {
        // Iterate over all connected
        for (PathElement element: source.getPrevSegments().values()) {
            WeightedLineTo prev = (WeightedLineTo) element;
            if (prev.getSource() == adjacent)
                return prev;
        }

        for (PathElement element: source.getNextSegments().values()) {
            WeightedLineTo next = (WeightedLineTo) element;
            if (next.getDestination() == adjacent)
                return next;
        }

        return null;
    }

    private static StationReference getShortestDistance() {
        // Starting stats for station
        StationReference shortestStation = null;
        double shortestDist = Double.MAX_VALUE;

        // Iterate over all the stations in the map
        for (StationReference station: stationDistances.keySet()) {
            double distance = stationDistances.get(station);

            // Update only if we have not visited it yet and it is less than
            // what we have
            if (!stationVisited.get(station) && distance <= shortestDist) {
                shortestDist = distance;
                shortestStation = station;
            }
        }

        //System.out.println(stationDistances.toString());

        return shortestStation;
    }

    public static String routeInfo() {
        StringBuilder sb = new StringBuilder();

        // Trace back to original station for shortest path
        StationReference cursor = endStation;
        if (stationParent.get(endStation) == null) {
            AppDialogs.showMessageDialog(app.getStage(), "No Path Found",
                    "Unable to find path to destination.");
            return null;
        }
        WeightedLineTo lastEdge = getAssociatedSegment(endStation.getStation(),
                stationParent.get(endStation).getStation());
        sb.append("Disembark " + lastEdge.getLine()
                .getLineName() + " at " + endStation.toString() + "\n");
        Map<String, Integer> stops = new LinkedHashMap<>();
        while (stationParent.get(cursor) != startStation) {
            WeightedLineTo curEdge = getAssociatedSegment(cursor.getStation(),
                    stationParent.get(cursor).getStation());

            // Increment the number of stops needed to be taken
            if (!stops.keySet().contains(curEdge.getLine().getLineName()))
                stops.put(curEdge.getLine().getLineName(), 2);
            else
                stops.put(curEdge.getLine().getLineName(), stops.get(curEdge
                        .getLine().getLineName()) + 1);

            // Check if there was a line change, add transfer if there is
            if (!curEdge.getLine().equals(lastEdge.getLine())) {
                sb.insert(0, "Transfer to " + lastEdge.getLine() + " at " +
                        cursor.getStation().toString() + "\n");

                // Subtract one from the transferring line
                stops.put(lastEdge.getLine().getLineName(), stops
                        .get(lastEdge.getLine().getLineName()) - 1);
            }

            cursor = stationParent.get(cursor);
            lastEdge = curEdge;
        }

        // Handle the case where there is one stop that is on a different line
        WeightedLineTo firstEdge = getAssociatedSegment(cursor.getStation(), startStation
                .getStation());
        if (!firstEdge.getLine().equals(lastEdge.getLine())) {
            sb.insert(0, "Transfer to " + lastEdge.getLine() + " at " +
                    cursor.getStation().toString() + "\n");
            // Check to see if the entry exists
            if (!stops.keySet().contains(firstEdge.getLine().getLineName()))
                stops.put(firstEdge.getLine().getLineName(), 1); // 1 extra stop
            else
                stops.put(firstEdge.getLine().getLineName(), stops.get(firstEdge
                        .getLine().getLineName()) + 1); // Update the count
        }

        // If the route only has 1 stop and was not picked up by the while loop
        if (!stops.keySet().contains(lastEdge.getLine().getLineName()))
            stops.put(lastEdge.getLine().getLineName(), 1);

        // Add the boarding information
        sb.insert(0, "Board " + getAssociatedSegment(cursor.getStation(),
                stationParent.get(cursor).getStation()).getLine().getLineName() + " at " +
                stationParent.get(cursor).getStation().toString() + "\n");

        // Prepend all the other line info backwards
        sb.insert(0, "\n");
        // Rough estimation of time where every 100px is 1km and runs about
        // 60 km/h or 40 mph average
        // Reference: Line A from end to end is 32 minutes
//        int time = new Double(((stationDistances.get
//                (endStation) * 2) / 4000) * 60).intValue();
        int time = new Double((stationDistances.get
                (endStation) * 3)).intValue();
        sb.insert(0, "\nEstimated time: " + time + " minutes");

        for (String s: stops.keySet())
            sb.insert(0, s + " Line (" + stops.get(s) + " stops)\n");

        sb.insert(0, "Destination: " + endStation.getStation().toString()
                + "\n");
        sb.insert(0, "Origin: " + startStation.getStation().toString() +
                "\n");

        return sb.toString();
    }
}
