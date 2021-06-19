package webdata;

public class ProductScoreData implements Comparable<ProductScoreData>{
    private String productId;
    private double score;

    /**
     * Constructor
     */
    public ProductScoreData(String productId, double score) {
        this.productId = productId;
        this.score = score;
    }

    public String getProductId() {
        return productId;
    }

    public double getScore() {
        return score;
    }

    /**
     * Returns an int representing the order between this line and the given other line.
     * @param o The other line to compare to.
     * @return Negative number if this line is smaller than other, 0 if equal, and positive if it is greater.
     */
    @Override
    public int compareTo(ProductScoreData o) {
        double res = o.score - this.score;
        if (res > 0) return 1;
        else if (res == 0) return this.productId.compareTo(o.productId);
        else return -1;
    }
}
