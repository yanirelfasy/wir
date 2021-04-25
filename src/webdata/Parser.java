package webdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;

/**
 * Class for parsing a file containing reviews
 */
public class Parser {

    private ArrayList<Review> reviews = new ArrayList<>();
    boolean isReadingText = false;

    public void parse(String inputPath){
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(inputPath)))){
            String line = reader.readLine();
            Review review = new Review();
            while(line != null){
                String value = "";
                // Get the product ID
                if(!(value = this.getFieldContent(line, "product/productId")).equals("-1")){
                    if(review.isReviewFull()){
                        reviews.add(review);
                    }
                    this.isReadingText = false;
                    review = new Review();
                    review.setProductID(value);
                }
                // Get the helpfulness
                else if(!(value = this.getFieldContent(line, "review/helpfulness")).equals("-1")){
                    review.setHelpfulness(value);
                }
                // Get score
                else if(!(value = this.getFieldContent(line, "review/score")).equals("-1")){
                    review.setScore(value);
                }
                // Mark as reading text
                else if(!(value = this.getFieldContent(line, "review/text")).equals("-1")){
                    this.isReadingText = true;
                    review.addText(value);
                }
                else if(isReadingText){
                    review.addText(line);
                }
                line = reader.readLine();
            }
            if(review.isReviewFull()){
                reviews.add(review);
            }
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    private String getFieldContent(String line, String fieldTitle){
        String regexCommand = String.format("^%s: (.*)", fieldTitle);
        Matcher regexWorker = Pattern.compile(regexCommand).matcher(line);
        String result = "-1";
        if(regexWorker.find()){
            result = regexWorker.group(1);
        }
        return result;
    }

    public ArrayList<Review> getParsedReviews(){
        return this.reviews;
    }



}
