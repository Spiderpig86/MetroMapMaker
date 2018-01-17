package djf.ui;

import djf.AppTemplate;
import djf.language.LanguageException;

import static djf.AppPropertyType.ABOUT_CONTENT;
import static djf.AppPropertyType.ABOUT_TITLE;

public class SettingsController {
    private AppTemplate app;

    public SettingsController(AppTemplate initApp) {
        app = initApp;
    }

    public void processLanguageRequest() {
        try {
            app.getLanguageSettings().promptForLanguage();
            app.getLanguageSettings().resetLanguage();
        } catch (LanguageException le) {
            System.out.println("Error Loading Laguage into UI");
        }
    }

    public void processHelpRequest() {
        // GET THE PATH OF THE HTML DOCUMENT THAT
        // CONTAINS THE APPLICATION HELP AND DISPLAY IT

    }
}
