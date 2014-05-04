/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package utils.fileutils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * File management utilities implemented in this class
 */
public class FileManager {

    /**
     * Reads a file a returns string content
     * @param filePath Relative file path
     * @return String content of the file
     * @throws java.io.IOException
     */
    public static String readFile(String filePath) throws IOException {
        BufferedReader reader;
        StringBuilder stringBuilder;
        String line;
        String ls;
        reader = new BufferedReader(new FileReader(filePath));
        stringBuilder = new StringBuilder();
        ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();
        return stringBuilder.toString();
    }
}
