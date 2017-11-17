package com.zjaxn.jobs.temp;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties loadProperty(String path) {
        Properties properties = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(path));
            properties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void updateProperty(Properties properties, String outFilePath, Map<String, String> params) {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
            FileOutputStream outputFile = new FileOutputStream(outFilePath);
            properties.store(outputFile, null);
            outputFile.flush();
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showProperty(Properties properties) {
        if (properties == null) {
            return;
        }

        for (String key : properties.stringPropertyNames()) {
//            System.out.println(key + "=" + properties.getProperty(key)+"("+properties.getProperty(key).getClass()+")");
            System.out.println(key + "=" + properties.getProperty(key));
        }
    }

    public static String getProjectPath() {
        File file = new File("");
        String projectPath = "";
        try {
            projectPath = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projectPath;
    }
}
