package org.saliya.dsctools.davs;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.DoubleStream;

/**
 * Created by pulasthi on 6/9/16.
 */
public class FileSpliter {

    public static void main(String[] args) {
        String filename = args[0];
        int numberOfFiles = 20;
        int currentFileNuber = 0;
        ListMultimap<Double, String> sortingmap = MultimapBuilder.treeKeys().arrayListValues().build();

       try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
       //    String outputFileName = filename.replace(".txt",".sorted.experimentClusters.txt");

        //   FileWriter writer = new FileWriter(outputFileName);
       //    PrintWriter printWriter = new PrintWriter(writer);
           String line;
           double mindiff = 1000000000;
           double maxdiff = -1;
           double[] diffs = new double[10];
           double prevmz = -1;
           boolean breakpoint = false;
           int breakpointcount = 0;
           int breakcount = 0;
           int count = 0;
           while (!Strings.isNullOrEmpty(line = reader.readLine())){
               String[] values = line.split(" ");
               double m_over_z = Double.valueOf(values[1]);

               if(prevmz == -1){
                   prevmz = m_over_z;
               }

               double diff = Math.abs(prevmz - m_over_z);

               if(diff > 150){
                    breakpoint = true;
               }

               if(breakpoint && breakpointcount < 10){
                   diffs[breakpointcount] = diff;
                   breakpointcount++;
               }else if(breakpoint){
                   double average = DoubleStream.of(diffs).sum()/10;
                   System.out.println("Average at break Point near " + count + " is " + average);
                   if(average > 15) breakcount++;
                   breakpointcount = 0;
                   breakpoint = false;
               }

               prevmz = m_over_z;
            count++;
           }
           System.out.println("Total Number of candidate breaks" + breakcount);

//           Collection<String> valuesminus = sortingmap.values();
//           for(String s: valuesminus){
//               printWriter.println(s);
//           }
//           printWriter.flush();
//           printWriter.close();

       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
