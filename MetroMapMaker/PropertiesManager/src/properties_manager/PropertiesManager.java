package properties_manager;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class is used for loading properties from XML files that can then be used throughout an application. Note that
 * this class is a singleton, and so can be accessed from anywhere. To get the singleton properties manager, just use
 * the static accessor:
 * <p>
 * PropertiesManager props = PropertiesManager.getPropertiesManager();
 * <p>
 * Now you can access all the properties it currently stores using the getProperty method. Note that the
 * properties_schema.xsd file specifies how these files are to be constructed. Also note that it is designed to be used
 * with enumerations such that properties are sent in as objects and keyed using their toStrings.
 *
 * @author THE McKilla Gorilla
 * @version 1.0
 */
public class PropertiesManager {
    // THIS CLASS IS A SINGLETON, AND HERE IS THE ONLY OBJECT
    private static PropertiesManager singleton = null;

    // WE'LL STORE PROPERTIES HERE
    private HashMap<String, String> properties;

    // LISTS OF PROPERTY OPTIONS CAN BE STORED HERE
    private HashMap<String, ArrayList<String>> propertyOptionsLists;

    // THIS WILL LOAD THE XML FOR US
    private XMLUtilities xmlUtil;

    // THESE CONSTANTS ARE USED FOR LOADING PROPERTIES AS THEY ARE
    // THE ESSENTIAL ELEMENTS AND ATTRIBUTES
    public static final String PROPERTY_ELEMENT              = "property";
    public static final String PROPERTY_LIST_ELEMENT         = "property_list";
    public static final String PROPERTY_OPTIONS_LIST_ELEMENT = "property_options_list";
    public static final String PROPERTY_OPTIONS_ELEMENT      = "property_options";
    public static final String OPTION_ELEMENT                = "option";
    public static final String NAME_ATT                      = "name";
    public static final String VALUE_ATT                     = "value";
    public static final String DATA_PATH_PROPERTY            = "DATA_PATH";

    @SuppressWarnings("unused")
    public static final String ABBR_ATT = "abbr";

    /**
     * The constructor is private because this is a singleton.
     */
    private PropertiesManager() {
        properties = new HashMap<>();
        propertyOptionsLists = new HashMap<>();
        xmlUtil = new XMLUtilities();
    }

    /**
     * This is the static accessor for the singleton.
     *
     * @return The singleton properties manager object.
     */
    public static PropertiesManager getPropertiesManager() {
        // IF IT'S NEVER BEEN RETRIEVED BEFORE THEN
        // FIRST WE MUST CONSTRUCT IT
        if (singleton == null) {
            // WE CAN CALL THE PRIVATE CONSTRCTOR FROM WITHIN THE CLASS
            singleton = new PropertiesManager();
        }
        // RETURN THE SINGLETON
        return singleton;
    }

    /**
     * This function adds the (property, value) tuple to the properties manager.
     *
     * @param property Key, i.e. property type for this pair.
     * @param value    The data for this pair.
     */
    public void addProperty(Object property, String value) {
        properties.put(property.toString(), value);
    }

    /**
     * Accessor method for getting a property from this manager.
     *
     * @param property The key for getting a property.
     * @return The value associated with the key.
     */
    public String getProperty(Object property) {
        return properties.get(property.toString());
    }

    /**
     * Accessor method for testing to see if a particular property has been loaded.
     *
     * @param property The key for getting a property.
     * @return true if property has been loaded, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean hasProperty(Object property) {
        return properties.containsKey(property.toString());
    }

    /**
     * Accessor method for getting a property options list associated with the property key.
     *
     * @param property The key for accessing the property options list.
     * @return The property options list associated with the key.
     */
    public ArrayList<String> getPropertyOptionsList(Object property) {
        return propertyOptionsLists.get(property.toString());
    }

    /**
     * This function loads the xmlDataFile in this property manager, first make sure it's a well formed document
     * according to the rules specified in the xmlSchemaFile.
     *
     * @param xmlDataFile   XML document to load.
     * @param xmlSchemaFile Schema that the XML document should conform to.
     * @throws InvalidXMLFileFormatException This is thrown if the XML file is invalid.
     */
    public void loadProperties(String xmlDataFile, String xmlSchemaFile) throws InvalidXMLFileFormatException {

        String dataPath = getProperty(DATA_PATH_PROPERTY);

        // ADD THE DATA PATH
        xmlDataFile = dataPath + xmlDataFile;
        xmlSchemaFile = dataPath + xmlSchemaFile;

        // FIRST LOAD THE FILE
        Document doc = xmlUtil.loadXMLDocument(xmlDataFile, xmlSchemaFile);

        // NOW LOAD ALL THE PROPERTIES
        Node            propertyListNode = xmlUtil.getNodeWithName(doc, PROPERTY_LIST_ELEMENT);
        ArrayList<Node> propNodes        = xmlUtil.getChildNodesWithName(propertyListNode, PROPERTY_ELEMENT);
        for (Node n : propNodes) {
            NamedNodeMap attributes = n.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                String attName  = attributes.getNamedItem(NAME_ATT).getTextContent();
                String attValue = attributes.getNamedItem(VALUE_ATT).getTextContent();
                properties.put(attName, attValue);
            }
        }

        // AND THE PROPERTIES FROM OPTION LISTS
        Node propertyOptionsListNode = xmlUtil.getNodeWithName(doc, PROPERTY_OPTIONS_LIST_ELEMENT);
        if (propertyOptionsListNode != null) {
            ArrayList<Node> propertyOptionsNodes = xmlUtil.getChildNodesWithName(propertyOptionsListNode,
                    PROPERTY_OPTIONS_ELEMENT);
            for (Node n : propertyOptionsNodes) {
                NamedNodeMap      attributes = n.getAttributes();
                String            name       = attributes.getNamedItem(NAME_ATT).getNodeValue();
                ArrayList<String> options    = new ArrayList<>();
                propertyOptionsLists.put(name, options);
                ArrayList<Node> optionsNodes = xmlUtil.getChildNodesWithName(n, OPTION_ELEMENT);
                for (Node oNode : optionsNodes) {
                    String option = oNode.getTextContent();
                    options.add(option);
                }
            }
        }
    }
}