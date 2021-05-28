package webdata;

import java.io.BufferedReader;
import java.io.IOException;

public class ReaderClass implements Comparable<ReaderClass>{
    BufferedReader bufferedReader;
    SortDataEntry lineRead;

    public ReaderClass(BufferedReader bufferedReader, SortDataEntry lineRead){
        this.bufferedReader = bufferedReader;
        this.lineRead = lineRead;
    }

    public SortDataEntry getLineRead(){
        return lineRead;
    }

    public boolean forward() throws IOException {
        String line = bufferedReader.readLine();
        if (line != null){
            lineRead = new SortDataEntry(line);
            return true;
        }
        lineRead = null;
        bufferedReader.close();
        return false;
    }

    @Override
    public int compareTo(ReaderClass o) {
        return lineRead.compareTo(o.lineRead);
    }
}
