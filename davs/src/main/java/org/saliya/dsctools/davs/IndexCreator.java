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
public class IndexCreator {

    public static void main(String[] args) {
        String filename = args[0];

       try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
           String outputFileName = filename.replace(".txt",".Indexed.txt");

           FileWriter writer = new FileWriter(outputFileName);
           PrintWriter printWriter = new PrintWriter(writer);
           String line;
           int count = 0;
           while (!Strings.isNullOrEmpty(line = reader.readLine())){
               String split = line.substring(line.indexOf(" ") + 1);
               String outline = count + " " + split;
               count++;
               printWriter.println(outline);
           }
           printWriter.flush();
           printWriter.close();

       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
