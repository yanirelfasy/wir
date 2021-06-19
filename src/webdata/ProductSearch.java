package webdata;

import java.util.*;

public class ProductSearch extends SearchMethod{

    private VectorSpaceSearch vectorSpaceSearch;
    private int scoresSum;

    public ProductSearch(IndexReader indexReader, VectorSpaceSearch vectorSpaceSearch){
        super(indexReader);
        this.vectorSpaceSearch = vectorSpaceSearch;
        scoresSum = 0;
    }

    /**
     * Gives a weight to each review according to it's place inside the results array.
     * @param numOfReviews The number of reviews in the results array.
     * @return weights An array of double that contains the weights of each review. All the weights sum to 1.
     */
    private double[] getReviewsWeight(int numOfReviews){
        double[] weights = new double[numOfReviews];
        int denominator = (numOfReviews * (numOfReviews + 1)) / 2;
        for (int i = 0; i < numOfReviews; ++i) {
            weights[i] = ((double) (numOfReviews - i)) / denominator;
        }
        return weights;
    }

    /**
     * Calculates the weight of the relevant product IDs
     * @param candidatesArray Array of top reviews
     * @param weightsArray Weights of the reviews.
     * @return A map of productID and weight. in case of similar product IDs, we sum the weights.
     */
    private HashMap<String, Double> getProductIDsWithWeights(List<Integer> candidatesArray, double[] weightsArray){
        HashMap<String, Double> productIDsWithWeight = new HashMap<>();
        for (int i = 0; i < candidatesArray.size(); i++) {
            String productId = this.indexReader.getProductId(candidatesArray.get(i));
            if (productIDsWithWeight.containsKey(productId)) {
                double weight = productIDsWithWeight.get(productId);
                productIDsWithWeight.put(productId, weight + weightsArray[i]);
            } else {
                productIDsWithWeight.put(productId, weightsArray[i]);
            }
        }
        return productIDsWithWeight;
    }

    /**
     * Calculates the score of the products according to the reviews in the positinglist and the weights.
     * @param productIDsWeights The weights of the products
     * @return a map of productID and it's score.
     */
    private HashMap<String, Double> getProductsScore(HashMap<String, Double> productIDsWeights){
        HashMap<String, Double> productsScore = new HashMap<>();
        for (String productId: productIDsWeights.keySet()) {
            Enumeration<Integer> productReviews = this.indexReader.getProductReviews(productId);

            ArrayList<Double> newReviewScores = new ArrayList<>();
            while (productReviews.hasMoreElements()) {
                int reviewID = productReviews.nextElement();
                int score = this.indexReader.getReviewScore(reviewID);
                int firstHelpfulness = this.indexReader.getReviewHelpfulnessNumerator(reviewID);
                int secondHelpfulness = this.indexReader.getReviewHelpfulnessDenominator(reviewID);
                if (firstHelpfulness <= secondHelpfulness) {
                    double helpfulness = (secondHelpfulness == 0) ?  1 : ((double) firstHelpfulness) / secondHelpfulness;
                    newReviewScores.add(score * helpfulness);
                }
            }

            // Calculate product score
            Collections.sort(newReviewScores);
            int numOfReviews = newReviewScores.size();
            double median;
            if (numOfReviews % 2 == 0) {
                median = (newReviewScores.get(numOfReviews / 2) + newReviewScores.get((numOfReviews / 2) - 1)) / 2;
            } else {
                median = newReviewScores.get(numOfReviews / 2);
            }
            double avg = 0;
            for (double newScore: newReviewScores) {
                avg += newScore;
            }
            avg /= numOfReviews;

            double newScore = (avg + median) / 2;
            productsScore.put(productId, newScore);
            this.scoresSum += newScore;
        }
        return productsScore;
    }

    public Collection<String> search(Enumeration<String> query, int k) {
        this.scoresSum = 0;
        // Get list of all relevant reviews the fit the query
        Enumeration<Integer> candidateEnum = this.vectorSpaceSearch.search(query, Consts.NUM_OF_REVIEWS_TO_FIND);
        List<Integer> candidatesArray = Collections.list(candidateEnum);

        // Calculate the weight of each review.
        double[] weights = this.getReviewsWeight(candidatesArray.size());

        // Extract Product IDs of each review.
        HashMap<String, Double> productIDsWeights = this.getProductIDsWithWeights(candidatesArray, weights);

        // Iterate over all reviews of all products
        HashMap<String, Double> productNewScores = this.getProductsScore(productIDsWeights);

        // Calculate final weight and normalize
        ProductScoreData[] productsScoreData = new ProductScoreData[productIDsWeights.size()];
        int i = 0;
        for (String productId: productIDsWeights.keySet()) {
            double normalizedScore = productNewScores.get(productId) / this.scoresSum;
            double curWeight = productIDsWeights.get(productId);
            productsScoreData[i] = new ProductScoreData(productId, (curWeight + normalizedScore) / 2);
            i++;
        }

        // Sort by values and return top k
        Arrays.sort(productsScoreData);
        int numOfBestResults = Math.min(k, productsScoreData.length);
        ArrayList<String> bestResults = new ArrayList<>();
        for (i = 0; i < numOfBestResults; ++i) {
            bestResults.add(productsScoreData[i].getProductId());
        }
        return bestResults;
    }
}
