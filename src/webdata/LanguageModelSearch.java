package webdata;

import java.util.*;

public class LanguageModelSearch extends SearchMethod{

    public LanguageModelSearch(IndexReader indexReader){
        super(indexReader);
    }

    private double mcProbCalculation(String token) {
        return ((double) this.indexReader.getTokenCollectionFrequency(token)) / ((double) this.indexReader.getTokenSizeOfReviews());
    }

    private double mdProbCalculation(String token, int reviewId) {
        Enumeration<Integer> reviewsWithFrequency = this.indexReader.getReviewsWithToken(token);
        int currentReviewID;
        double currentFrequency;
        while (reviewsWithFrequency.hasMoreElements()) {
            currentReviewID = reviewsWithFrequency.nextElement();
            currentFrequency = reviewsWithFrequency.nextElement();
            if (currentReviewID == reviewId) {
                return currentFrequency / (double) this.indexReader.getReviewLength(reviewId);
            }
        }
        return 0;
    }

    private double mixtureModelPerReview(List<String> query, double lambda, int reviewId) {
        double score = 1;
        double prob;
        for (String term: query) {
            prob = (lambda * mdProbCalculation(term, reviewId)) + ((1 - lambda) * mcProbCalculation(term));
            score *= prob;
        }
        return score;
    }

    private Set<Integer> getRelevantReviews(Set<String> querySet) {
        Set<Integer> result = new HashSet<>();
        int currentReviewID;
        for (String term: querySet) {
            Enumeration<Integer> termPostingList = this.indexReader.getReviewsWithToken(term);
            while (termPostingList.hasMoreElements()) {
                currentReviewID = termPostingList.nextElement();
                termPostingList.nextElement();
                result.add(currentReviewID);
            }
        }
        return result;
    }

    public Enumeration<Integer> search(Enumeration<String> query,double lambda, int k) {
        ArrayList<String> queryList = new ArrayList<>();
        while (query.hasMoreElements()) {
            queryList.add(query.nextElement().toLowerCase());
        }
        Set<String> querySet = new HashSet<>(queryList);
        Set<Integer> relevantReviews = getRelevantReviews(querySet);

        ReviewScoreData[] reviewWithScores = new ReviewScoreData[relevantReviews.size()];
        int i = 0;
        double score;
        for (int reviewId: relevantReviews) {
            score = mixtureModelPerReview(queryList, lambda, reviewId);
            ReviewScoreData reviewScoreData = new ReviewScoreData(reviewId, score);
            reviewWithScores[i] = reviewScoreData;
            i++;
        }
        return this.bestReviews(k, reviewWithScores);
    }
}
