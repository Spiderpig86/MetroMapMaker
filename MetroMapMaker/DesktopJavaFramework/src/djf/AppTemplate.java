package djf;

import djf.ui.*;
import djf.components.*;
import javafx.application.Application;
import javafx.stage.Stage;
import properties_manager.PropertiesManager;

import static djf.AppPropertyType.*;

import djf.language.AppLanguageSettings;

import static djf.language.AppLanguageSettings.*;

import jtps.jTPS;
import properties_manager.InvalidXMLFileFormatException;

/**
 * This is the framework's JavaFX application. It provides the start method that begins the program initialization,
 * which delegates component initialization to the application-specific child class' hook function.
 *
 * @author Richard McKenna
 * @version 1.0
 */
public abstract class AppTemplate extends Application {
    // THIS WILL MANAGE ALL THE LANGUAGE SETTINGS SUCH THAT
    // OUR USER INTERFACE CAN BE PRESENTED USING ANY LANGUAGE
    // LIKE ENGLISH, SPANISH, KOREAN, ETC SIMPLY BY PROVIDING
    // THE PROPER XML FILES WITH LANGUAGE CONTENT
    protected AppLanguageSettings languageSettings;

    // THIS IS THE APP'S FULL JavaFX GUI. NOTE THAT ALL APPS WOULD
    // SHARE A COMMON UI EXCEPT FOR THE CUSTOM WORKSPACE
    protected AppGUI gui;

    // THIS CLASS USES A COMPONENT ARCHITECTURE DESIGN PATTERN, MEANING IT
    // HAS OBJECTS THAT CAN BE SWAPPED OUT FOR OTHER COMPONENTS
    // THIS APP HAS 4 COMPONENTS

    // THE COMPONENT FOR MANAGING CUSTOM APP DATA
    protected AppDataComponent dataComponent;

    // THE COMPONENT FOR MANAGING CUSTOM FILE I/O
    protected AppFileComponent fileComponent;

    // THE COMPONENT FOR THE GUI WORKSPACE
    protected AppWorkspaceComponent workspaceComponent;

    // THE COMPONENT FOR THE CLIPBOARD
    protected AppClipboardComponent clipboardComponent;

    // THIS IS FOR MANAGING UNDO/REDO
    protected static jTPS tps;

    protected Stage stage;

    /**
     * This function must be overridden, it should initialize all of the components used by the app in the proper order
     * according to the particular app's dependencies.
     */
    public abstract void buildAppComponentsHook();

    // COMPONENT ACCESSOR METHODS

    /**
     * Accessor for the data component.
     */
    public AppDataComponent getDataComponent() { return dataComponent; }

    /**
     * Accessor for the file component.
     */
    public AppFileComponent getFileComponent() { return fileComponent; }

    /**
     * Accessor for the workspace component.
     */
    public AppWorkspaceComponent getWorkspaceComponent() { return workspaceComponent; }

    /**
     * Accessor for the clipboard.
     */
    public AppClipboardComponent getClipboardComponent() { return clipboardComponent; }

    /**
     * Accessor for the languageSettings.
     */
    public AppLanguageSettings getLanguageSettings() { return languageSettings; }

    /**
     * Accessor for the gui. Note that the GUI would contain the workspace.
     */
    public AppGUI getGUI() { return gui; }

    /**
     * Accessor method for the transaction processing system.
     */
    public jTPS getTPS() { return tps; }

    /**
     * Accessor method to get the main window.
     * @return
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * This is where our Application begins its initialization, it will load the custom app properties, build the
     * components, and fully initialize everything to get the app rolling.
     *
     * @param primaryStage This application's window.
     */
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        // FIRST SETUP THE PropertiesManager WITH
        // IT'S MINIMAL LANGUAGE PROPERTIES
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        props.addProperty(APP_ERROR_TITLE, "Application Error");
        props.addProperty(APP_ERROR_CONTENT, "An Error Occured in the Application");
        props.addProperty(PROPERTIES_ERROR_TITLE, "Properties File Error");
        props.addProperty(PROPERTIES_ERROR_CONTENT, "There was an Error Loading a Properties File");

        try {

            // FIRST LOAD THE APP PROPERTIES, WHICH CONTAINS THE UI SETTINGS THAT ARE NOT LANGUAGE-SPECIFIC.
            boolean success = loadProperties(APP_PROPERTIES_FILE_NAME);

            if (success) {
                // THIS IS THE TRANSACTION PROCESSING SYSTEM THAT WE'LL BE USING
                tps = new jTPS();

                // INIT THE LANGUAGE SETTINGS
                languageSettings = new AppLanguageSettings(this);
                languageSettings.init();

                // GET THE TITLE FROM THE XML FILE	
                String appTitle = props.getProperty(APP_TITLE);

                // BUILD THE BASIC APP GUI OBJECT FIRST
                gui = new AppGUI(primaryStage, appTitle, this);

                // THIS BUILDS ALL OF THE COMPONENTS, NOTE THAT
                // IT WOULD BE DEFINED IN AN APPLICATION-SPECIFIC
                // CHILD CLASS
                buildAppComponentsHook();

                // SETUP THE CLIPBOARD, IF IT'S BEING USED
                //gui.registerClipboardComponent();

                // LOAD ALL THE PROPER TEXT INTO OUR CONTROLS
                languageSettings.resetLanguage();

                // Bind close event to prompt saving
                primaryStage.setOnCloseRequest(e-> {
                    // Prevent default closing behavior
                    e.consume();

                    // Let the file controller handle the exiting
                    gui.getFileController().processExitRequest();

                });

                // NOW OPEN UP THE WINDOW
                primaryStage.show();
            }
        } catch (Exception e) {
            AppDialogs.showMessageDialog(gui.getWindow(), e, PROPERTIES_FILE_ERROR_MESSAGE);
            // THIS TYPE OF ERROR IS LIKELY DUE TO PROGRAMMER ERROR IN
            // THE APP ITSELF SO WE'LL PROVIDE A STACK TRACE DIALOG AND EXIT
            AppDialogs.showStackTraceDialog(gui.getWindow(), e, APP_ERROR_TITLE, APP_ERROR_CONTENT);
            System.exit(0);
        }
    }

    /**
     * Loads this application's properties file, which has a number of settings for initializing the user interface.
     *
     * @param propertiesFileName The XML file containing properties to be loaded in order to initialize the UI.
     * @return true if the properties file was loaded successfully, false otherwise.
     */
    public boolean loadProperties(String propertiesFileName) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        try {
            // LOAD THE SETTINGS FOR STARTING THE APP
            props.addProperty(PropertiesManager.DATA_PATH_PROPERTY, PATH_DATA);
            props.loadProperties(propertiesFileName, PROPERTIES_SCHEMA_FILE_NAME);
            return true;
        } catch (InvalidXMLFileFormatException ixmlffe) {
            // SOMETHING WENT WRONG INITIALIZING THE XML FILE
            AppDialogs.showMessageDialog(gui.getWindow(), PROPERTIES_ERROR_TITLE, PROPERTIES_ERROR_CONTENT);
            return false;
        }
    }

    /**
     * Process showing any intermediate dialogs before displaying the main
     * window.
     */
    public abstract void showIntermediateDialogs();
}