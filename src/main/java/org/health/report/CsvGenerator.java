package org.health.report;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class CsvGenerator {

    private static CsvGenerator csvGenerator = null;

    public static CsvGenerator getInstance(){
        if(csvGenerator == null){
            csvGenerator = new CsvGenerator();
        }
        return csvGenerator;
    }

    public void generateReport(String jsonString, String name) {
        try {
            System.out.println(jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);

            //Covert to Json Array
            JSONArray temp = new JSONArray();
            temp.put(jsonObject);
            JSONObject mainObj = new JSONObject();
            mainObj.put(name,temp);

            JSONArray docs = mainObj.getJSONArray(name);

            String csvString = CDL.toString(docs);
            File file = new File("report.csv");
            FileUtils.writeStringToFile(file, csvString, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
