import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Review {
  private String reviewerName;
  private String review;
  private double numberOfStars;
  private String dateOfReview;
  private boolean verified;

  public String getReviewerName() {
    return this.reviewerName;
  }

  public void setReviewerName(String reviewerName) {
    this.reviewerName = reviewerName;
  }

  public String getReview() {
    return this.review;
  }

  public void setReview(String review) {
    this.review = review;
  }

  public double getNumberOfStars() {
    return this.numberOfStars;
  }

  public void setNumberOfStars(double numberOfStars) {
    this.numberOfStars = numberOfStars;
  }

  public String getDateOfReview() {
    return this.dateOfReview;
  }

  public void setDateOfReview(String dateOfReview) {
    this.dateOfReview = dateOfReview;
  }

  public boolean isVerified() {
    return this.verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public String toString() {
    return String.format("%s reviewed %s/5.0 on %s (%s)", this.reviewerName, this.numberOfStars, this.dateOfReview,
        this.verified ? "Verified" : "Not Verified");
  }

  public void writeToXML(XMLStreamWriter writer) throws XMLStreamException {
    writer.writeStartElement("review"); // <review>
    Util.writeFullElement(writer, "name", getReviewerName());
    Util.writeFullElement(writer, "stars", Double.toString(getNumberOfStars()));
    Util.writeFullElement(writer, "verified", Boolean.toString(isVerified()));
    Util.writeFullElement(writer, "date", getDateOfReview());
    Util.writeFullElement(writer, "text", getReview());
    writer.writeEndElement(); // </review>
  }

  public static Review readFromXML(XMLStreamReader reader) throws XMLStreamException {
    Review review = new Review();

    // Make sure that we are inside a review object
    assert reader.getLocalName().equals("review");

    while (reader.hasNext()) {
      int event = reader.next();
      if (event == XMLStreamReader.START_ELEMENT) {
        switch (reader.getLocalName()) {
          case "name":
            review.setReviewerName(reader.getElementText());
            break;

          case "stars":
            review.setNumberOfStars(Double.parseDouble(reader.getElementText()));
            break;
          
          case "verified":
            review.setVerified(Boolean.parseBoolean(reader.getElementText()));
            break;

          case "date":
            review.setDateOfReview(reader.getElementText());
            break;
          
          case "text":
            review.setReview(reader.getElementText());
            break;
          
          default:
            throw new XMLStreamException("Unexpected element: " + reader.getLocalName());
        }
      } else if (event == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("review")) {
        break;
      }
    }

    return review;
  }

  public static Review parse(Element element) {
    Review review = new Review();

    Elements reviewerNameElements = element.select("span[class=a-profile-name]");
    if (!reviewerNameElements.isEmpty()) {
      review.setReviewerName(reviewerNameElements.text());
    }

    Elements reviewTextElements = element.select("div[data-hook=review-collapsed] span");
    if (!reviewTextElements.isEmpty()) {
      review.setReview(reviewTextElements.text());
    }

    // Lists it as 5.0 out of 5 stars
    Elements starRatingElements = element.select("span[class=a-icon-alt]");
    if (!starRatingElements.isEmpty()) {
      String[] starRatingText = starRatingElements.text().split(" ");
      double numberOfStars = Double.parseDouble(starRatingText[0]);
      review.setNumberOfStars(numberOfStars);
    }

    // Date is towards the end: ... November 30, 2023
    Elements dateElements = element.select("span[class=a-size-base a-color-secondary review-date]");
    if (!dateElements.isEmpty()) {
      String[] split = dateElements.text().split(" ");
      review.setDateOfReview(split[split.length - 3] + " " + split[split.length - 2] + " " + split[split.length - 1]);
    }

    Elements verifiedElements = element.select("span[class=a-size-mini a-color-state a-text-bold]");
    if (!verifiedElements.isEmpty()) {
      String verificationText = verifiedElements.text();
      review.setVerified(verificationText.equalsIgnoreCase("Verified Purchase"));
    }

    return review;
  }
}