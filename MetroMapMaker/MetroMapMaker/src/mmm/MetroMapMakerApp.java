package mmm;

import djf.AppTemplate;
import mmm.data.MetroData;
import mmm.file.MetroFiles;
import mmm.gui.MetroWorkspace;
import mmm.gui.WelcomeDialog;
import properties_manager.PropertiesManager;

import java.util.Locale;

/**
 * The main entry point for Metro Map Maker
 */
public class MetroMapMakerApp extends AppTemplate {

    PropertiesManager props;

    /**
     * This hook method must initialize all three components in the proper order ensuring proper dependencies are
     * respected, meaning all proper objects are already constructed when they are needed for use, since some may need
     * others for initialization.
     */
    @Override
    public void buildAppComponentsHook() {
        // CONSTRUCT ALL THREE COMPONENTS. NOTE THAT FOR THIS APP
        // THE WORKSPACE NEEDS THE DATA COMPONENT TO EXIST ALREADY
        // WHEN IT IS CONSTRUCTED, AND THE DATA COMPONENT NEEDS THE
        // FILE COMPONENT SO WE MUST BE CAREFUL OF THE ORDER
        fileComponent = new MetroFiles(this);
        dataComponent = new MetroData(this);
        workspaceComponent = new MetroWorkspace(this);

        // Get the properties manager form workspace
        props = ((MetroWorkspace) workspaceComponent).getPropertiesManager();

        // Process intermediate dialogs
        showIntermediateDialogs();
    }

    /**
     * This is where program execution begins. Since this is a JavaFX app it will simply call launch, which gets JavaFX
     * rolling, resulting in sending the properly initialized Stage (i.e. window) to the start method inherited from
     * AppTemplate, defined in the Desktop Java Framework.
     */
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        launch(args);
    }

    @Override
    public void showIntermediateDialogs() {
        // Construct a welcome dialog to show
        WelcomeDialog dialog = WelcomeDialog.getInstance();
        dialog.showDialog(this);
    }
}
