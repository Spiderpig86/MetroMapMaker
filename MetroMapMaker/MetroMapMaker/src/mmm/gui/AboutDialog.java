package mmm.gui;

import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;

import djf.AppPropertyType;
import djf.AppTemplate;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Import language statics
import static mmm.MetroLanguageProperty.APP_TITLE;
import static mmm.MetroLanguageProperty.APP_LOGO;
import static mmm.MetroLanguageProperty.ABOUT_TOOLTIP;
import static mmm.MetroLanguageProperty.ABOUT_CREDITS;
import static mmm.MetroLanguageProperty.ABOUT_COPYRIGHT;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import mmm.css.MetroStyle;
import properties_manager.PropertiesManager;

/**
 * About dialog for the goLogoLo application.
 * @author Stanley Lim
 */
public class AboutDialog {

    private static AboutDialog dialog;

    private AboutDialog() {}

    /**
     * Return a new instance of the about dialog if it did not exist before
     * @return
     */
    public static AboutDialog getInstance() { return (dialog == null ?
            (dialog = new AboutDialog()) : dialog); }

    public void showDialog(AppTemplate app) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // Construct a new stage
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.WINDOW_MODAL);
        window.setTitle("About");

        // Create our base layout
        VBox vLayout = new VBox(10);
        vLayout.setAlignment(Pos.CENTER);
        vLayout.setPadding(new Insets(10, 50, 10, 50));
        Image logo = new Image(FILE_PROTOCOL + PATH_IMAGES + props.getProperty(APP_LOGO));
        ImageView logoContainer = new ImageView(logo);
        logoContainer.setFitHeight(150);
        logoContainer.setFitWidth(150);

        // Now create our app title labels
        Label appTitle = new Label(props.getProperty(APP_TITLE));
        appTitle.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        Label appCred = new Label(props.getProperty(ABOUT_CREDITS));
        appCred.setTextAlignment(TextAlignment.CENTER);
        Label frameworks = new Label("Frameworks used: \n Desktop Java " +
                "Framework\n PropertiesManager\n jTPS");
        frameworks.setTextAlignment(TextAlignment.CENTER);
        appCred.setWrapText(true);
        Label appCopyright = new Label(props.getProperty(ABOUT_COPYRIGHT));
        appCopyright.setTextAlignment(TextAlignment.CENTER);

        // Add controls to our layout
        vLayout.getChildren().addAll(logoContainer, appTitle, frameworks,
                appCred, appCopyright);

        // Add layout to scene
        Scene scene = new Scene(vLayout, 500, 400);

        // Set the scene style
        MetroStyle.initStyle(app, scene);

        // SET THE APP ICON
        String appIcon = FILE_PROTOCOL + PATH_IMAGES + props.getProperty(AppPropertyType.APP_LOGO);
        window.getIcons().add(new Image(appIcon));

        window.setScene(scene);
        window.initModality(Modality.APPLICATION_MODAL);
        window.showAndWait();
    }
}
