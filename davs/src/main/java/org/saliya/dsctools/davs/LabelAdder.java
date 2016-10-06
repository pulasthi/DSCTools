package org.saliya.dsctools.davs;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by pulasthi on 6/9/16.
 */
public class LabelAdder {

    public static void main(String[] args) {
        String filename = args[0];

       try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
           String outputFileName = filename.replace(".txt",".Labeled.txt");

           FileWriter writer = new FileWriter(outputFileName);
           PrintWriter printWriter = new PrintWriter(writer);
           String line;
           while (!Strings.isNullOrEmpty(line = reader.readLine())){
               String[] splits = line.split(" ");
//               String label = splits[4];
               String label = splits[1] + "," + splits[2];
               //printWriter.println(line + " " + label);
               printWriter.println(line.substring(0,line.lastIndexOf(" ")) + " " + label);
           }
           printWriter.flush();
           printWriter.close();

       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
