package webdata;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public abstract class Dictionary implements Serializable {
    protected String dictValues;
    protected int [] freq;
    protected byte [] length;
    protected byte [] prefix;
    protected int [] valuesPtr;
    protected long [] postingListPtr;

    protected int numOfBlocks;
    protected int numOfValues;
    protected String outputPath;
    protected String postinglistOutputName;


    protected void initProps(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allValues){
        this.numOfValues = allValues.size();
        this.numOfBlocks = (int)Math.ceil(numOfValues / (double)Consts.K);
        this.valuesPtr = new int[numOfBlocks];
        this.freq = new int[numOfValues];
        this.postingListPtr = new long[numOfValues];
        this.length = new byte[numOfValues];
        this.prefix = new byte[numOfValues];
        this.dictValues = "";
    }

    protected byte getCommonPrefixLength(String s1, String s2){
        int shortestLength = Math.min(s1.length(), s2.length());
        for (int i = 0; i < shortestLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return (byte)i;
            }
        }
        return (byte)shortestLength;
    }

    protected void generatePostinglist(ArrayList<Integer> postingListRawData, int position, String postingListName){
        ArrayList<Byte> encodedData = GroupVarint.compress(postingListRawData, true);
        this.postingListPtr[position] = Utils.writeToFile(encodedData, this.outputPath + File.separator + postingListName);
    }

    public int search(String value){
        return binSearch(0, this.numOfBlocks - 1, value);
    }

    private int binSearch(int leftBound, int rightBound, String value){
        if(rightBound == leftBound){
            if (value.equals(this.dictValues.substring(this.valuesPtr[leftBound], this.valuesPtr[leftBound] + this.length[leftBound * Consts.K]))){
                return leftBound * Consts.K;
            }
            return searchInRange(leftBound, value);
        }
        else if (rightBound > leftBound){
            int middle = leftBound + (rightBound - leftBound) / 2;
            if (value.equals(this.dictValues.substring(this.valuesPtr[middle], this.valuesPtr[middle] + this.length[middle * Consts.K]))){
                return middle * Consts.K;
            }
            if (value.compareTo(this.dictValues.substring(this.valuesPtr[middle], this.valuesPtr[middle] + this.length[middle * Consts.K])) < 0){
                return binSearch(leftBound, middle - 1, value);
            }
            if (value.compareTo(this.dictValues.substring(this.valuesPtr[middle + 1],
                    this.valuesPtr[middle + 1] + length[(middle + 1) * Consts.K])) < 0) {
                return binSearch(middle, middle, value);
            }
            return binSearch(middle + 1, rightBound, value);
        }
        return -1;
    }

    private int searchInRange(int leftBound, String value){
        int startingPointer = this.valuesPtr[leftBound];
        int i = leftBound * Consts.K ;
        int rightBound = i + Consts.K;
        if((leftBound == this.valuesPtr.length - 1) && (this.numOfValues - i < Consts.K)){
            rightBound = this.numOfValues;
        }
        String prevValue = this.dictValues.substring(startingPointer, startingPointer + this.length[i]);
        String currentValue;
        startingPointer = startingPointer + this.length[i];
        i++;
        while (i < rightBound) {
            currentValue = this.dictValues.substring(startingPointer, startingPointer + length[i] - this.prefix[i]);
            String currentPrefix = prevValue.substring(0, this.prefix[i]);
            currentValue = currentPrefix.concat(currentValue);
            if (value.equals(currentValue)) {
                return i;
            }
            prevValue = currentValue;
            startingPointer = startingPointer +  this.length[i] - this.prefix[i];
            i++;
        }
        return -1;
    }

    public long getPostingListPointer(int tokenIndex){
        if(tokenIndex >= this.postingListPtr.length){
            return -1;
        }
        return this.postingListPtr[tokenIndex];
    }


    public int getPostinglistLength(long postinglistPointer, long nextListPointer){
        int result = 0;
        try{
            ArrayList<Integer> allPostingData = this.getDecodedPostinglist(postinglistPointer, nextListPointer, false);
            result = Utils.splitAtEven(allPostingData).size();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return result;
    }

    public ArrayList<Integer> getDecodedPostinglist(long postinglistPointer, long nextListPointer, boolean decodeGaps){
        ArrayList<Integer> sequence = new ArrayList<>();
        try (RandomAccessFile postingList = new RandomAccessFile(this.outputPath + File.separator + this.postinglistOutputName, "rw")) {
            nextListPointer = (nextListPointer == -1) ? postingList.length(): nextListPointer;
            postingList.seek(postinglistPointer);
            byte [] allBytes = new byte[(int) (nextListPointer - postinglistPointer)];
            postingList.read(allBytes);
            sequence = GroupVarint.decodeSequence(allBytes, decodeGaps);
            return sequence;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return sequence;
    }


    public int getFrequency(int valueIndex){
        return this.freq[valueIndex];
    }

    public void writeDictionaryToDisk(){
        ArrayList<Byte> encodedFreq = GroupVarint.compress(this.freq, false);
        ArrayList<Byte> encodedValuePtr = GroupVarint.compress(this.valuesPtr, false);
        ArrayList<Byte> encodedPostinglistPtr = GroupVarint.compress(this.postingListPtr, true);
        ArrayList<Byte> encodedString = Utils.convertToBytesList(this.dictValues.getBytes());
        String writingPath = this.outputPath + File.separator;

        Utils.writeToFile(encodedFreq, writingPath + Consts.FILE_NAMES.Freq);
        Utils.writeToFile(encodedValuePtr, writingPath + Consts.FILE_NAMES.ValuePtr);
        Utils.writeToFile(encodedPostinglistPtr, writingPath + Consts.FILE_NAMES.PostinglistPtr);
        Utils.writeToFile(encodedString, writingPath + Consts.FILE_NAMES.ConcatString);
        Utils.writeToFile(Utils.convertToBytesList(this.length), writingPath + Consts.FILE_NAMES.Length);
        Utils.writeToFile(Utils.convertToBytesList(this.prefix), writingPath + Consts.FILE_NAMES.Prefix);
        Utils.writeToFile(GroupVarint.convertIntToBytes(this.numOfValues), writingPath + Consts.FILE_NAMES.NumOfValues);
    }



    private long[] readLongArrayFromDisk(String path){
        try{
            Path inputFilePath = Paths.get(path);
            byte[] resultAsBytes = Files.readAllBytes(inputFilePath);
            ArrayList<Long> result = GroupVarint.decodeSequenceAsLong(resultAsBytes, false);
            return Utils.convertToLongArray(result);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return new long [0];
    }

    private byte[] readBytesFromDisk(String path){
        try{
            Path inputFilePath = Paths.get(path);
            return Files.readAllBytes(inputFilePath);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return new byte[0];
    }

    protected int[] readFreqFromDisk(){
        return Utils.readIntArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.Freq);
    }

    protected int[] readValuesPtrFromDisk(){
        return Utils.readIntArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.ValuePtr);
    }

    protected long[] readPostinglistPtrFromDisk(){
        return this.readLongArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.PostinglistPtr);
    }

    protected String readConcatStringFromDisk(){
        return Utils.readStringFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.ConcatString);
    }

    protected byte[] readPrefixFromDisk(){
        return this.readBytesFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.Prefix);
    }

    protected byte[] readLengthFromDisk(){
        return this.readBytesFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.Length);
    }

    protected int readNumOfValuesFromDisk(){
        return Utils.readIntFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.NumOfValues);
    }



    protected abstract void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allValues, ArrayList<Review> parsedReviews);
}
