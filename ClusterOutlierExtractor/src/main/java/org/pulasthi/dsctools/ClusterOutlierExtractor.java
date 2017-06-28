package org.pulasthi.dsctools;

import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by pulasthi on 6/20/17.
 */
public class ClusterOutlierExtractor {
    static final int dustClusterId = 100000;

    public static void main(String[] args) {
        Map<Integer, Double> cutOffs = new HashMap<Integer, Double>();
        double[][] points;
        int[] clusters;
        HashSet dustPoints = new HashSet();


        try{
            String clusterFile = args[0];
            String pointsFile = args[1];
            String outputFile = args[2];
            String outputFileWithoutDust = args[2];
            String outputFilePlot = args[3];
            double defaultCutOff = Double.parseDouble(args[4]);
            int numPoints = Integer.parseInt(args[5]);
            points = new double[numPoints][3];
            clusters = new int[numPoints];
            if(args.length >= 8){
                for (int i = 7; i < args.length; i++) {
                    String line = args[i];
                    String[] splits = line.split("-");
                    cutOffs.put(Integer.parseInt(splits[0]), Double.parseDouble(splits[1]));
                }
            }

            readPoints(pointsFile,points);
            HashMap<Integer, Double[]> meansmap = new HashMap<Integer, Double[]>();
            HashMap<Integer, Double> clusterSigmasmap = new HashMap<Integer, Double>();

            calculateMeans(points,clusters,meansmap);
            calculateSigmaValues(points, meansmap, clusters, numPoints, clusterSigmasmap);

//            for (int i = 0; i < numClusters; i++) {
//                if(cutOffs.containsKey(i)){
//                    cutoffsFinalmap.put()
//                    cutoffsFinal[i] = cutOffs.get(i);
//                }else{
//                    cutoffsFinal[i] = defaultCutOff;
//                }
//            }

            //genereate outlier cluster and write data to files
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                int cluster = clusters[pointIndex];

                if(cluster == dustClusterId){
                    dustPoints.add(pointIndex);
                    continue;
                }
                double multiplier = defaultCutOff;
                if(cutOffs.containsKey(cluster)){
                    multiplier = cutOffs.get(cluster);
                }
                double cutOff = clusterSigmasmap.get(cluster) * multiplier;



                    double tmp0 = points[pointIndex][0] - meansmap.get(cluster)[0];
                double tmp1 = points[pointIndex][1] - meansmap.get(cluster)[1];
                double tmp2 = points[pointIndex][2] - meansmap.get(cluster)[2];
                double distance =  Math.sqrt(tmp0 * tmp0 + tmp1 * tmp1 + tmp2 * tmp2);

                if(distance > cutOff){
                    dustPoints.add(pointIndex);
                }
            }

            System.out.println("Size of dust cluster: " + dustPoints.size());
//            System.out.println("Length of group 2 dust: " + dustPoints[1].size());
            String file = outputFile;
            String fileMDS = outputFilePlot;
            WriteDustResults(file,numPoints,clusters,dustPoints);
            WriteDustResultsWithMDS(fileMDS,numPoints,points,clusters,dustPoints);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // Write cluster results with dust cluster to file
    public static void WriteDustResults(String file,int numPoints, int[] clusters, HashSet dustPoints){

        Path filePath = Paths.get(file);
        OpenOption mode = StandardOpenOption.CREATE;
        int dustClusterIndex = dustClusterId;

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, Charset.defaultCharset(), mode),
                true)) {
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                int curGroup = clusters[pointIndex];
                if(dustPoints.contains(pointIndex)){
                    writer.println(pointIndex + "\t" + dustClusterIndex);
                }else{
                    writer.println(pointIndex + "\t" + curGroup);
                }

            }
            writer.close();
        } catch (IOException e) {
            System.err.format("Failed writing cluster results due to I/O exception: %s%n", e);
        }

    }

    // Write cluster results with dust cluster to file with MDS data
    public static void WriteDustResultsWithMDS(String file,int numPoints, double[][] points, int[] clusters, HashSet dustPoints){

        Path filePath = Paths.get(file);
        OpenOption mode = StandardOpenOption.CREATE;
        int dustClusterIndex = dustClusterId;

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, Charset.defaultCharset(), mode),
                true)) {
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                int curGroup = clusters[pointIndex];
                if(dustPoints.contains(pointIndex)){
                    writer.println(pointIndex + " " + points[pointIndex][0] + " " + points[pointIndex][1] + " " + points[pointIndex][2] + " " + dustClusterIndex + " " + "Dust");
                }else{
                    writer.println(pointIndex + " " + points[pointIndex][0] + " " + points[pointIndex][1] + " " + points[pointIndex][2] + " " + curGroup + " " + curGroup);
                }

            }
            writer.close();
        } catch (IOException e) {
            System.err.format("Failed writing cluster results due to I/O exception: %s%n", e);
        }

    }

    private static void calculateSigmaValues(double[][] points, HashMap<Integer, Double[]> means, int[] clusters, int numPoints, HashMap<Integer, Double> clusterSigmas) {
        // calculate sigma using ecludian distance for each group
        HashMap<Integer, Integer> clusterCountsMap = new HashMap<Integer, Integer>();


        for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
            int group = clusters[pointIndex];
            if(!clusterSigmas.containsKey(group)){
                clusterSigmas.put(group,0.0);
                clusterCountsMap.put(group,0);
            }
            double tmp0 = points[pointIndex][0] - means.get(group)[0];
            double tmp1 = points[pointIndex][1] -  means.get(group)[1];
            double tmp2 = points[pointIndex][2] -  means.get(group)[2];
            double distance =  Math.sqrt(tmp0 * tmp0 + tmp1 * tmp1 + tmp2 * tmp2);
            clusterSigmas.put(group,clusterSigmas.get(group) + distance);
            clusterCountsMap.put(group,clusterCountsMap.get(group) + 1);
        }
        for (Integer key : clusterSigmas.keySet()) {
            clusterSigmas.put(key,clusterSigmas.get(key)/ clusterCountsMap.get(key) );
        }

    }

    public static void readPoints(String fname, double[][] points) throws IOException{
        if (Strings.isNullOrEmpty(fname)) {
            throw new IOException("File name not specified");
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(fname), Charset.defaultCharset())) {
            String line;
            int count = 0;
            Pattern pattern = Pattern.compile("[\t ]");
            while ((line = br.readLine()) != null) {
                if (!Strings.isNullOrEmpty(line)) {
                    if (Strings.isNullOrEmpty(line)) {
                        continue; // continue on empty lines - "while" will break on null anyway;
                    }
                    String[] splits = pattern.split(line.trim());
                    int index = Integer.parseInt(splits[0]);
                    points[index][0] = Double.parseDouble(splits[1]);
                    points[index][1] = Double.parseDouble(splits[2]);
                    points[index][2] = Double.parseDouble(splits[3]);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int readClusters(String fname, int[] clusters) throws IOException{
        if (Strings.isNullOrEmpty(fname)) {
            throw new IOException("File name not specified");
        }
        HashSet tempclustervount = new HashSet();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(fname), Charset.defaultCharset())) {
            String line;
            int count = 0;
            Pattern pattern = Pattern.compile("[\t ]");
            while ((line = br.readLine()) != null) {
                if (!Strings.isNullOrEmpty(line)) {
                    if (Strings.isNullOrEmpty(line)) {
                        continue; // continue on empty lines - "while" will break on null anyway;
                    }
                    String[] splits = pattern.split(line.trim());
                    int index = Integer.parseInt(splits[0]);
                    clusters[index] = Integer.parseInt(splits[1]);
                    tempclustervount.add(Integer.parseInt(splits[1]));
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempclustervount.size();
    }

    public static void calculateMeans(double[][] points, int[] clusters, HashMap<Integer, Double[]> meansmap){
        HashMap<Integer, Integer> clusterCountsMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < points.length; i++) {
            double[] point = points[i];
            int cluster = clusters[i];
            if(!meansmap.containsKey(cluster)){
                Double[] temp = {0.0,0.0,0.0};
                meansmap.put(cluster,temp);
                clusterCountsMap.put(cluster,0);
            }
            meansmap.get(cluster)[0] += point[0];
            meansmap.get(cluster)[1] += point[1];
            meansmap.get(cluster)[2] += point[2];
            clusterCountsMap.put(cluster,clusterCountsMap.get(cluster) + 1);
        }

        for (Integer key : meansmap.keySet()) {
            int clusterCount = clusterCountsMap.get(key);
            meansmap.get(key)[0] /= clusterCount;
            meansmap.get(key)[1] /= clusterCount;
            meansmap.get(key)[2] /= clusterCount;
        }
    }
}


