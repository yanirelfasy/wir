package webdata;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static ArrayList<Integer> splitAtOdd(ArrayList<Integer> toSplit){
        ArrayList<Integer> result = new ArrayList<>();
        for(int i = 0; i < toSplit.size(); i++){
            if(i % 2 != 0){
                result.add(toSplit.get(i));
            }
        }
        return result;
    }

    public static ArrayList<Integer> splitAtEven(ArrayList<Integer> toSplit){
        ArrayList<Integer> result = new ArrayList<>();
        for(int i = 0; i < toSplit.size(); i++){
            if(i % 2 == 0){
                result.add(toSplit.get(i));
            }
        }
        return result;
    }

    public static ArrayList<Byte> convertToBytesList(byte[] arr){
        ArrayList<Byte> result = new ArrayList<>();
        for(byte fragment : arr){
            result.add(fragment);
        }
        return result;
    }

    public static byte[] convertToByteArray(ArrayList<Byte> list){
        byte[] result = new byte[list.size()];
        for( int i = 0; i < list.size(); i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static int[] convertToIntArray(ArrayList<Integer> list){
        int[] result = new int[list.size()];
        for( int i = 0; i < list.size(); i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static long[] convertToLongArray(ArrayList<Long> list){
        long[] result = new long[list.size()];
        for( int i = 0; i < list.size(); i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static int[] readIntArrayFromDisk(String path){
        try{
            Path inputFilePath = Paths.get(path);
            byte[] resultAsBytes = Files.readAllBytes(inputFilePath);
            ArrayList<Integer> result = GroupVarint.decodeSequence(resultAsBytes, false);
            return Utils.convertToIntArray(result);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return new int [0];
    }

    public static String readStringFromDisk(String path){
        try{
            Path inputFilePath = Paths.get(path);
            byte[] resultAsBytes = Files.readAllBytes(inputFilePath);
            return new String(resultAsBytes);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return "";
    }

    public static int readIntFromDisk(String path){
        try{
            Path inputFilePath = Paths.get(path);
            byte[] resultAsBytes = Files.readAllBytes(inputFilePath);
            return Utils.convertBytesToInt(GroupVarint.makeFourByteArray(resultAsBytes));
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public static int convertBytesToInt(byte[] value){
        ByteBuffer buffer = ByteBuffer.wrap(value);
        return buffer.getInt();
    }
}
