package org.pulasthi.dsctools;

import mpi.MPIException;

import javax.rmi.CORBA.Util;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Created by pulasthi on 11/19/16.
 */
public class DistanceCalculation {

    public static void main(String args[]){
        List<String> lines = new ArrayList<String>();
        try {
            ParallelOps.setupParallelism(args);
            Utils.printMessage("Starting with " + ParallelOps.worldProcsCount + "Processes");
            String inputFile = args[0];
            String outputFile = args[3];
            int numPoints = Integer.valueOf(args[1]);
            int dimension = Integer.valueOf(args[2]);
            BufferedReader br = Files.newBufferedReader(Paths.get(inputFile));
            FileOutputStream fos = new FileOutputStream(outputFile);
            FileChannel fc = fos.getChannel();

            ParallelOps.setParallelDecomposition(numPoints,dimension);
            double[][] points = new double[numPoints][dimension];
            double[][] localDistances = new double[ParallelOps.procRowCount][numPoints];

            String line = null;
            int count = 0;
            while((line = br.readLine()) != null){
                String splits[] = line.split(",");
                for(int j = 0; j < splits.length; j++){
                    points[count][j] = Double.valueOf(splits[j]);
                }
                count++;
            }

            //Calculate means and sd of features //TODO need to parallise
            Utils.printMessage("Start calculating mean and sd");

            double[] means = new double[dimension];
            double[] sd = new double[dimension];
            double max = Double.MIN_VALUE;
            for(int i = 0; i < numPoints; i++){
                for (int j = 0; j < dimension; j++) {
                    means[j] += points[i][j];
                }
            }
            for (int i = 0; i < dimension; i++) {
                means[i] = means[i]/numPoints;
            }
            for(int i = 0; i < numPoints; i++){
                for (int j = 0; j < dimension; j++) {
                    sd[j] += (points[i][j] - means[j])*(points[i][j] - means[j]);
                }
            }
            for (int i = 0; i < dimension; i++) {
                sd[i] = Math.sqrt(sd[i]/numPoints);
            }
            Utils.printMessage("End calculating mean and sd");

            //Update value with new normalized values
            Utils.printMessage("Start calculating normalized data");

            for(int i = 0; i < numPoints; i++){
                for (int j = 0; j < dimension; j++) {
                    if(sd[j] == 0) continue;
                    points[i][j] = (points[i][j] - means[j])/sd[j];
                }
            }

            Utils.printMessage("End calculating normalized data");

            for (int i = 0; i < ParallelOps.procRowCount; i++) {
                for (int j = 0; j < numPoints; j++) {
                    double distance = calculateEuclideanDistance(points[i + ParallelOps.procRowStartOffset],points[j],dimension);
                    localDistances[i][j] = distance;
                    if(distance > max){
                        max = distance;
                    }
                }
                if(i%1000 == 0) Utils.printMessage("Distance calculation ......");
            }

            max = ParallelOps.allReduceMax(max);

            short[] row = new short[numPoints];
            long filePosition = ((long)ParallelOps.procRowStartOffset)*numPoints*2;
            for (int i = 0; i < ParallelOps.procRowCount; i++) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(numPoints*2);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                for (int j = 0; j < numPoints; j++) {
                    row[j] = (short)((localDistances[i][j]/max)*Short.MAX_VALUE);
                }
                byteBuffer.clear();
                byteBuffer.asShortBuffer().put(row);
                if(i%500 == 0) Utils.printMessage("Writing to file calculation ......");
                fc.write(byteBuffer,(filePosition + ((long)i)*numPoints*2));
            }

            fc.close();
            System.out.println(ParallelOps.worldProcRank);
            ParallelOps.tearDownParallelism();



        } catch (MPIException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public static double calculateEuclideanDistance(double x[], double y[], int dimension){
        double sum = 0;
        for (int i = 0; i < dimension; i++) {
            sum += (x[i]-y[i])*(x[i]-y[i]);
        }

        return Math.sqrt(sum);
    }



}

