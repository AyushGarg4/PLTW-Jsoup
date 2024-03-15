import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TargetWordLoader {
  private HashMap<String, HashMap<String, Double>> wordList;

  public TargetWordLoader() throws ParserConfigurationException, SAXException, IOException {
    this.wordList = new HashMap<>();
    loadWordList();
  }

  public HashMap<String, HashMap<String, Double>> getWordList() {
    return this.wordList;
  }

  private void loadWordList() throws ParserConfigurationException, SAXException, IOException {
    // Load word list from file
    // HashMap<String, Double> pokemon = new HashMap<>();
    // pokemon.put("pokemon", 10.0);
    // pokemon.put("cards", 5.0);
    // wordList.put("pokemon", pokemon);

    // HashMap<String, Double> test = new HashMap<>();
    // test.put("trading", 2.0);
    // test.put("game", 3.0);
    // wordList.put("test", test);

    // We don't need to stream this file as it is quite small
    File file = new File("targetWords.xml");
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = factory.newDocumentBuilder();
    Document document = db.parse(file);

    NodeList categories = document.getElementsByTagName("category");
    for (int i = 0; i < categories.getLength(); i++) {
      HashMap<String, Double> category = new HashMap<>();
      String categoryName = categories.item(i).getAttributes().getNamedItem("name").getNodeValue();
      NodeList words = categories.item(i).getChildNodes();

      for (int j = 0; j < words.getLength(); j++) {
        if (words.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && words.item(j).getNodeName().equals("targetWord")) {
          String word = words.item(j).getAttributes().getNamedItem("word").getNodeValue();
          double weight = Double.parseDouble(words.item(j).getTextContent());
          category.put(word, weight);
        }
      }

      wordList.put(categoryName, category);
    }
  }

  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
    TargetWordLoader loader = new TargetWordLoader();
    System.out.println(loader.getWordList());
  }
}
