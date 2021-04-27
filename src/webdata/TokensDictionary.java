package webdata;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class TokensDictionary extends Dictionary{

    public TokensDictionary(ArrayList<Review> parsedReviews, String outputPath){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.tokensDictionary.name();
        this.postinglistOutputName = Consts.FILE_NAMES.tokensPostinglist.name();
        this.buildDictionary(this.combineTokens(parsedReviews), parsedReviews);
    }

    public TokensDictionary(String outputPath){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.tokensDictionary.name();
        this.postinglistOutputName = Consts.FILE_NAMES.tokensPostinglist.name();
        this.freq = this.readFreqFromDisk();
        this.valuesPtr = this.readValuesPtrFromDisk();
        this.postingListPtr = this.readPostinglistPtrFromDisk();
        this.dictValues = this.readConcatStringFromDisk();
        this.prefix = this.readPrefixFromDisk();
        this.length = this.readLengthFromDisk();
        this.numOfValues = this.readNumOfValuesFromDisk();
        this.numOfBlocks = (int)Math.ceil(numOfValues / (double)Consts.K);
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

    @Override
    protected void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allTokens, ArrayList<Review> parsedReviews){
        this.initProps(allTokens);
        int tokenIndex = 0;
        String lastAddedToken = "";
        for(String token : allTokens.keySet()){
            if (tokenIndex % Consts.K == 0) {
                this.valuesPtr[(tokenIndex / Consts.K)] = this.dictValues.length();
                this.prefix[tokenIndex] = 0;
                this.dictValues = this.dictValues.concat(token);
            }
            else {
                byte prefixLength = getCommonPrefixLength(lastAddedToken, token);
                this.prefix[tokenIndex] = prefixLength;
                this.dictValues = this.dictValues.concat(token.substring(prefixLength));
            }
            ArrayList<Integer> postingListRaw = allTokens.get(token).get(allTokens.get(token).firstKey());
            ArrayList<Integer> postingListRawWithFreq = new ArrayList<>();
            for(int reviewIndex : postingListRaw){
                postingListRawWithFreq.add(reviewIndex);
                postingListRawWithFreq.add(parsedReviews.get(reviewIndex - 1).getTokens().get(token));
            }
            this.freq[tokenIndex] = allTokens.get(token).firstKey();
            this.length[tokenIndex] = (byte)(token.length());
            this.generatePostinglist(postingListRawWithFreq, tokenIndex, this.postinglistOutputName);
            tokenIndex++;
            lastAddedToken = token;
        }
    }


}
