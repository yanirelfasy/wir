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
        ArrayList<Byte> encodedScore = GroupVarint.compress(this.score, false, false);
        ArrayList<Byte> encodedHelpfulnessFirst = GroupVarint.compress(this.firstHelpfulness, false, false);
        ArrayList<Byte> encodedHelpfulnessSecond = GroupVarint.compress(this.secondHelpfulness, false, false);
        ArrayList<Byte> encodedNumOfTokens = GroupVarint.compress(this.numOfTokens, false, false);
        ArrayList<Byte> encodedNumOfReviews = GroupVarint.convertIntToBytes(this.numOfReviews);
        ArrayList<Byte> encodedString = Utils.convertToBytesList(this.productIDs.getBytes());
        String writingPath = output + File.separator;

        Utils.writeToFile(encodedScore, writingPath + "Score");
        Utils.writeToFile(encodedHelpfulnessFirst, writingPath + "HelpFirst");
        Utils.writeToFile(encodedHelpfulnessSecond, writingPath + "HelpSecond");
        Utils.writeToFile(encodedNumOfTokens, writingPath + "NumOfTokens");
        Utils.writeToFile(encodedNumOfReviews, writingPath + "NumOfReviews");
        Utils.writeToFile(encodedString, writingPath + "ProductIDs");
    }

}
