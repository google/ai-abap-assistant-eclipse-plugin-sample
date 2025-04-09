package com.google.abapassist.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.Platform;

public class LogUtil {

    public static void writeToLog(String message, String fileName) {
        try {
        	 Date now = new Date();
        	 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	 String formattedDateTime = formatter.format(now);
        	 
            String configPath = Platform.getInstallLocation().getURL().getPath();
            File logFile = new File(configPath + File.separator + fileName);
            FileWriter writer = new FileWriter(logFile, true); // Append mode
            writer.write(formattedDateTime + message + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }
}