package webdata;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class TokensDictionary {
    private static final int K = 100;

    private String tokens;
    private int [] freq;
    private byte [] length;
    private byte [] prefix;
    private int [] tokenPtr;
    private long [] postingListPtr;

    private int numOfBlocks;
    private int numOfTerms;
    private String outputPath;

    public TokensDictionary(ArrayList<Review> parsedReviews, String outputPath){
        this.outputPath = outputPath;
        this.buildDictionary(this.combineTokens(parsedReviews));
    }

    private TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> combineTokens(ArrayList<Review> parsedReviews){
        TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allTokens = new TreeMap<>();
        for(int i = 0; i < parsedReviews.size(); i++){
            TreeMap<String, Integer> tokens = parsedReviews.get(i).getTokens();
            String [] sortedTokens = tokens.keySet().toArray(new String[0]);
            for(String token : sortedTokens){
                TreeMap<Integer, ArrayList<Integer>> newData = new TreeMap<>();
                if(allTokens.containsKey(token)){
                    int oldFreq = allTokens.get(token).firstKey();
                    int newFeq = tokens.get(token) + oldFreq;
                    allTokens.get(token).get(oldFreq).add(i + 1);
                    newData.put(newFeq, allTokens.get(token).get(oldFreq));
                }
                else{
                    ArrayList<Integer> reviews = new ArrayList<>();
                    reviews.add(i + 1);
                    newData.put(tokens.get(token), reviews);
                }
                allTokens.put(token, newData);
            }
        }
        return allTokens;
    }

    private void initProps(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allTokens){
        this.numOfTerms = allTokens.size();
        this.numOfBlocks = (int)Math.ceil(numOfTerms / (double)K);
        this.tokenPtr = new int[numOfBlocks];
        this.freq = new int[numOfTerms];
        this.postingListPtr = new long[numOfTerms];
        this.length = new byte[numOfTerms];
        this.prefix = new byte[numOfTerms];
        this.tokens = "";
    }

    private byte getCommonPrefixLength(String s1, String s2){
        int shortestLength = Math.min(s1.length(), s2.length());
        for (int i = 0; i < shortestLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return (byte)i;
            }
        }
        return (byte)shortestLength;
    }

    private void generatePostinglist(ArrayList<Integer> postingListRawData, int position){
       ArrayList<Byte> encodedData = GroupVarint.compress(postingListRawData);
       this.postingListPtr[position] = Utils.writeToFile(encodedData, this.outputPath + File.separator + "tokenPostinglist");
       System.out.println(encodedData);
    }

    private void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allTokens){
        this.initProps(allTokens);
        int tokenIndex = 0;
        String lastAddedToken = "";
        for(String token : allTokens.keySet()){
            if (tokenIndex % K == 0) {
                this.tokenPtr[(tokenIndex / K)] = this.tokens.length();
                this.prefix[tokenIndex] = 0;
                this.tokens = this.tokens.concat(token);
            }
            else {
                byte prefixLength = getCommonPrefixLength(lastAddedToken, token);
                this.prefix[tokenIndex] = prefixLength;
                this.tokens = this.tokens.concat(token.substring(prefixLength));
            }
            ArrayList<Integer> postingListRaw = allTokens.get(token).get(allTokens.get(token).firstKey());
            this.freq[tokenIndex] = allTokens.get(token).firstKey();
            this.length[tokenIndex] = (byte)(token.length());
            this.generatePostinglist(postingListRaw, tokenIndex);
            tokenIndex++;
            lastAddedToken = token;
        }
        System.out.println("DONE");
    }



}
