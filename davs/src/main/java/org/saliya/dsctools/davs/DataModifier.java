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
        String outputfile = config.get("OutputFile");
        double rt_rangeMin = Double.valueOf(config.get("RT_Rangemin"));
        double rt_rangeMax = Double.valueOf(config.get("RT_Rangemax"));
        double m_z_rangeMin = Double.valueOf(config.get("M_Z_Rangemin"));
        double m_z_rangeMax = Double.valueOf(config.get("M_Z_Rangemax"));

        Set<Integer> exprtset = new HashSet<>();

        populateVariations(variationfile);
        FileWriter writer = new FileWriter(outputfile + "_:" + rt_rangeMin + ":" + rt_rangeMax + "_" + m_z_rangeMin + ":" + m_z_rangeMax);
        PrintWriter printWriter = new PrintWriter(writer);

        try(BufferedReader buf = Files.newBufferedReader(Paths.get(datafile))){
            String line;
            line = buf.readLine();
            printWriter.println(line);
            int count = 0;
            int counttotal = 0;
            double rtmax = 0,rtmin = Double.MAX_VALUE;
            double m_zmax = 0,m_zmin = Double.MAX_VALUE;
            while (!Strings.isNullOrEmpty(line = buf.readLine())){
                counttotal++;
                String[] values = line.split(",");
                int exprt = Integer.valueOf(values[0]);
                double m_over_z = Double.valueOf(values[1]);
                double rt = Double.valueOf(values[2]);
                m_over_z = m_over_z + (variations.get(exprt)*1.e-6);
                values[1] = String.valueOf(m_over_z);

                rtmax = Math.max(rtmax,rt);
                m_zmax = Math.max(m_over_z,m_zmax);
                rtmin = Math.min(rtmin, rt);
                m_zmin = Math.min(m_over_z, m_zmin);

                if(rt_rangeMin > rt || rt_rangeMax < rt) continue;
                if(m_z_rangeMin > m_over_z || m_z_rangeMax < m_over_z) continue;

                for (String value : values) {
                    printWriter.print(value);
                    printWriter.print(",");
                }
                exprtset.add(exprt);
                printWriter.println();
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