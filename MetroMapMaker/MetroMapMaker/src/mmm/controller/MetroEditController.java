package mmm.controller;

import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import mmm.controls.*;
import mmm.data.*;
import mmm.gui.*;
import mmm.transactions.ColorFillTransaction;
import mmm.transactions.background.BackgroundImageTransaction;
import mmm.transactions.TextFontTransaction;
import mmm.transactions.background.BackgroundSizeTransaction;
import mmm.transactions.element.ElementMoveTransaction;
import mmm.transactions.metroline.NewLineTransaction;
import mmm.transactions.metroline.RemoveLineTransaction;
import mmm.transactions.metrostation.NewStationTransaction;
import mmm.transactions.metrostation.RemoveStationTransaction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MetroEditController {

    public static final int GRID_OFFSET = 20;
    private AppTemplate app;
    private MetroData dataManager;
    private ArrayList<Line> gridLines;
    private ObservableList<MetroLine> metroLines;
    private ObservableList<StationReference> metroStations;
    private boolean gridShown;
    private double scaleFactor = 1; // Keep track of how much it is scaled

    public MetroEditController(AppTemplate app) {
        this.app = app;
        dataManager = (MetroData) app.getDataComponent();
    }

    /* LINE METHODS */
    public void processUpdateLineColor(Color c) {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        MetroLine line = workspace.getLineCombo().getValue();
        if (line == null)
            return;

        // Update the line color
        line.setLineColor(c, true);
    }

    public void processNewLine() {
        // Initializing the list for the first time
        if (metroLines == null) {
            MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
            this.metroLines = workspace.getLineCombo().getItems();
        }

        LineConfig config = NewLineDialog.showDialog(app); // Show dialog
        if (config == null) // User did not enter valid info
            return;

        // Create the new line
        MetroLine line = new MetroLine(config.getName(), config.getColor(),
                dataManager.getShapes(), app, false);
        Rectangle clip = ((MetroWorkspace) app.getWorkspaceComponent())
                .cloneCanvasClip();
        line.getPath().setClip(clip); // Part 13: Set shape clipping
        NewLineTransaction transaction = new NewLineTransaction(line,
                metroLines, dataManager.getShapes(), app);
        app.getTPS().addTransaction(transaction);
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        app.getGUI().getFileController().markAsEdited(app.getGUI());
    }

    public void processDeleteLine() {
        // Get the selected line from the combobox
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        MetroLine line = workspace.getLineCombo().getValue();

        if (line == null) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Please " +
                    "select a line from the dropdown menu on the left.");
            return;
        }

        // Verify deletion
        ButtonType response = AppDialogs.showYesNoCancelDialog(app.getStage(),
                "Delete Line " +
                        "Confirmation", "Are you sure you want to delete the " +
                        "line?");

        if (response == ButtonType.YES) {
            // Remove the line
            for (StationReference station: line.getLineStations()) {
                station.getStation().removeLine(line); // Disassociate line from
                // station
                // TODO: Delete stations that are only tied to this line?
            }
            // Delete the line ends
//            line.getLineStart().removeLabel();
//            line.getLineEnd().removeLabel();
//            dataManager.removeElement(line.getLineStart());
//            dataManager.removeElement(line.getLineEnd());
//            dataManager.removeElement(line.getPath()); // Delete path from GUI
//            workspace.getLineCombo().getItems().remove(line); // Remove the
//            // line from the observable list
            RemoveLineTransaction transaction = new RemoveLineTransaction
                    (line, metroLines, dataManager.getShapes(), app);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        }
    }

    public void processAddStationsToLine() {
        // CHANGE THE CURSOR
        Scene scene = app.getGUI().getPrimaryScene();
        scene.setCursor(Cursor.CROSSHAIR);

        // CHANGE THE STATE
        dataManager.setState(MetroState.ADDING_STATIONS_TO_LINE);

        // ENABLE/DISABLE THE PROPER BUTTONS
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        workspace.reloadWorkspace(dataManager);
    }

    public void processRemoveStationsFromLine() {
        // CHANGE THE CURSOR
        Scene scene = app.getGUI().getPrimaryScene();
        scene.setCursor(Cursor.CROSSHAIR);

        // CHANGE THE STATE
        dataManager.setState(MetroState.REMOVING_STATIONS_FROM_LINE);

        // ENABLE/DISABLE THE PROPER BUTTONS
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        workspace.reloadWorkspace(dataManager);
    }

    public void processShowLineStations() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        MetroLine line = workspace.getLineCombo().getValue();

        if (line == null) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Please " +
                    "select a line from the dropdown menu on the left.");
            return;
        }

        LineStationsDialog.showDialog(app, line);
    }

    public void processEditLine() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (workspace.getLineCombo().getValue() == null) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Please " +
                    "select a line from the dropdown on the left.");
            return;
        }

        LineConfig config = EditLineDialog.showDialog(app, workspace
                .getLineCombo().getValue()); // Show dialog
        if (config == null) //
            // User did not enter valid info
            return;

        // Update the line given config data
        MetroLine line = workspace.getLineCombo().getValue();
        workspace.getLineCombo().getItems().remove(line);

        // If the config is not the same as the line props, mark as not saved
        if (!config.getColor().equals(line.getLineColor()) || !config.getName
                ().equals(line.getLineName()))
            app.getGUI().getFileController().markAsEdited(app.getGUI());

        line.setLineColor(config.getColor(), true); // Update line color
        line.setLineName(config.getName()); // Update line name
        workspace.getLineCombo().getItems().add(line);

    }

    public void processLineSlider() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        MetroLine line = workspace.getLineCombo().getValue();

        if (line == null)
            return;
        line.getPath().setStrokeWidth(workspace.getLineEditSlider().getValue());
        app.getGUI().getFileController().markAsEdited(app.getGUI());
    }

    /* STATION METHODS */
    public void processUpdateStationColor(Color c) {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        MetroStation station = workspace.getStationCombo().getValue().getStation();
        if (station == null)
            return;

        // Update the station color
        ColorFillTransaction transaction = new ColorFillTransaction
                (station, (Color) station.getFill(), c,
                        app);
        app.getTPS().addTransaction(transaction);
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        app.getGUI().updateToolbarControls(false);
    }

    public void processNewStation() {
        // Adding a station for the first
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (metroStations == null) {
            metroStations = workspace.getStationCombo().getItems();
        }

        // Create input dialog to add station
        String name = AppDialogs.showTextInputDialog(app.getStage(), "Enter " +
                        "Station Name","Please enter the name of the new station.");

        // Return if user cancelled
        if (name.equals("(cancelled)"))
            return;

        if (name.length() > 0 && !containsStation(name, workspace)) {
            MetroStation station = new MetroStation(name, dataManager
                    .getShapes(), true, app);
            //dataManager.addElement(station);
            NewStationTransaction transaction = new NewStationTransaction
                    (station, dataManager.getShapes());
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            metroStations.add(station.getStationReference());
            app.getGUI().getFileController().markAsEdited(app.getGUI());
        } else
            AppDialogs.showMessageDialog(app.getStage(), "Invalid Station " +
                    "Name", "Please try again with a valid " +
                    "non-duplicate station name.");
    }

    public void processDeleteStation() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (workspace.getStationCombo().getValue() == null) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Please " +
                    "select a station first from the dropdown list on the " +
                    "left.");
            return;
        }

        if (!(dataManager.getSelectedShape() instanceof MetroStation) &&
                workspace.getStationCombo().getValue() == null)
            return;

        if (metroStations == null)
            metroStations = workspace.getStationCombo().getItems();

        ButtonType response = AppDialogs.showYesNoCancelDialog(app.getStage(),
                "Delete Station " +
                "Confirmation", "Are you sure you want to delete the " +
                "station?");

        if (response == ButtonType.YES) {
            // Remove the element from canvas
            MetroStation station = workspace.getStationCombo().getValue()
                    .getStation();
            metroStations.remove(station.getStationReference());

            RemoveStationTransaction transaction = new
                    RemoveStationTransaction(station, dataManager.getShapes()
                    , metroStations);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            app.getGUI().getFileController().markAsEdited(app.getGUI());
        }
    }

    public void processSnapStation() {
        // Snap the station to the nearest grid line demarcations
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (workspace.getStationCombo().getValue() != null) {
            MetroStation station = workspace.getStationCombo().getValue().getStation();
            double newX = Math.round(station.getX() / GRID_OFFSET) * GRID_OFFSET;
            double newY = Math.round(station.getY() / GRID_OFFSET) * GRID_OFFSET;
//            station.getAssociatedLabel().setLocationAndSize(newX, newY); //
//            // Update the station label location
            ElementMoveTransaction transaction = new ElementMoveTransaction
                    (station, station.getPoint(), new Point2D(newX - station
                            .getRadiusX(), newY - station.getRadiusY()));
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            app.getGUI().getFileController().markAsEdited(app.getGUI());
        } else
            AppDialogs.showMessageDialog(app.getStage(), "Select a Station",
                    "Please select a station first.");
    }

    public void processCycleStationLabel() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (workspace.getStationCombo().getValue() != null) {
            MetroStation station = workspace.getStationCombo().getValue().getStation();
            station.getAssociatedLabel().cycleLocation();
        } else
            AppDialogs.showMessageDialog(app.getStage(), "Select a Station",
                    "Please select a station first.");
    }

    public void processRotateStationLabel() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (workspace.getStationCombo().getValue() != null) {
            MetroStation station = workspace.getStationCombo().getValue().getStation();
            station.getAssociatedLabel().cycleRotation();
        } else
            AppDialogs.showMessageDialog(app.getStage(), "Select a Station",
                    "Please select a station first.");
    }

    public void processStationSlider() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        StationReference station = workspace.getStationCombo().getValue();

        if (station == null)
            return;
        MetroStation metroStation = station.getStation();
        metroStation.setRadiusX(workspace.getStationEditSlider().getValue());
        metroStation.setRadiusY(workspace.getStationEditSlider().getValue());
    }

    /* ROUTE FINDER */
    public void processFindRoute() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        if (workspace.getFindOriginCombo().getValue() == null || workspace
                .getFindDestCombo().getValue() == null) {
            AppDialogs.showMessageDialog(app.getStage(), "Select Stations",
                    "Please select a source and destination station first.");
            return;
        }

        RouteDialog.showDialog(app, workspace.getStationCombo().getItems(),
                workspace.getFindOriginCombo().getValue(), workspace
                        .getFindDestCombo().getValue());
    }

    /* DECORATION METHODS */

    public void processSelectBackgroundColor() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        Color selectedColor = workspace.getBackgroundColorPicker().getValue();
        if (selectedColor != null) {
            dataManager.setBackgroundColor(selectedColor, true);
            app.getGUI().getFileController().markAsEdited(app.getGUI());
        }

        if (workspace.getShowGridChecked().selectedProperty().getValue())
            constructGridLines(workspace);
    }

    public void processSelectBackgroundImage() {
        // Show dialog asking if a background image wants to be used
        ButtonType type = AppDialogs.showYesNoCancelDialog(app.getStage(), "Use Background " +
                "Image", "Would you like to use a background image?");

        // If user cancels, just exit the method
        if (type == ButtonType.CANCEL)
            return;

        // Clear the background image
        if (type == ButtonType.NO) {
            if (dataManager.getBackgroundImage() != null) {
                // Add the transaction only when there was a previous
                // background image
                BackgroundImageTransaction transaction = new
                        BackgroundImageTransaction(dataManager.getBackgroundImage
                        (), null, app);
                app.getTPS().addTransaction(transaction);
                app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            }
            return;
        }

        // Create out file chooser
        FileChooser fileChooser = new FileChooser();
        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file == null) return; // If file is invalid or user did not select something, return

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null); // Extract the image
            // Create abstract image object
            ExtendedImage imageEx = new ExtendedImage(image, file.getAbsolutePath());
            //dataManager.setBackgroundImage(imageEx);
            BackgroundImageTransaction transaction = new
                    BackgroundImageTransaction(dataManager.getBackgroundImage
                    (), imageEx, app);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processInsertImage() {
        // Create out file chooser
        FileChooser fileChooser = new FileChooser();

        // Set extension filter

//        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
//        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
//        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.BMP");
//        FileChooser.ExtensionFilter extFilterGIF = new FileChooser.ExtensionFilter("GIF files (*.gif)", "*.GIF");
//        //FileChooser.ExtensionFilter extFilterAll = new FileChooser.ExtensionFilter("All files", "*.*");
//        fileChooser.getExtensionFilters().addAll(extFilterPNG, extFilterJPG, extFilterBMP, extFilterGIF);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file == null) return; // If file is invalid or user did not select something, return

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null); // Extract the image
            // Create abstract image object
            ExtendedImage imageEx = new ExtendedImage(image, file.getAbsolutePath());
            dataManager.initNewImage(imageEx);

            // CHANGE THE STATE
            dataManager.setState(MetroState.DRAGGING_NOTHING);
        } catch (IOException ex) {

        }
    }

    /**
     * Method to handle user entering text to enter
     */
    public void processInsertText() {
        // Create a new dialog for user to enter the text
        String text = AppDialogs.showTextInputDialog(app.getStage(), "Insert " +
                "Label", "Enter label text");
        if (text.length() > 0  && !text.equals("(cancelled)")) {
            // Now to insert the label to the canvas
            dataManager.initNewLabel(text);
            dataManager.setState(MetroState.DRAGGING_NOTHING);
        }
    }


    public void processRemoveSelectedElement() {
        if (dataManager.getSelectedShape() instanceof MetroStation ||
                dataManager.getSelectedShape() instanceof LinePath ||
                dataManager.getSelectedShape() instanceof MetroStationLabel)
            return;

        dataManager.removeSelectedElement();

        // ENABLE/DISABLE THE PROPER BUTTONS
        MetroWorkspace workspace = (MetroWorkspace)app.getWorkspaceComponent();
        workspace.reloadWorkspace(dataManager);
        app.getGUI().updateToolbarControls(false);
    }

    /* FONT METHODS */
    public void processUpdateFontColor() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        Color selectedColor = workspace.getTextColorPicker().getValue();
        if (selectedColor != null) {
            Shape shape = dataManager.getSelectedShape();
            // If the station was selected instead, update the shape to the
            // label
            if (dataManager.getSelectedShape() instanceof MetroStation) {
                MetroStation station = (MetroStation) dataManager
                        .getSelectedShape();
                shape = station.getAssociatedLabel();
            }

            // Exit if the selected shape is not a draggable text
            if (!(shape instanceof DraggableText))
                return;

            // Set color of the object
            ColorFillTransaction transaction = new ColorFillTransaction
                    (shape, (Color) dataManager
                            .getSelectedShape().getFill(), selectedColor, app);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        }
    }

    public void processUpdateLabelFont(String oldValue, String newValue) {
        MetroData dataManager = (MetroData) app.getDataComponent();

        if (dataManager.getSelectedShape() instanceof DraggableText ||
                dataManager.getSelectedShape() instanceof MetroStation) {
            DraggableText label;
            if (dataManager.getSelectedShape() instanceof MetroStation)
                label = ((MetroStation) dataManager.getSelectedShape())
                        .getAssociatedLabel();
            else
                label = (DraggableText) dataManager.getSelectedShape();

            // Get the original font for the transaction
            Font oldFont = label.getFont();
            Font newFont = new Font(newValue, label.getFont().getSize());

            // Only add transaction with change
            if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                // Create the transaction and add it
                TextFontTransaction transaction = new TextFontTransaction(label, oldFont, newFont);
                app.getTPS().addTransaction(transaction);
                app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            }

        }
    }

    public void processUpdateLabelFontsize(String oldValue, String newValue) {
        MetroData dataManager = (MetroData) app.getDataComponent();
        if (dataManager.getSelectedShape() instanceof DraggableText ||
                dataManager.getSelectedShape() instanceof MetroStation) {

            DraggableText label;
            // Add case when user selects a station
            if (dataManager.getSelectedShape() instanceof MetroStation)
                label = ((MetroStation) dataManager.getSelectedShape())
                        .getAssociatedLabel();
            else
                label = (DraggableText) dataManager.getSelectedShape();

            // Get the original font for the transaction
            Font oldFont = label.getFont();
            Font newFont = new Font(label.getFont().getFamily(), Integer
                    .parseInt(newValue.trim()));

            label.setFont(newFont);
            // Check if it is bolded or not
            if (label.isBolded()) label.setBolded(true); // Bypasses bug where boldness resets on font change
            if (label.isItalicized()) label.setItalicized(true);

//            // Check if the old value and new values are the same and update accordingly - IMPORTANT OR ELSE IT WILL ADD TOO MANY TRANSACTIONS
            if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                // Create the transaction and add it
                TextFontTransaction transaction = new TextFontTransaction(label, oldFont, newFont);
                app.getTPS().addTransaction(transaction);
                app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            }
        }
    }

    public void processBoldFont() {
        MetroData dataManager = (MetroData) app.getDataComponent();
        if (dataManager.getSelectedShape() instanceof DraggableText) {
            DraggableText label = (DraggableText) dataManager.getSelectedShape();
            // Get the original font for the transaction
            Font oldFont = label.getFont();
            label.setBolded(!label.isBolded());
            if (label.isItalicized()) label.setItalicized(true);

            Font newFont = label.getFont();
            // Create the transaction and add it
            TextFontTransaction transaction = new TextFontTransaction(label, oldFont, newFont);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        }
    }

    public void processItalicizeFont() {
        MetroData dataManager = (MetroData) app.getDataComponent();
        if (dataManager.getSelectedShape() instanceof DraggableText) {
            DraggableText label = (DraggableText) dataManager.getSelectedShape();
            // Get the original font for the transaction
            Font oldFont = label.getFont();
            label.setItalicized(!label.isItalicized());
            if (label.isBolded()) label.setBolded(true);

            Font newFont = label.getFont();

            // Create the transaction and add it
            TextFontTransaction transaction = new TextFontTransaction(label, oldFont, newFont);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        }
    }

    /* NAVIGATION METHODS */

    public void toggleGrid() {
        // Add a bunch of line objects into the pane using an arraylist
        // If we do not need it, we can just remove the whole arraylist from
        // the canvas.

        MetroWorkspace workspace = (MetroWorkspace) app
                .getWorkspaceComponent();
        if (!gridShown) {
            constructGridLines(workspace);
            gridShown = true;
        } else {
            if (gridLines == null)
                return;
            workspace.getCanvas().getChildren().removeAll(gridLines);
            gridShown = false;
        }
    }

    public void processZoomIn() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        workspace.getCanvas().setScaleX(workspace.getCanvas()
                .getScaleX() * 1.1);
        workspace.getCanvas().setScaleY(workspace.getCanvas()
                .getScaleY() * 1.1);
        scaleFactor += 0.1;
    }

    public void processZoomOut() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        workspace.getCanvas().setScaleX(workspace.getCanvas()
                .getScaleX() * 0.9);
        workspace.getCanvas().setScaleY(workspace.getCanvas()
                .getScaleY() * 0.9);
        scaleFactor -= 0.1;
    }

    public void processScaleUp() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        Point2D oldSize = new Point2D(workspace.getCanvas().getPrefWidth(), workspace.getCanvas().getPrefHeight());
        Point2D newSize = new Point2D(workspace.getCanvas().getPrefWidth() *
                1.1, workspace.getCanvas().getPrefHeight() *
                1.1);
        BackgroundSizeTransaction transaction = new BackgroundSizeTransaction
                (workspace.getCanvas(), oldSize, newSize, app);
        app.getTPS().addTransaction(transaction);
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        if (workspace.getShowGridChecked().selectedProperty().getValue())
            constructGridLines(workspace);
    }

    public void processScaleDown() {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        double newWidth = workspace.getCanvas().getPrefWidth() * 0.9;
        double newHeight = workspace.getCanvas().getPrefHeight() * 0.9;

        Point2D oldSize = new Point2D(workspace.getCanvas().getPrefWidth(), workspace.getCanvas().getPrefHeight());
        Point2D newSize = new Point2D(newWidth, newHeight);


        // If the size goes below 200x200, show message box TODO disable
        // button too if needed
        if (newWidth < 200 || newHeight < 200) {
            AppDialogs.showMessageDialog(app.getStage(), "Minimum Map Size " +
                            "Reached",
                    "Unable to decrease map size further. Minimum dimensions" +
                            " reached (200 x 200).");
            return;
        }


        BackgroundSizeTransaction transaction = new BackgroundSizeTransaction
                (workspace.getCanvas(), oldSize, newSize, app);
        app.getTPS().addTransaction(transaction);
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
        if (workspace.getShowGridChecked().selectedProperty().getValue())
            constructGridLines(workspace);
    }

    /* HELPER METHODS */
    public void constructGridLines(MetroWorkspace workspace) {
        // Clear the old lines
        if (gridLines != null)
            workspace.getCanvas().getChildren().removeAll(gridLines);

        // Grid lines for canvas
        gridLines = new ArrayList<>();

        // Get the width of the pane and divide it by the spacing to see
        // how many lines we would include
        double canvasWidth = workspace.getCanvas().getWidth();
        double canvasHeight = workspace.getCanvas().getHeight();

        // Add the vertical lines
        Line line;
        Color lineColor = inverseGrayScale(dataManager.getBackgroundColor());
        for (int i = 0; i < canvasWidth; i += GRID_OFFSET) {
            line = new Line(i, 0, i, canvasHeight);
            line.setStroke(lineColor);
            gridLines.add(line);
        }

        // Add the horizontal lines
        for (int i = 0; i < canvasHeight; i += GRID_OFFSET) {
            line = new Line(0, i, canvasWidth, i);
            line.setStroke(lineColor);
            gridLines.add(line);
        }
        workspace.getCanvas().getChildren().addAll(gridLines);
        gridLines.forEach(l -> l.toBack());
    }

    private Color inverseGrayScale(Color c) {
        double average = (c.getRed() + c.getBlue() + c.getGreen()) / 3;
        Color grayScaleInvert = new Color(1.0 - average, 1.0 - average,
                1.0 - average, 0.2);
        return grayScaleInvert;
    }

    private boolean containsStation(String name, MetroWorkspace workspace) {
        for (StationReference station: workspace.getStationCombo().getItems())
            if (station.getStation().toString().equals(name))
                return true;
        return false;
    }

    public void setMetroLines(ObservableList<MetroLine> lines) { this
            .metroLines = lines; }

    public double getScaleFactor() { return scaleFactor; }
}
