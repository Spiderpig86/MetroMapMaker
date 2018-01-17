package djf.ui;

import static djf.AppPropertyType.*;

import djf.AppTemplate;

import static djf.language.AppLanguageSettings.PATH_WORK;
import static djf.language.AppLanguageSettings.PROPERTIES_FILE_ERROR_MESSAGE;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import djf.components.AppFileComponent;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import properties_manager.PropertiesManager;

public class FileController {
    // HERE'S THE APP
    AppTemplate app;

    // WE WANT TO KEEP TRACK OF WHEN SOMETHING HAS NOT BEEN SAVED
    boolean saved;

    // THIS IS THE FILE FOR THE WORK CURRENTLY BEING WORKED ON
    File currentWorkFile;

    String name = "";

    /**
     * This constructor just keeps the app for later.
     *
     * @param initApp The application within which this controller will provide file toolbar responses.
     */
    public FileController(AppTemplate initApp) {
        // NOTHING YET
        saved = true;
        app = initApp;
    }

    /**
     * This method starts the process of editing new Work. If work is already being edited, it will prompt the user to
     * save it first.
     */
    public void processNewRequest() {
        try {
            // WE MAY HAVE TO SAVE CURRENT WORK
            boolean continueToMakeNew = true;
            name = ""; // Reset the name (so that this works properly when
            // opening more projects
            if (!saved) {
                // THE USER CAN OPT OUT HERE WITH A CANCEL
                continueToMakeNew = promptToSave();
            }

            // Make sure the user enters a name for the new map
            TextInputDialog dialog = new TextInputDialog();
            dialog.initOwner(app.getStage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Enter Project Name");
            dialog.setContentText("Enter new project name:");
            // Add event filter to ok button
            final Button okButton = (Button) dialog.getDialogPane()
                    .lookupButton(ButtonType.OK);
            final Button cancelButton = (Button) dialog.getDialogPane()
                    .lookupButton(ButtonType.CANCEL);

            // Handle the ok button
            okButton.addEventFilter(ActionEvent.ACTION, ae -> {
                // Return if cancelled
                if (dialog.getEditor().getText().length() == 0) {
                    ae.consume(); // Keep the dialog open
                    AppDialogs.showMessageDialog(app.getStage(), "Error",
                            "Unable to create project directory. Please try again " +
                            "with a valid name.");
                    return;
                }

                // Use MetroFiles to create new directory and project file
                AppFileComponent files = app.getFileComponent();
                String path = files.createProjectDirectory(dialog.getEditor()
                        .getText(), false);

                // Load the file when successful
                if (path.length() == 0) {
                    // Could not create the new folder
                    ae.consume(); // Persist the dialog
                    AppDialogs.showMessageDialog(app.getStage(), "Error", "Unable " +
                            "to create project directory. Please try again " +
                            "with a valid name.");
                }

                // We can proceed with creating the new project directory
                this.setProjectName(dialog.getEditor().getText());

                app.getStage().setTitle("Metro Map Maker - " + dialog.getEditor().getText());
                // Indicate currently opened project in window title
            });

            // Handle the cancel button - reset the name and prevent the
            // workspace from loading
            cancelButton.addEventFilter(ActionEvent.ACTION, ae -> {
                this.setProjectName("(cancelled)"); // Set the status to
                // cancelled
            });

            Optional<String> result = dialog.showAndWait();

            // If the user cancelled, the input length is 0 and should not
            // continue
            if (name.length() == 0 || name.equals("(cancelled)"))
                continueToMakeNew = false;

            // IF THE USER REALLY WANTS TO MAKE A NEW COURSE
            if (continueToMakeNew) {

                // Create the project directory and file name (handled inside
                // MetroMapMaker)
                app.getFileComponent().createProjectDirectory(name, true);

                // RESET THE DATA
                app.getDataComponent().resetData();

                // NOW RELOAD THE WORKSPACE WITH THE RESET DATA
                app.getWorkspaceComponent().reloadWorkspace(app.getDataComponent());

                // MAKE SURE THE WORKSPACE IS ACTIVATED
                app.getWorkspaceComponent().activateWorkspace(app.getGUI().getAppPane());

                // WORK IS NOT SAVED
                saved = false;
                currentWorkFile = null;

                // REFRESH THE GUI, WHICH WILL ENABLE AND DISABLE THE APPROPRIATE CONTROLS
                AppGUI gui = app.getGUI();
                if (gui != null) {
                    gui.updateToolbarControls(saved);
                    // TELL THE USER NEW WORK IS UNDERWAY
                    AppDialogs.showMessageDialog(gui.getWindow(), NEW_SUCCESS_TITLE, NEW_SUCCESS_CONTENT);
                }
            }
        } catch (IOException ioe) {
            // SOMETHING WENT WRONG, PROVIDE FEEDBACK
            AppDialogs.showMessageDialog(app.getGUI().getWindow(), NEW_ERROR_TITLE, NEW_ERROR_CONTENT);
        }
    }

    private void setProjectName(String name) {
        this.name = name;
    }

    /**
     * This method lets the user open a Course saved to a file. It will also make sure data for the current Course is
     * not lost.
     */
    public void processLoadRequest() {
        try {
            // WE MAY HAVE TO SAVE CURRENT WORK

            boolean continueToOpen = true;
            if (!saved) {
                // THE USER CAN OPT OUT HERE WITH A CANCEL
                continueToOpen = promptToSave();
            }

            // IF THE USER REALLY WANTS TO OPEN A Course
            if (continueToOpen) {
                // GO AHEAD AND PROCEED LOADING A Course
                promptToOpen();
            }
        } catch (IOException ioe) {
            // SOMETHING WENT WRONG
            AppDialogs.showMessageDialog(app.getGUI().getWindow(), LOAD_ERROR_TITLE, LOAD_ERROR_CONTENT);
        }
    }

    public void processCloseRequest() {
        // @todo
    }

    /**
     * This method will save the current course to a file. Note that we already know the name of the file, so we won't
     * need to prompt the user.
     */
    public void processSaveRequest() {
        // WE'LL NEED THIS TO GET CUSTOM STUFF
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        try {
            // MAYBE WE ALREADY KNOW THE FILE
            if (currentWorkFile != null) {
                saveWork(currentWorkFile);
            }
            // OTHERWISE WE NEED TO PROMPT THE USER
            else {
                // PROMPT THE USER FOR A FILE NAME
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(new File(PATH_WORK));
                fc.setTitle(props.getProperty(SAVE_WORK_TITLE));
//                fc.getExtensionFilters().addAll(
//                        new FileChooser.ExtensionFilter(props.getProperty(WORK_FILE_EXT_DESC),
//                                                               props.getProperty(WORK_FILE_EXT)));

                File selectedFile = fc.showSaveDialog(app.getGUI().getWindow());
                if (selectedFile != null) {
                    saveWork(selectedFile);
                }
            }
        } catch (IOException ioe) {
            AppDialogs.showMessageDialog(app.getGUI().getWindow(), SAVE_ERROR_TITLE, SAVE_ERROR_CONTENT);
        }
    }

    public void processSaveAsRequest() {
        //@todo
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        try {
            // PROMPT THE USER FOR A FILE NAME
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File(PATH_WORK));
            fc.setTitle(props.getProperty(SAVE_WORK_TITLE));
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(props.getProperty(WORK_FILE_EXT_DESC),
                            props.getProperty(WORK_FILE_EXT)));

            File selectedFile = fc.showSaveDialog(app.getGUI().getWindow());
            if (selectedFile != null) {
                saveWork(selectedFile);
            }

        } catch (IOException ioe) {
            AppDialogs.showMessageDialog(app.getGUI().getWindow(), SAVE_ERROR_TITLE, SAVE_ERROR_CONTENT);
        }

//        TextInputDialog dialog = new TextInputDialog();
//        dialog.initOwner(app.getStage());
//        dialog.initModality(Modality.APPLICATION_MODAL);
//        dialog.setTitle("Save As");
//        dialog.setContentText("Enter new project name:");
//        // Add event filter to ok button
//        Optional<String> result = dialog.showAndWait();
//        if (result.equals(Optional.empty()))
//            return;
//        try {
//            app.getFileComponent().saveAsData(app.getDataComponent(), result.get());
//        } catch (IOException e) {
//            // TODO
//        }
    }

    public void processExportRequest() {
        // @todo
        try {
            app.getFileComponent().exportData(app.getDataComponent());
        } catch (IOException e) {
            AppDialogs.showMessageDialog(app.getStage(), e,
                    PROPERTIES_FILE_ERROR_MESSAGE);
        }
    }

    /**
     * This method will exit the application, making sure the user doesn't lose any data first.
     */
    public void processExitRequest() {
        try {
            // WE MAY HAVE TO SAVE CURRENT WORK
            boolean continueToExit = true;
            if (!saved) {
                // THE USER CAN OPT OUT HERE
                continueToExit = promptToSave();
            }

            // IF THE USER REALLY WANTS TO EXIT THE APP
            if (continueToExit) {
                // EXIT THE APPLICATION
                System.exit(0);
            }
        } catch (IOException ioe) {
            AppDialogs.showMessageDialog(app.getGUI().getWindow(), SAVE_ERROR_TITLE, SAVE_ERROR_CONTENT);
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. Note that it could be used in multiple contexts before doing other actions, like creating new work, or
     * opening another file. Note that the user will be presented with 3 options: YES, NO, and CANCEL. YES means the
     * user wants to save their work and continue the other action (we return true to denote this), NO means don't save
     * the work but continue with the other action (true is returned), CANCEL means don't save the work and don't
     * continue with the other action (false is returned).
     *
     * @return true if the user presses the YES option to save, true if the user presses the NO option to not save,
     *         false if the user presses the CANCEL option to not continue.
     */
    private boolean promptToSave() throws IOException {
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // CHECK TO SEE IF THE CURRENT WORK HAS
        // BEEN SAVED AT LEAST ONCE

        // PROMPT THE USER TO SAVE UNSAVED WORK
        ButtonType selection = AppDialogs.showYesNoCancelDialog(app.getGUI().getWindow(),
                SAVE_VERIFY_TITLE,
                SAVE_VERIFY_CONTENT);

        // IF THE USER SAID YES, THEN SAVE BEFORE MOVING ON
        if (selection == ButtonType.YES) {

            if (currentWorkFile == null) {
                // PROMPT THE USER FOR A FILE NAME
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(new File(PATH_WORK));
                fc.setTitle(props.getProperty(SAVE_WORK_TITLE));
                fc.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter(props.getProperty(WORK_FILE_EXT_DESC),
                                                               props.getProperty(WORK_FILE_EXT)));

                File selectedFile = fc.showSaveDialog(app.getGUI().getWindow());
                if (selectedFile != null) {
                    saveWork(selectedFile);
                    saved = true;
                }
            } else {
                saveWork(currentWorkFile);
                saved = true;
            }
        } // IF THE USER SAID CANCEL, THEN WE'LL TELL WHOEVER
        // CALLED THIS THAT THE USER IS NOT INTERESTED ANYMORE
        else if (selection == ButtonType.CANCEL) {
            return false;
        }

        // IF THE USER SAID NO, WE JUST GO ON WITHOUT SAVING
        // BUT FOR BOTH YES AND NO WE DO WHATEVER THE USER
        // HAD IN MIND IN THE FIRST PLACE
        return true;
    }

    /**
     * This helper method asks the user for a file to open. The user-selected file is then loaded and the GUI updated.
     * Note that if the user cancels the open process, nothing is done. If an error occurs loading the file, a message
     * is displayed, but nothing changes.
     */
    private void promptToOpen() {
        // WE'LL NEED TO GET CUSTOMIZED STUFF WITH THIS
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // AND NOW ASK THE USER FOR THE FILE TO OPEN
        FileChooser fc = new FileChooser();
        File dir = new File(PATH_WORK);
        if (!dir.exists()) {
            AppDialogs.showMessageDialog(app.getStage(), "Error", "Error " +
                    "loading works folder. Please check if directory exists.");
            return;
        }
        fc.setInitialDirectory(dir);
        fc.setTitle(props.getProperty(LOAD_WORK_TITLE));
        File selectedFile = fc.showOpenDialog(app.getGUI().getWindow());

        // ONLY OPEN A NEW FILE IF THE USER SAYS OK
        if (selectedFile != null) {
            try {
                // RESET THE WORKSPACE
                //app.getWorkspaceComponent().resetLanguage();

                // RESET THE DATA
                app.getDataComponent().resetData();

                // LOAD THE FILE INTO THE DATA
                app.getFileComponent().loadData(app.getDataComponent(), selectedFile.getAbsolutePath());

                // Update the current editing file
                currentWorkFile = selectedFile;

                // MAKE SURE THE WORKSPACE IS ACTIVATED
                app.getWorkspaceComponent().activateWorkspace(app.getGUI().getAppPane());

                // AND MAKE SURE THE FILE BUTTONS ARE PROPERLY ENABLED
                saved = true;
                app.getGUI().updateToolbarControls(saved);

            } catch (Exception e) {
                e.printStackTrace();
                AppDialogs.showMessageDialog(app.getGUI().getWindow(), LOAD_ERROR_TITLE, LOAD_ERROR_CONTENT);
            }
        }
    }

    // HELPER METHOD FOR SAVING WORK
    private void saveWork(File selectedFile) throws IOException {
        // SAVE IT TO A FILE
        app.getFileComponent().saveData(app.getDataComponent(), selectedFile.getPath());

        // MARK IT AS SAVED
        currentWorkFile = selectedFile;
        saved = true;

        // TELL THE USER THE FILE HAS BEEN SAVED
        AppDialogs.showMessageDialog(app.getGUI().getWindow(), SAVE_SUCCESS_TITLE, SAVE_SUCCESS_CONTENT);

        // AND REFRESH THE GUI, WHICH WILL ENABLE AND DISABLE
        // THE APPROPRIATE CONTROLS
        AppGUI gui = app.getGUI();
        gui.updateToolbarControls(saved);
    }

    /**
     * This method marks the appropriate variable such that we know that the current Work has been edited since it's
     * been saved. The UI is then updated to reflect this.
     *
     * @param gui The user interface editing the Work.
     */
    public void markAsEdited(AppGUI gui) {
        // THE WORK IS NOW DIRTY
        saved = false;

        // LET THE UI KNOW
        //noinspection ConstantConditions
        gui.updateToolbarControls(saved);
    }

    /**
     * This mutator method marks the file as not saved, which means that when the user wants to do a file-type
     * operation, we should prompt the user to save current work first. Note that this method should be called any time
     * the course is changed in some way.
     */
    @SuppressWarnings("unused")
    public void markFileAsNotSaved() {
        saved = false;
    }

    public void markFileAsSaved() { saved = true; }

    /**
     * Accessor method for checking to see if the current work has been saved since it was last edited.
     *
     * @return true if the current work is saved to the file, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean isSaved() {
        return saved;
    }

    public void updateCurrentSaveFile(File selectedFile) {
        this.currentWorkFile = selectedFile;
    }
}
