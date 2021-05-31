package webdata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)))){
            String line = reader.readLine();
            String textBuffer = "";
            boolean isReadingText = false;
            String value = "";
            long startTime = System.nanoTime();
            while (line != null){

                if (isReadingText && (value = Utils.getFieldContent(line, "product/productId")).equals("-1")) {
                    if(!line.isEmpty()){
                        textBuffer = textBuffer.concat(" ").concat(line);
                    }
                    line = reader.readLine();
                    continue;
                }

                if (!(value = Utils.getFieldContent(line, "product/productId")).equals("-1")) {
                    isReadingText = false;
                    if (!textBuffer.isEmpty()) {
                        extractTokens(textBuffer.toLowerCase());
                    }
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

    private void secondStep(String resultPath, String tmpPath, String tmpFileTemplate){
        double numOfMerges = Math.ceil(Math.log(this.numOfTempFiles) / Math.log(Consts.M));
        long startTime = System.nanoTime();
        if(numOfMerges == 0){
            File input = Paths.get(tmpPath, String.format(tmpFileTemplate, 0, 0)).toFile();
            copyFileContent(input, resultPath);
            try{
                Files.deleteIfExists(input.toPath());
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }
        }
        else{
            double numOfFiles = this.numOfTempFiles;
            String fileToInspect = resultPath;
            for (int step = 1; step <= numOfMerges; step++){
                numOfFiles = Math.ceil(numOfFiles / Consts.M);
                int start = 0;
                int end = start + Consts.M;
                for (int outputFileIndex = 0; outputFileIndex < numOfFiles; outputFileIndex++){
                    if ((step == numOfMerges) && (outputFileIndex == (numOfFiles - 1))) {
                        fileToInspect = resultPath;
                    } else {
                        fileToInspect = Paths.get(this.tmpPath, String.format(tmpFileTemplate, step, outputFileIndex)).toString();
                    }
                    mergeStep(fileToInspect, tmpPath, start, end , tmpFileTemplate, step - 1, startTime);
                    start = end;
                    end += Consts.M;
                }
            }
        }
    }

    private void mergeStep(String out, String tmpPath, int start, int end, String fileName, int prevStep, long startTime) {
        PriorityQueue<ReaderClass> readers = new PriorityQueue<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(out)))) {
            this.fillWithReaders(readers, tmpPath, start, end, fileName, prevStep);
            final int readersLength = readers.size();
            while (!readers.isEmpty()) {
                ReaderClass minReader = readers.poll();
                writer.write(minReader.getLineRead().toString());
                writer.newLine();
                if (minReader.forward()){
                    readers.add(minReader);
                }
            }
            writer.flush();
            deleteTempFiles(start, end, fileName, prevStep);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void deleteTempFiles(int start, int end, String fileName, int prevStep) {
        try {
            for (;start < end; ++start) {
                Files.deleteIfExists(Paths.get(this.tmpPath, String.format(fileName, prevStep, start)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillWithReaders(PriorityQueue<ReaderClass> dest, String tmpPath, int startingFileIndex, int endingFileIndex, String fileName, int prevStep) throws IOException {
        for (int i = startingFileIndex; i < endingFileIndex; i++) {
            Path filePath = Paths.get(tmpPath, String.format(fileName, prevStep, i));
            if (Files.exists(filePath)){
                BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()));
                String line = br.readLine();
                if (line != null){
                    dest.add(new ReaderClass(br, new SortDataEntry(line)));
                }
            }else{
                break;
            }
        }
    }

    private void copyFileContent(File source, String dest) {
        try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dest)))) {
                String line = reader.readLine();
                while (line != null) {
                    writer.write(line);
                    writer.newLine();
                    line = reader.readLine();
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    void clear() {
        this.tokenPairs = new HashMap<>();
        this.productIDPairs = new HashMap<>();
    }

    public void sort(String input, String tokensOutput, String productsOutput){
        firstStep(input);
        this.clear();
        this.secondStep(tokensOutput, this.tmpPath, Consts.TEMP_TOKEN_PAIRS);
        this.secondStep(productsOutput, this.tmpPath, Consts.TEMP_PRODUCT_IDS_PAIRS);
    }

    public ArrayList<String> getRawTokens(){
        return this.rawTokens;
    }
    public ArrayList<String> getRawProductIDs(){
        return this.rawProductIDs;
    }
}
