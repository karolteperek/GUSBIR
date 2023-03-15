package pl.karolteperek;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class App
{
    public static void main( String[] args ) throws ParserConfigurationException {
        System.out.println( "Hello World!" );
        try{
            //Konfiguracja zapisywania logów do pliku. Teoretycznie zawartość pliku .log jest w formacie .xml
            Files.deleteIfExists(Paths.get("default.log"));
            FileHandler handler = new FileHandler("default.log", true);

            Logger logger = Logger.getLogger("com.gargoylesoftware");
            logger.addHandler(handler);
            logger.severe("severe message");
            logger.warning("warning message");
            logger.info("info message");
            logger.config("config message");
            logger.fine("fine message");
            logger.finer("finer message");
            logger.finest("finest message");

            //Inicjalizacja klienta webowego
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            HtmlPage page = webClient.getPage("https://wyszukiwarkaregon.stat.gov.pl/appBIR/index.aspx");

            Thread.sleep(2000);

            //Kod javascript do wydobycia danych firmy z wyszukiwarki regon
            String NIP = "5261645000";
            String javaScriptCode = "var arr\n" +
                    "var nazwa\n" +
                    "var kodPocztowy\n" +
                    "var miejscowosc\n" +
                    "var ulica\n" +
                    "\n" +
                    "setTimeout(function(){document.getElementById(\"txtNip\").value = \"" + NIP + "\"},1)\n" +
                    "setTimeout(function(){daneSzukaj('identyfikator')},5000)\n" +
                    "setTimeout(function(){arr = document.getElementsByClassName('tabelaZbiorczaListaJednostekAltRow')},5000)\n" +
                    "setTimeout(function(){\n" +
                    "\tnazwa = arr[0].cells[2];\n" +
                    "\tkodPocztowy = arr[0].cells[6];\n" +
                    "\tmiejscowosc = arr[0].cells[7];\n" +
                    "\tulica = arr[0].cells[8];\n" +
                    "\tconsole.log(\"NAZWA: \" + nazwa.textContent)\n" +
                    "\tconsole.log(\"KOD POCZTOWY: \" + kodPocztowy.textContent)\n" +
                    "\tconsole.log(\"MIEJSCOWOŚĆ: \" + miejscowosc.textContent)\n" +
                    "\tconsole.log(\"ULICA: \" +ulica.textContent)\n" +
                    "\t},5000)\n";
            page.executeJavaScript(javaScriptCode);
            Thread.sleep(10000);
            handler.close();
        } catch (IOException | InterruptedException e){
            System.out.println("Webclient error: " + e);
        }

        try{
            //read xml

            //Ścieżka do odczytu pliku z logami (W katalogu musi być plik dtd)
            File file = new File("default.log");
            System.out.println("CZY JEST PLIK: " + file.exists());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            //Iteracja pliku xml
            NodeList nodeList = doc.getElementsByTagName("record");
            //Tablica, w której przechowywane są dane firmy wydobyte z pliku log
            String[] daneFirmy = {"Nazwa","Kod pocztowy","Miejscowość","Ulica"};

            //Wyodrębnianie danych z pliku. Zaczynamy od pozycji 5, ponieważ wtedy pojawia się output z konsoli javascriptowej (console.log)
            for (int itr = 5; itr < nodeList.getLength(); itr++) {
                Node node = nodeList.item(itr);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    daneFirmy[itr-5] = eElement.getElementsByTagName("message").item(0).getTextContent(); //Przypisanie danych z pliku log do tablicy
                }
            }

            System.out.println(daneFirmy[0].substring(7));
            System.out.println(daneFirmy[1].substring(14));
            System.out.println(daneFirmy[2].substring(13));
            System.out.println(daneFirmy[3].substring(7));
            //read xml

        } catch (IOException | SAXException e) {
            System.out.println("XML Reading error: " + e);
        }
    }
}
