package webdata;

import java.util.Collection;
import java.util.Enumeration;

public class ReviewSearch {
    private VectorSpaceSearch vectorSpaceSearch;
    private LanguageModelSearch languageModelSearch;
    private ProductSearch productSearch;
    /**
     * Constructor
     */
    public ReviewSearch(IndexReader iReader) {
        this.vectorSpaceSearch = new VectorSpaceSearch(iReader);
        this.languageModelSearch = new LanguageModelSearch(iReader);
        this.productSearch = new ProductSearch(iReader, this.vectorSpaceSearch);
    }

    /**
     * Returns a list of the id-s of the k most highly ranked reviews for the
     * given query, using the vector space ranking function lnn.ltc (using the
     * SMART notation)
     * The list should be sorted by the ranking
     */
    public Enumeration<Integer> vectorSpaceSearch(Enumeration<String> query, int k) {
        return this.vectorSpaceSearch.search(query, k);
    }

    /**
     * Returns a list of the id-s of the k most highly ranked reviews for the
     * given query, using the language model ranking function, smoothed using a
     * mixture model with the given value of lambda
     * The list should be sorted by the ranking
     */
    public Enumeration<Integer> languageModelSearch(Enumeration<String> query, double lambda, int k) {
        return this.languageModelSearch.search(query, lambda, k);
    }

    /**
     * Returns a list of the id-s of the k most highly ranked productIds for the
     * * given query using a function of your choice
     * * The list should be sorted by the ranking
     * */
    public Collection<String> productSearch(Enumeration<String> query, int k) {
        return this.productSearch.search(query, k);
    }
}
