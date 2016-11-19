package org.pulasthi.dsctools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by pulasthi on 11/18/16.
 */
public class SingleFileGeneration {
    public static void main(String args[]){
        Map<String, ArrayList<String>> imageGroupMappings = new HashMap<String, ArrayList<String>>();
        Map<Integer, String> indexXgroup = new HashMap<Integer, String>();
        ArrayList<String> lines = new ArrayList<String>();
        int numFeature = 96;
        int numberofRows = 0;

        File dataFolder = new File("/home/pulasthi/work/FushengWang/fox");
        String files[] = dataFolder.list();

        for(String fileName : files){
            String group = fileName.substring(0,fileName.lastIndexOf("_mpp"));

            if(!imageGroupMappings.containsKey(group)) imageGroupMappings.put(group, new ArrayList<String>());

            imageGroupMappings.get(group).add(fileName);

        }

        try{
            PrintWriter printWriter = new PrintWriter(new FileWriter("/home/pulasthi/work/FushengWang/" + "TCGA-55-6972-01Z-00-DX1.0b441ad0-c30f-4f63-849a-36c98d6e2d3b.small" + ".data"));
            PrintWriter printWriterIndexes = new PrintWriter(new FileWriter("/home/pulasthi/work/FushengWang/" + "TCGA-55-6972-01Z-00-DX1.0b441ad0-c30f-4f63-849a-36c98d6e2d3b.small" + "_indexInfo.data"));
            int counterIndex = 0;
            Iterator<String> iter = imageGroupMappings.keySet().iterator();
            BufferedReader reader;
            String line;
            List<String> values;
            String key;
            while (iter.hasNext()){
                key = iter.next();

                if(!key.equals("TCGA-55-6972-01Z-00-DX1.0b441ad0-c30f-4f63-849a-36c98d6e2d3b")) continue;
                values = imageGroupMappings.get(key);

                for (String file: values){
                    reader = Files.newBufferedReader(Paths.get(dataFolder.getAbsolutePath() + "/" + file));

                    //read heaer line
                    line = reader.readLine();
                    while((line = reader.readLine()) != null){
                      //  lines.add(counterIndex, line);
                        printWriter.println(line.substring(0,line.lastIndexOf("[")-1));
                        printWriterIndexes.println(""+counterIndex + "," + file);
                       // indexXgroup.put(counterIndex, key);
                        counterIndex++;
                        if(counterIndex > 657) break;
                    }

                }
               // counterIndex = 0;
            }
            printWriter.flush();
            printWriter.close();
            printWriterIndexes.flush();
            printWriterIndexes.close();
        }catch (IOException e){

        }

        System.out.println("Asdasda");
    }
}
