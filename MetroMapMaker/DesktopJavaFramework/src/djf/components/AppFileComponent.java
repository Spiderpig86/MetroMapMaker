package djf.components;

import java.io.IOException;

/**
 * This interface provides the structure for file components in
 * our applications. This lets us call save and load methods from
 * the framework.
 * 
 * @author Richard McKenna
 * @version 1.0
 */
public interface AppFileComponent {

    /**
     * This function must be overridden in the actual component and would
     * write app data to a file in the necessary format.
     */
    void saveData(AppDataComponent data, String filePath) throws IOException;

    /**
     * This function must be overridden in the actual component and would
     * read app data from a file in the necessary format.
     */
    void loadData(AppDataComponent data, String filePath) throws IOException;

    /**
     * This function must be overridden in the actual component and would
     * be used for exporting app data into another format.
     */
    @SuppressWarnings("unused")
    void exportData(AppDataComponent data) throws IOException;

    /**
     * This function must be overridden in the actual component and would
     * be used for importing app data from another format.
     */
    @SuppressWarnings("unused")
    void importData(AppDataComponent data, String filePath) throws IOException;

    /**
     * This function must be overridden in the actual component and is used
     * to create a project directory with an option to update the recently
     * opened projects list.
     * @param name
     *      The name of the project.
     * @param updateRecents
     *      Boolean to update the recently opened projects
     * @return
     *      Return the path to the main project file
     */
    String createProjectDirectory(String name, boolean updateRecents);

    void saveAsData(AppDataComponent data, String filePath) throws IOException;
}
