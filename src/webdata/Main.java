package webdata;

import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
//        SlowIndexWriter slowIndexWriter = new SlowIndexWriter();
//        slowIndexWriter.slowWrite("./smallDatasets/100.txt", "files");
        IndexWriter indexWriter = new IndexWriter();
        indexWriter.write("./smallDatasets/100.txt", "files");
//        indexWriter.write("./bigDataset/movies.txt", "files");
        IndexReader indexReader = new IndexReader("files");
        System.out.println(indexReader.getTokenSizeOfReviews());
        indexWriter.removeIndex("files");
    }
}
