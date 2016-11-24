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
            boolean stats = (args.length == 5) ? Boolean.valueOf(args[4]) : false;
            BufferedReader br = Files.newBufferedReader(Paths.get(inputFile));
            FileOutputStream fos = new FileOutputStream(outputFile);
            FileChannel fc = fos.getChannel();

            ParallelOps.setParallelDecomposition(numPoints,dimension);
            double[][] points = new double[numPoints][dimension];

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
            double disMean = 0;
            double disSd = 0;
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


            if(stats){
//                double[] localDistances = new double[ParallelOps.procRowCount*numPoints];
//                for (int i = 0; i < ParallelOps.procRowCount; i++) {
//                    for (int j = 0; j < numPoints; j++) {
//                        double distance = calculateEuclideanDistance(points[i + ParallelOps.procRowStartOffset],points[j],dimension);
//                        localDistances[i*numPoints + j] = distance;
//                        if(distance > max){
//                            max = distance;
//                        }
//                    }
//                    if(i%1000 == 0) Utils.printMessage("Distance calculation ......");
//                }
//
//                Arrays.sort(localDistances);
//                StringBuilder percentiles = new StringBuilder("Rank : " + ParallelOps.worldProcRank + " ");
//                percentiles.append(localDistances[ParallelOps.procRowCount*numPoints/100]);
//                percentiles.append(localDistances[0]);
//                for (int i = 5; i < 100 ; i += 5) {
//                    double dist = localDistances[(ParallelOps.procRowCount*numPoints/100)*i];
//                    percentiles.append(" " + dist);
//
//                }
//                percentiles.append(" " + localDistances[(ParallelOps.procRowCount*numPoints/100)*99]);
//                percentiles.append(" " + localDistances[(int)((ParallelOps.procRowCount*numPoints/100)*99.5)]);
//                percentiles.append(" " + localDistances[(int)((ParallelOps.procRowCount*numPoints/100)*99.8)]);
//                percentiles.append(" " + localDistances[(int)((ParallelOps.procRowCount*numPoints/100)*99.9)]);
//                percentiles.append(" " + localDistances[ParallelOps.procRowCount*numPoints - 10000]);
//                percentiles.append(" " + localDistances[ParallelOps.procRowCount*numPoints - 1000]);
//                percentiles.append(" " + localDistances[ParallelOps.procRowCount*numPoints - 100]);
//                percentiles.append(" " + localDistances[ParallelOps.procRowCount*numPoints - 10]);
//                percentiles.append(" " + localDistances[ParallelOps.procRowCount*numPoints - 1]);
//
//                System.out.println("**********************************"+ percentiles + "**********************************");

            }else {
                double[][] localDistances = new double[ParallelOps.procRowCount][numPoints];
                for (int i = 0; i < ParallelOps.procRowCount; i++) {
                    for (int j = 0; j < numPoints; j++) {
                        double distance = calculateEuclideanDistance(points[i + ParallelOps.procRowStartOffset],points[j],dimension);
                        localDistances[i][j] = distance;
                        disMean += distance;
                    }
                    if(i%1000 == 0) Utils.printMessage("Distance calculation ......");
                }



                disMean = ParallelOps.allReduce(disMean)/(((double)numPoints)*numPoints);
                Utils.printMessage("Distance mean : " + disMean);

                for (int i = 0; i < ParallelOps.procRowCount; i++) {
                    for (int j = 0; j < numPoints; j++) {
                        disSd += (localDistances[i][j] - disMean)*(localDistances[i][j] - disMean);
                    }
                }

                disSd = Math.sqrt(ParallelOps.allReduce(disSd)/(((double)numPoints)*numPoints));
                Utils.printMessage("Distance SD : " + disSd);


                Utils.printMessage("Replacing distance larger than 3*SD with 3*SD");
                for (int i = 0; i < ParallelOps.procRowCount; i++) {
                    for (int j = 0; j < numPoints; j++) {
                        if(localDistances[i][j] > (disMean + 3*disSd)) localDistances[i][j] = (disMean + 3*disSd);
                        if(localDistances[i][j] > max){
                            max = localDistances[i][j];
                        }
                    }
                }
                max = ParallelOps.allReduceMax(max);
                Utils.printMessage("Done Replacing distance larger than 3*SD with 3*SD, Max is : " + max    );

                short[] row = new short[numPoints];
                long filePosition = ((long) ParallelOps.procRowStartOffset) * numPoints * 2;
                for (int i = 0; i < ParallelOps.procRowCount; i++) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(numPoints * 2);
                    byteBuffer.order(ByteOrder.BIG_ENDIAN);
                    for (int j = 0; j < numPoints; j++) {
                        row[j] = (short) ((localDistances[i][j] / max) * Short.MAX_VALUE);
                    }
                    byteBuffer.clear();
                    byteBuffer.asShortBuffer().put(row);
                    if (i % 500 == 0) Utils.printMessage("Writing to file calculation ......");
                    fc.write(byteBuffer, (filePosition + ((long) i) * numPoints * 2));
                }

                fc.close();
            }
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

