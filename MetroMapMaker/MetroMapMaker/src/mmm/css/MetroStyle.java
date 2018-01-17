package mmm.css;

import djf.AppTemplate;
import javafx.scene.Scene;
import mmm.gui.MetroWorkspace;

import java.net.URL;

import static djf.AppPropertyType.APP_CSS;
import static djf.AppPropertyType.APP_PATH_CSS;

/**
 * This class is designed to provide all the styling for the main application
 * user interface including control types and CSS files.
 */
public class MetroStyle {
    public static final String CLASS_MAX_PANE = "max_pane";
    public static final String CLASS_RENDER_CANVAS = "render_canvas";
    public static final String CLASS_BUTTON = "button";
    public static final String CLASS_EDIT_TOOLBAR = "edit_toolbar";
    public static final String CLASS_EDIT_TOOLBAR_ROW = "edit_toolbar_row";
    public static final String CLASS_SIDEBAR_LABEL = "edit_toolbar_label";
    public static final String CLASS_SIDEBAR_BUTTON = "edit_toolbar_button";
    public static final String CLASS_COLOR_CHOOSER_PANE = "color_chooser_pane";
    public static final String CLASS_COLOR_CHOOSER_CONTROL = "color_chooser_control";
    public static final String CLASS_WELCOME_RECENT_LABEL =
            "welcome-recent-label";
    public static final String CLASS_WELCOME_LABEL = "welcome-label";
    public static final String CLASS_WELCOME_BUTTON = "welcome-create-btn";
    public static final String EMPTY_TEXT = "";
    public static final int BUTTON_TAG_WIDTH = 75;

    public static void initStyle(AppTemplate app, Scene
            primaryScene) {
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent
                ();
        String            stylesheet = workspace.getPropertiesManager().getProperty(APP_PATH_CSS);
        stylesheet += workspace.getPropertiesManager().getProperty(APP_CSS);
        Class  appClass       = app.getClass();
        URL stylesheetURL  = appClass.getResource(stylesheet);
        String stylesheetPath = stylesheetURL.toExternalForm();
        primaryScene.getStylesheets().add(stylesheetPath);
    }
}
