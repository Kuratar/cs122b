import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StarsParser{

    List<Stars> slist = new ArrayList<>();
    HashSet<String> databaseStars = new HashSet<>();
    Document dom;
    FileWriter inconsistencies;
    FileWriter sqlFile;
    int highestID;


    public StarsParser() {
        try {
            inconsistencies = new FileWriter("starInconsistencies.txt");
            sqlFile = new FileWriter("actors63Inserts.sql");
            sqlFile.write("USE moviedbexample;\n" +
                    "BEGIN;\n");
            highestID = -1;
        } catch (Exception e) {
            System.out.println("error from creating file: " + e.getMessage());
        }
    }

    public void runExample() {

        // parse the xml file and get the dom object
        parseXmlFile();

        loadDatabaseStars();
        // get each employee element and create a Employee object
        parseDocument();

        // iterate through the list and print the data
        printData();

    }

    public void loadDatabaseStars() {
        String query = "select name,birthYear from stars";
        String query2 = "select max(id) from stars";
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:" + "mysql" + ":///" + "moviedbexample" + "?autoReconnect=true&useSSL=false",
                    "mytestuser", "Nonie127");
            PreparedStatement statement = conn.prepareStatement(query);
            PreparedStatement statement2 = conn.prepareStatement(query2);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                String info = "";
                info += rs.getString("name") + rs.getString("birthYear");
                databaseStars.add(info);
            }
            ResultSet rs2 = statement2.executeQuery(); rs2.next();
            highestID = Integer.parseInt(rs2.getString("max(id)").substring(2));

            conn.close();
            rs.close();
            statement.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {

                // get the employee element
                Element element = (Element) nodeList.item(i);

                // get the Employee object
                Stars s = parseStar(element);

                String info = s.getName() + s.getBirthYear();
                // add it to list
                try {
                    if (!databaseStars.contains(info)) {
//                        sqlFile.write("INSERT INTO movies VALUES(\"" + movie.getTitle() + "\"," + movie.getYear() + ",\"" +
//                                movie.getDirector() + "\");\n");
                        if (s.getBirthYear() == 0) {
                            sqlFile.write(String.format("INSERT INTO stars VALUES(\"%s\",\"%s\",%s);\n",
                                    "nm" + (highestID + 1), s.getName().replace("\"",""), null));
                        }
                        else {
                            sqlFile.write(String.format("INSERT INTO stars VALUES(\"%s\",\"%s\",%d);\n",
                                    "nm" + (highestID + 1), s.getName().replace("\"",""), s.getBirthYear()));
                        }
                        highestID++;
                        databaseStars.add(info);
                    } else {
                        if (s.getBirthYear() == 0) {
                            inconsistencies.write("Star Name: " + s.getName() + "\n" +
                                    "Star DOB:        " + null + "\n" + "\n");
                        }
                        else {
                            inconsistencies.write("Star Name: " + s.getName() + "\n" +
                                    "Star DOB:        " + s.getBirthYear() + "\n" + "\n\n");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("error writing to file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private Stars parseStar(Element element) {

        // for each <employee> element get text or int values of
        // name ,id, age and name
        String name = getTextValue(element, "stagename");
        int dob = getIntValue(element, "dob");


        // create a new Employee with the value read from the xml nodes
        return new Stars(name, dob);
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            if (nodeList.item(0).getFirstChild() != null)
            {
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            }

        }
        return textVal;
    }


    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        if (getTextValue(ele, tagName)!= null && getTextValue(ele, tagName).matches("[0-9]+"))
        {
            return Integer.parseInt(getTextValue(ele, tagName));
        }
        return 0;
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printData() {
        try {
            sqlFile.write("COMMIT;");
            sqlFile.close();
            inconsistencies.close();
            System.out.println("Finished parsing actors63.xml\n" +
                               "queries are in actors63Inserts.sql\n" +
                               "inconsistencies in starInconsistencies.txt");
        } catch (Exception e) {
            System.out.println("error writing to file:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // create an instance
        StarsParser domParserExample = new StarsParser();

        // call run example
        domParserExample.runExample();
    }

}
