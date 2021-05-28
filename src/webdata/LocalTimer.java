package webdata;

import java.util.concurrent.TimeUnit;

public class LocalTimer {
    private long startTime;
    private long endTime;

    public void start(){
        this.startTime = System.nanoTime();
    }

    public void end(){
        this.endTime = System.nanoTime();
    }

    public void printTime(String prefix){
        System.out.print(prefix + " ");
        System.out.println(Utils.timeBetweenString(this.startTime, this.endTime));
        this.startTime = 0;
        this.endTime = 0;
    }
}
