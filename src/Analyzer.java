package webdata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.Random;

public class Analyzer {

    public IndexWriter indexWriter;
    public IndexReader indexReader;
    private final int[] sizes = {100, 1000, 10000, 100000, 1000000};


    public void analyze(String inputFile){
        File analyzePath = new File("analyze");
        if(!analyzePath.exists()){
            analyzePath.mkdir();
        }
        for (int size : sizes){
            this.indexWriter = new IndexWriter();
            this.indexWriter.makeOutputDir("analyze/files_" + size);
            this.indexWriter.makeLogsFolder("analyze/files_" + size);
            this.writeToLog(size, "===================================");
            this.writeToLog(size, size + " Reviews");
            this.writeToLog(size, "===================================");
            System.out.println("WORKING ON " + size + " Reviews");
            long startTime = System.nanoTime();
            this.indexWriter.write(inputFile, "analyze/files_" + size, size);
            long endTime = System.nanoTime();
            long seconds = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
            String unitsString = "MS";
            this.writeToLog(size, "INDEX CREATION TOOK: " + seconds + " " + unitsString);
            this.writeToLog(size, "INDEX SIZE: " + getIndexSize(Paths.get("analyze/files_" + size)));
            randomCalls(size);
            // MAKE 100 RANDOM REQUESTS TO getReviewsWithToken and long the time
//            indexWriter.removeIndex("files");
        }
    }

    private void randomCalls(int size){
        indexReader = new IndexReader("analyze/files_" + size);
        HashSet<String> tokens = this.getRandomTokes(100, indexReader);
        long startTime = System.nanoTime();
        for(String token : tokens){
            indexReader.getReviewsWithToken(token);
        }
        long endTime = System.nanoTime();
        long seconds = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        this.writeToLog(size, "getReviewsWithToken TOOK: " + seconds + " MS");
        startTime = System.nanoTime();
        for(String token : tokens){
            indexReader.getTokenFrequency(token);
        }
        endTime = System.nanoTime();
        seconds = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
        this.writeToLog(size, "getTokenFrequency TOOK: " + seconds + " MS");
    }

    private HashSet<String> getRandomTokes(int numOfTokens, IndexReader indexReader){
        HashSet<String> tokens = new HashSet<>();
        Random rnd = new Random();
        while(tokens.size() < numOfTokens){
            tokens.add(indexReader.getToken(rnd.nextInt(1000)));
        }
        return tokens;
    }

    private long getIndexSize(Path path){
        long size = 0;
        try (Stream<Path> walk = Files.walk(path)) {
            size = walk
                    //.peek(System.out::println) // debug
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        // ugly, can pretty it with an extract method
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            System.out.printf("Failed to get size of %s%n%s", p, e);
                            return 0L;
                        }
                    })
                    .sum();

        } catch (IOException e) {
            System.out.printf("IO errors %s", e);
        }

        return size;

    }

    private void writeToLog(int size, String data){
        try(FileWriter fw = new FileWriter("./analyze/files_" + size + File.separator + "logs" + File.separator + "log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
            {
                out.println(data + "\n");
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
    }
}
