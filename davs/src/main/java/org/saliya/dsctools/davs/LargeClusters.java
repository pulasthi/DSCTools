package org.saliya.dsctools.davs;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by pulasthi on 7/26/16.
 */
public class LargeClusters {
    private static int countcenters = 0;
    private static Map<Integer,Integer> counts = new HashMap<Integer,Integer>();

    public static void main(String[] args) {
        String filename = args[0];
        Map<Integer,Double> centersx = new HashMap<Integer,Double>();
        Map<Integer,Double> centersy = new HashMap<Integer,Double>();
        Map<Integer,Integer> largeClusters = new HashMap<Integer,Integer>();
        Map<Integer,Integer> otherClusters = new HashMap<Integer,Integer>();
        Map<Integer,Double> largeClusterscx = new HashMap<Integer,Double>();
        Map<Integer,Double> largeClusterscy = new HashMap<Integer,Double>();
        Map<Integer,Double> otherClusterscx = new HashMap<Integer,Double>();
        Map<Integer,Double> otherClusterscy = new HashMap<Integer,Double>();


        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
            String line;
            while (!Strings.isNullOrEmpty(line = reader.readLine())){
                String[] splits = line.split(" ");
                int cluster = Integer.valueOf(splits[4]);
                double x = Double.valueOf(splits[1]);
                double y = Double.valueOf(splits[2]);

                if(counts.containsKey(cluster)){
                    counts.put(cluster,counts.get(cluster) + 1);
                    centersx.put(cluster,centersx.get(cluster) + x);
                    centersy.put(cluster,centersy.get(cluster) + y);
                }else{
                    counts.put(cluster,1);
                    centersx.put(cluster,x);
                    centersy.put(cluster,y);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator iter = counts.entrySet().iterator();
        int largeClusterIdCount = 100000001;
        while (iter.hasNext()){
            Map.Entry pair = (Map.Entry)iter.next();
            if((Integer)pair.getValue() > 300){
                int key = (Integer)(pair.getKey());
                int value = (Integer)pair.getValue();
                largeClusters.put(key,largeClusterIdCount);
                largeClusterscx.put(key,centersx.get(key)/value);
                largeClusterscy.put(key,centersy.get(key)/value);
                largeClusterIdCount++;
            }else{
                int key = (Integer)(pair.getKey());
                int value = (Integer)pair.getValue();
                otherClusters.put(key,1);
                otherClusterscx.put(key,centersx.get(key)/value);
                otherClusterscy.put(key,centersy.get(key)/value);
                largeClusterscx.put(key,centersx.get(key)/value);
                largeClusterscy.put(key,centersy.get(key)/value);
            }
        }


        try(BufferedReader reader2 = Files.newBufferedReader(Paths.get(filename))){
            String outputFileName = filename.replace(".txt",".LargeClusters_LabledByCounts.txt");

            FileWriter writer = new FileWriter(outputFileName);
            PrintWriter printWriter = new PrintWriter(writer);
            String line;

            while (!Strings.isNullOrEmpty(line = reader2.readLine())){
                String[] splits = line.split(" ");
                int cluster = Integer.valueOf(splits[4]);

                if(largeClusters.containsKey(cluster)){
                    if(cluster == 0){
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + splits[4] + " " + 0 + " ");
                    }else if(cluster == 100000000){
                        String centerx = splits[1];
                        String centery = splits[2];
                        int count = counts.get(cluster);
                        int clusterid = CheckifSpecialCenter(centerx.substring(0,centerx.lastIndexOf(".") + 1),centery.substring(0,centery.lastIndexOf(".") + 2),largeClusterscx,largeClusterscy);
                        if(clusterid > 0 && largeClusters.containsKey(clusterid)){
                            printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + 110000000 + " " + clusterid + "," + counts.get(clusterid) + " ");
                        }else{
                            printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + splits[4] + " " + clusterid + "," + counts.get(clusterid) + " ");
                        }
                    }else{
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + largeClusters.get(cluster) + " " + cluster + " ");
                    }
                }else{
                    if(cluster == 0){
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + splits[4] + " " + 0 + " ");
                    }else if(cluster == 100000000){
                        String centerx = splits[1];
                        String centery = splits[2];
                        int clusterid = CheckifSpecialCenter(centerx.substring(0,centerx.lastIndexOf(".") + 1),centery.substring(0,centery.lastIndexOf(".") + 2),otherClusterscx,otherClusterscy);
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + splits[4] + " " + counts.get(clusterid) + " ");
                    }else{
                        printWriter.println(splits[0] + " " + splits[1] + " " + splits[2] + " " + splits[3] + " " + cluster + " " + cluster + " ");
                    }
                }
            }
            printWriter.flush();
            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int CheckifSpecialCenter(String centerx, String centery, Map<Integer, Double> largeClusterscx, Map<Integer, Double> largeClusterscy) {
        boolean isLargeCenter = false;
        int center = -1;
        Iterator iter = largeClusterscx.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry pair = (Map.Entry)iter.next();
            int key = (Integer)(pair.getKey());
            String calculatedx = String.valueOf(largeClusterscx.get(key));
            String calculatedy = String.valueOf(largeClusterscy.get(key));

            if(calculatedx.substring(0,calculatedx.lastIndexOf(".") + 1).equals(centerx) && calculatedy.substring(0,calculatedy.lastIndexOf(".") + 2).equals(centery)){
                countcenters++;
                center = key;
            }


        }

        return center;
    }

}
