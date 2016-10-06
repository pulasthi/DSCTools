package org.saliya.dsctools.davs;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pulasthi on 8/9/16.
 */
public class Pepsitide {

    public static void main(String[] args) {
        String originalFile = args[0];
        String davsFile = args[1];
        Map<String,Integer> clusters = new HashMap<String,Integer>();

        try(BufferedReader reader = Files.newBufferedReader(Paths.get(davsFile))){
            BufferedReader originalreader = Files.newBufferedReader(Paths.get(originalFile));

            String outputFileName = davsFile.replace(".txt",".peptide.txt");

            FileWriter writer = new FileWriter(outputFileName);
            PrintWriter printWriter = new PrintWriter(writer);
            String line;
            String originalLine;
            int clustercount = 0;
            while (!Strings.isNullOrEmpty(line = reader.readLine())){


                String[] splits = line.split(" ");
                String cluster = splits[4];
                String experiment = splits[5];

                if(cluster.equals("100000000")){
                    printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + splits[4] + " " + "Centers");
                }else{
                    originalLine = originalreader.readLine();
                    String[] originalsplits = originalLine.split("\t");
                    String peptide = originalsplits[4];
                    int currentKey = -1;
                    if(clusters.containsKey(peptide)){
                        currentKey = clusters.get(peptide);
                    }else{
                        currentKey = clustercount;
                        clusters.put(peptide,currentKey);
                        clustercount++;
                    }
                    if(peptide.equals("NA")){
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + currentKey + " " + experiment);
                    }else{
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + currentKey + " " + experiment + "|" +peptide);
                    }
                }


            }
            printWriter.flush();
            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
