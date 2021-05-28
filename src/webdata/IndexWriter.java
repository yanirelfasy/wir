package webdata;

import java.io.File;
import java.util.ArrayList;

public class IndexWriter {
    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void write(String inputFile, String dir, int datasetSize) {
        this.makeOutputDir(dir);
        Parser parser = new Parser();
        parser.parseFile(inputFile, datasetSize);

        ReviewsMetaData reviewsMetaData = new ReviewsMetaData(parser);
        reviewsMetaData.writeDataToDisk(dir + File.separator + Consts.SUB_DIRS.metaData);
        reviewsMetaData.clearData();
        parser.clearMetaData();

        this.makeTempFolder("./files/tmp");
        SortManager sorter = new SortManager(new ArrayList<>(parser.getTokenSet()), new ArrayList<>(parser.getProductIdSet()), "./files/tmp");
        String tokensSortResult = dir + File.separator + "sortedTokens";
        String productsSortResult = dir + File.separator + "sortedProducts";
        sorter.sort(inputFile, tokensSortResult, productsSortResult, datasetSize);
        removeIndex("./files/tmp");
        TokensDictionary tokensDictionary = new TokensDictionary(parser.getTotalNumOfTokens(), tokensSortResult, dir, sorter.getRawTokens());
        tokensDictionary.writeDictionaryToDisk();
        tokensDictionary = null;
        ProductsDictionary productsDictionary = new ProductsDictionary(parser.getNumOfproducts(), productsSortResult, dir, sorter.getRawProductIDs());
        productsDictionary.writeDictionaryToDisk();
        productsDictionary = null;
    }
    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {}

    public void makeOutputDir(String dir){
        File outputDir = new File(dir);
        if(outputDir.exists()){
            this.removeIndex(dir);
        }
        outputDir.mkdir();
        String[] subDirs = {Consts.SUB_DIRS.tokensDictionary.name(), Consts.SUB_DIRS.productsDictionary.name(), Consts.SUB_DIRS.metaData.name()};
        for (String subDir : subDirs){
            outputDir = new File(dir + File.separator + subDir);
            outputDir.mkdir();
        }
    }

    private void makeTempFolder(String tempPath){
        File outputDir = new File(tempPath);
        outputDir.mkdir();
    }
}
