package webdata;

import java.util.HashMap;

public class Consts {
    public enum SUB_DIRS {tokensDictionary, productsDictionary ,metaData, tmp}
    public enum DATA_TYPES {tokens, products, metaData}
    public enum FILE_NAMES {
        ConcatString, Freq, freqList, freqListPtr, Length, NumOfValues,
        PostinglistPtr, Prefix, tokensPostinglist, productsPostinglist,
        ValuePtr, HelpFirst,
        HelpSecond, NumOfReviews, NumOfTokens,
        ProductIDs, Score
    }
    public static final int K = 100;
    public static final String TOKENS_REGEX = "[^A-Za-z0-9]";
    public static final int REVIEWS_PER_FILE = 10000;
    public static final int M = 1000;

    public static final String TEMP_TOKEN_PAIRS = "tokens_%d_%d.txt";
    public static final String TEMP_PRODUCT_IDS_PAIRS = "products_%d_%d.txt";
    public static final String DATA_SEPARATOR = ",";
    public enum SORTED_FILES_NAMES {sortedTokens, sortedProducts}

    public static final int NUM_OF_REVIEWS_TO_FIND = 30;
}
