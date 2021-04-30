package webdata;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class ProductsDictionary extends Dictionary{

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
                byte prefixLength = getCommonPrefixLength(lastAddedID, productID);
                this.prefix[productIDIndex] = prefixLength;
                this.dictValues = this.dictValues.concat(productID.substring(prefixLength));
            }
            ArrayList<Integer> postingListRaw = allIDs.get(productID).get(allIDs.get(productID).firstKey());
            this.freq[productIDIndex] = allIDs.get(productID).firstKey();
            this.length[productIDIndex] = (byte)(productID.length());
            this.generatePostinglist(postingListRaw, productIDIndex, this.postinglistOutputName, false);
            productIDIndex++;
            lastAddedID = productID;
        }
    }
}
