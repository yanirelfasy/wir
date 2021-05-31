package webdata;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class TokensDictionary extends Dictionary{

    public TokensDictionary(int totalNumOfTerms, String sortedTermsFilePath, String outputPath, ArrayList<String> mapping){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.tokensDictionary.name();
        this.postinglistOutputName = Consts.FILE_NAMES.tokensPostinglist.name();
        this.frequenciesListOutputName = Consts.FILE_NAMES.freqList.name();

        this.initProps(totalNumOfTerms);
        this.buildDictionary(sortedTermsFilePath, mapping);
    }

    public TokensDictionary(ArrayList<Review> parsedReviews, String outputPath){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.tokensDictionary.name();
        this.postinglistOutputName = Consts.FILE_NAMES.tokensPostinglist.name();
        this.frequenciesListOutputName = Consts.FILE_NAMES.freqList.name();
        this.buildDictionary(this.combineTokens(parsedReviews), parsedReviews);
    }

    public TokensDictionary(String outputPath){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.tokensDictionary.name();
        this.postinglistOutputName = Consts.FILE_NAMES.tokensPostinglist.name();
        this.frequenciesListOutputName = Consts.FILE_NAMES.freqList.name();
        this.freq = this.readFreqFromDisk();
        this.valuesPtr = this.readValuesPtrFromDisk(true);
        this.postingListPtr = this.readPostinglistPtrFromDisk();
        this.freqListPtr = this.readFreqListPtrFromDisk();
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
                int prefixLength = getCommonPrefixLength(lastAddedToken, token);
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
            this.generatePostingList(postingListRawWithFreq, tokenIndex, this.postinglistOutputName);
            tokenIndex++;
            lastAddedToken = token;
        }
    }

    @Override
    protected void buildDictionary(String sortedFile, ArrayList<String> valueIDPair) {
        StringBuilder stringBuilder = new StringBuilder();
        long startTime = System.nanoTime();
        int tokenIndex = -1;
        try (BufferedReader sortedReader = new BufferedReader(new FileReader(new File(sortedFile)), (int)Math.pow(2, 20))){
            String line;
            TreeMap<Integer, Integer> data = new TreeMap<>();
            String prevTerm = "";
            ArrayList<Integer> reviews = new ArrayList<>();
            ArrayList<Integer> frequencies = new ArrayList<>();
            while((line = sortedReader.readLine()) != null){
                if(!line.isEmpty()){
                    SortDataEntry dataEntry = new SortDataEntry(line);
                    String currentTerm = valueIDPair.get(dataEntry.getValueID());
                    int reviewID = dataEntry.getReviewID();
                    int frequency = dataEntry.getFrequency();
                    if(!currentTerm.equals(prevTerm)){
                        if (tokenIndex > -1){
                            reviews = new ArrayList<>(data.keySet());
                            frequencies = new ArrayList<>(data.values());
                            this.generatePostingList(reviews, tokenIndex, this.postinglistOutputName);
                            this.generateFreqList(frequencies, tokenIndex, this.frequenciesListOutputName);
                            this.freq[tokenIndex] = frequencies.stream().mapToInt(Integer::intValue).sum();
                            data = new TreeMap<>();
                        }
                        tokenIndex++;
                        if( tokenIndex % Consts.K == 0){
                            this.valuesPtr[(tokenIndex / Consts.K)] = stringBuilder.length();
                            this.prefix[tokenIndex] = 0;
                            stringBuilder.append(currentTerm);
                        }
                        else{
                            int prefixLength = getCommonPrefixLength(prevTerm, currentTerm);
                            this.prefix[tokenIndex] = prefixLength;
                            stringBuilder.append(currentTerm.substring(prefixLength));
                        }
                        this.length[tokenIndex] = (byte)(currentTerm.length());
                        prevTerm = currentTerm;
                    }
                    data.put(reviewID, frequency);
                }
            }
            if(data.size() > 0){
                if (tokenIndex > -1){
                    reviews = new ArrayList<>(data.keySet());
                    frequencies = new ArrayList<>(data.values());
                    this.generatePostingList(reviews, tokenIndex, this.postinglistOutputName);
                    this.generateFreqList(frequencies, tokenIndex, this.frequenciesListOutputName);
                    this.freq[tokenIndex] = frequencies.stream().mapToInt(Integer::intValue).sum();
                    data = new TreeMap<>();
                }
            }
            this.dictValues = stringBuilder.toString();

        } catch (IOException e){
            System.err.println(e.getMessage());
        }
    }


}
