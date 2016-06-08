package org.esaliya.distancechecker;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.*;
import java.util.stream.IntStream;

public class PearsonCorrelationTester {
    public static void main(String[] args) throws IOException {
/*
        String v1String = "48.97\t49.9\t49.9\t50.08\t50.52\t50.39\t50.35\t50.45\t50" +
                    ".61\t51.55\t52.79\t53.52\t53.32\t53.38\t53.55\t52.59\t52" +
                    ".55\t52.66\t53.19\t53\t52.87\t52.67\t52.5\t52.46\t52" +
                    ".45\t52.14\t52.27\t52.72\t52.71\t52.7\t52.8\t52.49\t52" +
                    ".58\t52.71\t53.62\t53.59\t54.47\t53.84\t54.62\t53.78\t53" +
                    ".01\t52.75\t52.87\t52.69\t53.4\t54.39\t54.38\t54.11\t53" +
                    ".77\t53.63\t52.88\t53.18\t54.48\t54.68\t54.5\t53.86\t57" +
                    ".24\t56.6\t55.76\t55.81\t55.8\t55.81\t56.67\t56.29\t57" +
                    ".02\t57.21\t57.06\t57\t57.41\t58.49\t58.88\t59\t58" +
                    ".03\t57.24\t57.85\t57.21\t57.17\t57.56\t56.91\t56.76\t56" +
                    ".62\t57.01\t57.19\t57.41\t58.3\t58.95\t58.84\t58.68\t57" +
                    ".33\t57.84\t57.95\t58.68\t59.09\t58.9\t59.43\t58.63\t57" +
                    ".69\t57.21\t56.46\t56.37\t56.65\t55.84\t56.35\t56.3\t55" +
                    ".62\t55.49\t54.74\t53.98\t54.29\t54.06\t53.34\t54.19\t54" +
                    ".19\t54.72\t54.54\t54.68\t54.28\t53.8\t52.86\t54.05\t53" +
                    ".97\t53.97\t53.72\t53.55\t54.32\t54.32\t54.23\t54\t53" +
                    ".71\t52.92\t53.21\t53.76\t53.24\t53.56\t53.39\t53.7\t54" +
                    ".32\t52.87\t53.92\t52.51\t52.3\t51.09\t50.03\t50.82\t50" +
                    ".69\t50.95\t51.21\t51.09\t53.12\t54.3\t56.3\t54.93\t55" +
                    ".22\t53.89\t52.47\t52.35\t51.86\t52.73\t52.39\t52.36\t52" +
                    ".14\t52.16\t52.53\t52.01\t50.79\t51.49\t51.4\t51.91\t51" +
                    ".58\t51.72\t52.9\t51.86\t51.68\t51.96\t52.25\t52.16\t52" +
                    ".1\t51.99\t53.6\t53.95\t53.69\t54.44\t53.86\t54" +
                    ".35\t54\t54.05\t53.62\t55.21\t55.46\t55.53\t55" +
                    ".54\t55\t54.94\t54.86\t54.24\t53.36\t52.64\t51.92\t51" +
                    ".97\t51.2\t51.67\t51.4\t51.64\t51.82\t52.86\t53.87\t54" +
                    ".07\t54.45\t54.65\t54.62\t54.12\t54.05\t54.02\t54.34\t53" +
                    ".75\t53.92\t54.12\t54.67\t55.11\t54.31\t54.48\t55.21\t54" +
                    ".82\t55.58\t54.73\t55.49\t54.25\t55.91\t56.42\t57\t57" +
                    ".51\t57.01\t57.5\t58.16\t58.15\t58.22\t58.9\t58.09\t58" +
                    ".11\t58.38\t57.51\t56.77\t57.69\t57.23\t57.6\t58.26\t58" +
                    ".35\t57.88\t56.98\t57.23\t56.15";
*/
        String v1String = "19.23\t19.42\t19.42\t19.125\t18.83\t18.84\t19" +
                          ".38\t19.68\t19.42\t19.4\t19.35\t19.51\t19.17\t19" +
                          ".03\t19.26\t19.16\t18.89\t18.85\t19.02\t18.49\t18" +
                          ".45\t18.61\t18.75\t19.07\t19.17\t19.27\t19.29\t19" +
                          ".45\t19.43\t19.74\t20.65\t21.13\t20.63\t21\t20" +
                          ".97\t21.18\t21.02\t20.42\t20.46\t20.35\t20.62\t20" +
                          ".37\t20.36\t20.66\t20.08\t20.01\t20.04\t20.47\t20" +
                          ".36\t20.62\t20.33\t20.01\t19.79\t19.52\t19.52\t19" +
                          ".21\t19.22\t19.02\t19\t19.06\t19.04\t19.06\t19" +
                          ".188\t19.06\t19.16\t19.13\t19.23\t19.11\t19.27\t19" +
                          ".19\t19.21\t18.88\t18.88\t18.88\t18.67\t18.71\t18" +
                          ".69\t18.92\t18.94\t18.85\t18.76\t18.46\t18.35\t18" +
                          ".69\t18.84\t18.8\t18.57\t18.44\t18.42\t18.78\t18" +
                          ".77\t19\t18.74\t18.61\t18.63\t18.65\t18.64\t18" +
                          ".73\t18.66\t18.36\t18.28\t18.3\t18.079\t18.02\t18" +
                          ".1\t18.06\t18.26\t18.28\t18.32\t18.46\t18.46\t18" +
                          ".76\t18.92\t18.85\t18.94\t19.05\t19.92\t20.03\t20" +
                          ".05\t20.08\t20.19\t20.24\t20.17\t20.25\t20.21\t20" +
                          ".3\t20.29\t20.17\t20.21\t20.27\t20.52\t20.57\t20" +
                          ".1\t19.88\t19.91\t20.29\t20.02\t20.1\t20.1\t20" +
                          ".08\t19.58\t19.87\t19.81\t19.78\t19.78\t19.58\t19" +
                          ".52\t19.4\t19.05\t19.23\t19.17\t19.19\t19.23\t19" +
                          ".17\t19.04\t19.1\t18.86\t18.87\t18.7\t18.72\t18" +
                          ".69\t18.69\t18.6\t18.63\t18.66\t18.74\t18.71\t18" +
                          ".41\t18.43\t18.32\t18.42\t18.26\t18.31\t18.24\t17" +
                          ".959\t17.89\t17.92\t17.95\t17.77\t17.77\t17.19\t17" +
                          ".08\t17.07\t16.99\t16.83\t16.9\t16.78\t16.35\t16" +
                          ".48\t16.41\t16.12\t16.6\t16.48\t16.68\t16.62\t16" +
                          ".64\t16.73\t16.68\t16.71\t16.67\t16.58\t16.73\t16" +
                          ".81\t16.46\t16.5\t16.5\t16.42\t16.49\t16.38\t15" +
                          ".82\t15.5\t15.48\t15.36\t14.66\t14.82\t15.1\t14" +
                          ".92\t15.11\t15.03\t14.97\t15.63\t15.5\t15.55\t15" +
                          ".48\t15.6\t15.79\t15.78\t15.98\t15.87\t14.83\t15" +
                          ".78\t15.75\t15.73\t15.77\t15.61\t15.71\t16.32\t16" +
                          ".37\t16.33\t16.3\t15.47\t15.08\t15.1\t15.2499\t15" +
                          ".39\t14.99\t15.04\t15.09\t15.35\t15.11\t15.58\t15" +
                          ".06";

/*
        String v2String = "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1" +
                    "\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1";
*/

        String v2String = "19.72\t19.92\t19.92\t19.61\t19.3\t19.3\t19.86\t20" +
                          ".16\t19.92\t19.89\t19.81\t20.01\t19.64\t19.5\t19" +
                          ".75\t19.62\t19.36\t19.31\t19.52\t18.92\t18.92\t19" +
                          ".1\t19.23\t19.58\t19.6761\t19.77\t19.78\t19" +
                          ".935\t19.91\t20.23\t21.19\t21.67\t21.136\t21" +
                          ".55\t21.52\t21.69\t21.548\t21.05\t20.99\t20.83\t21" +
                          ".12\t20.89\t20.9\t21.17\t20.6\t20.54\t20.55\t21" +
                          ".01\t20.895\t21.15\t20.85\t20.53\t20.3\t20.02\t20" +
                          ".05\t19.71\t19.72\t19.494\t19.47\t19.536\t19" +
                          ".53\t19.55\t19.69\t19.55\t19.65\t19.6\t19.71\t19" +
                          ".61\t19.78\t19.69\t19.7\t19.37\t19.4\t19.36\t19" +
                          ".15\t19.2\t19.16\t19.41\t19.42\t19.32\t19.25\t18" +
                          ".94\t18.8118\t19.19\t19.33\t19.28\t19.0484\t18" +
                          ".93\t18.9\t19.28\t19.26\t19.5\t19.24\t19.09\t19" +
                          ".1\t19.15\t19.15\t19.22\t19.16\t18.84\t18.74\t18" +
                          ".78\t18.55\t18.49\t18.58\t18.53\t18.75\t18.756\t18" +
                          ".82\t18.94\t18.94\t19.29\t19.41\t19.34\t19.43\t19" +
                          ".56\t20.43\t20.56\t20.57\t20.62\t20.7228\t20" +
                          ".785\t20.7\t20.8\t20.74\t20.85\t20.83\t20.69\t20" +
                          ".74\t20.82\t21.06\t21.12\t20.62\t20.38\t20.43\t20" +
                          ".83\t20.55\t20.62\t20.63\t20.616\t20.08\t20.38\t20" +
                          ".32\t20.285\t20.3\t20.1599\t20.01\t19.91\t19" +
                          ".54\t19.73\t19.66\t19.69\t19.73\t19.66\t19.56\t19" +
                          ".61\t19.31\t19.37\t19.19\t19.22\t19.18\t19" +
                          ".1879\t19.08\t19.1\t19.16\t19.28\t19.19\t18.88\t18" +
                          ".93\t18.81\t18.92\t18.7\t18.78\t18.7001\t18.45\t18" +
                          ".36\t18.4099\t18.43\t18.22\t18.2\t17.65\t17.55\t17" +
                          ".5252\t17.438\t17.27\t17.35\t17.22\t16.78\t16" +
                          ".93\t16.83\t16.54\t17.07\t16.93\t17.14\t17.07\t17" +
                          ".09\t17.19\t17.1345\t17.18\t17.13\t17.01\t17" +
                          ".1799\t17.26\t16.9\t16.96\t16.94\t16.87\t16.94\t16" +
                          ".83\t16.23\t15.906\t15.94\t15.784\t15.03\t15" +
                          ".22\t15.52\t15.311\t15.508\t15.41\t15.3799\t16" +
                          ".04\t15.93\t15.99\t15.88\t16.0147\t16.19\t16.2\t16" +
                          ".4399\t16.3\t15.2539\t16.21\t16.2\t16.16\t16.2\t16" +
                          ".0399\t16.14\t16.7684\t16.82\t16.77\t16.72\t15" +
                          ".9\t15.5\t15.52\t15.65\t15.81\t15.4\t15.4201\t15" +
                          ".5\t15.78\t15.51\t16.04\t15.43";
        String [] v1Splits = v1String.split("\t");
        String [] v2Splits = v2String.split("\t");
        if (v1Splits.length != v2Splits.length){
            System.out.println("Error lengths don't match");
            return;
        }
        int length = v1Splits.length;
        double [] v1 = new double[length];
        double [] v2 = new double[length];
        IntStream.range(0, length).forEach(i -> {
                                              v1[i] = Double.parseDouble(v1Splits[i]);
                                              v2[i] = Double.parseDouble(v2Splits[i]);
                                           });

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        double cor = pearsonsCorrelation.correlation(v1, v2);
        System.out.println("PC Corr = " + cor);
        double dist = 1 - (1 + cor) / 2;
        System.out.println("Distance = " + dist);
        System.out.println("if dist < 1e-5? = " + (dist < 0.00001));
        short shortDist = (short) (dist * Short.MAX_VALUE);
        System.out.println("dist*Short.MAX_VALUE (as double) = " + (dist * Short.MAX_VALUE));
        System.out.println("dist*Short.MAX_VALUE (as short) = " + shortDist);

        DataOutputStream dos = new DataOutputStream(new FileOutputStream("test.bin"));
        dos.writeShort(shortDist);
        dos.flush();dos.close();

        DataInputStream dis = new DataInputStream(new FileInputStream("test.bin"));

        short shortDistFromFile = dis.readShort();
        System.out.println("Short dist from file = " + shortDistFromFile);

    }
}