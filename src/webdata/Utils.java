package webdata;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static long writeToFile(byte[] encodedData, String output){
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

    public static int[] readIntArrayFromDisk(String path, boolean asGaps){
        try{
            Path inputFilePath = Paths.get(path);
            byte[] resultAsBytes = Files.readAllBytes(inputFilePath);
            ArrayList<Integer> result = GroupVarint.decodeSequence(resultAsBytes, asGaps, false);
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

    public static String getFieldContent(String line, String fieldTitle){
        String regexCommand = String.format("^%s: (.*)", fieldTitle);
        Matcher regexWorker = Pattern.compile(regexCommand).matcher(line);
        String result = "-1";
        if(regexWorker.find()){
            result = regexWorker.group(1);
        }
        return result;
    }

    private static String getTimeString(long timeInSeconds){
        long seconds = timeInSeconds % 60;
        long hours = timeInSeconds / 60;
        long minutes = hours % 60;
        hours = hours / 60;
        String hoursString = String.valueOf(hours);
        String minutesString = String.valueOf(minutes);
        String secondsString = String.valueOf(seconds);
        if(seconds < 10){
            secondsString = "0" + secondsString;
        }
        if(minutes < 10){
            minutesString = "0" + minutesString;
        }
        if(hours < 10){
            hoursString = "0" + hoursString;
        }
        return hoursString + ":" + minutesString + ":" + secondsString;
    }

    public static void printProgress(int current, int total, String stepName, long startTime){
        int percent = (current * 100) / total;
        int progressBars = (50 * percent) / 100;
        String progress = "|                                                  |";
        long elapsedTime = System.nanoTime() - startTime;
        long seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        if(percent < 100){
            for(int i = 0; i < progressBars; i++){
                progress = progress.replaceFirst(" ", "=");
            }
            progress = stepName + " " + progress + " " + percent + "%   Duration: " + getTimeString(seconds) + "\r";
        }
        else{
            progress = stepName + " DONE - Duration: " + getTimeString(seconds) + "\n";
        }
        System.out.print(progress);
    }

    public static String timeBetweenString(long startTime, long endTime){
        long elapsedTime = endTime - startTime;
        long seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        return getTimeString(seconds);
    }
}
