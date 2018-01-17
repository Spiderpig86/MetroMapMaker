package mmm.controller;

import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.shape.PathElement;
import mmm.controls.*;
import mmm.controls.LinePath;
import mmm.data.MetroData;
import mmm.data.MetroState;
import mmm.data.WeightedLineTo;
import mmm.gui.MetroWorkspace;
import mmm.transactions.element.ElementMoveTransaction;
import mmm.transactions.metrostation.AddStationToLineTransaction;
import mmm.transactions.metrostation.RemoveStationFromLineTransaction;

import static mmm.data.MetroState.*;

public class CanvasController {

    AppTemplate app;

    // Part 9: Undo/redo temp storage lists
    Point2D oldLocation;
    Point2D newLocation;
    ObservableList<Node> oldShapes;
    ObservableList<Node> newShapes;

    public CanvasController(AppTemplate initApp) {
        app = initApp;
    }

    /**
     * Respond to mouse presses on the rendering surface, which we call canvas,
     * but is actually a Pane.
     */
    public void processCanvasMousePress(int x, int y) {
        MetroData dataManager = (MetroData) app.getDataComponent();
        oldShapes = dataManager.getShapes(); // Important
        if (dataManager.isInState(SELECTING_ELEMENT)) {
            // SELECT THE TOP SHAPE
            Node shape = dataManager.selectTopShape(x, y);
            Scene scene = app.getGUI().getPrimaryScene();

            // AND START DRAGGING IT
            if (shape != null) {
                scene.setCursor(Cursor.MOVE);
                dataManager.setState(MetroState.DRAGGING_ELEMENT);

                if (shape instanceof Draggable) {
                    ((Draggable) shape).start(x, y); // Part 7: IMPORTANT TO UPDATE THE NEW START VALUE TO AVOID JUMPING. FIXES MOVING EXISTING SHAPES AND MOVING NEWLY CREATED ONES
                    ((Draggable) shape).drag(x, y);
                }

                // Also exclude any metro station label
//                if (!(dataManager.getSelectedShape() instanceof
//                        MetroStationLabel))
                oldLocation = new Point2D(shape.getLayoutBounds().getMinX(), shape.getLayoutBounds().getMinY());

            }

            // Part 6: If the selected shape is a textview, update the edit label row
            if (shape instanceof DraggableText && !(shape instanceof
                    MetroStationLabel)) {
                DraggableText label = (DraggableText) shape;
                ((MetroWorkspace) app.getWorkspaceComponent()).updateFontToolbar
                        (label);
            } else if (shape instanceof LinePath) {
                // Update the selected line
                MetroWorkspace workspace = (MetroWorkspace) app
                        .getWorkspaceComponent();
                LinePath linePath = (LinePath) shape;
                workspace.updateLineEditToolbar(linePath.getAssociatedLine());
            } else if (shape instanceof MetroStationLabel) {
                // Check if the label is associated with a line end
                MetroStationLabel label = (MetroStationLabel) shape;
                ((MetroWorkspace) app.getWorkspaceComponent()).updateFontToolbar
                        (label);

                if (label.getShapeType().equals(Draggable.LINE_END_LABEL)) {
                    // Update the selected line
                    MetroWorkspace workspace = (MetroWorkspace) app
                            .getWorkspaceComponent();
                    workspace.updateLineEditToolbar(label.getAssociatedStation()
                            .getAssociatedLines().get(0));

                    // Hide the label and show the line end
                    label.setSelected(false);
                }

            } else if (shape instanceof MetroStation && !(shape instanceof
                    MetroLineEnd)) {
                // Update the selected line
                MetroWorkspace workspace = (MetroWorkspace) app
                        .getWorkspaceComponent();
                MetroStation station = (MetroStation) shape;

                // Update the selected line if station is only associated with
                // one line
                if (station.getAssociatedLines().size() == 1)
                    workspace.updateLineEditToolbar(station
                            .getAssociatedLines().get(0));

                // Update the selected station
                // Ignore the line ends
                workspace.updateStationEditToolbar(station);
                workspace.updateFontToolbar(station.getAssociatedLabel());
//                System.out.println(station.getPrev().toString());
//                System.out.println(station.getNext().toString());
            }
        } else if (dataManager.getState() != ADDING_STATIONS_TO_LINE &&
                dataManager.getState() != REMOVING_STATIONS_FROM_LINE) {
            Scene scene = app.getGUI().getPrimaryScene();
            scene.setCursor(Cursor.DEFAULT);
            dataManager.setState(DRAGGING_NOTHING);
            app.getWorkspaceComponent().reloadWorkspace(dataManager);
        }

        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        workspace.reloadWorkspace(dataManager);
    }

    /**
     * Respond to mouse dragging on the rendering surface, which we call canvas,
     * but is actually a Pane.
     */
    public void processCanvasMouseDragged(int x, int y) {
        MetroData dataManager = (MetroData) app.getDataComponent();

        // Also exclude any metro station label
        if (dataManager.getSelectedShape() instanceof MetroStationLabel)
            return;

        if (dataManager.isInState(DRAGGING_ELEMENT) && dataManager.getSelectedShape() instanceof Draggable) {
            Draggable selectedDraggableShape = (Draggable) dataManager.getSelectedShape();
            selectedDraggableShape.drag(x, y);

        }
    }

    /**
     * Respond to mouse button release on the rendering surface, which we call canvas,
     * but is actually a Pane.
     */
    public void processCanvasMouseRelease(int x, int y) {
        MetroData dataManager = (MetroData) app.getDataComponent();

        if (dataManager.isInState(MetroState.DRAGGING_ELEMENT)) {
            dataManager.setState(SELECTING_ELEMENT);
            Scene scene = app.getGUI().getPrimaryScene();
            scene.setCursor(Cursor.DEFAULT);

            // If the object is not draggable, exit
            if (!(dataManager.getSelectedShape() instanceof Draggable))
                return;

            // Prevent these elements from making the move transaction
            if (dataManager.getSelectedShape() instanceof MetroStationLabel
                    || dataManager.getSelectedShape() instanceof LinePath)
                return;

            // Build our transaction - mouse release after moving shapes
            Draggable shape = (Draggable) dataManager.getSelectedShape();
            newLocation = new Point2D(shape.getX(), shape.getY());

            // Check if the shape actually moved before adding in a transaction
            if ((oldLocation.getX() != newLocation.getX() || oldLocation.getY() != newLocation.getY())) {

                if (shape instanceof DraggableText) {
                    DraggableText text = (DraggableText) shape;
                    oldLocation = new Point2D(oldLocation.getX(),
                            oldLocation.getY() + text.getFont().getSize() * 0.9); // Fix for text jumping up to offset location
                }

                if (Math.abs(oldLocation.getX() - newLocation.getX()) >= 2 || Math.abs(oldLocation.getY() - newLocation.getY()) >= 2) { // Helps to filter out random movements for text

                    // Build the transaction
                    ElementMoveTransaction transaction = new ElementMoveTransaction(shape, oldLocation, newLocation);

                    app.getTPS().addTransaction(transaction);
                    app.getGUI().getFileController().markAsEdited(app.getGUI());
                    app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
                }

            }

            // Recalculate the edge weights after moving a station
//            if (shape instanceof MetroStation) {
//                MetroStation station = (MetroStation) shape;
//
//                // Update weights
//                for (PathElement element: station.getPrevSegments().values()) {
//                    if (element instanceof WeightedLineTo) {
//                        WeightedLineTo edge = (WeightedLineTo) element;
//                        edge.refreshWeight();
//                        System.out.println("Prev: " + edge.getSource().toString() +
//                                " to " + edge.getDestination().toString() +
//                                " " + edge.getWeight());
//                    }
//                }
//                for (PathElement element: station.getNextSegments().values()) {
//                    if (element instanceof WeightedLineTo) {
//                        WeightedLineTo edge = (WeightedLineTo) element;
//                        edge.refreshWeight();
//                        System.out.println("Next: " + edge.getSource().toString() +
//                                " to " + edge.getDestination().toString() +
//                                " " + edge.getWeight());
//                    }
//                }
//            }

            // Select the shape again (reloading the canvas unselects all things)
            // Avoid selecting the label again after user selects the line end
            if (!(shape instanceof MetroLineEnd))
                dataManager.selectTopShape(x, y);

        } else if (dataManager.isInState(MetroState.DRAGGING_NOTHING)) {
            dataManager.setState(SELECTING_ELEMENT);
        } else if (dataManager.isInState(MetroState.ADDING_STATIONS_TO_LINE)) {
            // Check what state it is in if it is selecting a station
            if (dataManager.getTopShape(x, y) instanceof MetroStation) {
                MetroStation station = (MetroStation) dataManager
                        .getTopShape(x, y);

                // Check if it is a line end
                if (dataManager.getTopShape(x, y) instanceof MetroLineEnd) {
                    AppDialogs.showMessageDialog(app.getStage(), "Error",
                            "Selected object is not a station. Please try " +
                                    "again.");
                }

                MetroLine line = ((MetroWorkspace) app
                        .getWorkspaceComponent()).getLineCombo().getValue();

                // Check if user did not select a line yet
                if (line == null) {
                    AppDialogs.showMessageDialog(app.getStage(), "Error",
                            "Please select a line first.");
                    return;
                }

                // Create the transaction
                AddStationToLineTransaction transaction = new
                        AddStationToLineTransaction(station, line);
                app.getTPS().addTransaction(transaction);
                app.getGUI().getFileController().markAsEdited(app.getGUI());
            } else {
                Scene scene = app.getGUI().getPrimaryScene();
                scene.setCursor(Cursor.DEFAULT);
                dataManager.setState(SELECTING_ELEMENT);
                app.getWorkspaceComponent().reloadWorkspace(dataManager);
            }
        } else if (dataManager.isInState(MetroState
                .REMOVING_STATIONS_FROM_LINE)) {
            if (dataManager.getTopShape(x, y) instanceof MetroStation && !
                    (dataManager.getTopShape(x, y) instanceof MetroLineEnd)) {
                MetroStation station = (MetroStation) dataManager
                        .getTopShape(x, y);
                MetroLine line = ((MetroWorkspace) app
                        .getWorkspaceComponent()).getLineCombo().getValue();

                // Check if user did not select a line yet
                if (line == null)
                    return;

                RemoveStationFromLineTransaction transaction = new
                        RemoveStationFromLineTransaction(station, line);
                app.getTPS().addTransaction(transaction);
                app.getGUI().getFileController().markAsEdited(app.getGUI());
            } else {
                Scene scene = app.getGUI().getPrimaryScene();
                scene.setCursor(Cursor.DEFAULT);
                dataManager.setState(SELECTING_ELEMENT);
                app.getWorkspaceComponent().reloadWorkspace(dataManager);
            }
        } else {
            Scene scene = app.getGUI().getPrimaryScene();
            scene.setCursor(Cursor.DEFAULT);
            dataManager.setState(DRAGGING_NOTHING);
            app.getWorkspaceComponent().reloadWorkspace(dataManager);
        }
    }
}
