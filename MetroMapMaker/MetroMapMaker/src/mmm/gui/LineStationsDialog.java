package mmm.gui;

import djf.AppPropertyType;
import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mmm.controls.MetroLine;
import mmm.controls.MetroLineEnd;
import mmm.css.MetroStyle;
import mmm.data.LineConfig;
import properties_manager.PropertiesManager;

import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;
import static mmm.MetroLanguageProperty.*;

public class LineStationsDialog {
    public static void showDialog(AppTemplate app, MetroLine line) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();

        // Construct a new stage
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.WINDOW_MODAL);
        window.setTitle(props.getProperty(STOPS_TITLE));

        // Create our base layout
        VBox vLayout = new VBox(10);
        vLayout.setAlignment(Pos.CENTER);
        vLayout.setPadding(new Insets(10, 50, 10, 50));

        // Create the controls
        Label dialogLabel = new Label(line.getLineName() + props
                .getProperty(STOPS_LABEL));
        ListView<String> stationsList = new ListView<>();
        line.getLineStations().forEach(s -> {
            if (!(s.getStation() instanceof MetroLineEnd))
                stationsList.getItems().add(s.toString());
        });
        Button okButton = new Button(props.getProperty(BUTTON_OK));
        okButton.setOnAction(e -> window.close());
        okButton.setAlignment(Pos.CENTER_RIGHT);

        // Add elements to the pane
        vLayout.getChildren().addAll(dialogLabel, stationsList, okButton);

        // Add layout to scene
        Scene scene = new Scene(vLayout, 400, 300);

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
