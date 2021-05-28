package webdata;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private HashSet<String> tokenSet = new HashSet<>();
    private HashSet<String> productIdSet = new HashSet<>();
    private ArrayList<Integer> score = new ArrayList<>();
    private ArrayList<Integer> firstHelpfulness = new ArrayList<>();
    private ArrayList<Integer> secondHelpfulness = new ArrayList<>();
    private ArrayList<Integer> numOfTokens = new ArrayList<>();
    private int numOfReviews = 0;
    private StringBuilder productIds = new StringBuilder();

    void clearMetaData() {
        this.score = new ArrayList<>();
        this.firstHelpfulness = new ArrayList<>();
        this.secondHelpfulness = new ArrayList<>();
        this.numOfTokens = new ArrayList<>();
        numOfReviews = 0;
        productIds = new StringBuilder();
    }

    HashSet<String> getTokenSet() { return tokenSet; }

    HashSet<String> getProductIdSet() { return productIdSet; }

    ArrayList<Integer> getScore() { return score; }

    ArrayList<Integer> getFirstHelpfulness() { return firstHelpfulness; }

    ArrayList<Integer> getSecondHelpfulness() { return secondHelpfulness; }

    String getProductIds() { return productIds.toString(); }

    ArrayList<Integer> getNumOfTokens() { return numOfTokens; }

    int getNumOfReviews() { return numOfReviews;}

    int getTotalNumOfTokens() { return tokenSet.size(); }

    int getNumOfproducts() { return productIdSet.size(); }

    private void extractTokens(String text) {
        String[] tokens = text.split(Consts.TOKENS_REGEX);
        int tokenCounter = 0;
        for (String token: tokens) {
            if (!token.isEmpty()) {
                tokenSet.add(token);
                tokenCounter++;
            }
        }
        numOfTokens.add(tokenCounter);
    }

    private void saveReviewHelpfulness(String term) {
        String[] split = term.split("/");
        firstHelpfulness.add(Integer.parseInt(split[0]));
        secondHelpfulness.add(Integer.parseInt(split[1]));
    }

    private void saveReviewScore(String term) {
        score.add(Integer.parseInt(term.split("\\.")[0]));
    }



    public void parseFile(String inputFile, int datasetSize) {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)), (int)Math.pow(2, 20))){
            long startTime = System.nanoTime();
            String line = reader.readLine();
            String textBuffer = "";
            boolean isReadingText = false;
            String value = "";
            while (line != null){

                if (isReadingText && (value = Utils.getFieldContent(line, "product/productId")).equals("-1")) {
                    textBuffer = textBuffer.concat(" ").concat(line);
                    line = reader.readLine();
                    continue;
                }

                if (!(value = Utils.getFieldContent(line, "product/productId")).equals("-1")) {
                    isReadingText = false;
                    if (!textBuffer.isEmpty()) {
                        extractTokens(textBuffer.toLowerCase());
                    }
                    Utils.printProgress(numOfReviews, datasetSize, "Parsing Reviews", startTime);
                    numOfReviews++;
                    productIds.append(value);
                    productIdSet.add(value);
                    line = reader.readLine();
                    continue;
                }

                if(!(value = Utils.getFieldContent(line, "review/helpfulness")).equals("-1")) {
                    saveReviewHelpfulness(value);
                    line = reader.readLine();
                    continue;
                }

                if(!(value = Utils.getFieldContent(line, "review/score")).equals("-1")) {
                    saveReviewScore(value);
                    line = reader.readLine();
                    continue;
                }

                if(!(value = Utils.getFieldContent(line, "review/text")).equals("-1")) {
                    isReadingText = true;
                    textBuffer = value;
                    line = reader.readLine();
                    continue;
                }

                line = reader.readLine();
            }

            if (!textBuffer.isEmpty()) {
                extractTokens(textBuffer.toLowerCase());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
