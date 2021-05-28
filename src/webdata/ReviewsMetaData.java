package webdata;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class ReviewsMetaData implements Serializable {
    private int [] score;
    private int [] firstHelpfulness;
    private int [] secondHelpfulness;
    private int [] numOfTokens;
    private int numOfReviews;
    private String productIDs;

    public ReviewsMetaData(Parser parser){
        this.score = Utils.convertToIntArray(parser.getScore());
        this.firstHelpfulness = Utils.convertToIntArray(parser.getFirstHelpfulness());
        this.secondHelpfulness = Utils.convertToIntArray(parser.getSecondHelpfulness());
        this.numOfTokens = Utils.convertToIntArray(parser.getNumOfTokens());
        this.numOfReviews = parser.getNumOfReviews();
        this.productIDs = parser.getProductIds();
    }

    public ReviewsMetaData(ArrayList<Review> parsedReviews){
        this.initProps(parsedReviews.size());
        this.fillMetadata(parsedReviews);
    }

    public ReviewsMetaData(String dir){
        String metaDir = dir + File.separator + "metaData" + File.separator;
        this.score = Utils.readIntArrayFromDisk(metaDir + "Score", false);
        this.firstHelpfulness = Utils.readIntArrayFromDisk(metaDir + "HelpFirst", false);
        this.secondHelpfulness = Utils.readIntArrayFromDisk(metaDir + "HelpSecond", false);
        this.numOfTokens = Utils.readIntArrayFromDisk(metaDir + "NumOfTokens", false);
        this.productIDs = Utils.readStringFromDisk(metaDir + "ProductIDs");
        this.numOfReviews = Utils.readIntFromDisk(metaDir + "NumOfReviews");
    }

    public void clearData(){
        this.productIDs = null;
        this.numOfReviews = 0;
        this.numOfTokens = null;
        this.score = null;
        this.secondHelpfulness = null;
        this.firstHelpfulness = null;
    }
    private void fillMetadata(ArrayList<Review> parsedReviews){
        for(Review review : parsedReviews){
            int reviewIndex = review.getReviewID() - 1;
            this.score[reviewIndex] = review.getScore();
            this.firstHelpfulness[reviewIndex] = review.getHelpfulnessFirst();
            this.secondHelpfulness[reviewIndex] = review.getHelpfulnessSecond();
            this.numOfTokens[reviewIndex] = this.getTotalNumOfTokens(review.getTokens());
            this.productIDs = this.productIDs.concat(review.getProductID());
        }
    }

    private int getTotalNumOfTokens(TreeMap<String, Integer> tokens){
        int sum = 0;
        for(int num : tokens.values()){
            sum = sum + num;
        }
        return sum;
    }

    private void initProps(int numOfReviews){
        this.numOfReviews = numOfReviews;
        this.score = new int[this.numOfReviews];
        this.firstHelpfulness = new int [this.numOfReviews];
        this.secondHelpfulness = new int [this.numOfReviews];
        this.numOfTokens = new int [this.numOfReviews];
        this.productIDs = "";
    }

    public String getProductIDByReviewID(int reviewID){
        return this.productIDs.substring((reviewID - 1) * 10, ((reviewID - 1) * 10) + 10);
    }

    public int getNumOfReviews(){
        return this.numOfReviews;
    }

    public int getScore(int reviewID){
        return this.score[reviewID - 1];
    }

    public int getFirstHelpfulness(int reviewID){
        return this.firstHelpfulness[reviewID - 1];
    }

    public int getSecondHelpfulness(int reviewID){
        return this.secondHelpfulness[reviewID - 1];
    }

    public int getNumOfTokens(int reviewID){
        return this.numOfTokens[reviewID - 1];
    }

    public int totalNumOfTokens(){
        int sum = 0;
        for (int num : numOfTokens){
            sum += num;
        }
        return sum;
    }

    public void writeDataToDisk(String output){
        String writingPath = output + File.separator;
        long startTime = System.nanoTime();
        Utils.printProgress(0, 11, "Write Meta Data - Encoding Score", startTime);
        ArrayList<Byte> encodedData = GroupVarint.compress(this.score, false);
        Utils.printProgress(1, 11, "Write Meta Data - Writing Score", startTime);
        Utils.writeToFile(encodedData, writingPath + "Score");
        Utils.printProgress(2, 11, "Write Meta Data - Encoding FirstHelp", startTime);
        encodedData = GroupVarint.compress(this.firstHelpfulness, false);
        Utils.printProgress(3, 11, "Write Meta Data - Writing FirstHelp", startTime);
        Utils.writeToFile(encodedData, writingPath + "HelpFirst");
        Utils.printProgress(4, 11, "Write Meta Data - Encoding SecondHelp", startTime);
        encodedData= GroupVarint.compress(this.secondHelpfulness, false);
        Utils.printProgress(5, 11, "Write Meta Data - Writing SecondHelp", startTime);
        Utils.writeToFile(encodedData, writingPath + "HelpSecond");
        Utils.printProgress(6, 11, "Write Meta Data - Encoding numOfTokens", startTime);
        encodedData = GroupVarint.compress(this.numOfTokens, false);
        Utils.printProgress(7, 11, "Write Meta Data - Writing numOfTokens", startTime);
        Utils.writeToFile(encodedData, writingPath + "NumOfTokens");
        Utils.printProgress(8, 11, "Write Meta Data - Encoding numOfReviews", startTime);
        encodedData = GroupVarint.convertIntToBytes(this.numOfReviews);
        Utils.printProgress(9, 11, "Write Meta Data - Writing numOfReviews", startTime);
        Utils.writeToFile(encodedData, writingPath + "NumOfReviews");
        Utils.printProgress(10, 11, "Write Meta Data - Writing ProductIDs", startTime);
        encodedData = null;
        Utils.writeToFile(this.productIDs.getBytes(), writingPath + "ProductIDs");
        Utils.printProgress(11, 11, "Write Meta Data", startTime);
    }

}
