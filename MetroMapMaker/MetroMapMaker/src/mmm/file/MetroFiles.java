package mmm.file;

import djf.AppTemplate;
import djf.components.AppDataComponent;
import djf.components.AppFileComponent;
import djf.ui.AppDialogs;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import mmm.controls.*;
import mmm.data.*;

import static djf.AppPropertyType.APP_ERROR_CONTENT;
import static djf.AppPropertyType.APP_ERROR_TITLE;
import static djf.language.AppLanguageSettings.*;

import javafx.scene.paint.Color;
import mmm.gui.MetroWorkspace;
import properties_manager.PropertiesManager;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.json.*;
import javax.json.stream.JsonGenerator;

/**
 * Created by slim1 on 11/2/2017.
 */
public class MetroFiles implements AppFileComponent {

    // Default XML values
    private static final String DEFAULT_DOCTYPE_DECLARATION = "<!doctype html>\n";
    private static final String DEFAULT_ATTRIBUTE_VALUE = "";

    // JSON storing
    private static final String JSON_BG_COLOR = "background";
    private static final String JSON_BG_IMG_PATH = "background_img_path";
    private static final String JSON_X = "x";
    private static final String JSON_Y = "y";
    private static final String JSON_WIDTH = "width";
    private static final String JSON_HEIGHT = "height";
    private static final String JSON_RED = "red";
    private static final String JSON_GREEN = "green";
    private static final String JSON_BLUE = "blue";
    private static final String JSON_ALPHA = "alpha";
    private static final String JSON_ELEMENTS = "elements";
    private static final String JSON_CANVAS_X = "canvas_x";
    private static final String JSON_CANVAS_Y = "canvas_y";
    private static final String JSON_TYPE = "type";
    private static final String JSON_FILL_COLOR = "fill_color";
    private static final String JSON_STROKE_WIDTH = "stroke_width";

    // Decorative stuff
    private static final String JSON_TEXT = "text";
    private static final String JSON_TEXT_FONT = "font";
    private static final String JSON_TEXT_SIZE = "font_size";
    private static final String JSON_TEXT_BOLD = "bold";
    private static final String JSON_TEXT_ITALICS = "italics";
    private static final String JSON_IMG_OVERLAY_PATH = "image_path";
    private static final String JSON_IMG_OVERLAY_X = "image_x";
    private static final String JSON_IMG_OVERLAY_Y = "image_y";

    // Stations / Lines
    private static final String JSON_LINE_NAME = "line_name";
    private static final String JSON_LINE_START_X = "line_start_x";
    private static final String JSON_LINE_START_Y = "line_start_y";
    private static final String JSON_LINE_END_X = "line_end_x";
    private static final String JSON_LINE_END_Y = "line_end_y";
    private static final String JSON_LINE_END_LABEL = "is_end_label"; // Denote label
    private static final String JSON_STATION_LABEL_COLOR =
            "station_label_color"; // Color for the station label
    // for line end
    private static final String JSON_CIRCULAR = "line_circular";
    private static final String JSON_LINE_STATIONS_ARRAY = "associated_stations";
    private static final String JSON_STATION_NAME = "station_name";
    private static final String JSON_STATION_LINES_ARRAY = "associated_lines"; //
    // Associated lines for a station object
    private static final String JSON_STATION_RADIUS = "station_radius";
    private static final String JSON_STATION_PREV = "stations_prev";
    private static final String JSON_STATION_NEXT = "stations_next";
    private static final String JSON_LABEL_LOCATION = "label_location";
    private static final String JSON_LABEL_ORIENTATION = "label_orientation";

    // Export Constants
    private static final String EXPORT_NAME = "name";
    private static final String EXPORT_LINES = "lines";
    private static final String EXPORT_CIRCULAR = "circular";
    private static final String EXPORT_COLOR = "color";
    private static final String EXPORT_STATION_NAMES = "station_names";
    private static final String EXPORT_STATIONS = "stations";

    private static final String recentProjectsJSONPath = PATH_DATA + "recents.json";

    // Store a map of recently opened projects
    private Map<String, String> recentProjectsMap;
    private List<String> recentProjectsList; // Store the projects in reverse
    private String currentProjectName = "";

    private AppTemplate app;
    private MetroWorkspace workspace;

    public MetroFiles(AppTemplate app) {
        this.app = app;
        this.workspace = (MetroWorkspace) app.getWorkspaceComponent();
    }

    /**
     * This function must be overridden in the actual component and would
     * write app data to a file in the necessary format.
     *
     * @param data
     * @param filePath
     */
    @Override
    public void saveData(AppDataComponent data, String filePath) throws IOException {
        MetroData dataManager = (MetroData) data; // Get our data manager

        // Get the background color of the canvas.
        Color bgColor = dataManager.getBackgroundColor();
        JsonObject bgJsonObj = makeJsonBgObject(bgColor, dataManager.getBackgroundImage());

        // Build our JSON objects to save
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        ObservableList<Node> shapes = dataManager.getShapes();

        for (Node node: shapes) {
            // Ignore the grid lines
            if (node instanceof Line)
                continue;

            // Process draggable shapes only
            Shape shape = (Shape) node;
            Draggable draggableShape = ((Draggable) shape);
            String type = draggableShape.getShapeType();
            double x = draggableShape.getX();
            double y = draggableShape.getY();
            // These may not be present for some shapes
            double width = draggableShape.getWidth();
            double height = draggableShape.getHeight();

            // COMMON PROPERTIES
            // Create color json object
            JsonObject fillColorJson = makeJsonColorObject(Color.TRANSPARENT);
            if (!type.equals(Draggable.IMAGE) && !type.equals(Draggable.LINE))
                fillColorJson = makeJsonColorObject((Color) shape.getFill());
            else if (type.equals(Draggable.LINE))
                fillColorJson = makeJsonColorObject((Color) shape.getStroke());
            // TODO: Need outline color object?
            double outlineThickness = shape.getStrokeWidth();

            // SPECIFIC PROPERTIES
            // Image properties (if present)
            String imgPath = "";
            double imgX = 0, imgY = 0;
            if (type.equals(Draggable.IMAGE)) {
                // Store the background image data
                DraggableImage image = (DraggableImage) shape;
                imgPath = image.getImagePath();
                imgX = image.getImage().getWidth(); // Get the image dimensions
                imgY = image.getImage().getHeight();
            }

            // Process the text properties
            String text = "", textFont = "", isBolded = "", isItalics = ""; // Textview properties
            double textSize = 0;
            if (type.equals(Draggable.TEXT)) {
                DraggableText textview = (DraggableText) shape;
                text = textview.getText();
                textFont = textview.getFont().getFamily(); // Font family
                textSize = textview.getFont().getSize(); // Get font size
                isBolded = Boolean.toString(textview.isBolded());
                isItalics = Boolean.toString(textview.isItalicized());
            }

            // Line properties
            JsonArrayBuilder lineStationsBuilder = Json.createArrayBuilder();
            String lineName = "";
            //double lineWidth = 0;
            double lineStartX = 0, lineStartY = 0, lineEndX = 0, lineEndY = 0;
            boolean isLineCircular = false;
            if (type.equals(Draggable.LINE)) {
                LinePath line = (LinePath) shape;
                lineName = line.getAssociatedLine().getLineName();
                //lineWidth = line.getStrokeWidth();

                // Get line coordinates
                lineStartX = line.getAssociatedLine().getLineStart().getX();
                lineStartY = line.getAssociatedLine().getLineStart().getY();
                lineEndX = line.getAssociatedLine().getLineEnd().getX();
                lineEndY = line.getAssociatedLine().getLineEnd().getY();

                // If the line is circular, update the property
                isLineCircular = line.getAssociatedLine().isCircular();

                // Add all the station names (they are in order already
                for (StationReference s: line.getAssociatedLine()
                        .getLineStations())
                    if (!(s.getStation() instanceof MetroLineEnd))
                        lineStationsBuilder.add(s.getStation().toString());


            }
            JsonArray lineStationsArray = lineStationsBuilder.build(); //
            // Array of station names associated with this line object

            // Station Properties
            JsonArrayBuilder stationLinesBuilder = Json.createArrayBuilder();
            JsonArrayBuilder stationPrevBuilder = Json.createArrayBuilder();
            JsonArrayBuilder stationNextBuilder = Json.createArrayBuilder();
            String stationName = "";
            double stationRadius = 0;
            // Station Label Properties
            String labelLocation = "";
            String labelOrientation = "";
            JsonObject labelColor = makeJsonColorObject(Color.TRANSPARENT);
            if (type.equals(Draggable.STATION)) {
                MetroStation station = (MetroStation) shape;
                stationName = station.toString();
                labelColor = makeJsonColorObject((Color) station
                        .getAssociatedLabel().getFill()); // Get text color
                // of the label
                stationRadius = station.getRadiusX();
                labelLocation = station.getAssociatedLabel().getLocation().toString();
                labelOrientation = station.getAssociatedLabel()
                        .getOrientation().toString();
                // Store the label font
                textFont = station.getAssociatedLabel().getFont().getFamily();
                textSize = station.getAssociatedLabel().getFont().getSize();
                isBolded = Boolean.toString(station.getAssociatedLabel()
                        .isBolded());
                isItalics = Boolean.toString(station.getAssociatedLabel()
                        .isBolded());
                for (MetroLine l: station.getAssociatedLines())
                    stationLinesBuilder.add(l.getLineName());
                for (MetroStation prev: station.getPrev().values())
                    stationPrevBuilder.add(prev.toString());
                for (MetroStation next: station.getNext().values())
                    stationNextBuilder.add(next.toString());

            }
            JsonArray stationLinesArray = stationLinesBuilder.build(); //
            // Array of line names associated with station
            JsonArray stationPrevArray = stationPrevBuilder.build();
            JsonArray stationNextArray = stationNextBuilder.build();

            // Line End Label Properties
            boolean isLineEndLabel = false;
            if (type.equals(Draggable.LINE_END_LABEL)) {
                isLineEndLabel = true; // Signify it is a line end label
            }


            // Build the JSON object
            JsonObject shapeJson = Json.createObjectBuilder()
                    .add(JSON_TYPE, type)
                    .add(JSON_X, x)
                    .add(JSON_Y, y)
                    .add(JSON_WIDTH, width)
                    .add(JSON_HEIGHT, height)
                    .add(JSON_FILL_COLOR, fillColorJson)
                    .add(JSON_STROKE_WIDTH, outlineThickness)
                    .add(JSON_IMG_OVERLAY_PATH, imgPath) // Write out image path
                    .add(JSON_IMG_OVERLAY_X, imgX)
                    .add(JSON_IMG_OVERLAY_Y, imgY)
                    .add(JSON_TEXT, text)
                    .add(JSON_TEXT_FONT, textFont)
                    .add(JSON_TEXT_SIZE, textSize)
                    .add(JSON_TEXT_BOLD, isBolded)
                    .add(JSON_TEXT_ITALICS, isItalics)
                    .add(JSON_LINE_NAME, lineName)
                    .add(JSON_LINE_END_LABEL, isLineEndLabel)
                    .add(JSON_LINE_START_X, lineStartX)
                    .add(JSON_LINE_START_Y, lineStartY)
                    .add(JSON_LINE_END_X, lineEndX)
                    .add(JSON_LINE_END_Y, lineEndY)
                    .add(JSON_CIRCULAR, Boolean.toString(isLineCircular))
                    .add(JSON_LINE_STATIONS_ARRAY, lineStationsArray)
                    .add(JSON_STATION_NAME, stationName)
                    .add(JSON_STATION_LABEL_COLOR, labelColor)
                    .add(JSON_STATION_LINES_ARRAY, stationLinesArray)
                    .add(JSON_STATION_RADIUS, stationRadius)
                    .add(JSON_STATION_PREV, stationPrevArray)
                    .add(JSON_STATION_NEXT, stationNextArray)
                    .add(JSON_LABEL_LOCATION, labelLocation)
                    .add(JSON_LABEL_ORIENTATION, labelOrientation)
                    .build(); // Writeout the text

            // TODO: Not sure if need to include the previous and next
            // stations of a station
            arrayBuilder.add(shapeJson);
        }

        JsonArray elementArray = arrayBuilder.build(); // Json array of elements
        double canvasX = workspace.getCanvas().getWidth(); // Width
        double canvasY = workspace.getCanvas().getHeight(); // Height

        // Create the root JSON object which holds the background color,
        // whether to display the image or not, and image path
        JsonObject dataManagerJObj = Json.createObjectBuilder()
                .add(JSON_BG_COLOR, bgJsonObj)
                .add(JSON_CANVAS_X, canvasX)
                .add(JSON_CANVAS_Y, canvasY)
                .add(JSON_ELEMENTS, elementArray)
                .build();

        // Write the JSON file
        createJSONFile(filePath, dataManagerJObj);
    }

    /**
     * This function must be overridden in the actual component and would
     * read app data from a file in the necessary format.
     *
     * @param data
     * @param filePath
     */
    @Override
    public void loadData(AppDataComponent data, String filePath) throws IOException {
        // Add to recently opened projects list
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd " +
                "HH:mm:ss").format(new Date());
        File f = new File(filePath);
        currentProjectName = f.getName().replace(".mmm", "");
        addRecentProject(f.getName(), PATH_WORK + currentProjectName
                + File.separatorChar + f.getName() + "@" + timeStamp);
        app.getGUI().updateToolbarControls(true); // No modifications yet
        app.getGUI().getFileController().markFileAsSaved();
        app.getStage().setTitle("Metro Map Maker - " + currentProjectName);
        // Indicate currently opened project in window title

        // Now to handle the actual data loading
        MetroData dataManager = (MetroData) data; // Get our data manager
        dataManager.resetData(); // Clear whatever was there before

        dataManager.resetTransactions(); // Delete edit history

        workspace = (MetroWorkspace) app.getWorkspaceComponent(); // Update
        // initialized workspace

        // Load the json file (.mmm) itself
        // TODO: Catch exception in method body
        JsonObject json = loadJSONFile(filePath);

        // Load the background color
        Color bgColor = loadColor(json, JSON_BG_COLOR);
        dataManager.setBackgroundColor(bgColor, false);

        // Load the background image (no image if null)
        ExtendedImage image = loadImage(json, JSON_BG_COLOR);
        if (image != null)
            dataManager.setBackgroundImage(image);

        // Create our array of elements
        // Initialize the lines after everything else
        JsonArray jsonShapeArray = json.getJsonArray(JSON_ELEMENTS);
        try {
            for (int i = 0; i < jsonShapeArray.size(); i++) {
                JsonObject jsonShape = jsonShapeArray.getJsonObject(i);
                Shape shape = loadElement(jsonShape, dataManager);

                // Note: Some elements only serve to provide info
                if (shape != null) {
                    dataManager.addElement(shape);
                }
            }
        } catch (Exception e) {
            AppDialogs.showStackTraceDialog(app.getStage(), e, "Error " +
                    "Loading File", "There was an error loading the project " +
                    "file. It may be corrupted.");
            return;
        }

        // Load the rest of the lines and then add the stations with
        // associated lines
        for (String stationName: dataManager.getMapStations().keySet()) {
            MetroStation station = dataManager.getMapStations().get
                    (stationName);

            // Add the associated lines for each station
            for (String line: dataManager.getMapLines().keySet())
                if (station.getAssociatedLinesStrings().contains(line))
                    station.addLine(dataManager.getMapLines().get(line));

            dataManager.addElement(station);
        }

        // For each of the lines, we will construct the path
        for (String lineName: dataManager.getMapLines().keySet()) {
            MetroLine line = dataManager.getMapLines().get(lineName);
            // For each of those new stations, add it to the line
            StationReference lastStation = null;

            for (String station: line.getLineStationStrings()) {
                StationReference stationReference = dataManager.getMapStations().get
                        (station).getStationReference();

                // Set up the neighboring stations (so the line segments
                // generate in the right order)
                if (lastStation == null) { // If this is the first station
                    stationReference.getStation().addPrev(line, line.getLineStart());
                    line.getLineStart().addNext(line, stationReference.getStation());
                } else {
                    // Not the first station
                    stationReference.getStation().addPrev(line, lastStation
                            .getStation());
                    lastStation.getStation().addNext(line, stationReference
                            .getStation());
                }
                if (!line.isCircular() || !stationReference.getStation()
                        .toString().equals(line.getLineName()))
                    line.getLineStations().add(Math.max(0,
                            line.getLineStations().size() - 1),
                            stationReference);
                // BIG CHANGE ABOVE SO STATIONS APPEAR IN BETWEEN THE LINE
                // ENDS IN GETLINESTATIONS()
                lastStation = stationReference;
            }

            // Add the last segment to the line end
            if (lastStation != null && !line.isCircular()) {
                lastStation.getStation().addNext(line, line.getLineEnd());
                line.getLineEnd().addPrev(line, lastStation.getStation());
                line.reloadSegments(); // Reload the line segments
            } else if (line.isCircular()) {
                // Bind the last station to the first station (not line end)
                lastStation.getStation().addNext(line, line.getLineStart()
                        .getNextByLine(line));
                line.getLineEnd().addPrev(line, lastStation.getStation());
                // Bind the first station to the last station
                line.getLineStart().getNextByLine(line).addPrev(line,
                        lastStation.getStation());

                // Reconstruct with terminus as the first station
                line.reloadSegments(line.getLineStart().getNextByLine(line),
                        true);
//                System.out.println(lineName + " start: " + line.getLineStart
//                        ().getNextByLine(line).toString());
//                System.out.println(lineName + " end: " + line.getLineEnd()
//                        .getPrevByLine(line).toString());
            } else {
                // This means the line has no stations, just built it with
                // the line start and line end
                line.getLineStart().addNext(line, line.getLineEnd());
                // Bind the first station to the last station
                line.getLineEnd().addPrev(line, line.getLineEnd());
                line.reloadSegments();
            }
        }

        // Update workspace comboboxes
        workspace.updateLinesAndStations();

        // Initialize canvas with initial dimensions TODO
        workspace.getCanvas().setPrefWidth(getDataAsDouble(json, JSON_CANVAS_X));
        workspace.getCanvas().setPrefHeight(getDataAsDouble(json, JSON_CANVAS_Y));

        // TODO: Set clipping property of all shapes on canvas
        for (Node n: dataManager.getShapes()) {
            Rectangle clip = ((MetroWorkspace) app.getWorkspaceComponent())
                    .cloneCanvasClip();
            n.setClip(clip); // Part 13: Set shape clipping
        }

        app.getGUI().getFileController().markFileAsSaved();
    }

    /**
     * This function must be overridden in the actual component and would
     * be used for exporting app data into another format.
     *
     * OLD EXPORT WHICH EXPORTED ACTUAL STATION NAMES
     * @param data
     */
    @Override
    public void exportData(AppDataComponent data) throws IOException {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();

        // First check if the export directory exists. If not, create it
        File exportDir = new File(PATH_EXPORT);
        if (!createDirectory(exportDir))
            return;

        // Now create the directory for the project
        File projectExportDir = new File(PATH_EXPORT + currentProjectName);
        if (!createDirectory(projectExportDir))
            return;

        // Create the actual export object
        JsonObjectBuilder exportObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder linesArrayBuilder = Json.createArrayBuilder();

        // Iterate over list of lines in combobox
        for (MetroLine line: workspace.getLineCombo().getItems()) {
            JsonObjectBuilder lineObjectBuilder = Json.createObjectBuilder();
            JsonObject color = makeJsonColorObject(line.getLineColor());

            // Create the associated stations
            JsonArrayBuilder stationsBuilder = Json.createArrayBuilder();
            for (StationReference station: line.getLineStations())
                if (!(station.getStation() instanceof MetroLineEnd))
                    stationsBuilder.add(station.getStation().toString());

            JsonObject lineObject = lineObjectBuilder.add(EXPORT_NAME, line.getLineName())
                    .add(EXPORT_CIRCULAR, false)
                    .add(EXPORT_COLOR, color)
                    .add(EXPORT_STATION_NAMES, stationsBuilder.build())
                    .build();

            // Add to the array
            linesArrayBuilder.add(lineObject);
        }
        JsonArray linesArray = linesArrayBuilder.build();

        // Iterate over list of stations
        JsonArrayBuilder stationsArrayBuilder = Json.createArrayBuilder();
        for (StationReference station: workspace.getStationCombo().getItems()) {
            if (station.getStation() instanceof MetroLineEnd)
                continue;

            JsonObjectBuilder stationObjectBuilder = Json.createObjectBuilder();
            JsonObject stationObject = stationObjectBuilder.add(EXPORT_NAME,
                    station.getStation().toString())
                    .add(JSON_X, station.getStation().getCenterX())
                    .add(JSON_Y, station.getStation().getCenterY())
                    .build();

            stationsArrayBuilder.add(stationObject);
        }
        JsonArray stationsArray = stationsArrayBuilder.build();

        // Add all the items together
        JsonObject mapExportObject = exportObjectBuilder.add(EXPORT_NAME,
                currentProjectName)
                .add(EXPORT_LINES, linesArray)
                .add(EXPORT_STATIONS, stationsArray)
                .build();

        // Export the object to file
        createJSONFile(projectExportDir.getCanonicalPath() + File.separatorChar +
                currentProjectName + " Metro.json", mapExportObject);

        Pane canvas = workspace.getCanvas();
        double oldWidth = canvas.getScaleX();
        double oldHeight = canvas.getScaleY();
//        SnapshotParameters parameters = new SnapshotParameters();
        double scaleFactor = workspace.getController().getScaleFactor();
//        parameters.setViewport(new Rectangle2D(canvas.getLayoutX(),
//                canvas.getLayoutY(), canvas.getWidth() *
//               scaleFactor, canvas.getHeight() * scaleFactor));
        canvas.setScaleX(canvas.getScaleX() / scaleFactor);
        canvas.setScaleY(canvas.getScaleY() / scaleFactor);
        WritableImage image = canvas.snapshot(new SnapshotParameters(), null);

        File file = new File(projectExportDir.getCanonicalPath() + File
                .separatorChar + currentProjectName + " Metro.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }

        // Return to old size of canvas
        canvas.setScaleX(oldWidth);
        canvas.setScaleY(oldHeight);

        AppDialogs.showMessageDialog(((MetroData) data).getApp().getStage
                (), "Export Successful", "Map project files " +
                "successfully exported.");
    }

    /**
     * This function must be overridden in the actual component and would
     * be used for importing app data from another format.
     *
     * @param data
     * @param filePath
     */
    @Override
    public void importData(AppDataComponent data, String filePath) throws IOException {

    }

    /**
     * Initializes a directory for a new project with a blank project file
     * @param name
     *      The name of the project
     * @return
     *      The folder path of the new project
     */
    public String createProjectDirectory(String name, boolean updateRecents) {
        // Create the new directory
        boolean success = new File(PATH_WORK + name).mkdir();
        String filePath = "";

        if (success) {
            // Create blank JSON file
            filePath = PATH_WORK + name + File
                    .separatorChar + name + ".mmm";
            createJSONFile(filePath, createBlankObject());

            String timeStamp = new SimpleDateFormat("yyyy/MM/dd " +
                    "HH:mm:ss").format(new Date());
            if (updateRecents)
                addRecentProject(name + ".mmm", filePath + "@" + timeStamp);
        }
        return filePath;
    }

    @Override
    public void saveAsData(AppDataComponent data, String name) throws
            IOException {
        // Check if the folder already exists
        File projectDir = new File(PATH_WORK + name);
        // Return if cancelled
        if (name.length() == 0 || (projectDir.isDirectory() && projectDir.exists
                ())) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Unable " +
                    "to create project directory. Please try again " +
                    "with a unique name.");
            return;
        }

        // Use MetroFiles to create new directory and project file
        MetroFiles files = (MetroFiles) app.getFileComponent();
        String path = files.createProjectDirectory(name, false);

        // Load the file when successful
        if (path.length() > 0) {
            // The new project directory has been created and we can now save
            // our data there
            saveData(app.getDataComponent(), projectDir.getCanonicalPath() + File
                    .separatorChar + name + ".mmm");
        } else {
            // Could not create the new folder
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Unable " +
                    "to create project directory. Please try again " +
                    "with a valid name.");
        }
    }

    public Map<String, String> getRecentProjects() {
        // Refresh list of recently opened projects
        if (recentProjectsMap == null)
            loadRecentJSON();
        return recentProjectsMap;
    }

    public void addRecentProject(String name, String pathAndDate) {
        if (recentProjectsMap.size() > 5) {
            // Iterate through map and see which project was opened earliest
            // and delete it
            String earliestProjectName = "";
            Long minTime = System.currentTimeMillis();
            for (String s: recentProjectsMap.keySet()) {
                String date = recentProjectsMap.get(s).split("@")[1];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd " +
                        "HH:mm:ss");
                try {
                    Date dateObj = sdf.parse(date);
                    Long projTime = dateObj.getTime();
                    if (minTime > projTime) {
                        minTime = projTime;
                        earliestProjectName = s;
                    }
                } catch (ParseException e) {
                    AppDialogs.showMessageDialog(app.getStage(), e,
                            PROPERTIES_FILE_ERROR_MESSAGE);
                }
            }

            // Delete the earliest entry
            if (!earliestProjectName.equals("")) {
                recentProjectsMap.remove(earliestProjectName);
                recentProjectsList.remove(earliestProjectName);
            }
        }

        if (recentProjectsList.contains(name)) {
            // If it does contain it, remove it from the list and insert it
            // at the top
            recentProjectsList.remove(name);
            // Note we only need to modify recentProjectsList which would
            // take care of the ordering
        }

        recentProjectsList.add(0, name);
        recentProjectsMap.put(name, pathAndDate);
        saveRecentJSON(); // Update recent projects file
    }

    public void loadRecentJSON() {
        recentProjectsMap = new LinkedHashMap<>();
        recentProjectsList = new ArrayList<>();
        File recentsJSON = new File(recentProjectsJSONPath);

        if (recentsJSON.exists() && recentsJSON.isFile()) {
            // Load the contents of the recent projects file
            try {
                JsonObject recentObject = loadJSONFile(recentProjectsJSONPath);
                JsonArray recentArray = recentObject.getJsonArray("recent");
                // Get the array of recently opened projects

                for (int i = 0; i < recentArray.size(); i++) {
                    JsonObject recentItem = recentArray.getJsonObject(i);

                    // Check if the project file still exists and add to list
                    // only if it does
                    File file = new File(recentItem.getString("project_path"));
                    if (!file.exists())
                        continue; // Skip (delete) non-existing entry

                    recentProjectsMap.put(recentItem.getString("name"),
                            recentItem.getString("project_path") + "@" +
                                    recentItem.getString("date_opened"));
                    recentProjectsList.add(recentItem.getString("name")); //
                    // When loading, the projects are already in the right
                    // order (last project opened first)
                }
            } catch (IOException e) {
                PropertiesManager props = PropertiesManager
                        .getPropertiesManager();
                AppDialogs.showStackTraceDialog(app.getStage(), e, props.getProperty(APP_ERROR_TITLE),
                        props.getProperty(APP_ERROR_CONTENT));
            }

        } else {
            // Create the recents file if it does not exist
            JsonArray recents = Json.createArrayBuilder().build();
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("recent", recents)
                    .build();
            createJSONFile(recentsJSON.getPath(), jsonObject);
        }
    }

    public void saveRecentJSON() {
        File recentsJSON = new File(recentProjectsJSONPath);

        if (recentsJSON.exists() && recentsJSON.isFile()) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (String key: recentProjectsList) {
                JsonObject recentJSONItem = Json.createObjectBuilder()
                    .add("name", key)
                    .add("project_path", recentProjectsMap.get(key).split
                            ("@")[0])
                    .add("date_opened", recentProjectsMap.get(key).split
                            ("@")[1])
                    .build();

                // Add the object to the array builder
                arrayBuilder.add(recentJSONItem);
            }

            // Add the array to the parent object
            createJSONFile(recentProjectsJSONPath, Json.createObjectBuilder()
                    .add("recent", arrayBuilder.build()).build());
        }
    }

    /**
     * Helper method to write a JSON object to a JSON file
     * @param filePath
     *      The path to write the file to
     * @param json
     *      The JSON object contents we are writing
     */
    private void createJSONFile(String filePath, JsonObject json) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(sw);
        jsonWriter.writeObject(json);
        jsonWriter.close();

        JsonWriter jsonFileWriter;
        String prettyPrinted;
        PrintWriter pw;
        new File(filePath); // Create the blank file first
        try (OutputStream os = new FileOutputStream(filePath)) {
            jsonFileWriter = Json.createWriter(os);
            jsonFileWriter.writeObject(json);
            prettyPrinted = sw.toString();
            pw = new PrintWriter(filePath);
            pw.write(prettyPrinted);
            pw.close();
        } catch (IOException e) {
            AppDialogs.showMessageDialog(app.getStage(), e,
                    PROPERTIES_FILE_ERROR_MESSAGE);
        }
    }

    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
        InputStream is = new FileInputStream(jsonFilePath);
        JsonReader jsonReader = Json.createReader(is);
        JsonObject json = jsonReader.readObject();
        jsonReader.close();
        is.close();
        return json;
    }

    /**
     * Helper method mainly used by export to create the directory
     * @param file
     * @return
     *      Return if the folder was successfully created or it exists already
     */
    private boolean createDirectory(File file) {
        if (!file.exists()) {
            boolean canMakeDir;
            canMakeDir = file.mkdir(); // Create the directory if it does
            // not exist

            // If the directory could not be created, show error message and return
            if (!canMakeDir) {
                AppDialogs.showMessageDialog(app.getStage(), "Error", "Unable " +
                        "to create export directory. Please check user " +
                        "permissions.");
                return false;
            }
        }
        // Return true even if the folder exists already
        return true;
    }

    public JsonObject createBlankObject() {

        // Get the background color of the canvas.
        Color bgColor = Color.WHITE;
        JsonObject bgJsonObj = makeJsonBgObject(bgColor, null);

        // Build our JSON objects to save
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        JsonArray elementArray = arrayBuilder.build(); // Json array of elements

        // Create the root JSON object which holds the background color,
        // whether to display the image or not, and image path
        JsonObject dataManagerJObj = Json.createObjectBuilder()
                .add(JSON_BG_COLOR, bgJsonObj)
                .add(JSON_CANVAS_X, 1200)
                .add(JSON_CANVAS_Y, 1200)
                .add(JSON_ELEMENTS, elementArray)
                .build();

        return dataManagerJObj;
    }

    /* HELPER JSON METHODS */

    /**
     * Build a json object from color
     * @param color
     * @return
     */
    private JsonObject makeJsonBgObject(Color color, ExtendedImage
            image) {
        JsonObject colorJson = Json.createObjectBuilder()
                .add(JSON_RED, color.getRed())
                .add(JSON_GREEN, color.getGreen())
                .add(JSON_BLUE, color.getBlue())
                .add(JSON_ALPHA, color.getOpacity())
                .add(JSON_BG_IMG_PATH, (image != null) ? image.getImagePath()
                : "").build();
        return colorJson;
    }

    private JsonObject makeJsonColorObject(Color color) {
        JsonObject colorJson = Json.createObjectBuilder()
                .add(JSON_RED, color.getRed())
                .add(JSON_GREEN, color.getGreen())
                .add(JSON_BLUE, color.getBlue())
                .add(JSON_ALPHA, color.getOpacity()).build();
        return colorJson;
    }

    private double getDataAsDouble(JsonObject json, String dataName) {
        JsonValue value = json.get(dataName);
        JsonNumber number = (JsonNumber)value;
        return number.bigDecimalValue().doubleValue();
    }

    private String getDataAsString(JsonObject json, String dataName) {
        JsonValue value = json.get(dataName); // Get the value at the entry
        JsonString string = (JsonString) value; // Get the string value
        return string.getString();
    }

    private Color loadColor(JsonObject json, String colorToGet) {
        JsonObject jsonColor = json.getJsonObject(colorToGet);
        double red = getDataAsDouble(jsonColor, JSON_RED);
        double green = getDataAsDouble(jsonColor, JSON_GREEN);
        double blue = getDataAsDouble(jsonColor, JSON_BLUE);
        double alpha = getDataAsDouble(jsonColor, JSON_ALPHA);
        Color loadedColor = new Color(red, green, blue, alpha);
        return loadedColor;
    }

    private ExtendedImage loadImage(JsonObject json, String imageField) {
        JsonObject jsonImagePath = json.getJsonObject(imageField);
        // Get the path from json
        String imagePath = getDataAsString(jsonImagePath, JSON_BG_IMG_PATH);

        // First check if the file at the path exists
        File img = new File(imagePath);
        if (!img.exists())
            return null; // The image no longer exists

        // Construct a new extended image object since file exists
        try {
            BufferedImage bufferedImage = ImageIO.read(img);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null); // Extract the image
            ExtendedImage extendedImage = new ExtendedImage(image, imagePath);

            return extendedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Something went wrong loading the image
    }

    private Shape loadElement(JsonObject jsonShape, MetroData data) {
        // First construct the correct shape types
        String type = jsonShape.getString(JSON_TYPE);
        Shape shape = null;
        ObservableList<Node> shapes = data.getShapes();

        // Load the file properties
        Color fillColor = loadColor(jsonShape, JSON_FILL_COLOR);
        double outlineThickness = getDataAsDouble(jsonShape, JSON_STROKE_WIDTH);

        // AND THEN ITS DRAGGABLE PROPERTIES
        double x = getDataAsDouble(jsonShape, JSON_X);
        double y = getDataAsDouble(jsonShape, JSON_Y);
        double width = getDataAsDouble(jsonShape, JSON_WIDTH);
        double height = getDataAsDouble(jsonShape, JSON_HEIGHT);

        switch (type) {
            case Draggable.LINE:
                String lineName = getDataAsString(jsonShape, JSON_LINE_NAME);

                // Load line bounds
                double lineStartX = getDataAsDouble(jsonShape,
                        JSON_LINE_START_X);
                double lineStartY = getDataAsDouble(jsonShape,
                        JSON_LINE_START_Y);
                double lineEndX = getDataAsDouble(jsonShape, JSON_LINE_END_X);
                double lineEndY = getDataAsDouble(jsonShape, JSON_LINE_END_Y);
                boolean isLineCircular = (getDataAsString(jsonShape,
                        JSON_CIRCULAR).equals("true"));

                // The constructor will automatically add the line, labels and
                // line ends to the canvas
                MetroLine line = new MetroLine(lineName, shapes,
                        app, new Point2D(lineStartX, lineStartY), new Point2D
                        (lineEndX, lineEndY), isLineCircular); // Color set later
                line.setLineColor(fillColor, false);
                line.getPath().setStrokeWidth(outlineThickness);
                line.setCircular(isLineCircular); // The line is circular

                // Load line stations list
                JsonArray stationsArray = jsonShape.getJsonArray
                        (JSON_LINE_STATIONS_ARRAY);
                // For loop to skip the first and last entries (both line ends)
                for (int i = 0; i < stationsArray.size(); i++) {
                    String stationName = ((JsonString) stationsArray.get(i))
                            .getString();
                    if (!line.getLineStationStrings().contains(stationName))
                        line.getLineStationStrings().add(stationName);
                }

                // Add the line to the data manager
                data.getMapLines().put(lineName, line);
                break;
            case Draggable.STATION:
                // Add the stations to map and then use station list in line
                // to add stations in order
                String stationName = getDataAsString(jsonShape,
                        JSON_STATION_NAME);

                // Get the previous and next stations
                JsonArray prevStations = jsonShape.getJsonArray
                        (JSON_STATION_PREV);
                JsonArray nextStations = jsonShape.getJsonArray
                        (JSON_STATION_NEXT);
                MetroStation station = new MetroStation(stationName, shapes,
                        true, app);

                // Get the associated lines
                JsonArray associatedLines = jsonShape.getJsonArray
                        (JSON_STATION_LINES_ARRAY);

                // Get the location of the station
                double stationX = getDataAsDouble(jsonShape, JSON_X);
                double stationY = getDataAsDouble(jsonShape, JSON_Y);
                double stationWidth = getDataAsDouble(jsonShape, JSON_WIDTH);
                double stationHeight = getDataAsDouble(jsonShape, JSON_HEIGHT);
                double stationRadius = getDataAsDouble(jsonShape,
                        JSON_STATION_RADIUS);
                Color stationColor = loadColor(jsonShape, JSON_FILL_COLOR);
                Color labelColor = loadColor(jsonShape,
                        JSON_STATION_LABEL_COLOR);
                String fontFamily = getDataAsString(jsonShape, JSON_TEXT_FONT);
                double fontSize = getDataAsDouble(jsonShape, JSON_TEXT_SIZE);
                boolean isBolded = Boolean.parseBoolean(getDataAsString(jsonShape, JSON_TEXT_BOLD));
                boolean isItalicized = Boolean.parseBoolean(getDataAsString(jsonShape, JSON_TEXT_ITALICS));
                station.setLocationAndSize(stationX, stationY, stationWidth,
                        stationHeight);
                station.setFill(stationColor);
                station.getAssociatedLabel().setLocationAndSize(stationX +
                                stationRadius, stationY + stationRadius); // Update station label location
                station.getAssociatedLabel().setFill(labelColor); // Set
                // label color
                station.getAssociatedLabel().setFont(Font.font(fontFamily,
                        fontSize));
                station.getAssociatedLabel().setBolded(isBolded);
                station.getAssociatedLabel().setItalicized(isItalicized);
                station.setRadiusX(stationRadius);
                station.setRadiusY(stationRadius);

                // Set the label location and orientation
                station.getAssociatedLabel().setLocation(MetroLabelLocation
                        .valueOf(getDataAsString(jsonShape, JSON_LABEL_LOCATION)));
                station.getAssociatedLabel().setRotation(MetroLabelOrientation
                        .valueOf(getDataAsString(jsonShape, JSON_LABEL_ORIENTATION)));

                // Add the neighboring stations TODO: May not be needed
                for (JsonValue prev: prevStations) {
                    station.getPrevStations().add(((JsonString) prev)
                            .getString());
                }
                for (JsonValue next: nextStations) {
                    station.getNextStations().add(((JsonString) next)
                            .getString());
                }

                // Add the associated lines
                for (JsonValue lines: associatedLines) {
                    station.getAssociatedLinesStrings().add(((JsonString) lines)
                            .getString());
                }

                data.getMapStations().put(stationName, station); //
                // Add the stations later after the lines have been constructed
                break;
            case Draggable.IMAGE:
                String imgPath = getDataAsString(jsonShape,
                        JSON_IMG_OVERLAY_PATH);
                File file = new File(imgPath);
                try {
                    BufferedImage bufferedImage = ImageIO.read(file);
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null); // Extract the image
                    ExtendedImage imageEx = new ExtendedImage(image, imgPath);
                    shape = new DraggableImage(imageEx);
                } catch (IOException e) {

                }
                break;
            case Draggable.TEXT:
                // First get the text from the text field
                String labelText = getDataAsString(jsonShape, JSON_TEXT);

                DraggableText newTextView = new DraggableText(labelText, app);

                // Load the rest of the text properties: family, size, bold, italics
                fontFamily = getDataAsString(jsonShape, JSON_TEXT_FONT);
                fontSize = getDataAsDouble(jsonShape, JSON_TEXT_SIZE);
                isBolded = Boolean.parseBoolean(getDataAsString(jsonShape, JSON_TEXT_BOLD));
                isItalicized = Boolean.parseBoolean(getDataAsString(jsonShape, JSON_TEXT_ITALICS));

                newTextView.setFont(Font.font(fontFamily, fontSize));
                newTextView.setBolded(isBolded);
                newTextView.setItalicized(isItalicized);

                shape = newTextView;
                break;
        }

        // If the shape is null here, it means we do not need to add the element
        if (shape == null)
            return null;

        // Set the fill color if it is not the image view
        if (!(shape instanceof DraggableImage) && !(shape instanceof LinePath))
            shape.setFill(fillColor);

        if (!(shape instanceof MetroStation))
            shape.setStrokeWidth(outlineThickness);

        Draggable draggableShape = (Draggable)shape;
        draggableShape.setLocationAndSize(x, y, width, height);

        return shape;
    }
}
