package webdata;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static webdata.Consts.DATA_SEPARATOR;

public class SortManager {

    private ArrayList<String> rawTokens;
    private HashMap<String, Integer> tokenPairs;
    private ArrayList<SortDataEntry> tokensData;
    private ArrayList<String> rawProductIDs;
    private HashMap<String, Integer> productIDPairs;
    private ArrayList<SortDataEntry> productsData;
    private int numOfReviews;
    private int numOfTempFiles;
    private String tmpPath;

    SortManager(ArrayList<String> rawTokens, ArrayList<String> rawProductIDs, String tmpPath){
        this.numOfTempFiles = 0;
        this.numOfReviews = 0;

        Collections.sort(rawTokens);
        this.rawTokens = rawTokens;
        this.tokenPairs = getWithID(this.rawTokens);
        this.tokensData = new ArrayList<>();

        Collections.sort(rawProductIDs);
        this.rawProductIDs = rawProductIDs;
        this.productIDPairs = getWithID(this.rawProductIDs);
        this.productsData = new ArrayList<>();

        this.tmpPath = tmpPath;

    }

    private static HashMap<String, Integer> getWithID(ArrayList<String> rawData) {
        HashMap<String, Integer> couples = new HashMap<>();
        for (int i = 0; i < rawData.size(); ++i) {
            couples.put(rawData.get(i), i);
        }
        return couples;
    }

    private void extractTokens(String text) {
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(text.split(Consts.TOKENS_REGEX)));
        Collections.sort(tokens);
        String prevToken = "";
        int frequency = 1;
        for (String token: tokens) {
            if (!token.isEmpty()) {
                if (!token.equals(prevToken)) {
                    if (!prevToken.isEmpty()) {
                        tokensData.add(new SortDataEntry(getDataString(tokenPairs.get(prevToken), numOfReviews, frequency)));
                    }
                    prevToken = token;
                    frequency = 1;
                } else {
                    frequency++;
                }
            }
        }

        if (!prevToken.isEmpty()) {
            tokensData.add(new SortDataEntry(getDataString(tokenPairs.get(prevToken), numOfReviews, frequency)));
        }
    }

    private void writeBlocksToFile(ArrayList<SortDataEntry> blocks, String fileName){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Paths.get(tmpPath, String.format(fileName, 0, numOfTempFiles)).toString())))){
            // Writes the block lines to the temp file
            for (SortDataEntry data:blocks) {
                writer.write(data.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void writeTmpFile() {
        Collections.sort(this.tokensData);
        Collections.sort(this.productsData);
        writeBlocksToFile(this.tokensData, Consts.TEMP_TOKEN_PAIRS);
        writeBlocksToFile(this.productsData, Consts.TEMP_PRODUCT_IDS_PAIRS);
        numOfTempFiles++;
        this.tokensData = new ArrayList<>();
        this.productsData = new ArrayList<>();
    }

    private String getDataString(int value, int reviewID, int frequency){
        return value + DATA_SEPARATOR +reviewID + DATA_SEPARATOR + frequency;
    }

    private void firstStep(String inputFile){
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)), (int)Math.pow(2, 20))){
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
                    Utils.printProgress(numOfReviews, 7850071, "Sort - First Step");
                    numOfReviews++;
                    if (numOfReviews % (Consts.REVIEWS_PER_FILE + 1)  == 0) {
                        writeTmpFile();
                    }
                    this.productsData.add(new SortDataEntry(getDataString(productIDPairs.get(value),numOfReviews, 1)));
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
            this.writeTmpFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sort(String input){
        firstStep(input);
    }


}
