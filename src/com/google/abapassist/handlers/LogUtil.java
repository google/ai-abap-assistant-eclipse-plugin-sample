/*********************************************************************
*  Copyright 2025 Google LLC                                         *
*                                                                    *
*  Licensed under the Apache License, Version 2.0 (the "License");   *
*  you may not use this file except in compliance with the License.  *
*  You may obtain a copy of the License at                           *
*      https://www.apache.org/licenses/LICENSE-2.0                   *
*  Unless required by applicable law or agreed to in writing,        *
*  software distributed under the License is distributed on an       *
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,      *
*  either express or implied.                                        *
*  See the License for the specific language governing permissions   *
*  and limitations under the License.                                *
*********************************************************************/
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