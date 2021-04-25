package webdata;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public abstract class Dictionary {
    protected static final int K = 100;

    protected String concatString;
    protected int [] freq;
    protected byte [] length;
    protected byte [] prefix;
    protected int [] valuesPtr;
    protected long [] postingListPtr;

    protected int numOfBlocks;
    protected int numOfIDs;
    protected String outputPath;


    protected void initProps(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allValues){
        this.numOfIDs = allValues.size();
        this.numOfBlocks = (int)Math.ceil(numOfIDs / (double)K);
        this.valuesPtr = new int[numOfBlocks];
        this.freq = new int[numOfIDs];
        this.postingListPtr = new long[numOfIDs];
        this.length = new byte[numOfIDs];
        this.prefix = new byte[numOfIDs];
        this.concatString = "";
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
        ArrayList<Byte> encodedData = GroupVarint.compress(postingListRawData);
        this.postingListPtr[position] = Utils.writeToFile(encodedData, this.outputPath + File.separator + postingListName);
    }

    protected abstract void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allValues);
}
