package org.spidal.pulasthi;

import com.google.common.base.Strings;
import com.google.common.io.FileWriteMode;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by pulasthi on 10/6/16.
 */
public class SequenceFilter {

    public static void main(String[] args) {
        String filename = args[0];
        HashMap<String, Integer> sequences = new HashMap<String, Integer>();
        HashMap<String, String> seq_names = new HashMap<String, String>();
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filename))) {

            String outputFileName = filename.substring(0,filename.lastIndexOf("/") + 1) + "unique_" + filename.substring(filename.lastIndexOf("/")+1);
            String line;
            String name;
            while (!Strings.isNullOrEmpty(line = bufferedReader.readLine())){
                name = line;
                if(!Strings.isNullOrEmpty(line = bufferedReader.readLine())){
                    line.toUpperCase();
                    if(sequences.containsKey(line)){
                        sequences.put(line, sequences.get(line) + 1);
                        seq_names.put(line, name);
                    }else{
                        sequences.put(line,1);
                        seq_names.put(line, name);
                    }
                }

            }

            FileWriter fileWriter = new FileWriter(outputFileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            int singleOccuranceSeq = 0;
            int filterdSeq[] = {0};
            sequences.forEach((s, integer) -> {
                if(integer > 1){
                    printWriter.println(seq_names.get(s));
                    printWriter.println(s);
                    filterdSeq[0]++;
                }
            });


            System.out.println("Number of sequences :" + sequences.size());
            System.out.println("Number of sequences Without Single Occurrence :" + filterdSeq[0]);
            System.out.println("Number of sequence of Single Occurrence :" + (sequences.size() - filterdSeq[0]));
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
