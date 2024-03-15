import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Util {
  public static void writeFullElement(XMLStreamWriter writer, String elementName, String text)
      throws XMLStreamException {
    writer.writeStartElement(elementName);
    writer.writeCharacters(text);
    writer.writeEndElement();
  }
}
