package webdata;

import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        SlowIndexWriter slowIndexWriter = new SlowIndexWriter();
        slowIndexWriter.slowWrite("./src/smallDatasets/2.txt", "files");
        IndexReader indexReader = new IndexReader("files");
        System.out.println(indexReader.getTokenSizeOfReviews());
        slowIndexWriter.removeIndex("files");
    }
}
