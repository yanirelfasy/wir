package webdata.tests;
import java.io.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import webdata.SlowIndexWriter;


class SlowIndexWriterTest {

    @Test
    public void createOutputFolder(){
            SlowIndexWriter slowIndexWriter = new SlowIndexWriter();
            slowIndexWriter.makeOutputDir("indexOutput");
            File outputDir = new File("indexOutput");
            Assertions.assertTrue(outputDir.isDirectory());
            Assertions.assertTrue(outputDir.exists());
    }
}