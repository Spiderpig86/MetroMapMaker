package mmm.gui;

import djf.AppPropertyType;
import djf.AppTemplate;

import djf.ui.AppDialogs;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static djf.AppPropertyType.*;
import static djf.language.AppLanguageSettings.FILE_PROTOCOL;
import static djf.language.AppLanguageSettings.PATH_IMAGES;
import static mmm.MetroLanguageProperty.*;
import static mmm.MetroLanguageProperty.APP_LOGO;
import static mmm.css.MetroStyle.*;

import mmm.css.MetroStyle;
import mmm.data.MetroData;
import mmm.file.MetroFiles;
import properties_manager.PropertiesManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class WelcomeDialog {

    private static WelcomeDialog dialog;
    private static AppTemplate app;
    private static MetroWorkspace workspace; // Let's us get the language strings
    private static Stage stage; // Access to the window

    // UI Controls
    private Button createNewButton;
    private VBox leftPane;
    private VBox recentItemsList;
    private GridPane centerPane;

    private WelcomeDialog() { }

    public static WelcomeDialog getInstance() {
        return (dialog == null) ? (dialog = new WelcomeDialog()) : dialog;
    }

    public void showDialog(AppTemplate app) {
        this.app = app;
        this.workspace = (MetroWorkspace) app.getWorkspaceComponent();
        PropertiesManager props = workspace.getPropertiesManager();

        // Construct the dialog
        stage = new Stage();
        stage.setTitle(props.getProperty(WELCOME_DIALOG_TITLE));

        // Build the dialog UI
        Scene scene = buildLayout(props);
        bindEvents();

        // Populate list of recently opened projects
        loadRecentProjects(props);

        // Add the CSS styling
        MetroStyle.initStyle(app, scene);
        // SET THE APP ICON
        String appIcon = FILE_PROTOCOL + PATH_IMAGES + props.getProperty(AppPropertyType.APP_LOGO);
        stage.getIcons().add(new Image(appIcon));
        stage.setScene(scene);
        stage.setResizable(false); // Remove maximize button
        stage.showAndWait();
    }

    private VBox constructRecentElement(String projectName, String projectPath) {
        // Construct a pane that represents a recent work element for the vbox
        VBox recentProjectItem = new VBox();
        recentProjectItem.setPadding(new Insets(10, 20, 10, 20));
        recentProjectItem.getStyleClass().add("recent-item");
        Label nameLabel = new Label(projectName);
        nameLabel.getStyleClass().add("recent-item-title");
        Label dirLabel = new Label(projectPath);
        dirLabel.getStyleClass().add("recent-item-subtitle");

        // Bind click event
        recentProjectItem.setOnMouseClicked(e -> {
            MetroFiles files = (MetroFiles) app.getFileComponent();
            loadProject(files, projectPath);
            // Update the recents file
            files.saveRecentJSON();
            // Close the welcome dialog
            stage.close();
        });

        recentProjectItem.getChildren().addAll(nameLabel, dirLabel);
        return recentProjectItem;
    }


    private Scene buildLayout(PropertiesManager props) {
        // Create the base pane of the scene
        BorderPane borderPane = new BorderPane();
        leftPane = new VBox(10); // Contains a label and recentItemsList
        Label recentsLabel = new Label(props.getProperty(WELCOME_RECENT_WORK));
        recentsLabel.setPadding(new Insets(20));
        recentsLabel.getStyleClass().add(CLASS_WELCOME_RECENT_LABEL);
        recentsLabel.setGraphic(new ImageView(FILE_PROTOCOL + PATH_IMAGES +
                props.getProperty(WELCOME_RECENT_ICON)));

        recentItemsList = new VBox(10);

        // Fill recent items list

        // Add the label and list to the leftPane
        leftPane.getChildren().addAll(recentsLabel, recentItemsList);
        leftPane.getStyleClass().add("welcome-recent-pane");

        // Create the center pane
        centerPane = new GridPane();
        centerPane.getStyleClass().add("welcome-center");
        centerPane.setAlignment(Pos.CENTER);
        VBox startPane = new VBox(50);
        startPane.getStyleClass().add("center");

        // Get the logo
        Image logo = new Image(FILE_PROTOCOL + PATH_IMAGES + props.getProperty(APP_LOGO));
        ImageView logoContainer = new ImageView(logo);
        logoContainer.setFitHeight(200);
        logoContainer.setFitWidth(200);

        // Build the welcome label
        Label welcomeLabel = new Label(props.getProperty(WELCOME_DIALOG_TITLE));
        welcomeLabel.getStyleClass().add(CLASS_WELCOME_LABEL);

        // Create our "label", which is a button since label does not have a
        // click event
        createNewButton = new Button(props.getProperty
                (WELCOME_CREATE_NEW).toUpperCase());
        createNewButton.getStyleClass().add(CLASS_WELCOME_BUTTON);
        createNewButton.setPadding(new Insets(20));
        createNewButton.setGraphic(new ImageView(FILE_PROTOCOL + PATH_IMAGES
                + props.getProperty(WELCOME_NEW_ICON)));

        startPane.getChildren().addAll(logoContainer, welcomeLabel,
                createNewButton);
        centerPane.getChildren().add(startPane);

        // Set the layouts of the borderpane
        borderPane.setLeft(leftPane);
        borderPane.setCenter(centerPane);

        // Create a new scene out of base layout 1000 x 650
        return new Scene(borderPane, 1000, 650);
    }

    private void bindEvents() {
        // Bind new button action
        createNewButton.setOnAction(e -> {
            // Open a dialog to change the text
            // Create a new dialog for user to enter the text

            TextInputDialog dialog = new TextInputDialog();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Enter Project Name");
            dialog.setContentText("Enter new project name:");
            // Add event filter to ok button
            final Button okButton = (Button) dialog.getDialogPane()
                    .lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, ae -> {
                // Return if cancelled
                if (dialog.getEditor().getText().length() == 0) {
                    ae.consume(); // Keep the dialog open
                    AppDialogs.showMessageDialog(stage, "Error", "Unable " +
                            "to create project directory. Please try again " +
                            "with a valid name.");
                    return;
                }

                // Use MetroFiles to create new directory and project file
                MetroFiles files = (MetroFiles) app.getFileComponent();
                String path = files.createProjectDirectory(dialog.getEditor()
                        .getText(), false);

                // Load the file when successful
                if (path.length() > 0) {
                    loadProject(files, path); // Load the project data, will
                    // be added to list automatically
                    app.getGUI().getFileController().markFileAsNotSaved();
                    // Close the welcome dialog
                    // AND MAKE SURE THE FILE BUTTONS ARE PROPERLY ENABLED
                    app.getGUI().getFileController().markAsEdited(app.getGUI());
                    // New file not saved
                    stage.close();
                } else {
                    ae.consume();
                    // Could not create the new folder
                    AppDialogs.showMessageDialog(stage, "Error", "Unable " +
                            "to create project directory. Please try again " +
                            "with a valid name.");
                }
            });
            Optional<String> result = dialog.showAndWait();

        });
    }

    /**
     * Populate the recent projects list with recently opened projects
     * @param props
     *      The PropertiesManager to retrieve app constants
     */
    private void loadRecentProjects(PropertiesManager props) {
        // Get the list of recently opened projects and add it to list
        MetroFiles files = (MetroFiles) app.getFileComponent();
        Map<String, String> recentProjects = files.getRecentProjects();
        if (recentProjects.size() > 0)
            for (String projectName: recentProjects.keySet())
                recentItemsList.getChildren().add(constructRecentElement(projectName,
                        recentProjects.get(projectName).split("@")[0]));
        else {
            // There are no recent projects, add some placeholder
            Label emptyLabel = new Label(props.getProperty(WELCOME_RECENT_EMPTY));
            emptyLabel.setStyle("-fx-text-fill: #326B49; -fx-font-size: 16;");
            emptyLabel.setPadding(new Insets(20));
            emptyLabel.setGraphic(new ImageView(FILE_PROTOCOL + PATH_IMAGES
                    + props.getProperty(WELCOME_RECENT_EMPTY_ICON)));

            recentItemsList.getChildren().add(emptyLabel);
            recentItemsList.setAlignment(Pos.CENTER_LEFT);
        }
    }

    private void loadProject(MetroFiles files, String path) {
        try {
            MetroWorkspace workspace = (MetroWorkspace) app
                    .getWorkspaceComponent();
            workspace.resetWorkspace();
            MetroData data = (MetroData) app.getDataComponent();
            data.resetData(); // Reset the data
            files.loadData(app.getDataComponent(), path);
            app.getGUI().getFileController().updateCurrentSaveFile(new File
                    (path));

            // Exit dialog and show workspace
            app.getWorkspaceComponent().activateWorkspace(app.getGUI().getAppPane());
            app.getGUI().updateToolbarControls(true);
        } catch (IOException e1) {
            PropertiesManager props = workspace.getPropertiesManager();
            AppDialogs.showStackTraceDialog(app.getStage(), e1,
                    props.getProperty(APP_ERROR_TITLE),
                    props.getProperty(APP_ERROR_CONTENT));
        }
    }
}
