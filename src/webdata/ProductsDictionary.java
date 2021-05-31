package webdata;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class ProductsDictionary extends Dictionary{

    public ProductsDictionary(int totalNumOfProducts, String sortedProductsFilePath, String outputPath, ArrayList<String> mapping){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.productsDictionary.name();
        this.postinglistOutputName = Consts.FILE_NAMES.productsPostinglist.name();
        this.frequenciesListOutputName = Consts.FILE_NAMES.freqList.name();
        this.initProps(totalNumOfProducts);
        this.buildDictionary(sortedProductsFilePath, mapping);
    }

    public ProductsDictionary(ArrayList<Review> parsedReviews, String outputPath){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.productsDictionary.name();;
        this.postinglistOutputName = Consts.FILE_NAMES.productsPostinglist.name();
        this.buildDictionary(this.combineIDs(parsedReviews), parsedReviews);
    }

    public ProductsDictionary(String outputPath){
        super();
        this.outputPath = outputPath + File.separator + Consts.SUB_DIRS.productsDictionary.name();;
        this.postinglistOutputName = Consts.FILE_NAMES.productsPostinglist.name();
        this.freq = this.readFreqFromDisk();
        this.valuesPtr = this.readValuesPtrFromDisk(true);
        this.postingListPtr = this.readPostinglistPtrFromDisk();
        this.dictValues = this.readConcatStringFromDisk();
        this.prefix = this.readPrefixFromDisk();
        this.length = this.readLengthFromDisk();
        this.numOfValues = this.readNumOfValuesFromDisk();
        this.numOfBlocks = (int)Math.ceil(numOfValues / (double)Consts.K);
    }

    private TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> combineIDs(ArrayList<Review> parsedReviews){
        TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allIDs = new TreeMap<>();
        for(int i = 0; i < parsedReviews.size(); i++){
            String productID = parsedReviews.get(i).getProductID();
            TreeMap<Integer, ArrayList<Integer>> newData = new TreeMap<>();
            if(allIDs.containsKey(productID)){
                int oldFreq = allIDs.get(productID).firstKey();
                int newFeq = oldFreq + 1;
                allIDs.get(productID).get(oldFreq).add(i + 1);
                newData.put(newFeq, allIDs.get(productID).get(oldFreq));
            }
            else{
                ArrayList<Integer> reviews = new ArrayList<>();
                reviews.add(i + 1);
                newData.put(1, reviews);
            }
            allIDs.put(productID, newData);
        }
        return allIDs;
    }

    @Override
    protected void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allIDs, ArrayList<Review> parsedReviews){
        this.initProps(allIDs);
        int productIDIndex = 0;
        String lastAddedID = "";
        for(String productID : allIDs.keySet()){
            if (productIDIndex % Consts.K == 0) {
                this.valuesPtr[(productIDIndex / Consts.K)] = this.dictValues.length();
                this.prefix[productIDIndex] = 0;
                this.dictValues = this.dictValues.concat(productID);
            }
            else {
                int prefixLength = getCommonPrefixLength(lastAddedID, productID);
                this.prefix[productIDIndex] = prefixLength;
                this.dictValues = this.dictValues.concat(productID.substring(prefixLength));
            }
            ArrayList<Integer> postingListRaw = allIDs.get(productID).get(allIDs.get(productID).firstKey());
            this.freq[productIDIndex] = allIDs.get(productID).firstKey();
            this.length[productIDIndex] = (byte)(productID.length());
            this.generatePostingList(postingListRaw, productIDIndex, this.postinglistOutputName);
            productIDIndex++;
            lastAddedID = productID;
        }
    }

    @Override
    protected void buildDictionary(String sortedFile, ArrayList<String> valueIDPair) {
        StringBuilder stringBuilder = new StringBuilder();
        long startTime = System.nanoTime();
        int productIndex = -1;
        try (BufferedReader sortedReader = new BufferedReader(new FileReader(sortedFile), (int)Math.pow(2, 20))){
            String line;
            TreeMap<Integer, Integer> data = new TreeMap<>();
            String prevProduct = "";
            ArrayList<Integer> reviews;
            ArrayList<Integer> frequencies = new ArrayList<>();
            while((line = sortedReader.readLine()) != null){
                if(!line.isEmpty()){
                    SortDataEntry dataEntry = new SortDataEntry(line);
                    String currentProduct = valueIDPair.get(dataEntry.getValueID());
                    int reviewID = dataEntry.getReviewID();
                    int frequency = dataEntry.getFrequency();
                    if(!currentProduct.equals(prevProduct)){
                        if (productIndex > -1){
                            reviews = new ArrayList<>(data.keySet());
                            frequencies = new ArrayList<>(data.values());
                            this.generatePostingList(reviews, productIndex, this.postinglistOutputName);
                            this.freq[productIndex] = frequencies.stream().mapToInt(Integer::intValue).sum();
                            data = new TreeMap<>();
                        }
                        productIndex++;
                        if( productIndex % Consts.K == 0){
                            this.valuesPtr[(productIndex / Consts.K)] = stringBuilder.length();
                            this.prefix[productIndex] = 0;
                            stringBuilder.append(currentProduct);
                        }
                        else{
                            int prefixLength = getCommonPrefixLength(prevProduct, currentProduct);
                            this.prefix[productIndex] = prefixLength;
                            stringBuilder.append(currentProduct.substring(prefixLength));
                        }
                        this.length[productIndex] = (byte)(currentProduct.length());
                        prevProduct = currentProduct;
                    }
                    data.put(reviewID, frequency);
                }
            }
            if(data.size() > 0){
                if (productIndex > -1){
                    reviews = new ArrayList<>(data.keySet());
                    frequencies = new ArrayList<>(data.values());
                    this.generatePostingList(reviews, productIndex, this.postinglistOutputName);
                    this.freq[productIndex] = frequencies.stream().mapToInt(Integer::intValue).sum();
                    data = new TreeMap<>();
                }
            }
            this.dictValues = stringBuilder.toString();

        } catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
