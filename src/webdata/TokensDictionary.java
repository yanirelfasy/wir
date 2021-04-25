package webdata;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class TokensDictionary extends Dictionary{

    public TokensDictionary(ArrayList<Review> parsedReviews, String outputPath){
        super();
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

    @Override
    protected void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allTokens){
        this.initProps(allTokens);
        int tokenIndex = 0;
        String lastAddedToken = "";
        for(String token : allTokens.keySet()){
            if (tokenIndex % K == 0) {
                this.valuesPtr[(tokenIndex / K)] = this.concatString.length();
                this.prefix[tokenIndex] = 0;
                this.concatString = this.concatString.concat(token);
            }
            else {
                byte prefixLength = getCommonPrefixLength(lastAddedToken, token);
                this.prefix[tokenIndex] = prefixLength;
                this.concatString = this.concatString.concat(token.substring(prefixLength));
            }
            ArrayList<Integer> postingListRaw = allTokens.get(token).get(allTokens.get(token).firstKey());
            this.freq[tokenIndex] = allTokens.get(token).firstKey();
            this.length[tokenIndex] = (byte)(token.length());
            this.generatePostinglist(postingListRaw, tokenIndex, "tokenPostinglist");
            tokenIndex++;
            lastAddedToken = token;
        }
    }



}
