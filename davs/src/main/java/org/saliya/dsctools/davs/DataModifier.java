package org.saliya.dsctools.davs;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataModifier{

    public static Map<Integer,Double> variations = new HashMap<>();
    public static Map<Integer,Double[]> experimentShifts = new HashMap<>();
    public static Map<String,String> config = new HashMap<>();


    public static void main(String[] args) throws IOException {
        String configFile = args[0];
        populateConfig(configFile);
        String variationfile = config.get("VariationsFile");
        String datafile = config.get("DataFile");
        String outputfileDir = config.get("OutputFileDir");
        String outputfile = config.get("OutputFile");
        String experimentShiftFile = config.get("ExperimentShiftFile");
        String experimentShift = config.get("ExperimentShift");
        Boolean isDataFromRun = Boolean.valueOf(config.get("datafromrun"));
        Boolean shiftInputFile = Boolean.valueOf(config.get("shiftinoutfile"));
        Boolean sortonmz = Boolean.valueOf(config.get("sortonmz"));

        if(experimentShift.equals("true")){
            populateExperimentShift(experimentShiftFile);
        }

        double rt_rangeMin = Double.valueOf(config.get("RT_Rangemin"));
        double rt_rangeMax = Double.valueOf(config.get("RT_Rangemax"));
        double m_z_rangeMin = Double.valueOf(config.get("M_Z_Rangemin"));
        double m_z_rangeMax = Double.valueOf(config.get("M_Z_Rangemax"));
        int chargeval = Integer.valueOf(config.get("charge"));

        ListMultimap<Double, String> mhmminus = MultimapBuilder.treeKeys().arrayListValues().build();

        Set<Integer> exprtset = new HashSet<>();

        populateVariations(variationfile);
        outputfileDir += rt_rangeMin + "x" + rt_rangeMax + "x" + m_z_rangeMin + "x" + m_z_rangeMax + "x" + chargeval;
        new File(outputfileDir).mkdirs();

        try(BufferedReader buf = Files.newBufferedReader(Paths.get(datafile))){
            String line;
            if(!isDataFromRun){
                line = buf.readLine();
            }
            int count = 0;
            int counttotal = 0;
            double rtmax = 0,rtmin = Double.MAX_VALUE;
            double m_zmax = 0,m_zmin = Double.MAX_VALUE;

            String outputline;
            String outputlineminus;
            int exprt;
            double m_over_z;
            double rt;
            int charge;
            double val1;
            double val2;
            ArrayList<String> centers = new ArrayList<>();
            ArrayList<String> temparraylist = new ArrayList<>();
            int[] exprimentcounts = new int[37];

            String ouputfileFinal = null;
            if(!isDataFromRun && !shiftInputFile) {
                while (!Strings.isNullOrEmpty(line = buf.readLine())) {
                    counttotal++;
                    String[] values = line.split(",");
                    exprt = Integer.valueOf(values[0]);
                    m_over_z = Double.valueOf(values[1]);
                    rt = Double.valueOf(values[2]);
                    charge = Integer.valueOf(values[3]);
                    m_over_z = m_over_z - (variations.get(exprt) * 1.e-6 * m_over_z);
                    values[1] = String.valueOf(m_over_z);

                    if (experimentShift.equals("true")) {
                        m_over_z = m_over_z - 2.1700e-6 * m_over_z * (experimentShifts.get(exprt)[0]);
                        rt = rt - 3.13 * experimentShifts.get(exprt)[2];
                    }

                    rtmax = Math.max(rtmax, rt);
                    m_zmax = Math.max(m_over_z, m_zmax);
                    rtmin = Math.min(rtmin, rt);
                    m_zmin = Math.min(m_over_z, m_zmin);

                    if (rt_rangeMin > rt || rt_rangeMax < rt) continue;
                    if (m_z_rangeMin > m_over_z || m_z_rangeMax < m_over_z) continue;
                    if (chargeval != -1 && charge != chargeval) continue;

                    outputlineminus = count + "\t" + m_over_z + '\t' + rt + '\t' +
                            values[3] + '\t' + values[4] + '\t' + -1 +
                            '\t' + -1 + '\t' + -1 + '\t' + exprt;


                    mhmminus.put(m_over_z, outputlineminus);
                    exprtset.add(exprt);
                    exprimentcounts[exprt - 1]++;
                    count++;
                }
                ouputfileFinal =  outputfileDir + "/" +outputfile.substring(0,outputfile.indexOf(".")) + ".rt." + rt_rangeMin + "_" + rt_rangeMax + ".mz" + m_z_rangeMin + "_" + m_z_rangeMax + ".formatted.minus1.txt";
            }else if(!shiftInputFile){
                int clusterNumber;
                while (!Strings.isNullOrEmpty(line = buf.readLine())) {
                    counttotal++;
                    String[] values = line.split(" ");
                    exprt = Integer.valueOf(values[9]);
                    m_over_z = Double.valueOf(values[1]);
                    rt = Double.valueOf(values[2]);
                    clusterNumber = Integer.valueOf(values[4]);

                    if(clusterNumber == 100000000 ) {
                        centers.add(line);
                        continue;
                    }

                    if (experimentShift.equals("true")) {
                        m_over_z = m_over_z - 2.1700e-6 * m_over_z * (experimentShifts.get(exprt)[0]);
                       rt = rt - 3.13 * experimentShifts.get(exprt)[2];
                    }
                    outputlineminus = values[0] + " " + m_over_z + ' ' + rt + ' ' +
                            values[3] + ' ' + values[4] + ' ' + values[5] +
                            ' ' + values[6] + ' ' + values[7] + ' ' + values[8] + ' ' + exprt;



                 //   mhmminus.put(m_over_z, outputlineminus);
                    temparraylist.add(outputlineminus);
                    exprtset.add(exprt);
                    exprimentcounts[exprt - 1]++;
                    count++;
                }
                ouputfileFinal = datafile.replace(".txt",".shifted.unsorted.txt");
            }else{

                while (!Strings.isNullOrEmpty(line = buf.readLine())) {
                    counttotal++;
                    String[] values = line.split("\t");
                    exprt = Integer.valueOf(values[8]);
                    m_over_z = Double.valueOf(values[1]);
                    rt = Double.valueOf(values[2]);


                    if (experimentShift.equals("true") && exprt == 14) {
                        m_over_z = m_over_z - (2.1700e-6 * m_over_z * (-1.1223612409));
                       // rt = rt + 1.451429232;
                    }

                    outputlineminus = values[0] + "\t" + m_over_z + '\t' + rt + '\t' +
                            values[3] + '\t' + values[4] + '\t' + values[5] +
                            '\t' + values[6] + '\t' + values[7] + '\t' + values[8];

                    if(sortonmz){
                        mhmminus.put(m_over_z, outputlineminus);
                    }else{
                        temparraylist.add(outputlineminus);
                    }
                    exprtset.add(exprt);
                    exprimentcounts[exprt - 1]++;

                    count++;
                }
                ouputfileFinal =  outputfileDir + "/" +outputfile.substring(0,outputfile.indexOf(".")) + ".rt." + rt_rangeMin + "_" + rt_rangeMax + ".mz" + m_z_rangeMin + "_" + m_z_rangeMax + ".formatted.shifted.mzonly_exrt14r1.temp.minus1.txt";

            }

            FileWriter writer3 = new FileWriter(ouputfileFinal);
            PrintWriter printWriter3 = new PrintWriter(writer3);

            int count2 = 0;

            if(!shiftInputFile){
                Collection<String> valuesminus = mhmminus.values();
                if(!isDataFromRun){
                    printWriter3.println("idx\tmz\trt\tcharge\tSimulatedClusterLable\tpeptide" +
                            ".id\tmclust\tmedea\texprt");
                    for(String s: valuesminus){
                        printWriter3.println(s);
                        count2++;
                    }
                }

                if(isDataFromRun){
                    temparraylist.forEach(printWriter3::println);
                    centers.forEach(printWriter3::println);
                }
            }else{
                printWriter3.println("idx\tmz\trt\tcharge\tSimulatedClusterLable\tpeptide" +
                        ".id\tmclust\tmedea\texprt");
                if(sortonmz){
                    Collection<String> valuesminus = mhmminus.values();
                    for(String s: valuesminus){
                        printWriter3.println(s);
                        count2++;
                    }
                }else{
                    temparraylist.forEach(printWriter3::println);
                }
            }


            printWriter3.flush();
            printWriter3.close();

            System.out.format("RT Min: (%f) and Max: (%f) \n", rtmin,rtmax);
            System.out.format("M/Z Min: (%f) and Max: (%f) \n", m_zmin,m_zmax);
            System.out.format("Total Number of data Points: %d \n", counttotal);
            System.out.format("Number of data Points after filter: %d \n", count);
            System.out.format("Number of data Points after filter in formatted: %d \n", count2);
            System.out.format("Number of experiments in filtered data: %d \n", exprtset.size());
            for (int i = 0; i < exprimentcounts.length; i++) {
                int exprimentcount = exprimentcounts[i];
                System.out.format("Number of poitns in : (%d) is: (%d) \n", i,exprimentcount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void populateExperimentShift(String experimentShiftFile) {
        try(BufferedReader buf = Files.newBufferedReader(Paths.get(experimentShiftFile))){
            String line = buf.readLine();
            while(!Strings.isNullOrEmpty(line = buf.readLine())){
                String[] values = line.split("\t");
                Double[] shifts = new Double[4];
                shifts[0] = Double.valueOf(values[1]);
                shifts[1] = Double.valueOf(values[2]);
                shifts[2] = Double.valueOf(values[3]);
                shifts[3] = Double.valueOf(values[4]);
                experimentShifts.put(Integer.valueOf(values[0]),shifts);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void populateConfig(String configFile) {
        try(BufferedReader buf = Files.newBufferedReader(Paths.get(configFile))){
            String line;
            while(!Strings.isNullOrEmpty(line = buf.readLine())){
                line = line.replaceAll("\\s+","");
                String[] values = line.split("=");
                config.put(values[0],values[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void populateVariations(String variationfile) {
        try(BufferedReader buf = Files.newBufferedReader(Paths.get(variationfile))){
            String line;
            buf.readLine();
            while (!Strings.isNullOrEmpty(line = buf.readLine())){
                String[] values = line.split(",");
                variations.put(Integer.valueOf(values[0]),Double.valueOf(values[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}