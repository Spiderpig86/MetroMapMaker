package properties_manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class provides general purpose utilities for the loading and extracting of data from XML Files. This class is
 * useful for the validation and loading of XML docs and for easy extraction of data from. Note that it to be a really
 * useful library it would need to be extended a bit, with some more similar functionality added.
 *
 * @author THE McKilla Gorilla
 * @author Ritwik Banerjee
 * @version 1.0
 */
public class XMLUtilities {
    // THIS REFERENCES A STANDARD SCHEMA FORMAT. NOTE THIS IS NOT THE SCHEMA
    public static final String SCHEMA_STANDARD_SPEC_URL = "http://www.w3.org/2001/XMLSchema";

    /**
     * Default Constructor, no data needs to be initialized.
     */
    public XMLUtilities() {}

    /**
     * This method validates the xmlDocNameAndPath doc against the xmlSchemaNameAndPath schema and returns true if
     * valid, false otherwise. Note that this is taken directly (with comments) from <a
     * href="http://www.ibm.com/developerworks/xml/library/x-javaxmlvalidapi/index.html">this example</a> on the IBM
     * site with only slight modifications.
     *
     * @param xmlDocNameAndPath    XML Doc to validate
     * @param xmlSchemaNameAndPath XML Schema to use in validation
     * @return true if the xml doc is validate, false if it does not.
     */
    public boolean validateXMLDoc(String xmlDocNameAndPath,
                                  String xmlSchemaNameAndPath) {
        try {
            // 1. Lookup a factory for the W3C XML Schema language
            SchemaFactory factory =
                    SchemaFactory.newInstance(SCHEMA_STANDARD_SPEC_URL);

            // 2. Compile the schema. 
            // Here the schema is loaded from a java.io.File, but you could use 
            // a java.net.URL or a javax.xml.transform.Source instead.
            File   schemaLocation = new File(xmlSchemaNameAndPath);
            Schema schema         = factory.newSchema(schemaLocation);

            // 3. Get a validator from the schema.
            Validator validator = schema.newValidator();

            // 4. Parse the document you want to check.
            Source source = new StreamSource(xmlDocNameAndPath);

            // 5. Check the document
            validator.validate(source);
            return true;
        }
        // FOR ANY EXCEPTION THAT OCCURS WE'LL BLAME
        // IT ON AN INVALID XML FILE
        catch (SAXException | IOException e) {
            return false;
        }
    }

    /**
     * This method reads in the xmlFile, validates it against the schemaFile, and if valid, loads it into a
     * WhitespaceFreeXMLDoc and returns it, which helps because that's a much easier format for us to deal with.
     *
     * @param xmlFile Path and name of xml file to load.
     * @param xsdFile Path and name of schema file to use for validation.
     * @return A normalized Document object fully loaded with the data found in the xmlFile.
     * @throws InvalidXMLFileFormatException Thrown if the xml file validation fails.
     */
    public Document loadXMLDocument(String xmlFile, String xsdFile)
            throws InvalidXMLFileFormatException {
        // FIRST VALIDATE
        boolean isValid = validateXMLDoc(xmlFile, xsdFile);
        if (!isValid) {
            throw new InvalidXMLFileFormatException(xmlFile, xsdFile);
        }

        // THIS IS JAVA API STUFF
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // FIRST RETRIEVE AND LOAD THE FILE INTO A TREE
            DocumentBuilder db     = dbf.newDocumentBuilder();
            Document        xmlDoc = db.parse(xmlFile);
            xmlDoc.getDocumentElement().normalize();

            // LET'S RETURN THE DOC
            return xmlDoc;
        }
        // THESE ARE XML-RELATED ERRORS THAT COULD HAPPEN DURING
        // LOADING AND PARSING IF THE XML FILE IS NOT WELL FORMED
        // OR IS NOW WHERE AND WHAT WE SAY IT IS
        catch (ParserConfigurationException | SAXException | IOException pce) {
            throw new InvalidXMLFileFormatException(xmlFile);
        }
    }

    /**
     * This method extracts the data found in the doc argument that corresponds to the tagName and returns it as text.
     * If no data is found, null is returned. Note that this method is only good for elements that are unique to an XML
     * file, meaning there is only one of them.
     *
     * @param doc     Fully-loaded DOM Document corresponding to a loaded XML file from which we are loading the data.
     * @param tagName Name of the tag (i.e. field name) we are looking to load data for.
     * @return The data in the doc that corresponds to the tagName element. Note that if no data is found, null is
     *         returned.
     */
    public String getTextData(Document doc, String tagName) {
        // IT WAS FOUND, SO GET THE DATA
        Node node = getNodeWithName(doc, tagName);
        if (node == null) {
            return null;
        } else {
            return node.getTextContent();
        }
    }

    /**
     * This method can be used to get the node in the document that is an element of type tagName. null is returned if
     * none is found.
     *
     * @param doc     The XML document to search
     * @param tagName The name of the XML element/tag to search for.
     * @return The first node found named tagName. If none is found in the document, null is returned.
     */
    public Node getNodeWithName(Document doc, String tagName) {
        // GET THE NODE FOR THE tagName ELEMENT
        NodeList nodeList = doc.getElementsByTagName(tagName);

        // IF NOT FOUND, DON'T GO ON
        if (nodeList.getLength() == 0) {
            return null;
        }

        // IT WAS FOUND, SO GET THE DATA
        return nodeList.item(0);
    }

    /**
     * Method for accessing a child node of the parent node argument that is of type tagname.
     *
     * @param parent  The parent node, whose children will be searched.
     * @param tagName The child node tag name we're looking for.
     * @return The child node of type tagName, null if it's not found.
     */
    public ArrayList<Node> getChildNodesWithName(Node parent, String tagName) {
        ArrayList<Node> nodesToReturn = new ArrayList<>();
        NodeList        childNodes    = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node testNode = childNodes.item(i);
            if (testNode.getNodeName().equals(tagName)) {
                nodesToReturn.add(testNode);
            }
        }
        return nodesToReturn;
    }


    /**
     * This method extracts the data found in the doc argument that corresponds to the tagName and returns it as an int.
     * If no data is found, null is returned. Note that this method is only good for elements that are unique to an XML
     * file, meaning there is only one of them.
     *
     * @param doc     Fully-loaded DOM Document corresponding to a loaded XML file from which we are loading the data.
     * @param tagName Name of the tag (i.e. field name) we are looking to load data for.
     * @return The data in the doc that corresponds to the tagName element. Note that if no data is found, null is
     *         returned.
     */
    @SuppressWarnings("unused")
    public Integer getIntData(Document doc, String tagName) {
        // USE THE HELPER METHOD TO EXTRACT THE TEXT
        String data = getTextData(doc, tagName);

        // LET'S AVOID A NULL POINTER EXCEPTION
        if (data == null) {
            return null;
        }
        // PARSE AND RETURN
        else {
            return Integer.parseInt(data);
        }
    }

    /**
     * This method extracts the data found in the doc argument that corresponds to the tagName and returns it as a
     * boolean. If no data is found, null is returned. Note that this method is only good for elements that are unique
     * to an XML file, meaning there is only one of them.
     *
     * @param doc     Fully-loaded DOM Document corresponding to a loaded XML file from which we are loading the data.
     * @param tagName Name of the tag (i.e. field name) we are looking to load data for.
     * @return The data in the doc that corresponds to the tagName element. Note that if no data is found, null is
     *         returned.
     */
    @SuppressWarnings("unused")
    public Boolean getBooleanData(Document doc, String tagName) {
        // USE THE HELPER METHOD TO EXTRACT THE TEXT
        String data = getTextData(doc, tagName);

        // LET'S AVOID A NULL POINTER EXCEPTION
        if (data == null) {
            return null;
        }
        // PARSE AND RETURN
        else {
            return Boolean.parseBoolean(data);
        }
    }

    /**
     * This method can be used for getting Node that appear in a sequence in an XML file. It will return the Node at the
     * index location in the sequence.
     *
     * @param doc     The XML Document to extract the Node from.
     * @param tagName The name of the element to be searched for in the doc.
     * @param index   The index location in the sequence of the node we are looking for.
     * @return The node at the index location in the doc.
     */
    @SuppressWarnings("unused")
    public Node getNodeInSequence(Document doc, String tagName, int index) {
        // GET THE NODE FOR THE tagName ELEMENT
        NodeList nodeList = doc.getElementsByTagName(tagName);

        // IF NOT FOUND, DON'T GO ON
        if (nodeList.getLength() == 0) {
            return null;
        }

        // IT WAS FOUND, SO GET THE DATA
        return nodeList.item(index);
    }

    /**
     * This simple helper method returns the number of nodes found in the doc of the type specified by the tagName
     * argument.
     *
     * @param doc     Loaded XML DOM tree.
     * @param tagName Element we're looking for.
     * @return The number of elements of tagName in this doc.
     */
    @SuppressWarnings("unused")
    public int getNumNodesOfElement(Document doc, String tagName) {
        // GET THE NODE LIST FOR THE tagName ELEMENT TYPE
        NodeList nodeList = doc.getElementsByTagName(tagName);

        // AND RETURN ITS TYPE
        return nodeList.getLength();
    }

    /**
     * This method finds a child node with the tagName argument value as a name in the parent node and returns it. If no
     * child node is found with that name, null is returned.
     *
     * @param parent  The node to search through for a tagName child.
     * @param tagName The element name for the tag we're looking for.
     * @return The child node in the parent with an element name of tagName. If not found, return null.
     */
    @SuppressWarnings("unused")
    public Node getChildNodeWithName(Node parent, String tagName) {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node testNode = childNodes.item(i);
            if (testNode.getNodeName().equals(tagName)) {
                return testNode;
            }
        }
        return null;
    }

    // WE COULD ADD LOTS AND LOTS OF ADDITIONAL SERVICE METHOD. METHODS
    // FOR EXTRACTING OTHER TYPES OF DATA, OR FOR MORE COMPLEX ARRANGEMENTS
    // LIKE SERIES OF DATA AND OBJECT DATA
}