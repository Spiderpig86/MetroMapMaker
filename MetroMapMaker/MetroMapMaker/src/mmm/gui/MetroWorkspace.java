package mmm.gui;

import djf.AppTemplate;
import djf.components.AppDataComponent;
import djf.ui.AppGUI;
import djf.components.AppWorkspaceComponent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static djf.language.AppLanguageSettings.*;
import static mmm.css.MetroStyle.*;
import static mmm.MetroLanguageProperty.*;

import mmm.controller.CanvasController;
import mmm.controller.MetroEditController;
import mmm.controls.Draggable;
import mmm.controls.DraggableText;
import mmm.controls.MetroLine;
import mmm.controls.MetroStation;
import mmm.data.MetroData;
import mmm.data.MetroState;
import mmm.data.StationReference;
import mmm.transactions.StrokeWidthTransaction;
import properties_manager.PropertiesManager;

import java.util.ArrayList;


/**
 * Created by slim1 on 11/2/2017.
 */
public class MetroWorkspace extends AppWorkspaceComponent {

    /* General Components */
    AppTemplate app;
    AppGUI gui;
    Pane canvas; // Pane to hold all canvas objects
    ScrollPane scrollPane; // The base pane to hold the canvas
    Text debugText; // For debugging

    /* UI Components */
    VBox editSideBar; // The main sidebar for controls
    ScrollPane editScrollPane;

    /* Metro Line Toolbar */
    VBox lineEditBox;
    // Detail row
    HBox lineEditDetailRow;
    Label lineEditLabel;
    ComboBox<MetroLine> lineEditCombo;
    ColorPicker lineEditColorPicker;
    // Button row
    HBox lineEditButtonRow;
    Button lineEditAddBtn;
    Button lineEditRemoveBtn;
    Button lineEditBtn; // Shows the edit dialog
    Button lineEditAddStationBtn;
    Button lineEditRemoveStationBtn;
    Button lineEditListStationsBtn;
    Slider lineEditSlider;

    /* Metro Station Toolbar */
    VBox stationEditBox;
    // Detail row
    HBox stationEditRow;
    Label stationEditLabel;
    ComboBox<StationReference> stationEditCombo;
    ColorPicker stationEditColorPicker;
    // Button row
    HBox stationEditButtonRow;
    Button stationEditNewBtn;
    Button stationEditRemoveBtn;
    Button stationEditSnapBtn;
    Button stationEditMoveLabelBtn;
    Button stationEditRotateLabelBtn;
    Slider stationEditSlider;

    /* Direction Toolbar */
    HBox findBox;
    VBox findComboCol;
    ComboBox<StationReference> findOriginCombo;
    ComboBox<StationReference> findDestCombo;
    Button findRouteButton;

    /* Decor Toolbar */
    VBox decorBox;
    // Detail row
    HBox decorEditrow;
    Label decorEditLabel;
    ColorPicker decorEditColor;
    // Button row
    HBox decorEditButtonRow;
    Button decorBackgroundBtn;
    Button decorImgBtn;
    Button decorLabelBtn;
    Button decorRemoveBtn;

    /* Font Toolbar */
    VBox fontBox;
    // Detail Row
    HBox fontEditRow;
    Label fontEditLabel;
    ColorPicker fontColor;
    // Button Row
    HBox fontButtonRow;
    Button fontBoldBtn;
    Button fontItalicsBtn;
    ComboBox<String> fontSizeCombo;
    ComboBox<String> fontFamilyCombo;

    /* Navigation Toolbar */
    VBox navigationBox;
    // Detail Row
    HBox navigationEditRow;
    Label navigationEditLabel;
    CheckBox navigationGridCheckBox;
    // Button Row
    HBox navigationButtonRow;
    Button navigationZoomInBtn;
    Button navigationZoomOutBtn;
    Button navigationScaleUpBtn;
    Button navigationScaleDownBtn;

    public static final boolean ENABLED  = true;
    public static final boolean DISABLED = false;

    // Global properties manager
    PropertiesManager props;

    // App controller
    MetroEditController controller;
    // Canvas controller
    CanvasController canvasController;

    // Fix issue with shapes overriding top toolbar using a clipping rectangle
    Rectangle clipper;

    // Save slider value for undoRedo for line width
    double oldLineSliderValue = 0.0;
    double newLineSliderValue = 0.0;

    // Save slider value for undoRedo for station radius
    double oldStationSliderValue = 0.0;
    double newStationSliderValue = 0.0;

    // Used for comboboxes to keep track of what text element was last selected
    DraggableText lastSelectedTextFont;
    DraggableText lastSelectedSizeFont;

    // Group for shapes
    Group shapesGroup = new Group();

    public MetroWorkspace(AppTemplate initApp) {
        // Store reference to the app component
        app = initApp;
        props = PropertiesManager.getPropertiesManager();

        // Get the app GUI to display for the application
        gui = app.getGUI();

        // Create the canvas
        canvas = new Pane();

        // Initialize app components
        initLayout(); // Initialize the UI
        initControllers(); // Initialize the controllers
        initStyle(); // Load styling for the app

//        debugText = new Text("test");
//        canvas.getChildren().add(debugText);
//        debugText.setX(400);
//        debugText.setY(400);
    }

    // Initializations

    /**
     * Initialize layout and controls of the user interface
     */
    public void initLayout() {

        // Create the GUI components
        // Line Edit
        editSideBar = new VBox();
        editScrollPane = new ScrollPane();

        lineEditBox = new VBox();
        lineEditDetailRow = new HBox(10);
        lineEditLabel = new Label(props.getProperty(METRO_LINES_LABEL).toUpperCase());
        lineEditCombo = new ComboBox<>();
        lineEditCombo.setPrefWidth(125);
        lineEditColorPicker = new ColorPicker();
        lineEditDetailRow.getChildren().addAll(lineEditLabel,
                lineEditCombo, lineEditColorPicker); // Add controls to desc row


        lineEditButtonRow = new HBox(10);
        lineEditAddBtn = initButton(lineEditButtonRow,
                METRO_LINES_ADD_TOOLTIP.toString(), ENABLED);
        lineEditRemoveBtn = initButton(lineEditButtonRow,
                METRO_LINES_DELETE_TOOLTIP.toString(), ENABLED);
        lineEditAddStationBtn = initButton(lineEditButtonRow,
                METRO_LINES_ADD_STATION.toString(), ENABLED);
        lineEditRemoveStationBtn = initButton(lineEditButtonRow,
                METRO_LINES_REMOVE_STATION.toString(), ENABLED);
        lineEditListStationsBtn = initButton(lineEditButtonRow,
                METRO_LINES_LIST_STATIONS_TOOLTIP.toString(), ENABLED);
        lineEditBtn = initButton(lineEditButtonRow,
                METRO_LINES_EDIT_TOOLTIP.toString(), ENABLED);
//        lineEditButtonRow.getChildren().addAll(lineEditAddBtn,
//                lineEditRemoveBtn, lineEditAddStationBtn,
//                lineEditRemoveStationBtn, lineEditListStationsBtn); // Add controls to edit row

        lineEditSlider = new Slider(0, 10, 1);

        lineEditBox.getChildren().addAll(lineEditDetailRow,
                lineEditButtonRow, lineEditSlider); // Add controls to main
        // toolbar

        // Station Edit
        stationEditBox = new VBox();

        stationEditRow = new HBox(10);
        stationEditLabel = new Label(props.getProperty(METRO_STATIONS_LABEL).toUpperCase());
        stationEditCombo = new ComboBox<>();
        stationEditCombo.setPrefWidth(125);
        stationEditColorPicker = new ColorPicker();
        stationEditRow.getChildren().addAll(stationEditLabel,
                stationEditCombo, stationEditColorPicker); // Add controls to
        // desc row

        stationEditButtonRow = new HBox(10);
        stationEditNewBtn = initButton(stationEditButtonRow,
                METRO_STATIONS_ADD_TOOLTIP.toString(), ENABLED);
        stationEditRemoveBtn = initButton(stationEditButtonRow,
                METRO_STATIONS_DELETE_TOOLTIP.toString(), ENABLED);
        stationEditSnapBtn = initButton(stationEditButtonRow,
                METRO_STATIONS_SNAP.toString(), ENABLED);
        stationEditMoveLabelBtn = initButton(stationEditButtonRow,
                METRO_STATIONS_MOVE_LABEL.toString(), ENABLED);
        stationEditRotateLabelBtn = initButton(stationEditButtonRow,
                METRO_STATIONS_ROTATE_LABEL.toString(), ENABLED);
//        stationEditButtonRow.getChildren().addAll(stationEditNewBtn,
//                stationEditRemoveBtn, stationEditSnapBtn,
//                stationEditMoveLabelBtn, stationEditRotateLabelBtn); // Add
        // controls to button row

        stationEditSlider = new Slider(1, 30, 1);

        stationEditBox.getChildren().addAll(stationEditRow,
                stationEditButtonRow, stationEditSlider); // Add controls to
        // main staiton toolbar

        // Direction Toolbar
        findBox = new HBox(10);

        findComboCol = new VBox(5);
        findOriginCombo = new ComboBox<>();
        findOriginCombo.setPrefWidth(125);
        findDestCombo = new ComboBox<>();
        findDestCombo.setPrefWidth(125);
        // Bind comboboxes to contents of station combobox
        findOriginCombo.itemsProperty().bind(stationEditCombo.itemsProperty());
        findDestCombo.itemsProperty().bind(stationEditCombo.itemsProperty());
        findComboCol.getChildren().addAll(findOriginCombo, findDestCombo);
        findBox.getChildren().addAll(findComboCol); // Add
        // controls to find toolbar
        findRouteButton = initButton(findBox,
                METRO_DIRECTIONS_BUTTON_TOOLTIP.toString(), ENABLED);

        decorBox = new VBox();

        decorEditButtonRow = new HBox(10);
        HBox decorLabelRow = new HBox(10);
        decorEditLabel = new Label(props.getProperty(METRO_DECOR_LABEL).toUpperCase());
        decorEditColor = new ColorPicker();
        decorLabelRow.getChildren().addAll(decorEditLabel, decorEditColor);
        decorBackgroundBtn = initButton(decorEditButtonRow,
                METRO_DECOR_SET_IMAGE_BG.toString(), ENABLED);
        decorImgBtn = initButton(decorEditButtonRow,
                METRO_DECOR_ADD_IMAGE.toString(), ENABLED);
        decorLabelBtn = initButton(decorEditButtonRow,
                METRO_DECOR_ADD_LABEL.toString(), ENABLED);
        decorRemoveBtn = initButton(decorEditButtonRow,
                METRO_REMOVE_ELEMENT.toString(), ENABLED);
        decorBox.getChildren().addAll(decorLabelRow, decorEditButtonRow);
//        decorEditButtonRow.getChildren().addAll(decorBackgroundBtn, decorImgBtn,
//                decorLabelBtn, decorRemoveBtn); // Add controls to button row

        // Font Toolbar
        fontBox = new VBox();

        fontEditRow = new HBox(10);
        fontEditLabel = new Label(props.getProperty(METRO_FONT_LABEL).toUpperCase());
        fontColor = new ColorPicker();
        fontEditRow.getChildren().addAll(fontEditLabel, fontColor);

        fontButtonRow = new HBox(10);
        fontBoldBtn = initButton(fontButtonRow,
                METRO_FONT_BOLD_TOOLTIP.toString(), ENABLED);
        fontItalicsBtn = initButton(fontButtonRow,
                METRO_FONT_ITALICS_TOOLTIP.toString(), ENABLED);
        fontFamilyCombo = initComboBox(FONT_FAMILY_COMBO_BOX_OPTIONS.toString());
        fontSizeCombo = initComboBox(FONT_SIZE_COMBO_BOX_OPTIONS.toString());
        fontButtonRow.getChildren().addAll(fontFamilyCombo, fontSizeCombo);

        fontBox.getChildren().addAll(fontEditRow, fontButtonRow);

        // Navigation Toolbar
        navigationBox = new VBox();

        navigationEditRow = new HBox(10);
        navigationEditLabel = new Label(props.getProperty(METRO_NAV_LABEL).toUpperCase());
        navigationGridCheckBox = new CheckBox(props.getProperty
                (METRO_NAV_GRID_CHECK));
        navigationGridCheckBox.setText(props.getProperty(METRO_NAV_GRID_CHECK));
        navigationEditRow.getChildren().addAll(navigationEditLabel,
                navigationGridCheckBox);

        navigationButtonRow = new HBox(10);
        navigationZoomInBtn = initButton(navigationButtonRow,
                METRO_NAV_ZOOM_IN_TOOLTIP.toString(), ENABLED);
        navigationZoomOutBtn = initButton(navigationButtonRow,
                METRO_NAV_ZOOM_OUT_TOOLTIP.toString(), ENABLED);
        navigationScaleDownBtn = initButton(navigationButtonRow,
                METRO_NAV_SCALE_DOWN_TOOLTIP.toString(), ENABLED);
        navigationScaleUpBtn = initButton(navigationButtonRow,
                METRO_NAV_SCALE_UP_TOOLTIP.toString(), ENABLED);
//        navigationButtonRow.getChildren().addAll(navigationZoomInBtn,
//                navigationZoomOutBtn, navigationScaleUpBtn,
//                navigationScaleDownBtn);
        navigationBox.getChildren().addAll(navigationEditRow,
                navigationButtonRow);

        // Add all toolbars to the sidebar
        editSideBar.getChildren().addAll(lineEditBox, stationEditBox,
                findBox, decorBox, fontBox, navigationBox);

        editScrollPane.setContent(editSideBar);

        // Create the app workspace
        workspace = new BorderPane();
        scrollPane = new ScrollPane(shapesGroup); // Use group to wrap canvas

        ((BorderPane) workspace).setCenter(scrollPane);
        ((BorderPane) workspace).setLeft(editScrollPane);

        // Initialize our shapes
        ((MetroData) app.getDataComponent()).setShapes(canvas.getChildren());

        shapesGroup.getChildren().add(canvas); // Add the canvas to the group

        // Create the rectangle
        clipper = new Rectangle();

        // Clip shape to borderpane center region - IMPORTANT
        canvas.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            clipper.setWidth(newValue.getWidth());
            clipper.setHeight(newValue.getHeight());
        });

    }

    /**
     * Helper method to attach events to oontrols
     */
    public void initControllers() {
        controller = new MetroEditController(app); // Construct our
        // controller

        // Line methods
        lineEditCombo.valueProperty().addListener((ObservableValue<? extends
                MetroLine> observable, MetroLine oldValue, MetroLine newValue) -> {
            if (newValue == null)
                return;
            boolean saved = app.getGUI().getFileController().isSaved();
           lineEditColorPicker.setValue(newValue.getLineColor());
           lineEditSlider.setValue(newValue.getPath().getStrokeWidth());
           if (saved) {
               app.getGUI().updateToolbarControls(true);
               app.getGUI().getFileController().markFileAsSaved();
           }
        });
        lineEditColorPicker.setOnAction(e -> {
            controller.processUpdateLineColor(lineEditColorPicker.getValue());
        });
        lineEditAddBtn.setOnAction(e -> {
            controller.processNewLine();
        });
        lineEditRemoveBtn.setOnAction(e -> {
            controller.processDeleteLine();
        });
        lineEditAddStationBtn.setOnAction(e -> {
            controller.processAddStationsToLine();
        });
        lineEditRemoveStationBtn.setOnAction(e -> {
            controller.processRemoveStationsFromLine();
        });
        lineEditListStationsBtn.setOnAction(e -> {
            controller.processShowLineStations();
        });
        lineEditBtn.setOnAction(e -> {
            controller.processEditLine();
        });
        lineEditSlider.valueProperty().addListener(e-> {
            controller.processLineSlider();
        });
        lineEditSlider.setOnMousePressed(e -> {
            oldLineSliderValue = lineEditSlider.getValue();
        });
        lineEditSlider.setOnMouseReleased(e -> {
            // TODO jTPS
            newLineSliderValue = lineEditSlider.getValue();
            MetroData dataManager = (MetroData) app.getDataComponent();
            if (lineEditCombo.getValue() != null && oldLineSliderValue !=
                    newLineSliderValue) {
                // Build the transaction object for when the user is not actively changing the slider value
                StrokeWidthTransaction transaction = new
                        StrokeWidthTransaction(lineEditCombo.getValue()
                        .getPath(), oldLineSliderValue, newLineSliderValue, app);
                app.getTPS().addTransaction(transaction);
                app.getGUI().disableUndoRedo(app.getTPS().canUndo(),
                        app.getTPS().canRedo());
            }
        });

        // Station methods
        stationEditCombo.valueProperty().addListener((ObservableValue<? extends
                StationReference> observable, StationReference oldValue, StationReference newValue) -> {
            if (newValue == null)
                return;

            stationEditColorPicker.setValue((Color) newValue.getStation().getFill
                    ());
            stationEditSlider.setValue(newValue.getStation().getRadiusX());
        });
        stationEditColorPicker.setOnAction(e -> {
            controller.processUpdateStationColor(stationEditColorPicker
                    .getValue());
        });
        stationEditNewBtn.setOnAction(e -> {
            controller.processNewStation();
        });
        stationEditRemoveBtn.setOnAction(e -> {
            controller.processDeleteStation();
        });
        stationEditSnapBtn.setOnAction(e -> {
            controller.processSnapStation();
        });
        stationEditMoveLabelBtn.setOnAction(e -> {
            controller.processCycleStationLabel();
        });
        stationEditRotateLabelBtn.setOnAction(e -> {
            controller.processRotateStationLabel();
        });
        stationEditSlider.valueProperty().addListener(e-> {
            controller.processStationSlider();
        });
        stationEditSlider.setOnMousePressed(e -> {
            oldStationSliderValue = stationEditSlider.getValue();
        });
        stationEditSlider.setOnMouseReleased(e -> {
            newStationSliderValue = stationEditSlider.getValue();
            MetroData dataManager = (MetroData) app.getDataComponent();
            if (stationEditCombo.getValue() != null && oldStationSliderValue !=
                    newStationSliderValue) {
                // Build the transaction object for when the user is not actively changing the slider value
                StrokeWidthTransaction transaction = new
                        StrokeWidthTransaction(stationEditCombo.getValue()
                        .getStation(), oldStationSliderValue, newStationSliderValue,
                        app);
                app.getTPS().addTransaction(transaction);
                app.getGUI().disableUndoRedo(app.getTPS().canUndo(),
                        app.getTPS().canRedo());
            }
        });

        // Route methods
        findRouteButton.setOnAction(e -> {
            controller.processFindRoute();
        });

        // Decor methods
        decorEditColor.setOnAction(e -> {
            controller.processSelectBackgroundColor();
        });
        decorBackgroundBtn.setOnAction(e -> {
            controller.processSelectBackgroundImage();
        });
        decorImgBtn.setOnAction(e -> {
            controller.processInsertImage();
        });
        decorLabelBtn.setOnAction(e -> {
            controller.processInsertText();
        });
        decorRemoveBtn.setOnAction(e -> {
            controller.processRemoveSelectedElement();
        });

        // Font methods
        fontColor.setOnAction(e -> {
            controller.processUpdateFontColor();
        });
        fontFamilyCombo.valueProperty().addListener((ObservableValue<? extends String>
                                               observable, String oldValue, String newValue) -> {
            MetroData dataManager = (MetroData) app.getDataComponent();
            if (lastSelectedTextFont != null && (dataManager.getSelectedShape
                    () == lastSelectedTextFont || dataManager.getSelectedShape()
                    instanceof MetroStation))
                controller.processUpdateLabelFont(oldValue, newValue);

            if (dataManager.getSelectedShape() instanceof DraggableText)
                lastSelectedTextFont = (DraggableText) dataManager.getSelectedShape();
            else if (dataManager.getSelectedShape() instanceof MetroStation)
                lastSelectedTextFont = ((MetroStation) dataManager
                        .getSelectedShape()).getAssociatedLabel();
            // Makes sure no extra transactions appear
        });
        fontSizeCombo.valueProperty().addListener((ObservableValue<? extends String>
                                                             observable, String oldValue, String newValue) -> {
            MetroData dataManager = (MetroData) app.getDataComponent();
            if (lastSelectedSizeFont != null && (dataManager.getSelectedShape
                    () == lastSelectedSizeFont || dataManager.getSelectedShape()
            instanceof MetroStation))
                controller.processUpdateLabelFontsize(oldValue, newValue);
            if (dataManager.getSelectedShape() instanceof DraggableText)
                lastSelectedSizeFont = (DraggableText) dataManager.getSelectedShape();
            else if (dataManager.getSelectedShape() instanceof MetroStation)
                lastSelectedSizeFont = ((MetroStation) dataManager
                        .getSelectedShape()).getAssociatedLabel();
            // Makes sure no extra transactions appear
        });
        fontBoldBtn.setOnAction(e -> {
            controller.processBoldFont();
        });
        fontItalicsBtn.setOnAction(e -> {
            controller.processItalicizeFont();
        });

        // Navigation methods
        navigationGridCheckBox.setOnAction(e -> {
            controller.toggleGrid();
        });
        navigationZoomInBtn.setOnAction(e -> {
            controller.processZoomIn();
        });
        navigationZoomOutBtn.setOnAction(e -> {
            controller.processZoomOut();
        });
        navigationScaleUpBtn.setOnAction(e -> {
            controller.processScaleUp();
        });
        navigationScaleDownBtn.setOnAction(e -> {
            controller.processScaleDown();
        });

        canvasController = new CanvasController(app);
        canvas.setOnMousePressed(e-> {
            canvasController.processCanvasMousePress((int)e.getX(), (int)e.getY());
        });
        canvas.setOnMouseReleased(e-> {
            canvasController.processCanvasMouseRelease((int)e.getX(), (int)e.getY());
        });
        canvas.setOnMouseDragged(e-> {
            canvasController.processCanvasMouseDragged((int)e.getX(), (int)e.getY());
        });

        // Add keyboard mappings for panning using keys
        app.getStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, e ->
        {
            double deltaX = 0, deltaY = 0;
            switch(e.getCode()) {
                case W:
                    deltaY = -0.1;
                    break;
                case A:
                    deltaX = -0.1;
                    break;
                case D:
                    deltaX = 0.1;
                    break;
                case S:
                    deltaY = 0.1;
            }
            scrollPane.setHvalue(scrollPane.getHvalue() + deltaX);
            scrollPane.setVvalue(scrollPane.getVvalue() + deltaY);
        });

    }

    /**
     * This function specifies the CSS style classes for all the UI components
     * known at the time the workspace is initially constructed. Note that the
     * tag editor controls are added and removed dynamically as the application
     * runs so they will have their style setup separately.
     */
    public void initStyle() {
        // Style the canvas
        canvas.getStyleClass().add(CLASS_RENDER_CANVAS);

        // Style the color pickers
        lineEditColorPicker.getStyleClass().add(CLASS_BUTTON);
        stationEditColorPicker.getStyleClass().add(CLASS_BUTTON);
        decorEditColor.getStyleClass().add(CLASS_BUTTON);
        fontColor.getStyleClass().add(CLASS_BUTTON);

        // Style toolbars
        editScrollPane.getStyleClass().add(CLASS_EDIT_TOOLBAR);
        lineEditBox.getStyleClass().add(CLASS_EDIT_TOOLBAR_ROW);
        stationEditBox.getStyleClass().add(CLASS_EDIT_TOOLBAR_ROW);
        findBox.getStyleClass().add(CLASS_EDIT_TOOLBAR_ROW);
        decorBox.getStyleClass().add(CLASS_EDIT_TOOLBAR_ROW);
        fontBox.getStyleClass().add(CLASS_EDIT_TOOLBAR_ROW);
        navigationBox.getStyleClass().add(CLASS_EDIT_TOOLBAR_ROW);

        // Style labels
        lineEditLabel.getStyleClass().add(CLASS_SIDEBAR_LABEL);
        stationEditLabel.getStyleClass().add(CLASS_SIDEBAR_LABEL);
        decorEditLabel.getStyleClass().add(CLASS_SIDEBAR_LABEL);
        fontEditLabel.getStyleClass().add(CLASS_SIDEBAR_LABEL);
        navigationEditLabel.getStyleClass().add(CLASS_SIDEBAR_LABEL);
    }

    public Button initButton(Pane parent, String name, boolean enabled) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        name = name.replace("_TOOLTIP", ""); // Remove some of the
        // identifiers to make it easier to parse

        // LOAD THE ICON FROM THE PROVIDED FILE
        String iconProperty = name + "_ICON";
        String tooltipProperty = name + "_TOOLTIP";
        String imagePath = FILE_PROTOCOL + PATH_IMAGES + props.getProperty(iconProperty);
        Image buttonImage = new Image(imagePath);

        // NOW MAKE THE BUTTON
        Button button = new Button();
        button.setDisable(!enabled);
        if (props.getProperty(iconProperty) != null)
            button.setGraphic(new ImageView(buttonImage));
        else
            button.setText(props.getProperty(name)); // Show text if image does
        // not exist

        String tooltipText = props.getProperty(tooltipProperty);
        Tooltip buttonTooltip = new Tooltip(tooltipText);
        button.setTooltip(buttonTooltip);

        // Style the button
        button.getStyleClass().add(CLASS_SIDEBAR_BUTTON);

        // ADD IT TO THE PANE
        parent.getChildren().add(button);

        // AND RETURN THE COMPLETED BUTTON
        return button;
    }

    private ComboBox initComboBox(String comboPropertyList) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        ArrayList<String> comboOptions = props.getPropertyOptionsList(comboPropertyList);
        ObservableList oList = FXCollections.observableList(comboOptions);
        ComboBox cBox = new ComboBox(oList);
        //cBox.getSelectionModel().selectFirst();
        return cBox;
    }

    /**
     * This function must be defined in the actual workspace component class and should be called after data has been
     * loaded and the workspace must use it to initialize controls.
     *
     * @param dataComponent
     */
    @Override
    public void reloadWorkspace(AppDataComponent dataComponent) {
        MetroData dataManager = (MetroData) dataComponent;

        if (dataManager.isInState(MetroState.SELECTING_ELEMENT)
                || dataManager.isInState(MetroState.DRAGGING_ELEMENT)
                || dataManager.isInState(MetroState.DRAGGING_NOTHING)) {
            boolean shapeIsNotSelected = dataManager.getSelectedShape() == null;
            decorRemoveBtn.setDisable(shapeIsNotSelected);
        } else if (dataManager.isInState(MetroState.DRAGGING_NOTHING)) {
            // Disable label tools when nothing is selected
            clearLabelProperties();
        }

        decorRemoveBtn.setDisable(dataManager.getSelectedShape() == null);
        decorEditColor.setValue(dataManager.getBackgroundColor());
    }

    public void resetWorkspace() {

    }

    @Override
    public void handleAboutDialog() {
        AboutDialog dialog = AboutDialog.getInstance();
        dialog.showDialog(app);
    }

    /* Update toolbar methods */
    public void updateLineEditToolbar(MetroLine line) {
        if (line == null) {
            lineEditCombo.setValue(null);
            return;
        }

        lineEditCombo.setValue(line);
        // TODO add other properties
        lineEditColorPicker.setValue(line.getLineColor());
        lineEditSlider.setValue(line.getPath().getStrokeWidth());
    }

    public void updateStationEditToolbar(MetroStation station) {
        if (station == null) {
            stationEditCombo.setValue(null);
            return;
        }

        // Select the reference object wrapper
        stationEditCombo.setValue(station.getStationReference());
        stationEditColorPicker.setValue((Color) station.getFill());
        stationEditSlider.setValue(station.getRadiusX());
    }

    public void updateFontToolbar(DraggableText text) {
        fontFamilyCombo.setDisable(false);
        fontSizeCombo.setDisable(false);
        fontBoldBtn.setDisable(false);
        fontItalicsBtn.setDisable(false);
        fontColor.setDisable(false);
        fontColor.setValue((Color) text.getFill());
        fontFamilyCombo.getSelectionModel().select(text.getFont().getFamily());
        Double fontSize = text.getFont().getSize();
        fontSizeCombo.getSelectionModel().select(Integer.toString(fontSize.intValue()));
    }

    public void updateLinesAndStations() {
        // Add any loaded stations or lines (from data manager)
        MetroData dataManager = (MetroData) app.getDataComponent();
        lineEditCombo.getItems().addAll(dataManager.getMapLines().values());
        dataManager.getMapStations().values().forEach(s -> getStationCombo()
                .getItems().add(s.getStationReference()));

        controller.setMetroLines(lineEditCombo.getItems()); // Update the
        // controller's list of stations
    }

    public Rectangle cloneCanvasClip() {
        // Create the rectangle
        Rectangle clip = new Rectangle();
        clip.setWidth(clipper.getWidth());
        clip.setHeight(clipper.getHeight());

        // Part 13:
        // Clip shape to borderpane center region - IMPORTANT
        canvas.layoutBoundsProperty()
                .addListener((ov, oldValue, newValue) -> {
            clip.setWidth(newValue.getWidth());
            clip.setHeight(newValue.getHeight());
        });
        return clip;
    }

    /* HELPER METHODS */
    /**
     * Note that this is for displaying text during development.
     */
    public void setDebugText(String text) {
        //debugText.setText(text);
    }

    /**
     * Return the canvas or pane that holds all the objects
     * @return
     */
    public Pane getCanvas() {
        return canvas;
    }

    public Group getShapesGroup() {
        return shapesGroup;
    }

    /**
     * Return the properties manager from the workspace
     */
    public PropertiesManager getPropertiesManager() {
        return props;
    }

    public ComboBox<MetroLine> getLineCombo() {
        return lineEditCombo;
    }
    public Slider getLineEditSlider() {
        return lineEditSlider;
    }

    public ComboBox<StationReference> getStationCombo() { return stationEditCombo; }
    public Slider getStationEditSlider() {
        return stationEditSlider;
    }

    public ColorPicker getBackgroundColorPicker() {
        return decorEditColor;
    }

    public ColorPicker getTextColorPicker() {
        return fontColor;
    }

    /* Route */
    public ComboBox<StationReference> getFindOriginCombo() {
        return findOriginCombo;
    }

    public ComboBox<StationReference> getFindDestCombo() {
        return findDestCombo;
    }

    /* Font */
    public ComboBox<String> getFontFamilyCombo() { return fontFamilyCombo; }
    public ComboBox<String> getFontSizeCombo() { return fontSizeCombo; }

    public CheckBox getShowGridChecked() { return navigationGridCheckBox; }

    /**
     * Resets edit row controls since label is not selected
     */
    public void clearLabelProperties() {
        // Disable controls
        fontFamilyCombo.setDisable(true);
        fontSizeCombo.setDisable(true);
        fontBoldBtn.setDisable(true);
        fontItalicsBtn.setDisable(true);
        fontColor.setDisable(true);
    }

    /* Misc */

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
    public MetroEditController getController() {
        return controller;
    }
}
