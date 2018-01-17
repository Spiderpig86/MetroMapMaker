package mmm.data;

import djf.AppTemplate;
import djf.components.AppDataComponent;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import mmm.controls.*;
import mmm.gui.MetroWorkspace;
import mmm.transactions.background.BackgroundColorTransaction;
import mmm.transactions.element.DeleteTransaction;
import mmm.transactions.element.InsertTransaction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by slim1 on 11/2/2017.
 */
public class MetroData implements AppDataComponent {

    // App properties
    Color backgroundColor; // Canvas background color
    Color stationFillColor; // Color to fill station
    Color lineFillColor; // Color to fill line
    ExtendedImage bgImg; // The background image
    MetroState state; // Current station of the application

    // THESE ARE THE SHAPES TO DRAW
    Group shapesGroup; // Stores the shapes
    ObservableList<Node> shapes; // List of shapes in group
    Map<String, MetroStation> mapStations = new HashMap<>(); // Used to store map of
    // stations when loading a file
    Map<String, MetroLine> mapLines = new HashMap<>();

    // THIS IS THE SHAPE CURRENTLY BEING SIZED BUT NOT YET ADDED
    Shape newShape;

    // THIS IS THE SHAPE CURRENTLY SELECTED
    Shape selectedShape;

    // USE THIS WHEN THE SHAPE IS SELECTED
    Effect highlightedEffect;
    Effect lineEndHighlight;

    public static final String WHITE_HEX = "#FFFFFF";
    public static final String BLACK_HEX = "#000000";
    public static final String YELLOW_HEX = "#EEEE00";
    public static final String BLUE_HEX = "#cfebff";
    public static final Paint DEFAULT_BACKGROUND_COLOR = Paint.valueOf(WHITE_HEX);
    public static final Paint HIGHLIGHTED_COLOR = Paint.valueOf(YELLOW_HEX);
    public static final int HIGHLIGHTED_STROKE_THICKNESS = 3;

    AppTemplate app;

    public MetroData(AppTemplate initApp) {
        app = initApp;
        newShape = null;
        selectedShape = null;

        // INIT THE COLORS
        stationFillColor = Color.web(WHITE_HEX);
        lineFillColor = Color.web(BLACK_HEX);

        // THIS IS FOR THE SELECTED SHAPE
        DropShadow dropShadowEffect = new DropShadow();
        dropShadowEffect.setOffsetX(0.0f);
        dropShadowEffect.setOffsetY(0.0f);
        dropShadowEffect.setSpread(1.0);
        dropShadowEffect.setColor(Color.YELLOW);
        dropShadowEffect.setBlurType(BlurType.GAUSSIAN);
        dropShadowEffect.setRadius(10);
        highlightedEffect = dropShadowEffect;

        // Effect for selecting a line end
        dropShadowEffect = new DropShadow();
        dropShadowEffect.setOffsetX(0.0f);
        dropShadowEffect.setOffsetY(0.0f);
        dropShadowEffect.setSpread(1.0);
        dropShadowEffect.setColor(Color.CYAN);
        dropShadowEffect.setBlurType(BlurType.GAUSSIAN);
        dropShadowEffect.setRadius(10);
        lineEndHighlight = dropShadowEffect;

        // Shapes initialized by workspace
    }

    /**
     * This function would be called when initializing data.
     */
    @Override
    public void resetData() {
        setState(MetroState.SELECTING_ELEMENT);
        newShape = null;
        selectedShape = null;

        MetroWorkspace workspace = ((MetroWorkspace) app
                .getWorkspaceComponent());

        // INIT THE COLORS
        stationFillColor = Color.web(WHITE_HEX);
        lineFillColor = Color.web(BLACK_HEX);

        shapes.clear();
        workspace.getCanvas().getChildren().clear();

        mapLines.clear();
        mapStations.clear();

        // Clear the list of lines and stations in the combobox
        workspace.getLineCombo().getItems().clear();
        workspace.getStationCombo().getItems().clear();

        // Reset clipboard
        //((MetroWorkspace) app.getWorkspaceComponent()).resetClipboard(); TODO
    }

    public Shape selectTopShape(int x, int y) {
        Shape shape = getTopShape(x, y);

        // Check last selected shape
        if (shape == selectedShape) // If the shape we selected and the new shape we selected is the same
            return shape;

        if (selectedShape != null)
            unhighlightShape(selectedShape);

        if (shape != null && shape instanceof Shape && !(shape instanceof
                Line)) {
            highlightShape(shape);
            MetroWorkspace workspace = (MetroWorkspace) app
                    .getWorkspaceComponent();
            //workspace.loadSelectedShapeSettings((Shape) shape); TODO
        }
        selectedShape =  shape;
        if (shape != null && shape instanceof Draggable) {
            ((Draggable) shape).start(x, y);
        }
        return shape;
    }

    public Shape getTopShape(int x, int y) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Node temp = shapes.get(i);
            if (temp.contains(x, y) && temp instanceof Shape) {
                return (Shape) temp;
            }
        }
        return null;
    }

    public void addElement(Shape shape) {
        this.shapes.add(shape);
    }

    public void removeElement(Shape shape) { this.shapes.remove(shape); }

    public void removeSelectedElement() {
        if (selectedShape != null) {
            if (selectedShape instanceof LinePath || selectedShape instanceof
                    MetroStation || selectedShape instanceof MetroStationLabel)
                return;

            // Create the new transaction with information
            DeleteTransaction transaction = new DeleteTransaction(this, selectedShape, shapes);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo
                    ());

            selectedShape = null;
        }
    }

    public void unhighlightShape(Shape shape) {
        selectedShape.setEffect(null);
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
        workspace.clearLabelProperties();

        // If it is a line end, set the label to visible again
        if (shape instanceof MetroLineEnd) {
            MetroLineEnd end = (MetroLineEnd) shape;
            end.setSelected(false); // Hide circle and show label again
        }
    }

    public void highlightShape(Shape shape) {
        if (shape instanceof MetroLineEnd)
            shape.setEffect(lineEndHighlight);
        else
            shape.setEffect(highlightedEffect);

        if (!(shape instanceof DraggableText)) {
            MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();
            workspace.clearLabelProperties();
        }
    }

    public void resetTransactions() {
        app.getTPS().clearAllTransactions();
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(),
                app.getTPS().canRedo());
    }

    public AppTemplate getApp() {
        return app;
    }



    /* Canvas Methods */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public void setBackgroundColor(Color initBackgroundColor, boolean
            addTransaction) {
        Color oldBackground = backgroundColor; // Store old canvas background for transaction

        backgroundColor = initBackgroundColor;
        MetroWorkspace workspace = (MetroWorkspace)app.getWorkspaceComponent();
        workspace.getBackgroundColorPicker().setValue(initBackgroundColor);
        // Update the color picker
        Pane canvas = workspace.getCanvas();
        BackgroundFill fill = new BackgroundFill(backgroundColor, null, null);
        Background background = new Background(fill);

        // Add the transaction
        if (!backgroundColor.equals(oldBackground) && addTransaction) {
            BackgroundColorTransaction transaction = new BackgroundColorTransaction(canvas, oldBackground, initBackgroundColor, app);
            app.getTPS().addTransaction(transaction);
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
            app.getGUI().getFileController().markAsEdited(app.getGUI());
        } else {
            canvas.setBackground(background);
        }
    }

    public void setBackgroundImage(ExtendedImage image) {
        this.bgImg = image;

        // Update the image in the GUI
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();

// new BackgroundSize(width, height, widthAsPercentage, heightAsPercentage, contain, cover)
        //BackgroundSize backgroundSize = new BackgroundSize(100, 100,
                //true, true, true, true);
// new BackgroundImage(image, repeatX, repeatY, position, size)
//        BackgroundImage backgroundImage = new BackgroundImage(image.getImage(),
//                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
//                BackgroundPosition.CENTER, backgroundSize);
// new Background(images...)
        // This will end up stretching over the canvas
        Background background = new Background(new BackgroundFill(new
                ImagePattern(image.getImage()), null, null));
        workspace.getCanvas().setBackground(background);
        app.getGUI().getFileController().markAsEdited(app.getGUI());
    }

    public void clearBackgroundImage() {
        this.bgImg = null;
        setBackgroundColor(backgroundColor, true); // Set the background color
    }

    public ExtendedImage getBackgroundImage() {
        return this.bgImg;
    }

    public void initNewImage(ExtendedImage image) {
        DraggableImage imgControl = new DraggableImage(image);

        // Create the new transaction with information
        InsertTransaction transaction = new InsertTransaction(imgControl, this.shapes);
        app.getTPS().addTransaction(transaction);
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());

        imgControl.setClip(((MetroWorkspace) app.getWorkspaceComponent())
                .cloneCanvasClip()); // Part 13: Set shape clipping
    }

    public void initNewLabel(String lblText) {
        DraggableText textView = new DraggableText(lblText, app);
//        textView.setClip(((MetroWorkspace) app.getWorkspaceComponent())
//                .cloneCanvasClip());
        textView.setFont(new Font("Arial", 24));

        // Update properties for label
        MetroWorkspace workspace = (MetroWorkspace)app.getWorkspaceComponent();
        textView.setFill(workspace.getTextColorPicker().getValue());

        textView.setX(100);
        textView.setY(100);
        textView.start(100, 100);

        // Create the new transaction with information
        InsertTransaction transaction = new InsertTransaction(textView, this.shapes);
        app.getTPS().addTransaction(transaction);
        app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS().canRedo());
    }


    /* HELPER METHODS */
    public ObservableList<Node> getShapes() {
        return shapes;
    }

    public void setShapes(ObservableList<Node> shapes) {
        this.shapes = shapes;
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void setSelectedShape(Shape initSelectedShape) {
        selectedShape = initSelectedShape;
    }

    public MetroState getState() {
        return state;
    }

    public void setState(MetroState initState) {
        state = initState;
    }

    public boolean isInState(MetroState testState) {
        return state == testState;
    }

    public Map<String, MetroStation> getMapStations() {
        return mapStations;
    }

    public Map<String, MetroLine> getMapLines() {
        return mapLines;
    }
}
