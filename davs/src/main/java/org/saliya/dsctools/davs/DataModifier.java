package org.saliya.dsctools.davs;

import com.google.common.base.Strings;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataModifier{

    public static Map<Integer,Double> variations = new HashMap<>();

    public static void main(String[] args) throws IOException {
        String variationfile = args[0];
        String datafile = args[1];
        String outputfile = args[2];
        populateVariations(variationfile);
        FileWriter writer = new FileWriter(outputfile);
        PrintWriter printWriter = new PrintWriter(writer);

        try(BufferedReader buf = Files.newBufferedReader(Paths.get(datafile))){
            String line;
            line = buf.readLine();
            printWriter.println(line);
            int count = 0;
            while (!Strings.isNullOrEmpty(line = buf.readLine())){
                String[] values = line.split(",");
                int exprt = Integer.valueOf(values[0]);
                double m_over_z = Double.valueOf(values[1]);
                m_over_z = m_over_z + (variations.get(exprt)*1.e-6);
                values[1] = String.valueOf(m_over_z);
                for (String value : values) {
                    printWriter.print(value);
                    printWriter.print(",");
                }
                printWriter.println();
                if(count%10000 == 0){
                    System.out.println(count);
                }
                count++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        printWriter.flush();
        printWriter.close();
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