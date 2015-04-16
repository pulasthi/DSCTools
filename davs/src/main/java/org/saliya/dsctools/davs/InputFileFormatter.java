package org.saliya.dsctools.davs;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class InputFileFormatter {
    public static void main(String[] args) {
        String inputFile = "E:\\Sali\\InCloud\\IUBox\\Box " +
                "Sync\\Sponge\\data2\\input\\MSSampleafterjitter.txt";
        String outputDirectory = "E:\\Sali\\InCloud\\IUBox\\Box " +
                "Sync\\Sponge\\data2\\input\\formatcorrected";
        int charge = 2;


        Path correctedFile = Paths.get(outputDirectory,
                                       Files.getNameWithoutExtension(
                                               inputFile) + "_correct.txt");
        Path metaFile = Paths.get(
                outputDirectory, Files.getNameWithoutExtension(inputFile)
                        + "_meta.txt");

        try (BufferedReader reader = Files
                .newReader(new File(inputFile), Charset.defaultCharset());
             PrintWriter writer = new PrintWriter(
                     Files.newWriter(new File(correctedFile.toString()),
                                     Charset.defaultCharset()), true);
             PrintWriter metaWriter = new PrintWriter(
                     Files.newWriter(new File(metaFile.toString()),
                                     Charset.defaultCharset()), true)) {


            Pattern pattern = Pattern.compile("[\t ]");
            String line;

            int id = -1;
            Hashtable<String, Integer[]> namesToCountAndID = new Hashtable<>();
            Hashtable<Integer, String> idToName = new Hashtable<>();

            while (!Strings.isNullOrEmpty(line = reader.readLine())){
                String [] splits = pattern.split(line.trim());
                assert splits.length == 3;
                String name = splits[0];
                if (!namesToCountAndID.containsKey(name)){
                    namesToCountAndID.put(name, new Integer[]{1,++id});
                    idToName.put(id, name);
                } else {
                    ++namesToCountAndID.get(name)[0];
                }
            }

            BufferedReader reader2 = Files
                    .newReader(new File(inputFile), Charset.defaultCharset());
            writer.println("idx\tmz\trt\tcharge\tpeptide\tpeptide" +
                                   ".id\tmclust\tmedea");

            int idx = 0;
            while (!Strings.isNullOrEmpty(line = reader2.readLine())) {
                String[] splits = pattern.split(line.trim());
                String name = splits[0];
                Integer[] countAndID = namesToCountAndID.get(name);
                writer.println(
                        idx + "\t" + splits[1] + '\t' + splits[2] + '\t' +
                                charge + '\t' + name + '\t' + countAndID[1] +
                                '\t' + countAndID[0] + '\t' + countAndID[0]);

                ++idx;
            }

            for (int i = 0; i < idToName.size(); ++i){
                String name = idToName.get(i);
                metaWriter.println(i + "\t" + name + '\t' + namesToCountAndID.get(name)[0]);
            }

            reader2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
