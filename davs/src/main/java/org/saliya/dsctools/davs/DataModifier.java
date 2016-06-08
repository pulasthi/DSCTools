package org.saliya.dsctools.davs;

import com.google.common.base.Strings;
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

        Set<Integer> exprtset = new HashSet<>();

        populateVariations(variationfile);
        outputfileDir += rt_rangeMin + "x" + rt_rangeMax + "x" + m_z_rangeMin + "x" + m_z_rangeMax + "x";
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

                printWriter.println(
                            count + "\t" + m_over_z + '\t' + rt + '\t' +
                                    values[3] + '\t' + values[4] + '\t' + 0 +
                                    '\t' + 0 + '\t' + 0 + '\t' + exprt);

                exprtset.add(exprt);
                count++;
            }
            System.out.format("RT Min: (%f) and Max: (%f) \n", rtmin,rtmax);
            System.out.format("M/Z Min: (%f) and Max: (%f) \n", m_zmin,m_zmax);
            System.out.format("Total Number of data Points: %d \n", counttotal);
            System.out.format("Number of data Points after filter: %d \n", count);
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