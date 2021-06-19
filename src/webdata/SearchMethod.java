package webdata;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

public abstract class SearchMethod {
    protected IndexReader indexReader;

    public SearchMethod(IndexReader indexReader){
        this.indexReader = indexReader;
    }

    protected Enumeration<Integer> bestReviews(int k, ReviewScoreData[] reviewWithScores) {
        Arrays.sort(reviewWithScores);
        int numOfBestResults = Math.min(k, reviewWithScores.length);
        Integer[] bestResults = new Integer[numOfBestResults];
        for (int i = 0; i < bestResults.length; ++i) {
            bestResults[i] = reviewWithScores[i].getReviewNumber();
        }

        Vector<Integer> bestReviews = new Vector<>(Arrays.asList(bestResults));
        return bestReviews.elements();
    }

}
