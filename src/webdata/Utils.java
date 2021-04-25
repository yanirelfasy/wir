package webdata;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Utils {

    public static long writeToFile(ArrayList<Byte> encodedData, String output){
        long positionInFile = -1;
        try (RandomAccessFile resultFile = new RandomAccessFile(output, "rw")){
            resultFile.seek(resultFile.length());
            positionInFile = resultFile.getFilePointer();
            for (byte encodedByte : encodedData) {
                resultFile.writeByte(encodedByte);
            }
            return positionInFile;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return positionInFile;
        }
    }
}
