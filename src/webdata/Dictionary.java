package webdata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;

public abstract class Dictionary implements Serializable {
    protected String dictValues;
    protected int [] freq;
    protected byte [] length;
    protected int [] prefix;
    protected int [] valuesPtr;
    protected long [] postingListPtr;
    protected long [] freqListPtr;

    protected int numOfBlocks;
    protected int numOfValues;
    protected String outputPath;
    protected String postinglistOutputName;
    protected String frequenciesListOutputName;
    protected long postingListFilePointer;
    protected long freqListFilePointer;


    protected void initProps(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allValues){
        this.postingListFilePointer = 0;
        this.freqListFilePointer = 0;
        this.numOfValues = allValues.size();
        this.numOfBlocks = (int)Math.ceil(numOfValues / (double)Consts.K);
        this.valuesPtr = new int[numOfBlocks];
        this.freq = new int[numOfValues];
        this.postingListPtr = new long[numOfValues];
        this.length = new byte[numOfValues];
        this.prefix = new int[numOfValues];
        this.dictValues = "";
    }

    protected void initProps(int numOfValues){
        this.numOfValues = numOfValues;
        this.numOfBlocks = (int)Math.ceil(numOfValues / (double)Consts.K);
        this.valuesPtr = new int[numOfBlocks];
        this.freq = new int[numOfValues];
        this.postingListPtr = new long[numOfValues];
        this.freqListPtr = new long[numOfValues];
        this.length = new byte[numOfValues];
        this.prefix = new int[numOfValues];
        this.dictValues = "";
    }

    protected int getCommonPrefixLength(String s1, String s2){
        int shortestLength = Math.min(s1.length(), s2.length());
        for (int i = 0; i < shortestLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        return shortestLength;
    }

    protected void generatePostingList(ArrayList<Integer> postingListRawData, int position, String postingListName){
        ArrayList<Byte> encodedData = GroupVarint.compress(postingListRawData, true);
        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(new File(this.outputPath + File.separator + postingListName)))) {
            this.postingListPtr[position] = this.writeDataToFile(encodedData, writer, false);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    protected void generateFreqList(ArrayList<Integer> freqListRawData, int position, String freqListName){
        ArrayList<Byte> encodedData = GroupVarint.compress(freqListRawData, false);
        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(new File(this.outputPath + File.separator + freqListName)))) {
            this.freqListPtr[position] = this.writeDataToFile(encodedData, writer, true);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
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
            ArrayList<Integer> allPostingData = this.getDecodedPostinglist(postinglistPointer, nextListPointer, false, true);
            result = Utils.splitAtEven(allPostingData).size();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return result;
    }

    public ArrayList<Integer> getDecodedPostinglist(long postinglistPointer, long nextListPointer, boolean decodeGaps, boolean hasFreq){
        ArrayList<Integer> sequence = new ArrayList<>();
        try (RandomAccessFile postingList = new RandomAccessFile(this.outputPath + File.separator + this.postinglistOutputName, "rw")) {
            nextListPointer = (nextListPointer == -1) ? postingList.length(): nextListPointer;
            postingList.seek(postinglistPointer);
            byte [] allBytes = new byte[(int) (nextListPointer - postinglistPointer)];
            postingList.read(allBytes);
            sequence = GroupVarint.decodeSequence(allBytes, decodeGaps, hasFreq);
            return sequence;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return sequence;
    }

    protected long writeDataToFile(ArrayList<Byte> encodedData, BufferedOutputStream writer, boolean isFreqList) throws IOException {
        long pos  = this.postingListFilePointer;
        if (isFreqList){
            pos = this.freqListFilePointer;
        }
        byte[] arrayToWrite = Utils.convertToByteArray(encodedData);
        writer.write(arrayToWrite);
        if (isFreqList){
            this.freqListFilePointer += arrayToWrite.length;
        }
        else{
            this.postingListFilePointer += arrayToWrite.length;
        }

        return pos;
    }


    public int getFrequency(int valueIndex){
        return this.freq[valueIndex];
    }

    public void writeDictionaryToDisk(){
        String writingPath = this.outputPath + File.separator;
        long startTime = System.nanoTime();
        Utils.printProgress(1, 15, "Write Dictionary - compress freq", startTime);
        ArrayList<Byte> encodedData = GroupVarint.compress(this.freq, false);
        Utils.printProgress(2, 15, "Write Dictionary - write freq", startTime);
        Utils.writeToFile(encodedData, writingPath + Consts.FILE_NAMES.Freq);
        Utils.printProgress(3, 15, "Write Dictionary - compress valuesPtr", startTime);
        encodedData = GroupVarint.compress(this.valuesPtr, true);
        Utils.printProgress(4, 15, "Write Dictionary - write valuesPtr", startTime);
        Utils.writeToFile(encodedData, writingPath + Consts.FILE_NAMES.ValuePtr);
        Utils.printProgress(5, 15, "Write Dictionary - compress postingListPtr", startTime);
        encodedData = GroupVarint.compress(this.postingListPtr, true);
        Utils.printProgress(6, 15, "Write Dictionary - write postingListPtr", startTime);
        Utils.writeToFile(encodedData, writingPath + Consts.FILE_NAMES.PostinglistPtr);
        Utils.printProgress(7, 15, "Write Dictionary - compress dictValues", startTime);
        encodedData = Utils.convertToBytesList(this.dictValues.getBytes());
        Utils.printProgress(8, 15, "Write Dictionary - write dictValues", startTime);
        Utils.writeToFile(encodedData, writingPath + Consts.FILE_NAMES.ConcatString);
        Utils.printProgress(9, 15, "Write Dictionary - compress prefix", startTime);
        encodedData = GroupVarint.compress(this.prefix, false);
        Utils.printProgress(10, 15, "Write Dictionary - write prefix", startTime);
        Utils.writeToFile(encodedData, writingPath + Consts.FILE_NAMES.Prefix);
        Utils.printProgress(11, 15, "Write Dictionary - compress freqListPtr", startTime);
        encodedData = GroupVarint.compress(this.freqListPtr, true);
        Utils.printProgress(12, 15, "Write Dictionary - write freqListPtr", startTime);
        Utils.writeToFile(encodedData, writingPath + Consts.FILE_NAMES.freqListPtr);
        encodedData = null;
        Utils.printProgress(13, 15, "Write Dictionary - write length", startTime);
        Utils.writeToFile(this.length, writingPath + Consts.FILE_NAMES.Length);
        Utils.printProgress(14, 15, "Write Dictionary - write numOfValues", startTime);
        Utils.writeToFile(GroupVarint.convertIntToBytes(this.numOfValues), writingPath + Consts.FILE_NAMES.NumOfValues);
        Utils.printProgress(15, 15, "Write Dictionary", startTime);
    }



    private long[] readLongArrayFromDisk(String path){
        try{
            Path inputFilePath = Paths.get(path);
            byte[] resultAsBytes = Files.readAllBytes(inputFilePath);
            ArrayList<Long> result = GroupVarint.decodeSequenceAsLong(resultAsBytes, true);
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
        return Utils.readIntArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.Freq, false);
    }

    protected int[] readValuesPtrFromDisk(boolean asGaps){
        return Utils.readIntArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.ValuePtr, asGaps);
    }

    protected long[] readPostinglistPtrFromDisk(){
        return this.readLongArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.PostinglistPtr);
    }

    protected String readConcatStringFromDisk(){
        return Utils.readStringFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.ConcatString);
    }

    protected int[] readPrefixFromDisk(){
        return Utils.readIntArrayFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.Prefix, false);
    }

    protected byte[] readLengthFromDisk(){
        return this.readBytesFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.Length);
    }

    protected int readNumOfValuesFromDisk(){
        return Utils.readIntFromDisk(this.outputPath + File.separator + Consts.FILE_NAMES.NumOfValues);
    }



    protected abstract void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allValues, ArrayList<Review> parsedReviews);
    protected abstract void buildDictionary(String sortedTermsFile, ArrayList<String> mapping);
}
