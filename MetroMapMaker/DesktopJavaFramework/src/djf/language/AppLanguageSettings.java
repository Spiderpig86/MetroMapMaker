package djf.language;

import djf.AppTemplate;

import static djf.AppPropertyType.DEFAULT_LANGUAGE;
import static djf.AppPropertyType.LANGUAGE_OPTIONS;
import static djf.AppPropertyType.LANGUAGE_DIALOG_TITLE;
import static djf.AppPropertyType.LANGUAGE_DIALOG_HEADER_TEXT;
import static djf.AppPropertyType.LANGUAGE_DIALOG_CONTENT_TEXT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import properties_manager.InvalidXMLFileFormatException;
import properties_manager.PropertiesManager;

/**
 * This class manages the language settings for the application.
 *
 * @author Richard McKenna
 * @author Ritwik Banerjee
 * @version 1.0
 */
public class AppLanguageSettings {
    // WE NEED THESE CONSTANTS JUST TO GET STARTED
    // LOADING SETTINGS FROM OUR XML FILES

    // THIS IS THE FILE FOR STORING THE DEFAULT LANGUAGE IN USE
    public static final String APP_LANGUAGE_FILE = "language_settings.txt";

    // XML PROPERTIES FILE WHERE ALL LANGUAGE-SPECIFIC TEXT CAN BE FOUND
    public static final String APP_PROPERTIES_FILE_NAME   = "app_properties.xml";
    public static final String LANGUAGE_PROPERTIES_PREFIX = "language_properties_";
    public static final String XML_EXT                    = ".xml";

    // XML SCHEMA FOR VALIDATING THE XML PROPERTIES FILE
    public static final String PROPERTIES_SCHEMA_FILE_NAME = "properties_schema.xsd";

    // JSON FILE FOR LOOKING UP LANGUAGE ABBREVIATIONS
    public static final String LANGUAGES_LIST_FILE_NAME = "language_codes.json";
    public static final String JSON_LANGUAGES           = "languages";
    public static final String JSON_NAME                = "name";
    public static final String JSON_ABBREVIATION        = "code";

    // PROTOCOLS AND PATHS NEEDED FOR LOADING CERTAIN FILES
    public static final String FILE_PROTOCOL = "file:";
    public static final String PATH_DATA     = "MetroMapMaker/data/";   //
    // revert to ./data for NetBeans
    public static final String PATH_WORK     = "MetroMapMaker/work/";   // revert to ./work for NetBeans
    public static final String PATH_IMAGES   = "MetroMapMaker/images/"; // revert to ./images for NetBeans
    public static final String PATH_EXPORT = "MetroMapMaker/export/";

    // ERROR MESSAGE ASSOCIATED WITH PROPERTIES FILE LOADING ERRORS. WE CAN'T LOAD THIS FROM THE XML FILE IS THAT WE
    // DISPLAY IT WHEN THE LOADING OF THAT FILE FAILS
    public static String PROPERTIES_FILE_ERROR_MESSAGE = "Error Loading " + APP_PROPERTIES_FILE_NAME;

    // ERROR DIALOG CONTROL
    @SuppressWarnings("unused")
    public static String CLOSE_BUTTON_LABEL = "Close";

    // LANGUAGE SETTINGS
    private String                  currentLanguage;
    private HashMap<String, String> languageCodes;
    private ArrayList<String>       languages;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private AppTemplate app;

    // THESE ARE THE CONTROLS THAT HAVE TO BE UPDATED EACH
    // TIME THE APP LANGUAGE CHANGES
    protected HashMap<String, Labeled> labeledControls;

    /**
     * Constructor, it keeps the app for later.
     */
    public AppLanguageSettings(AppTemplate initApp) {
        app = initApp;
        labeledControls = new HashMap<>();
    }

    public void addLabeledControl(Object key, Labeled control) {
        labeledControls.put(key.toString(), control);
    }

    public void setLanguage(String initLanguage) throws LanguageException {
        currentLanguage = initLanguage;
        PropertiesManager props                 = PropertiesManager.getPropertiesManager();
        String            languagePropsFilePath = getLanguagePropertiesFilePath();

        try {
            props.loadProperties(languagePropsFilePath, PROPERTIES_SCHEMA_FILE_NAME);
            resetLanguage();
        } catch (InvalidXMLFileFormatException ixffe) {
            throw new LanguageException("Error loading " + languagePropsFilePath);
        }
    }

    public String getLanguagePropertiesFilePath() {
        String post = languageCodes.get(currentLanguage) + XML_EXT;
        return LANGUAGE_PROPERTIES_PREFIX + post;
    }

    /**
     * This method initializes the languages for use.
     *
     * @throws LanguageException This method reflects a LanguageException whenever there is a error setting up the
     *                           languages.
     */
    public void init() throws LanguageException {
        try {
            PropertiesManager props         = PropertiesManager.getPropertiesManager();
            ArrayList<String> languageNames = props.getPropertyOptionsList(LANGUAGE_OPTIONS);
            addLanguages(languageNames);

            // NOW SETUP THE LANGUAGE TO ACTUALLY START OFF USING
            loadDefaultLanguage();
        } catch (Exception e) {
            throw new LanguageException("Unable to Load Language Settings");
        }
    }

    private void addLanguages(ArrayList<String> languageNames) throws IOException {
        // READ THE FILE WITH THE LIST OF LANGUAGES
        InputStream is         = new FileInputStream(PATH_DATA + LANGUAGES_LIST_FILE_NAME);
        JsonReader  jsonReader = Json.createReader(is);
        JsonObject  json       = jsonReader.readObject();
        jsonReader.close();
        is.close();

        // GET THE LANGUAGES ARRAY
        JsonArray languagesArrayJSON = json.getJsonArray(JSON_LANGUAGES);
        languageCodes = new HashMap<>();
        for (int i = 0; i < languagesArrayJSON.size(); i++) {
            JsonObject jsonLanguage = languagesArrayJSON.getJsonObject(i);
            String     name         = jsonLanguage.getString(JSON_NAME);
            String     abbreviation = jsonLanguage.getString(JSON_ABBREVIATION);
            languageCodes.put(name, abbreviation);
        }

        // NOW LET'S GET THE ABBREVIATIONS FOR THE LANGUAGES
        languages = new ArrayList<>();
        for (String languageName : languageNames) {
            String languageAbbreviation = languageCodes.get(languageName);
            if (languageAbbreviation != null)
                languages.add(languageName);
        }
    }

    private void loadDefaultLanguage() throws Exception {
        File languageFile = new File(APP_LANGUAGE_FILE);
        if (languageFile.exists()) {
            FileReader     fr = new FileReader(languageFile);
            BufferedReader br = new BufferedReader(fr);
            currentLanguage = br.readLine();
            br.close();
            setLanguage(currentLanguage);
        } else {
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            currentLanguage = props.getProperty(DEFAULT_LANGUAGE);
            promptForLanguage();
            PrintWriter pw = new PrintWriter(languageFile);
            pw.println(currentLanguage);
            pw.flush();
            pw.close();
        }
    }

    public void promptForLanguage() throws LanguageException {
        PropertiesManager    props          = PropertiesManager.getPropertiesManager();
        ChoiceDialog<String> languageDialog = new ChoiceDialog<>(currentLanguage, languages);
        String               title          = props.getProperty(LANGUAGE_DIALOG_TITLE);
        String               headerText     = props.getProperty(LANGUAGE_DIALOG_HEADER_TEXT);
        String               contentText    = props.getProperty(LANGUAGE_DIALOG_CONTENT_TEXT);
        languageDialog.setTitle(title);
        languageDialog.setHeaderText(headerText);
        languageDialog.setContentText(contentText);
        languageDialog.showAndWait();
        String selectedLanguage = languageDialog.getSelectedItem();
        if (selectedLanguage != null) {
            setLanguage(selectedLanguage);
        }
    }

    public void resetLanguage() {
        // RELOAD ALL DJF CONTROLS WITH SELECTED LANGUAGE
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        for (String nodeKey : labeledControls.keySet()) {
            Labeled control         = labeledControls.get(nodeKey);
            String  tooltipPropName = nodeKey + "_TOOLTIP";
            String  tooltipProp     = props.getProperty(tooltipPropName);
            String  textPropName    = nodeKey + "_TEXT";
            String  textProp        = props.getProperty(textPropName);
            control.setTooltip(new Tooltip(tooltipProp));
            control.setText(textProp);
        }
    }
}