package properties_manager;

/**
 * The InvalidXMLFileFormatException is a checked exception that represents the occasion where an XML document does not
 * validate against its schema (XSD).
 *
 * @author THE McKilla Gorilla
 * @version 1.0
 */
public class InvalidXMLFileFormatException extends Exception {
    // NAME OF XML FILE THAT DID NOT VALIDATE
    private String xmlFileWithError;

    // NAME OF XML SCHEMA USED FOR VALIDATION
    private String xsdFile;

    /**
     * Constructor for this exception, these are simple objects, we'll just store some info about the error.
     *
     * @param initXMLFileWithError XML doc file name that didn't validate
     * @param initXSDFile          XML schema file used in validation
     */
    public InvalidXMLFileFormatException(String initXMLFileWithError, String initXSDFile) {
        // KEEP IT FOR LATER
        xmlFileWithError = initXMLFileWithError;
        xsdFile = initXSDFile;
    }

    /**
     * Constructor that records which xml file produced the error, but not the schema.
     *
     * @param initXMLFileWithError the XML file that produced the error
     */
    public InvalidXMLFileFormatException(String initXMLFileWithError) {
        xmlFileWithError = initXMLFileWithError;
    }

    /**
     * This method builds and returns a textual description of this object, which basically summarizes what went wrong.
     *
     * @return This message will be useful for describing where validation failed.
     */
    @Override
    public String toString() {
        return "XML Document (" + xmlFileWithError + ") does not conform to Schema (" + xsdFile + ")";
    }
}