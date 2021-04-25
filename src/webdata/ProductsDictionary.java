package webdata;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class ProductsDictionary extends Dictionary{

    public ProductsDictionary(ArrayList<Review> parsedReviews, String outputPath){
        super();
        this.outputPath = outputPath;
        this.buildDictionary(this.combineIDs(parsedReviews));
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
    protected void buildDictionary(TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> allIDs){
        this.initProps(allIDs);
        int productIDIndex = 0;
        String lastAddedID = "";
        for(String productID : allIDs.keySet()){
            if (productIDIndex % K == 0) {
                this.valuesPtr[(productIDIndex / K)] = this.concatString.length();
                this.prefix[productIDIndex] = 0;
                this.concatString = this.concatString.concat(productID);
            }
            else {
                byte prefixLength = getCommonPrefixLength(lastAddedID, productID);
                this.prefix[productIDIndex] = prefixLength;
                this.concatString = this.concatString.concat(productID.substring(prefixLength));
            }
            ArrayList<Integer> postingListRaw = allIDs.get(productID).get(allIDs.get(productID).firstKey());
            this.freq[productIDIndex] = allIDs.get(productID).firstKey();
            this.length[productIDIndex] = (byte)(productID.length());
            this.generatePostinglist(postingListRaw, productIDIndex, "productIDsPostinglist");
            productIDIndex++;
            lastAddedID = productID;
        }
    }
}
