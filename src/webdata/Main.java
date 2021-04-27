package webdata;

import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        SlowIndexWriter slowIndexWriter = new SlowIndexWriter();
        slowIndexWriter.slowWrite("./src/smallDatasets/2.txt", "files");
        IndexReader indexReader = new IndexReader("files");
        Enumeration<Integer> e = indexReader.getProductReviews("B001E9KFG0");
        while(e.hasMoreElements()){
            System.out.println(e.nextElement());
        }
        slowIndexWriter.removeIndex("files");
    }
}
