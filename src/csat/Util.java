/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

/**
 * 
 */
public class Util {
    static private final int DEFAULT_BUFFER_SIZE = 256 * 256 - 400; 
    
    /**
     * 
     */
    static public void writeFile(String iFileName, String contents) throws IOException {
        writeFile(new File(iFileName), contents);
    }

    /**
     * 
     */
    static public void writeFile(File iFile, String contents) throws IOException {
        FileWriter fw = new FileWriter(iFile);
        
        fw.write(contents);
        fw.close();
    }

    /**
     * 
     */
    static public String readFile(String iFileName) throws IOException {
        return readFile(new File(iFileName));
    }

    /**
     * 
     */
    static public String readFile(File iFile) throws IOException {
        long iFileLength = iFile.length();
        
        if (0 == iFileLength) return "";
        int bufferSize = (int) Math.min(iFileLength, (long) DEFAULT_BUFFER_SIZE);
        
        FileReader fr = new FileReader(iFile);
        String result = readStream(fr, bufferSize);
        fr.close();
        
        return result;
    }

    /**
     * 
     */
    static public String readStream(Reader rStream, int bufferSize) throws IOException {
        if (bufferSize <= 0) bufferSize = DEFAULT_BUFFER_SIZE;
        bufferSize = Math.min(bufferSize, DEFAULT_BUFFER_SIZE);
        
        char[] buffer    = new char[bufferSize];
        StringWriter sr  = new StringWriter(bufferSize);
        
        int read;
        while ((read = rStream.read(buffer)) > 0) {
            sr.write(buffer, 0, read);
        }
        
        return sr.toString();
    }

}
