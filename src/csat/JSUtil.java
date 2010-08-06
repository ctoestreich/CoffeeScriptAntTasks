package csat;

import java.io.IOException;

import org.apache.tools.ant.BuildException;

/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
public class JSUtil {
    
    /**
     * 
     */
    public JSUtil() {
        super();
    }
    
    /**
     * 
     */
    public String readFile(String iFileName) {
        try {
            return Util.readFile(iFileName);
        }
        catch (IOException e) {
            String message = "IOException reading '" + iFileName + "': " + e.getMessage();
            System.err.println(message);
            throw new BuildException(message);
        }
    }
    
    /**
     * 
     */
    public void writeFile(String oFileName, String contents) {
        try {
            Util.writeFile(oFileName, contents);
        }
        catch (IOException e) {
            String message = "IOException writing '" + oFileName + "': " + e.getMessage();
            System.err.println(message);
            throw new BuildException(message);
        }
    }
    
}
