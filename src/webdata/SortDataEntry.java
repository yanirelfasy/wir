package webdata;

import static webdata.Consts.DATA_SEPARATOR;

public class SortDataEntry implements Comparable<SortDataEntry>{

    private static final int VALUE_INDEX = 0;
    private static final int REVIEW_ID_INDEX = 1;
    private static final int FREQUENCY_INDEX = 2;

    private int valueID;
    private int reviewID;
    private int frequency;

    public SortDataEntry(String dataAsString){
        String[] rawData = dataAsString.split(DATA_SEPARATOR);
        this.valueID = Integer.parseInt(rawData[VALUE_INDEX]);
        this.reviewID = Integer.parseInt(rawData[REVIEW_ID_INDEX]);
        this.frequency = Integer.parseInt(rawData[FREQUENCY_INDEX]);
    }

    public int getValueID(){ return this.valueID; }
    public int getReviewID(){ return this.reviewID; }
    public int getFrequency(){ return this.frequency; }

    @Override
    public int compareTo(SortDataEntry o) {
        if (this.valueID == o.valueID){
            return this.reviewID - o.reviewID;
        }
        return this.valueID - o.valueID;
    }

    @Override
    public String toString() {
        return this.valueID + DATA_SEPARATOR + this.reviewID  + DATA_SEPARATOR + this.frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SortDataEntry)) {
            return false;
        }
        SortDataEntry l = (SortDataEntry) o;
        return compareTo(l) == 0;
    }
}
