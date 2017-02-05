package org.spidal.pulasthi;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Interner;
import com.google.common.io.FileWriteMode;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by pulasthi on 10/6/16.
 */
public class SequenceFilter {

    public static void main(String[] args) {
        String filename = args[0];
        HashMap<String, Integer> sequences = new HashMap<String, Integer>();
        HashMap<String, String> seq_names = new HashMap<String, String>();
        HashMap<Integer, Integer> histos = new HashMap<Integer, Integer>();
        HashMap<String, Integer> seq_clusters = new HashMap<String, Integer>();
        HashMap<Integer, String> countsUpto = new HashMap<Integer, String>();
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filename))) {

            String outputFileName = filename.substring(0,filename.lastIndexOf("/") + 1) + "unique_withcounts_justindexes" + filename.substring(filename.lastIndexOf("/")+1);
            String outputFileName2 = filename.substring(0,filename.lastIndexOf("/") + 1) + "unique_withcounts_histos" + filename.substring(filename.lastIndexOf("/")+1);
            String outputFileName3 = filename.substring(0,filename.lastIndexOf("/") + 1) + "unique_accumilative_clusters" + filename.substring(filename.lastIndexOf("/")+1);
            String line;
            String name;
            int totalcount = 0;
            while (!Strings.isNullOrEmpty(line = bufferedReader.readLine())){
                name = line;
                if(!Strings.isNullOrEmpty(line = bufferedReader.readLine())){
                    line.toUpperCase();
                    if(sequences.containsKey(line)){
                        totalcount++;
                        sequences.put(line, sequences.get(line) + 1);
                        seq_names.put(line, name);
                    }else{
                        totalcount++;
                        sequences.put(line,1);
                        seq_names.put(line, name);
                    }
                }

            }

            FileWriter fileWriter = new FileWriter(outputFileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);


            FileWriter fileWriter1 = new FileWriter(outputFileName2);
            PrintWriter printWriter1 = new PrintWriter(fileWriter1);

            FileWriter fileWriter2 = new FileWriter(outputFileName3);
            PrintWriter printWriter2 = new PrintWriter(fileWriter2);

            int singleOccuranceSeq = 0;
            int filterdSeq[] = {0};
            int countindex = 0;
            sequences.forEach((s, integer) -> {
                if(integer > 1){
                   // printWriter.println(seq_names.get(s));
                    printWriter.println(filterdSeq[0] + " " + integer);
                    filterdSeq[0]++;
                }
            });



            sequences.forEach((s, integer) -> {

                    if(!histos.containsKey(integer)){
                        histos.put(integer,1);
                    }else{
                        histos.put(integer, histos.get(integer)+1);
                    }
            });

            histos.forEach((x, y) -> {
                printWriter1.println(x + " " + y);
            });


            //counts histogram calculation
            int[] sortedArray = new int[histos.size()];
            int count[] = {0};
            histos.forEach( (x,y) -> {
                sortedArray[count[0]] = x;
                count[0]++;
            });

            Arrays.sort(sortedArray);


            //sectioning
            int[] margins = new int[10];

            countsUpto.put(0,"0-1000");
            countsUpto.put(1,"1000-2000");
            countsUpto.put(2,"2000-5000");
            countsUpto.put(3,"5000-10000");
            countsUpto.put(4,"10000-15000");
            countsUpto.put(5,"15000-25000");
            countsUpto.put(6,"25000-45000");
            countsUpto.put(7,"45000-70000");
            countsUpto.put(8,"3");
            countsUpto.put(9,"2");

            int counthisto = 0;
            for (int i = sortedArray.length - 1; i >= 0; i--) {
                if(counthisto <= 1000){
                    margins[0] = sortedArray[i];
                }else if(1000 < counthisto && counthisto <= 2000){
                    margins[1] = sortedArray[i];
                }else if(2000 < counthisto && counthisto <= 5000){
                    margins[2] = sortedArray[i];
                }else if(5000 < counthisto && counthisto <= 10000){
                    margins[3] = sortedArray[i];
                }else if(10000 < counthisto && counthisto <= 15000){
                    margins[4] = sortedArray[i];
                }else if(15000 < counthisto && counthisto <= 25000){
                    margins[5] = sortedArray[i];
                }else if(25000 < counthisto && counthisto <= 45000){
                    margins[6] = sortedArray[i];
                }else if(45000 < counthisto && counthisto <= 70000){
                    margins[7] = sortedArray[i];
                }
                counthisto += histos.get(sortedArray[i]);
            }

            margins[8] = 3;
            margins[9] = 2;

            for (int i = margins.length - 1; i >= 0; i--) {
                final int cur = margins[i];
                int curindex[] = {i};
                sequences.forEach((seq,countseq) -> {
                    if(countseq > 1){
                        if(countseq >= cur){
                            seq_clusters.put(seq,curindex[0]);
                        }
                    }
                });
            }
            int filterdSeq2[] = {0};
            sequences.forEach((s, integer) -> {
                if(integer > 1) {
                    printWriter2.println(s + " " + filterdSeq2[0] + " " + seq_clusters.get(s) + " " + countsUpto.get(seq_clusters.get(s)));
                    filterdSeq2[0]++;
                }
            });
            System.out.println("Number of sequences :" + sequences.size());
            System.out.println("Number of sequences Without Single Occurrence :" + filterdSeq[0]);
            System.out.println("Number of sequence of Single Occurrence :" + (sequences.size() - filterdSeq[0]));
            printWriter.flush();
            printWriter.close();
            printWriter1.flush();
            printWriter1.close();
            printWriter2.flush();
            printWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
