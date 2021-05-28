package webdata;

import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
//        SlowIndexWriter slowIndexWriter = new SlowIndexWriter();
//        slowIndexWriter.slowWrite("./smallDatasets/100.txt", "files");
        long startTime = System.nanoTime();
        IndexWriter indexWriter = new IndexWriter();
        int datasetSize = 184886;
//        indexWriter.write("./smallDatasets/100.txt", "files", datasetSize);
        indexWriter.write("./bigDataset/Baby.txt", "files", datasetSize);
        System.out.println("INDEX CREATION TOOK " + Utils.timeBetweenString(startTime, System.nanoTime()));
        IndexReader indexReader = new IndexReader("files");
        System.out.println(indexReader.getTokenSizeOfReviews());
        indexWriter.removeIndex("files");
    }
}
