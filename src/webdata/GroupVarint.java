package webdata;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class GroupVarint {

    public static ArrayList<Byte> compress(ArrayList<Integer> values, boolean encodeAsGaps){
        byte sizesByte = 0;
        ArrayList<Byte> compressed = new ArrayList<>();
        ArrayList<Byte> container = new ArrayList<>();
        int prevValue = 0;
        int index = 0;
        int groupsCounter = 1;
        for(int value : values){
            ArrayList<Byte> gap;
            gap = convertIntToBytes(value - prevValue);
            int gapSize = gap.size();
            sizesByte = (byte)((sizesByte << 2) + (gapSize - 1));
            container.addAll(gap);
            if (groupsCounter % 4 == 0){
                compressed.add(sizesByte);
                compressed.addAll(container);
                sizesByte = 0;
                groupsCounter = 0;
                container.clear();
            }
            prevValue = encodeAsGaps ? value : 0;
            groupsCounter++;
            index++;
        }
        if(!container.isEmpty()){
            for(int i = 0; i < 4 - (groupsCounter - 1); i++){
                sizesByte = (byte)(sizesByte << 2);
            }
            compressed.add(sizesByte);
            compressed.addAll(container);
        }
        return compressed;
    }

    public static ArrayList<Byte> compress(int[] values, boolean encodeAsGaps) {
        byte sizesByte = 0;
        ArrayList<Byte> compressed = new ArrayList<>();
        ArrayList<Byte> container = new ArrayList<>();
        int prevValue = 0;
        int index = 0;
        int groupsCounter = 1;
        for(int value : values){
            ArrayList<Byte> gap;
            gap = convertIntToBytes(value - prevValue);
            int gapSize = gap.size();
            sizesByte = (byte)((sizesByte << 2) + (gapSize - 1));
            container.addAll(gap);
            if (groupsCounter % 4 == 0){
                compressed.add(sizesByte);
                compressed.addAll(container);
                sizesByte = 0;
                groupsCounter = 0;
                container.clear();
            }
            prevValue = encodeAsGaps ? value : 0;
            groupsCounter++;
            index++;
        }
        if(!container.isEmpty()){
            for(int i = 0; i < 4 - (groupsCounter - 1); i++){
                sizesByte = (byte)(sizesByte << 2);
            }
            compressed.add(sizesByte);
            compressed.addAll(container);
        }
        return compressed;
    }

    public static ArrayList<Byte> compress(long[] values, boolean encodeAsGaps) {
        byte sizesByte = 0;
        ArrayList<Byte> compressed = new ArrayList<>();
        ArrayList<Byte> container = new ArrayList<>();
        long prevValue = 0;
        int groupsCounter = 1;
        for(long value : values){
            ArrayList<Byte> gap = convertIntToBytes((int)(value - prevValue));
            int gapSize = gap.size();
            sizesByte = (byte)((sizesByte << 2) + (gapSize - 1));
            container.addAll(gap);
            if (groupsCounter % 4 == 0){
                compressed.add(sizesByte);
                compressed.addAll(container);
                sizesByte = 0;
                container.clear();
            }
                prevValue = encodeAsGaps ? value : 0;
            groupsCounter++;
        }
        if(!container.isEmpty()){
            for(int i = 0; i < groupsCounter - 1; i++){
                sizesByte = (byte)(sizesByte << 2);
            }
            compressed.add(sizesByte);
            compressed.addAll(container);
        }
        return compressed;
    }

    public static ArrayList<Byte> convertIntToBytes(int value){
        BigInteger num = BigInteger.valueOf(value);
        ArrayList<Byte> byteResult = new ArrayList<>();
        for(byte fragment : num.toByteArray()){
            byteResult.add(fragment);
        }
        return byteResult;
    }

    public static ArrayList<Byte> convertLongToBytes(long value){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        ArrayList<Byte> byteResult = new ArrayList<>();
        for(byte fragment : buffer.array()){
            byteResult.add(fragment);
        }
        return byteResult;
    }



    private static long convertBytesToLong(byte[] value){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(value);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static int[] decodeSizesByte(byte sizes) {
        int[] decoded = new int[4];
        byte slicer = 3;
        for (int i = 3; i >= 0; i--) {
            decoded[i] = (sizes & slicer) + 1;
            sizes >>= 2;
        }
        return decoded;
    }

    public static ArrayList<Integer> decodeSequence(byte[] sequence, boolean decodeGaps){
        int bytesRead = 0;
        int [] sizes;
        int prevValue = 0;
        ArrayList<Integer> result = new ArrayList<>();
        while(bytesRead < sequence.length){
            sizes = decodeSizesByte(sequence[bytesRead]);
            bytesRead++;
            for(int size : sizes){
                if(bytesRead + size <= sequence.length){
                    byte [] subSequence = Arrays.copyOfRange(sequence, bytesRead, bytesRead + size);
                    subSequence = GroupVarint.makeFourByteArray(subSequence);
                    int converted = Utils.convertBytesToInt(subSequence);
                    result.add(converted + prevValue);
                    prevValue = decodeGaps ? converted + prevValue : 0;
                    bytesRead += size;
                }
                else{
                    break;
                }
            }
        }
        return result;
    }

    public static ArrayList<Long> decodeSequenceAsLong(byte[] sequence, boolean decodeGaps){
        int bytesRead = 0;
        int [] sizes;
        long prevValue = 0;
        ArrayList<Long> result = new ArrayList<>();
        while(bytesRead < sequence.length){
            sizes = decodeSizesByte(sequence[bytesRead]);
            bytesRead++;
            for(int size : sizes){
                if(bytesRead + size <= sequence.length){
                    byte [] subSequence = Arrays.copyOfRange(sequence, bytesRead, bytesRead + size);
                    subSequence = GroupVarint.makeEightByteArray(subSequence);
                    long converted = GroupVarint.convertBytesToLong(subSequence);
                    result.add(converted + prevValue);
                    prevValue = decodeGaps ? converted + prevValue : 0;
                    bytesRead += size;
                }
                else{
                    break;
                }
            }
        }
        return result;
    }

    public static byte[] makeFourByteArray(byte[] arr){
        byte[] result = {0, 0, 0, 0};
        int addedIndex = 0;
        if(arr.length < 4){
            for(int i = 0; i < 4; i++){
                if(i > 4 - arr.length - 1){
                    result[i] = arr[addedIndex];
                    addedIndex++;
                }
            }
        }
        return result;
    }


    public static byte[] makeEightByteArray(byte[] arr){
        byte[] result = {0, 0, 0, 0, 0, 0, 0, 0};
        int addedIndex = 0;
        if(arr.length < 8){
            for(int i = 0; i < 8; i++){
                if(i > 8 - arr.length - 1){
                    result[i] = arr[addedIndex];
                    addedIndex++;
                }
            }
        }
        return result;
    }

}
