import java.io.*;
import java.util.ArrayList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Scraper {
  private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.38 Safari/537.36";

  public static ArrayList<String> scrapeSearchPage(String searchTerm, int page) throws IOException {
    final String url = "https://www.amazon.com/s?k=%s&page=%s";

    Document doc = Jsoup.connect(String.format(url, searchTerm, 1))
      .userAgent(userAgent)
      .get();

    // Select all titles of products on the given page
    Elements titles = doc.select(".puis-card-container h2 a");

    // Grab all links to products on paper
    ArrayList<String> links = new ArrayList<String>(titles.size());
    for (int i = 0; i < titles.size(); i++) {
      links.add("https://www.amazon.com/" + titles.get(i).attr("href"));
    }

    return links;
  }

  public static ArrayList<Review> scrapeReviews(String url) throws IOException {
    Document doc = Jsoup.connect(url)
      .userAgent(userAgent)
      .maxBodySize(0)
      .get();

    // Select all reviews on the page
    Elements reviews = doc.select("div[data-hook=top-customer-reviews-widget] div[data-hook=review]");
    System.out.print(" Found " + reviews.size() + " reviews...");

    // Convert each div to Review objects
    ArrayList<Review> reviewObjects = new ArrayList<Review>(reviews.size());
    for (int i = 0; i < reviews.size(); i++) {
      reviewObjects.add(Review.parse(reviews.get(i)));
    }

    return reviewObjects;
  }

  private static final String searchTerm = "trading+cards";

  public static void main(String[] args) throws IOException, XMLStreamException {
    // Scape 5 pages
    ArrayList<String> links = new ArrayList<String>();
    for (int i = 1; i <= 5; i++) {
      System.out.print("Scraping page " + i + "...");
      links.addAll(Scraper.scrapeSearchPage(searchTerm, i));
      System.out.println(" Done");
    }

    System.out.println("Found " + links.size() + " links.");

    // Scrape all reviews on the first page
    OutputStream os = new FileOutputStream("reviews.xml");
    XMLOutputFactory output = XMLOutputFactory.newInstance();
    XMLStreamWriter writer = output.createXMLStreamWriter(os, "UTF-8");
    writer.writeStartDocument("UTF-8", "1.0");

    writer.writeStartElement("reviews");
    for (int i = 0; i < links.size(); i++) {
      System.out.print(
          String.format("Scraping reviews from product %s/%s (%.2f%% done)...", i + 1, links.size(), ((double)i / (double)links.size()) * 100.0d));

      writer.writeStartElement("product"); // <product>
      writer.writeAttribute("url", links.get(i));

      ArrayList<Review> reviews = Scraper.scrapeReviews(links.get(i));
      for (Review review : reviews)
        review.writeToXML(writer);

      writer.writeEndElement(); // </product>

      System.out.println(" Done");

      if (i % 10 == 0) {
        writer.flush();
        System.out.println("Flushed buffer to disk.");
      }
    }

    writer.writeEndElement(); // </reviews>
    writer.writeEndDocument();

    writer.close();
  }

}
