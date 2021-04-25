package webdata;

import java.math.BigInteger;
import java.util.ArrayList;

public class GroupVarint {

    public static ArrayList<Byte> compress(ArrayList<Integer> values){
        byte sizesByte = 0;
        ArrayList<Byte> compressed = new ArrayList<>();
        ArrayList<Byte> container = new ArrayList<>();
        int prevValue = 0;
        int groupsCounter = 1;
        for(int value : values){
            ArrayList<Byte> gap = convertIntToBytes(value - prevValue);
            int gapSize = gap.size();
            sizesByte = (byte)((sizesByte << 2) + (gapSize - 1));
            container.addAll(gap);
            if (groupsCounter % 4 == 0){
                compressed.add(sizesByte);
                compressed.addAll(container);
                sizesByte = 0;
                container.clear();
            }
            prevValue = value;
            groupsCounter++;
        }
        if(!container.isEmpty()){
            compressed.add(sizesByte);
            compressed.addAll(container);
        }
        return compressed;
    }

    private static ArrayList<Byte> convertIntToBytes(int value){
        BigInteger num = BigInteger.valueOf(value);
        ArrayList<Byte> byteResult = new ArrayList<>();
        for(byte fragment : num.toByteArray()){
            byteResult.add(fragment);
        }
        return byteResult;
    }

}
