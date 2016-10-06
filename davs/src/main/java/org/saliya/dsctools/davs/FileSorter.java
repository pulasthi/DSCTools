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

/**
 * Created by pulasthi on 6/9/16.
 */
public class FileSorter {

    public static void main(String[] args) {
        String filename = args[0];
        ListMultimap<Double, String> sortingmap = MultimapBuilder.treeKeys().arrayListValues().build();

       try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
           String outputFileName = filename.replace(".txt",".sorted.txt");

           FileWriter writer = new FileWriter(outputFileName);
           PrintWriter printWriter = new PrintWriter(writer);
           String line;
           while (!Strings.isNullOrEmpty(line = reader.readLine())){
               String[] values = line.split(" ");
               double m_over_z = Double.valueOf(values[1]);

               String outline = line;
//               for (int i = 0; i < values.length; i++) {
//                   String value = values[i];
//                   if(i == 4){
//                       if(value.equals("0")){
//                           values[5] = "0";
//                       }else if(value.equals("100000000")){
//                           values[5] = "100000000";
//                       }
//                       continue;
//                   }
//
//                   outline += value;
//                   if(i < values.length - 1 ){
//                       outline += " ";
//                   }
//               }

               sortingmap.put(m_over_z,outline);

           }

           Collection<String> valuesminus = sortingmap.values();
           for(String s: valuesminus){
               printWriter.println(s);
           }
           printWriter.flush();
           printWriter.close();

       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
