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
import mmm.controls.MetroStation;
import mmm.css.MetroStyle;
import mmm.data.LineConfig;
import mmm.data.MetroData;
import mmm.transactions.metroline.CircularTransaction;
import properties_manager.PropertiesManager;

import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;
import static mmm.MetroLanguageProperty.*;

public class EditLineDialog {

    private static TextField textField;
    private static ColorPicker colorPicker;
    private static boolean oldCircular = false;

    public static LineConfig showDialog(AppTemplate app, MetroLine line) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        MetroWorkspace workspace = (MetroWorkspace) app.getWorkspaceComponent();

        // Construct a new stage
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.WINDOW_MODAL);
        window.setTitle(props.getProperty(EDIT_LINE_TITLE));

        // Create our base layout
        VBox vLayout = new VBox(10);
        vLayout.setAlignment(Pos.CENTER);
        vLayout.setPadding(new Insets(10, 50, 10, 50));

        // Create the controls
        Label promptLabel = new Label(props.getProperty(EDIT_LINE_LABEL));
        textField = new TextField();
        Label colorLabel = new Label("Select a color for your line:");
        colorPicker = new ColorPicker();
        CheckBox isCircularCheck = new CheckBox("Circular Line");
        isCircularCheck.setSelected(line.isCircular());
        oldCircular = line.isCircular();

        Button btnCancel = new Button(props.getProperty(BUTTON_CANCEL));
        Button btnConfirm = new Button(props.getProperty(BUTTON_OK));

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(btnConfirm, btnCancel);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        // Bind event for circular checkbox and make this approve if there are
        isCircularCheck.setOnAction(e -> {
            if (line.getLineStations().size() < 3) {
                AppDialogs.showMessageDialog(app.getStage(), "Unable to Make" +
                        " Line Circular", "Please make sure that the line " +
                        "has at least 3 stations.");
                isCircularCheck.setSelected(false);
                return;
            }

        });

        btnCancel.setOnAction(e -> {
            textField.setText("");
            colorPicker.setValue(null);
            window.close();
        });

        btnConfirm.setOnAction(e -> {
            // Check if it has been modified yet
            if (oldCircular != isCircularCheck.isSelected()) {
                CircularTransaction transaction;
                // Update based on circular line property
                transaction = new CircularTransaction
                        (line, isCircularCheck.isSelected(), app, line
                                .getLineStart().getPoint(), line.getLineEnd()
                                .getPoint());
                app.getTPS().addTransaction(transaction);
            }
            app.getGUI().disableUndoRedo(app.getTPS().canUndo(), app.getTPS()
                    .canRedo());
            window.close();
        });

        vLayout.getChildren().addAll(promptLabel, textField, colorLabel,
                colorPicker, isCircularCheck, buttonContainer);

        // Load the line properties
        loadLineProperties(line);

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

    private static void loadLineProperties(MetroLine line) {
        textField.setText(line.getLineName());
        colorPicker.setValue(line.getLineColor());
    }

    private static boolean containsLine(String name, MetroWorkspace workspace) {
        for (MetroLine line: workspace.getLineCombo().getItems())
            if (line.getLineName().equals(name))
                return true;
        return false;
    }
}
