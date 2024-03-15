import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Advertisement {
  private String type;
  private String message;

  public Advertisement(String type, String message) {
    this.type = type;
    this.message = message;
  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public String toString() {
    return String.format("Type: %s, Message: %s", this.type, this.message);
  }

  public String formatTemplate(Review review, String product) {
    String formatted = this.message;
    formatted = formatted.replace("$name", review.getReviewerName());
    formatted = formatted.replace("$stars", String.valueOf(review.getNumberOfStars()));
    formatted = formatted.replace("$verified", String.valueOf(review.isVerified()));
    formatted = formatted.replace("$date", review.getDateOfReview());
    formatted = formatted.replace("$review", review.getReview());
    formatted = formatted.replace("$product", product);

    return formatted;
  }

  public static Advertisement loadAdvertisement(XMLStreamReader reader) throws XMLStreamException {
    String category = reader.getAttributeValue(null, "type");
    
    int event = reader.next();
    event = reader.next();
    assert event == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("message");

    String message = reader.getElementText();

    return new Advertisement(category, message);
  }

  public static ArrayList<Advertisement> loadAdvertisements() throws FileNotFoundException, XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream("advertisements.xml"));

    ArrayList<Advertisement> ads = new ArrayList<Advertisement>();
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("template")) {
        ads.add(loadAdvertisement(reader));
      }
    }

    return ads;
  }
}
