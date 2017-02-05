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
            PrintWriter printWriter = new PrintWriter(new FileWriter("/home/pulasthi/work/FushengWang/" + "All_images_20K_each" + ".data"));
            PrintWriter printWriterIndexes = new PrintWriter(new FileWriter("/home/pulasthi/work/FushengWang/" + "All_images_20K_each" + "_indexInfo.data"));
            int counterIndex = 0;
            int counterIndexall = 0;
            Iterator<String> iter = imageGroupMappings.keySet().iterator();
            BufferedReader reader;
            String line;
            List<String> values;
            String key;
            double prob = 0.0;
            Random random = new Random();
            int cluskey = 0;
            while (iter.hasNext()){
                key = iter.next();
                if(key.equals("TCGA-55-6543-01Z-00-DX1.08806fe0-84d3-4fd6-8746-6cf557241958")) {prob = 0.047882974; cluskey = 0;};
                if(key.equals("TCGA-86-6562-01Z-00-DX1.5dea3015-e606-4837-9f99-ac14f0aa091b")) {prob = 0.041587218; cluskey = 1;};
                if(key.equals("TCGA-55-6972-01Z-00-DX1.0b441ad0-c30f-4f63-849a-36c98d6e2d3b")) {prob = 0.208774805; cluskey = 2;};
                if(key.equals("TCGA-55-7914-01Z-00-DX1.875bffe1-8c56-4c29-ab11-6840ee3a643c")) {prob = 0.124239808; cluskey = 3;};
                if(key.equals("TCGA-55-6969-01Z-00-DX1.713df9f6-1a91-4e0e-ad43-42e66dcca191")) {prob = 0.03113107; cluskey = 4;};
                if(key.equals("TCGA-55-8094-01Z-00-DX1.8dc29615-e124-4f17-81a1-c0b20c38d12c")) {prob = 0.242171797; cluskey = 5;};
                if(key.equals("TCGA-83-5908-01Z-00-DX1.381c8f82-61a0-4e9d-982d-1ad0af7bead9")) {prob = 0.019502587; cluskey = 6;};
                if(key.equals("TCGA-55-7994-01Z-00-DX1.a0858080-c471-4337-bc57-7af57e9d92d8")) {prob = 0.060031937; cluskey = 7;};
                if(key.equals("TCGA-55-8092-01Z-00-DX1.04e44341-c4bb-4b0b-965c-7ec83f3877a6")) {prob = 0.154892272; cluskey = 8;};
                if(key.equals("TCGA-55-8087-01Z-00-DX1.548f2800-8caf-4c0e-a7b5-6d3d28315d9c")) {prob = 0.066891422; cluskey = 9;};
                if(key.equals("TCGA-55-7995-01Z-00-DX1.fef24d04-35a0-4f57-8f51-7ad602a78871")) {prob = 0.070487314; cluskey = 10;};

                //if(!key.equals("TCGA-83-5908-01Z-00-DX1.381c8f82-61a0-4e9d-982d-1ad0af7bead9")) continue;
                values = imageGroupMappings.get(key);

                for (String file: values){
                    reader = Files.newBufferedReader(Paths.get(dataFolder.getAbsolutePath() + "/" + file));

                    //read heaer line
                    line = reader.readLine();
                    while((line = reader.readLine()) != null){
                      //  lines.add(counterIndex, line);

                        if(random.nextDouble() < prob ){ // get around prob of data
                            printWriter.println(line.substring(0,line.lastIndexOf("[")-1));
                            printWriterIndexes.println(""+counterIndexall + "," + file + "," + cluskey);
                            counterIndex++;
                            counterIndexall++;
                        }
                     //   if(counterIndex > 657) break;
                    }

                }
                System.out.println(key + " " + counterIndex);
                counterIndex = 0;
            }
            printWriter.flush();
            printWriter.close();
            printWriterIndexes.flush();
            printWriterIndexes.close();
        }catch (IOException e){

        }

    }
}
