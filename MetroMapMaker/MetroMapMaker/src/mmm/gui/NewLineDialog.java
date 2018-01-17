package mmm.gui;

import djf.AppPropertyType;
import djf.AppTemplate;
import djf.ui.AppDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import mmm.controls.MetroLine;
import mmm.css.MetroStyle;
import mmm.data.LineConfig;
import properties_manager.PropertiesManager;

import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;
import static mmm.MetroLanguageProperty.*;

public class NewLineDialog {
    public static LineConfig showDialog(AppTemplate app) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();

        // Construct a new stage
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.WINDOW_MODAL);
        window.setTitle("Add New Line");

        // Create our base layout
        VBox vLayout = new VBox(10);
        vLayout.setAlignment(Pos.CENTER);
        vLayout.setPadding(new Insets(10, 50, 10, 50));

        // Create the controls
        Label promptLabel = new Label("Enter line name:");
        TextField textField = new TextField();
        Label colorLabel = new Label("Select a color for your line:");
        ColorPicker colorPicker = new ColorPicker();
        Button btnCancel = new Button(props.getProperty(BUTTON_CANCEL));
        Button btnConfirm = new Button(props.getProperty(BUTTON_OK));

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(btnConfirm, btnCancel);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        btnCancel.setOnAction(e -> {
            textField.setText("");
            colorPicker.setValue(null);
            window.close();
        });

        btnConfirm.setOnAction(e -> {
            if (textField.getText().length() > 0 && colorPicker.getValue() !=
                    null && !containsLine(textField.getText(), workspace))
                window.close();
            else
                AppDialogs.showMessageDialog(app.getStage(), "Invalid Line " +
                        "Properties", "Please enter a valid line name and " +
                        "valid line color.");
        });

        vLayout.getChildren().addAll(promptLabel, textField, colorLabel,
                colorPicker, buttonContainer);

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

        if (textField.getText().length() > 0 && colorPicker.getValue() != null)
            return new LineConfig(textField.getText(), colorPicker.getValue());
        else
            return null;
    }

    private static boolean containsLine(String name, MetroWorkspace workspace) {
        for (MetroLine line: workspace.getLineCombo().getItems())
            if (line.getLineName().equals(name))
                return true;
        return false;
    }
}
