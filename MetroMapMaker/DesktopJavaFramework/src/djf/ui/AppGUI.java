package djf.ui;

import djf.AppTemplate;
import djf.components.AppClipboardComponent;
import djf.language.AppLanguageSettings;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jtps.jTPS;
import properties_manager.PropertiesManager;

import java.net.URL;

import static djf.AppPropertyType.*;
import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;

/**
 * This class provides the basic user interface for this application, including all the file controls, but not including
 * the workspace, which would be customly provided for each app.
 *
 * @author Richard McKenna
 * @author Ritwik Banerjee
 * @version 1.0
 */
public class AppGUI {
    // WE'LL NEED THIS TO ACCESS COMPONENTS
    protected AppTemplate app;

    // THIS IS THE APPLICATION WINDOW
    protected Stage primaryStage;

    // THIS IS THE STAGE'S SCENE GRAPH
    protected Scene primaryScene;

    // THIS PANE ORGANIZES THE BIG PICTURE CONTAINERS FOR THE
    // APPLICATION AppGUI. NOTE THAT THE WORKSPACE WILL GO
    // IN THE CENTER REGION OF THE appPane
    protected BorderPane appPane;

    // THIS IS THE TOP PANE WHERE WE CAN PUT TOOLBAR
    protected HBox topToolbarPane;

    // THIS IS THE FILE TOOLBAR AND ITS CONTROLS
    protected ToolBar fileToolbar;
    protected Button  newButton;
    protected Button  loadButton;
    protected Button  closeButton;
    protected Button  saveButton;
    protected Button  saveAsButton;
    protected Button  exportButton;
    protected Button  exitButton;

    // THIS IS FOR THE CUT/COPY/PASTE BUTTONS IF WE'RE USING THEM
//    protected ToolBar cutToolbar;
//    protected Button  cutButton;
//    protected Button  copyButton;
//    protected Button  pasteButton;

    // THIS IS FOR THE UNDO/REDO BUTTONS IF WE'RE USING THEM
    protected ToolBar undoToolbar;
    protected Button  undoButton;
    protected Button  redoButton;

    // THIS IS FOR THE SETTINGS/HELP/ABOUT BUTTONS IF WE'RE USING THEM
    protected ToolBar settingsToolbar;
//    protected Button  languageButton;
//    protected Button  helpButton;
    protected Button  aboutButton;

    // THIS TITLE WILL GO IN THE TITLE BAR
    protected String appTitle;

    // WE USE THESE THINGS FOR ASSEMBLING OUR BUTTONS-ONLY TOP TOOLBARS
    public static final String NEW_BUTTON_PREFIX      = "NEW";
    public static final String LOAD_BUTTON_PREFIX     = "LOAD";
    public static final String CLOSE_BUTTON_PREFIX    = "CLOSE";
    public static final String SAVE_BUTTON_PREFIX     = "SAVE";
    public static final String SAVE_AS_BUTTON_PREFIX  = "SAVE_AS";
    public static final String EXPORT_BUTTON_PREFIX   = "EXPORT";
    public static final String EXIT_BUTTON_PREFIX     = "EXIT";
    public static final String CUT_BUTTON_PREFIX      = "CUT";
    public static final String COPY_BUTTON_PREFIX     = "COPY";
    public static final String PASTE_BUTTON_PREFIX    = "PASTE";
    public static final String UNDO_BUTTON_PREFIX     = "UNDO";
    public static final String REDO_BUTTON_PREFIX     = "REDO";
    public static final String LANGUAGE_BUTTON_PREFIX = "LANGUAGE";
    public static final String HELP_BUTTON_PREFIX     = "HELP";
    public static final String ABOUT_BUTTON_PREFIX    = "ABOUT";

    @SuppressWarnings("unused")
    protected static final String ICON_PROPERTY_POSTFIX = "_ICON";

    @SuppressWarnings("unused")
    protected static final String TOOLTIP_PROPERTY_POSTFIX = "_TOOLTIP";

    public static final boolean ENABLED  = true;
    public static final boolean DISABLED = false;

    private FileController fileController;

    /**
     * This constructor initializes the file toolbar for use.
     *
     * @param initPrimaryStage The window for this application.
     * @param initAppTitle     The title of this application, which will appear in the window bar.
     * @param initApp          The app within this gui is used.
     */
    public AppGUI(Stage initPrimaryStage, String initAppTitle, AppTemplate initApp) {
        // SAVE THESE FOR LATER
        primaryStage = initPrimaryStage;
        appTitle = initAppTitle;
        app = initApp;

        // INIT THE TOOLBAR
        initTopToolbar();

        // AND FINALLY START UP THE WINDOW (WITHOUT THE WORKSPACE)
        initWindow();

        // INIT THE STYLESHEET AND THE STYLE FOR THE FILE TOOLBAR
        initStylesheet();
        initFileToolbarStyle();
    }

    /**
     * Accessor method for getting the application pane, within which all user interface controls are ultimately
     * placed.
     *
     * @return This application GUI's app pane.
     */
    public BorderPane getAppPane() {
        return appPane;
    }

    /**
     * Accessor method for getting the toolbar pane in the top, within which other toolbars are placed.
     *
     * @return This application GUI's app pane.
     */
    @SuppressWarnings("unused")
    public HBox getTopToolbarPane() {
        return topToolbarPane;
    }

    /**
     * Accessor method for getting the file toolbar, within which all file controls are placed.
     *
     * @return This application GUI's file toolbar.
     */
    @SuppressWarnings("unused")
    public ToolBar getFileToolbar() {
        return fileToolbar;
    }

//    /**
//     * Accessor method for getting the cut toolbar, within which all cut/copy/paste controls are placed.
//     *
//     * @return This application GUI's cut toolbar.
//     */
//    @SuppressWarnings("unused")
//    public ToolBar getCutToolbar() {
//        return cutToolbar;
//    }
//
//    /**
//     * Accessor method for getting the undo toolbar, within which all undo/redo controls are placed.
//     *
//     * @return This application GUI's undo toolbar.
//     */
//    @SuppressWarnings("unused")
//    public ToolBar getUndoToolbar() {
//        return undoToolbar;
//    }
//
//    /**
//     * Accesssor method for getting the settings toolbar, within which all settings controls are placed.
//     */
//    @SuppressWarnings("unused")
//    public ToolBar getSettingsToolbar() {
//        return settingsToolbar;
//    }

    /**
     * Accessor method for getting this application's primary stage's, scene.
     *
     * @return This application's window's scene.
     */
    @SuppressWarnings("unused")
    public Scene getPrimaryScene() {
        return primaryScene;
    }

    /**
     * Accessor method for getting this application's window, which is the primary stage within which the full GUI will
     * be placed.
     *
     * @return This application's primary stage (i.e. window).
     */
    public Stage getWindow() {
        return primaryStage;
    }

    /**
     * This method is used to activate/deactivate toolbar buttons after each work edit such that they can be usable or
     * not usable in order to enforce the rules of foolproof design.
     *
     * @param saved Describes whether the loaded Page has been saved or not.
     */
    public void updateToolbarControls(boolean saved) {
        // FROM THE FILE TOOLBAR, ONLY THE SAVE BUTTON
        // NEEDS TO BE ENABLED/DISABLED
        saveButton.setDisable(saved);

        // Enable exporting
        exportButton.setDisable(false);

        // AND THE UNDO/REDO BUTTONS DEPEND ON jTPS
        jTPS tps = app.getTPS();
        undoButton.setDisable(!tps.canUndo());
        redoButton.setDisable(!tps.canRedo());

        // THE SETTINGS BUTTONS ARE ALWAYS ENABLED
    }

    @SuppressWarnings("unused")
    public void disableButton(Button button, boolean disable) {
        if (button != null)
            button.setDisable(disable);
    }

    private boolean isTrue(Object property) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        return Boolean.valueOf(props.getProperty(property.toString()));
    }

    // BELOW ARE ALL THE PRIVATE HELPER METHODS WE USE FOR INITIALIZING OUR AppGUI *

    /**
     * This function initializes all the buttons in the toolbar at the top of the application window. These are related
     * to file management.
     */
    private void initTopToolbar() {
        if (isTrue(HAS_TOP_TOOLBAR)) {
            // THESE OPTIONAL TOOLBARS WILL GO HERE
            topToolbarPane = new HBox();
            if (isTrue(HAS_FILE_TOOLBAR)) initFileToolbar();
            //if (isTrue(HAS_CUT_TOOLBAR)) initCutToolbar();
            if (isTrue(HAS_UNDO_TOOLBAR)) initUndoToolbar();
            if (isTrue(HAS_SETTINGS_TOOLBAR)) initSettingsToolbar();
        }
    }

    private void initFileToolbar() {
        fileToolbar = new ToolBar();
        topToolbarPane.getChildren().add(fileToolbar);
        fileController = new FileController(app);
        if (isTrue(HAS_NEW)) {
            newButton = initToolBarChildButton(fileToolbar, NEW_BUTTON_PREFIX, ENABLED);
            newButton.setOnAction(e -> fileController.processNewRequest());
        }
        if (isTrue(HAS_LOAD)) {
            loadButton = initToolBarChildButton(fileToolbar, LOAD_BUTTON_PREFIX, ENABLED);
            loadButton.setOnAction(e -> fileController.processLoadRequest());
        }
        if (isTrue(HAS_CLOSE)) {
            closeButton = initToolBarChildButton(fileToolbar, CLOSE_BUTTON_PREFIX, DISABLED);
            closeButton.setOnAction(e -> fileController.processCloseRequest());
        }
        if (isTrue(HAS_SAVE)) {
            saveButton = initToolBarChildButton(fileToolbar, SAVE_BUTTON_PREFIX, DISABLED);
            saveButton.setOnAction(e -> fileController.processSaveRequest());
        }
        if (isTrue(HAS_SAVE_AS)) {
            saveAsButton = initToolBarChildButton(fileToolbar,
                    SAVE_AS_BUTTON_PREFIX, ENABLED);
            saveAsButton.setOnAction(e -> fileController.processSaveAsRequest());
        }
        if (isTrue(HAS_EXPORT)) {
            exportButton = initToolBarChildButton(fileToolbar,
                    EXPORT_BUTTON_PREFIX, DISABLED);
            exportButton.setOnAction(e -> fileController.processExportRequest());
        }
        if (isTrue(HAS_EXIT)) {
            exitButton = initToolBarChildButton(fileToolbar, EXIT_BUTTON_PREFIX, DISABLED);
            exitButton.setOnAction(e -> fileController.processExitRequest());
        }
    }

//    // HELPER METHOD FOR INITIALIZING THE CUT TOOLBAR
//    private void initCutToolbar() {
//        // NOTE THAT IN ORDER FOR THIS INITIALIZATION TO WORK
//        // PROPERLY THE AppClipboardComponent MUST ALREADY EXIST
//        cutToolbar = new ToolBar();
//        topToolbarPane.getChildren().add(cutToolbar);
//
//        // THIS IS AN ALL OR NOTHING TOOLBAR
//        cutButton = initToolBarChildButton(cutToolbar, CUT_BUTTON_PREFIX, ENABLED);
//        copyButton = initToolBarChildButton(cutToolbar, COPY_BUTTON_PREFIX, ENABLED);
//        pasteButton = initToolBarChildButton(cutToolbar, PASTE_BUTTON_PREFIX, ENABLED);
//    }
//
//    public void registerClipboardComponent() {
//        AppClipboardComponent clipboard = app.getClipboardComponent();
//        if (clipboard != null) {
//            cutButton.setOnAction(e -> clipboard.cut());
//            copyButton.setOnAction(e -> clipboard.copy());
//            pasteButton.setOnAction(e -> clipboard.paste());
//        }
//    }

    // HELPER METHOD FOR INITIALIZING THE UNDO TOOLBAR
    private void initUndoToolbar() {
        undoToolbar = new ToolBar();
        topToolbarPane.getChildren().add(undoToolbar);
        UndoController undoController = new UndoController(app);

        // THIS IS AN ALL OR NOTHING TOOLBAR
        undoButton = initToolBarChildButton(undoToolbar, UNDO_BUTTON_PREFIX, DISABLED);
        undoButton.setOnAction(e -> undoController.processUndoRequest());
        redoButton = initToolBarChildButton(undoToolbar, REDO_BUTTON_PREFIX, DISABLED);
        redoButton.setOnAction(e -> undoController.processRedoRequest());
    }

    // HELPER METHOD FOR INITIALIZING THE UNDO TOOLBAR
    private void initSettingsToolbar() {
        settingsToolbar = new ToolBar();
        topToolbarPane.getChildren().add(settingsToolbar);
        SettingsController settingsController = new SettingsController(app);
//        if (isTrue(HAS_LANGUAGE)) {
//            languageButton = initToolBarChildButton(settingsToolbar, LANGUAGE_BUTTON_PREFIX, ENABLED);
//            languageButton.setOnAction(e -> settingsController.processLanguageRequest());
//        }
//        if (isTrue(HAS_HELP)) {
//            helpButton = initToolBarChildButton(settingsToolbar, HELP_BUTTON_PREFIX, ENABLED);
//            helpButton.setOnAction(e -> settingsController.processHelpRequest());
//        }
        if (isTrue(HAS_ABOUT)) {
            aboutButton = initToolBarChildButton(settingsToolbar, ABOUT_BUTTON_PREFIX, ENABLED);
//            aboutButton.setOnAction(e -> settingsController.processAboutRequest());
            aboutButton.setOnAction(e -> app.getWorkspaceComponent().handleAboutDialog());
        }
    }

    // INITIALIZE THE WINDOW (i.e. STAGE) PUTTING ALL THE CONTROLS
    // THERE EXCEPT THE WORKSPACE, WHICH WILL BE ADDED THE FIRST
    // TIME A NEW Page IS CREATED OR LOADED
    private void initWindow() {
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // SET THE WINDOW TITLE
        primaryStage.setTitle(appTitle);

        // START FULL-SCREEN OR NOT, ACCORDING TO PREFERENCES
        primaryStage.setMaximized("true".equals(props.getProperty(START_MAXIMIZED)));

        // ADD THE TOOLBAR ONLY, NOTE THAT THE WORKSPACE
        // HAS BEEN CONSTRUCTED, BUT WON'T BE ADDED UNTIL
        // THE USER STARTS EDITING A COURSE
        appPane = new BorderPane();
        appPane.setTop(topToolbarPane);
        primaryScene = new Scene(appPane);

        // SET THE APP PANE PREFERRED SIZE ACCORDING TO THE PREFERENCES
        double prefWidth  = Double.parseDouble(props.getProperty(PREF_WIDTH));
        double prefHeight = Double.parseDouble(props.getProperty(PREF_HEIGHT));
        appPane.setPrefWidth(prefWidth);
        appPane.setPrefHeight(prefHeight);

        // SET THE APP ICON
        String appIcon = FILE_PROTOCOL + PATH_IMAGES + props.getProperty(APP_LOGO);
        primaryStage.getIcons().add(new Image(appIcon));

        // NOW TIE THE SCENE TO THE WINDOW
        primaryStage.setScene(primaryScene);
    }

    /**
     * This is a public helper method for initializing a simple button with an icon and tooltip and placing it into a
     * toolbar.
     *
     * @param toolbar the toolbar pane into which to place this button.
     * @param name    the name to be prefixed to the icon and tooltip for the button.
     * @param enabled <code>true</code> if the button is to start off enabled, false otherwise.
     * @return A constructed, fully initialized button placed into its appropriate pane container.
     */
    public Button initToolBarChildButton(ToolBar toolbar, String name, boolean enabled) {
        // MAKE THE BUTTON
        Button button = makeButton(name, enabled);

        // PUT THE BUTTON IN THE TOOLBAR
        toolbar.getItems().add(button);

        // AND RETURN IT
        return button;
    }

    public Button initPaneChildButton(Pane pane, String name, boolean enabled) {
        // MAKE THE BUTTON
        Button button = makeButton(name, enabled);

        // PUT THE BUTTON IN THE PANE
        pane.getChildren().add(button);

        // AND RETURN IT
        return button;
    }

    public Button makeButton(String name, boolean enabled) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // LOAD THE ICON FROM THE PROVIDED FILE
        String iconProperty    = name + "_ICON";
        String tooltipProperty = name + "_TOOLTIP";
        String imagePath       = FILE_PROTOCOL + PATH_IMAGES + props.getProperty(iconProperty);
        Image  buttonImage     = new Image(imagePath);

        // NOW MAKE THE BUTTON
        Button button = new Button();
        button.setDisable(!enabled);
        button.setGraphic(new ImageView(buttonImage));
        String  tooltipText   = props.getProperty(tooltipProperty);
        Tooltip buttonTooltip = new Tooltip(tooltipText);
        button.setTooltip(buttonTooltip);


        // MAKE SURE THE LANGUAGE MANAGER HAS IT
        // SO THAT IT CAN CHANGE THE LANGUAGE AS NEEDED
        AppLanguageSettings languageSettings = app.getLanguageSettings();
        languageSettings.addLabeledControl(name, button);

        // AND RETURN THE COMPLETED BUTTON
        return button;
    }

    /**
     * Note that this is the default style class for the top file toolbar and that style characteristics for this type
     * of component should be put inside app_properties.xml.
     */
    public static final String CLASS_BORDERED_PANE = "bordered_pane";

    /**
     * Note that this is the default style class for the buttons in the top toolbars and that style characteristics for
     * this type of component should be put inside app_properties.xml.
     */
    public static final String CLASS_TOOLBAR_BUTTON = "toolbar_button";

    /**
     * This function sets up the stylesheet to be used for specifying all style for this application. Note that it does
     * not attach CSS style classes to controls, that must be done separately.
     */
    private void initStylesheet() {
        // SELECT THE STYLESHEET
        PropertiesManager props      = PropertiesManager.getPropertiesManager();
        String            stylesheet = props.getProperty(APP_PATH_CSS);
        stylesheet += props.getProperty(APP_CSS);
        Class  appClass       = app.getClass();
        URL    stylesheetURL  = appClass.getResource(stylesheet);
        String stylesheetPath = stylesheetURL.toExternalForm();
        primaryScene.getStylesheets().add(stylesheetPath);
    }

    /**
     * This function specifies the CSS style classes for the controls managed by this framework.
     */
    private void initFileToolbarStyle() {
        // SET THE STYLE FOR THE TOP TOOLBAR
        topToolbarPane.getStyleClass().add(CLASS_BORDERED_PANE);

        // AND THEN ALL THE TOOLBARS IT CONTAINS
        for (Node toolbar : topToolbarPane.getChildren()) {
            ObservableList<String> styleClasses = toolbar.getStyleClass();
            styleClasses.add(CLASS_BORDERED_PANE);

            // AND FOR EVERY CONTROL IN THE TOOLBAR
            for (Node control : ((ToolBar) toolbar).getItems()) {
                control.getStyleClass().add(CLASS_TOOLBAR_BUTTON);
            }
        }
    }

    public FileController getFileController() {
        return fileController;
    }

    public void disableUndoRedo(boolean enableUndo, boolean enableRedo) {
        undoButton.setDisable(!enableRedo);
        redoButton.setDisable(!enableRedo);

        // Also means that file is edited, so mark it as edited
        getFileController().markAsEdited(this);
    }
}