import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.SAXException;

public class Main {
  private static double threshold = 25.0;

  private static TargetWordLoader targetWords;

  static {
    try {
      targetWords = new TargetWordLoader();
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
  };

  private static HashMap<String, Double> getReviewScores(Review review) {
    // Maps categories to categorical scores
    HashMap<String, Double> scores = new HashMap<>();
    for (String category : targetWords.getWordList().keySet())
      scores.put(category, 0.0d);

    // Add up naively
    for (String word : review.getReview().split(" ")) {
      for (String category : targetWords.getWordList().keySet()) {
        if (targetWords.getWordList().get(category).containsKey(word)) {
          scores.put(category, scores.get(category) + targetWords.getWordList().get(category).get(word));
        }
      }
    }

    // Take average over length of words to normalize
    for (String category : scores.keySet()) {
      double currScore = scores.get(category);
      currScore /= (double)review.getReview().length();

      // Add a huge amount of points if the category is present - this is not
      // normalized to help show the enormity of this
      if (review.getReview().contains(category))
        currScore += 30.0d;
      
      scores.put(category, currScore);
    }

    return scores;
  }

  public static void main(String[] args)
      throws XMLStreamException, ParserConfigurationException, SAXException, IOException {

    System.out.print("Reading and scoring all reviews...");
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream("reviews.xml"));

    // Stream reviews in case there are a lot of reviews
    ArrayList<Review> reviews = new ArrayList<>();
    ArrayList<String> products = new ArrayList<>();
    int totalReviews = 0;
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("review")) {
        Review review = Review.readFromXML(reader);
        HashMap<String, Double> scores = getReviewScores(review);
        totalReviews++;

        if (scores.values().stream().reduce(0.0d, Double::sum) > threshold) {
          reviews.add(review);
          products.add(Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey());
        }
      }
    }

    System.out.println(" Done");
    System.out.println(String.format("Found %s/%s total targets.", reviews.size(), totalReviews));

    ArrayList<Advertisement> ads = Advertisement.loadAdvertisements();

    System.out.print("Writing advertisements to file...");
    FileWriter output = new FileWriter("output.txt");
    for (int i = 0; i < reviews.size(); i++) {
      int adIdx = (int)(Math.random() * ads.size());
      Advertisement ad = ads.get(adIdx);

      output.append(ad.getType() + "\n");
      output.append(ad.formatTemplate(reviews.get(i), products.get(i)) + "\n");
      output.append("\n\n");
    }
    output.close();
    
    System.out.println(" Done");
  }
}
