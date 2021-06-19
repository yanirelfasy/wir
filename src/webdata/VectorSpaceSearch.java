package webdata;

import java.util.*;

public class VectorSpaceSearch extends SearchMethod{
    public VectorSpaceSearch(IndexReader indexReader){
        super(indexReader);
    }

    private double[] ltfCalculation(double[] termFrequencies) {
        Arrays.parallelSetAll(termFrequencies, i -> ((termFrequencies[i] == 0) ? 0 : (1 + Math.log10(termFrequencies[i]))));
        return termFrequencies;
    }

    private double[] tidfCalculation(double[] termFrequencies) {
        int numOfReviews = this.indexReader.getNumberOfReviews();
        Arrays.parallelSetAll(termFrequencies, i -> ((termFrequencies[i] == 0) ? 0 : (Math.log10(numOfReviews / termFrequencies[i]))));
        return termFrequencies;
    }

    private TreeMap<String, Integer> histogramQuery(Enumeration<String> query) {
        List<String> queryList = Collections.list(query);
        TreeMap<String, Integer> hist = new TreeMap<>();
        for (String term: queryList) {
            Integer freq = (hist.keySet().contains(term.toLowerCase())) ? hist.get(term.toLowerCase()) + 1 : 1;
            hist.put(term.toLowerCase(), freq);
        }
        return hist;
    }

    private TreeMap<Integer, TreeMap<String, Integer>> getReviews(Set<String> queryTerms) {
        TreeMap<Integer, TreeMap<String, Integer>> reviews = new TreeMap<>();
        for (String term: queryTerms) {
            Enumeration<Integer> termReviewAndFrequencies = this.indexReader.getReviewsWithToken(term);
            while (termReviewAndFrequencies.hasMoreElements()) {
                int reviewNumber = termReviewAndFrequencies.nextElement();
                int frequencyInReview = termReviewAndFrequencies.nextElement();
                TreeMap<String, Integer> currentMap;
                if (reviews.containsKey(reviewNumber)) {
                    currentMap = reviews.get(reviewNumber);
                } else {
                    currentMap = new TreeMap<>();
                    for (String t: queryTerms) {
                        currentMap.put(t, 0);
                    }
                }
                currentMap.replace(term, frequencyInReview);
                reviews.put(reviewNumber, currentMap);
            }
        }
        return reviews;
    }

    private double[] queryLTCCalculation(TreeMap<String, Integer> queryHist) {
        double[] ltf = ltfCalculation(Utils.intCollectionToDoubleArr(queryHist.values()));
        double[] termFrequencies = new double[ltf.length];
        int i = 0;
        for (String token: queryHist.keySet()) {
            termFrequencies[i] = this.indexReader.getTokenFrequency(token);
            i++;
        }
        tidfCalculation(termFrequencies);

        double[] ltc = new double[ltf.length];
        double cosNorm = 0;
        for (i = 0; i < ltf.length; i++) {
            ltc[i] = ltf[i] * termFrequencies[i];
            cosNorm += Math.pow(ltc[i], 2);
        }
        cosNorm = Math.sqrt(cosNorm);
        final double constForLoop = cosNorm; // Since parallelSetAll requires this var to be final.
        if (constForLoop != 0) {
            Arrays.parallelSetAll(ltc, j -> (ltc[j] / constForLoop));
        }
        return ltc;
    }

    private double calcReviewScore(double[] termFrequency, double[] qqq) {
        ltfCalculation(termFrequency);
        double score = 0;
        for (int i = 0; i < qqq.length; i++) {
            score += termFrequency[i] * qqq[i];
        }
        return score;
    }


    public Enumeration<Integer> search(Enumeration<String> query, int k){
        TreeMap<String, Integer> queryHist = histogramQuery(query);
        double[] ltc = queryLTCCalculation(queryHist);
        TreeMap<String, Double> queryVec = new TreeMap<>();
        int i = 0;
        for (String term: queryHist.keySet()) {
            if (ltc[i] != 0) {
                queryVec.put(term, ltc[i]);
            }
            i++;
        }
        TreeMap<Integer, TreeMap<String, Integer>> reviews = getReviews(queryVec.keySet());
        ReviewScoreData[] reviewWithScores = new ReviewScoreData[reviews.size()];
        i = 0;
        for (Integer review: reviews.keySet()) {
            double score = calcReviewScore(Utils.intCollectionToDoubleArr(reviews.get(review).values()),
                    Utils.doubleCollectionToDoubleArr(queryVec.values()));
            ReviewScoreData reviewScoreData = new ReviewScoreData(review, score);
            reviewWithScores[i] = reviewScoreData;
            i++;
        }
        return this.bestReviews(k, reviewWithScores);
    }
}
