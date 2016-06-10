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
    public static Map<String,String> config = new HashMap<>();


    public static void main(String[] args) throws IOException {
        String configFile = args[0];
        populateConfig(configFile);
        String variationfile = config.get("VariationsFile");
        String datafile = config.get("DataFile");
        String outputfileDir = config.get("OutputFileDir");
        String outputfile = config.get("OutputFile");
        double rt_rangeMin = Double.valueOf(config.get("RT_Rangemin"));
        double rt_rangeMax = Double.valueOf(config.get("RT_Rangemax"));
        double m_z_rangeMin = Double.valueOf(config.get("M_Z_Rangemin"));
        double m_z_rangeMax = Double.valueOf(config.get("M_Z_Rangemax"));
        int chargeval = Integer.valueOf(config.get("charge"));

//        List<Integer> index = new ArrayList<>();
//        List<Double> mz = new ArrayList<>();
//        List<Double> rt = new ArrayList<>();
//        List<Integer> charge = new ArrayList<>();
//        List<String> slabel = new ArrayList<>();
//        List<Integer> peptideid = new ArrayList<>();
//        List<Integer> mclustt = new ArrayList<>();
//        List<Integer> medea = new ArrayList<>();
//        List<Integer> expert = new ArrayList<>();

      //  Map<Double,String> mapbymz = new TreeMap<Double,String>();
        ListMultimap<Double, String> mhm = MultimapBuilder.treeKeys().arrayListValues().build();
        ListMultimap<Double, String> mhmminus = MultimapBuilder.treeKeys().arrayListValues().build();

        Set<Integer> exprtset = new HashSet<>();

        populateVariations(variationfile);
        outputfileDir += rt_rangeMin + "x" + rt_rangeMax + "x" + m_z_rangeMin + "x" + m_z_rangeMax + "x" + chargeval;
        new File(outputfileDir).mkdirs();

        FileWriter writer = new FileWriter(outputfileDir + "/" +outputfile.substring(0,outputfile.indexOf(".")) + ".rt." + rt_rangeMin + "_" + rt_rangeMax + ".mz" + m_z_rangeMin + "_" + m_z_rangeMax + ".txt");
        PrintWriter printWriter = new PrintWriter(writer);

        try(BufferedReader buf = Files.newBufferedReader(Paths.get(datafile))){
            String line;
            line = buf.readLine();
            int count = 0;
            int counttotal = 0;
            double rtmax = 0,rtmin = Double.MAX_VALUE;
            double m_zmax = 0,m_zmin = Double.MAX_VALUE;
            printWriter.println("idx\tmz\trt\tcharge\tSimulatedClusterLable\tpeptide" +
                    ".id\tmclust\tmedea\texprt");
            while (!Strings.isNullOrEmpty(line = buf.readLine())){
                counttotal++;
                String[] values = line.split(",");
                int exprt = Integer.valueOf(values[0]);
                double m_over_z = Double.valueOf(values[1]);
                double rt = Double.valueOf(values[2]);
                int charge = Integer.valueOf(values[3]);
                m_over_z = m_over_z - (variations.get(exprt)*1.e-6*m_over_z);
                values[1] = String.valueOf(m_over_z);

                rtmax = Math.max(rtmax,rt);
                m_zmax = Math.max(m_over_z,m_zmax);
                rtmin = Math.min(rtmin, rt);
                m_zmin = Math.min(m_over_z, m_zmin);

                if(rt_rangeMin > rt || rt_rangeMax < rt) continue;
                if(m_z_rangeMin > m_over_z || m_z_rangeMax < m_over_z) continue;
                if(chargeval != -1 && charge != chargeval) continue;
                String outputline = count + "\t" + m_over_z + '\t' + rt + '\t' +
                                values[3] + '\t' + values[4] + '\t' + 0 +
                                '\t' + 0 + '\t' + 0 + '\t' + exprt;

                String outputlineminus = count + "\t" + m_over_z + '\t' + rt + '\t' +
                        values[3] + '\t' + values[4] + '\t' + -1 +
                        '\t' + -1 + '\t' + -1 + '\t' + exprt;


                mhm.put(m_over_z,outputline);
                mhmminus.put(m_over_z,outputlineminus);
                printWriter.println(outputline);

                exprtset.add(exprt);

                count++;
            }

            FileWriter writer2 = new FileWriter(outputfileDir + "/" +outputfile.substring(0,outputfile.indexOf(".")) + ".rt." + rt_rangeMin + "_" + rt_rangeMax + ".mz" + m_z_rangeMin + "_" + m_z_rangeMax + ".formatted.txt");
            PrintWriter printWriter2 = new PrintWriter(writer2);
            FileWriter writer3 = new FileWriter(outputfileDir + "/" +outputfile.substring(0,outputfile.indexOf(".")) + ".rt." + rt_rangeMin + "_" + rt_rangeMax + ".mz" + m_z_rangeMin + "_" + m_z_rangeMax + ".formatted.minus1.txt");
            PrintWriter printWriter3 = new PrintWriter(writer3);

            int count2 = 0;
            Collection<String> values = mhm.values();
            printWriter2.println("idx\tmz\trt\tcharge\tSimulatedClusterLable\tpeptide" +
                    ".id\tmclust\tmedea\texprt");
            for(String s: values){
                printWriter2.println(s);
                count2++;
            }

            Collection<String> valuesminus = mhmminus.values();
            printWriter3.println("idx\tmz\trt\tcharge\tSimulatedClusterLable\tpeptide" +
                    ".id\tmclust\tmedea\texprt");
            for(String s: valuesminus){
                printWriter3.println(s);
            }

            printWriter2.flush();
            printWriter2.close();

            printWriter3.flush();
            printWriter3.close();

            System.out.format("RT Min: (%f) and Max: (%f) \n", rtmin,rtmax);
            System.out.format("M/Z Min: (%f) and Max: (%f) \n", m_zmin,m_zmax);
            System.out.format("Total Number of data Points: %d \n", counttotal);
            System.out.format("Number of data Points after filter: %d \n", count);
            System.out.format("Number of data Points after filter in formatted: %d \n", count2);
            System.out.format("Number of experiments in filtered data: %d \n", exprtset.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        printWriter.flush();
        printWriter.close();
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