package webdata;

import java.util.TreeMap;

public class Review {

    private int reviewID;
    private String productID;
    private int helpfulnessFirst;
    private int helpfulnessSecond;
    private int score;
    private TreeMap<String, Integer> tokens;

    public Review(int reviewID){
        this.reviewID = reviewID;
        this.tokens = new TreeMap<>();
        this.helpfulnessFirst = -1;
        this.helpfulnessSecond = -1;
        this.score = -1;
    }

    public void setProductID(String productID){
        this.productID = productID;
    }

    public void setHelpfulness(String helpfulness){
        String [] integers = helpfulness.split("/");
        if(integers.length == 2){
            this.helpfulnessFirst = Integer.parseInt(integers[0]);
            this.helpfulnessSecond = Integer.parseInt(integers[1]);
        }
    }

    public void setScore(String score){
        try{
            this.score = Integer.parseInt(score.substring(0, score.indexOf('.')));
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public void addText(String text){
        String [] tokens = text.split("[^A-Za-z0-9]");
        for(String token : tokens){
            if(!token.equals("")){
                String lowerToken = token.toLowerCase();
                if(!this.tokens.containsKey(lowerToken)){
                    this.tokens.put(lowerToken, 1);
                }
                else{
                    this.tokens.put(lowerToken, this.tokens.get(lowerToken) + 1);
                }
            }
        }
    }

    public boolean isReviewFull(){
        return (
                this.productID != null &&
                this.score >= 0 &&
                this.helpfulnessFirst >= 0 &&
                this.helpfulnessSecond >= 0 &&
                !this.tokens.isEmpty()
        );
    }

    public TreeMap<String, Integer> getTokens(){
        return this.tokens;
    }
    public String getProductID() {return this.productID;}
    public int getReviewID() {return this.reviewID;}
    public int getHelpfulnessFirst(){ return this.helpfulnessFirst; }
    public int getHelpfulnessSecond() {return this.helpfulnessSecond;}
    public int getScore() {return this.score;}
}
