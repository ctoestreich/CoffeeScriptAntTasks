package csat;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
public class JSUtil {
    private Task task;
    
    /**
     * 
     */
    public JSUtil(Task task) {
        super();
        
        this.task = task;
    }
    
    /**
     * 
     */
    public String readFile(String iFileName) {
        try {
            return Util.readFile(iFileName, task);
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
            Util.writeFile(oFileName, task, contents);
        }
        catch (IOException e) {
            String message = "IOException writing '" + oFileName + "': " + e.getMessage();
            System.err.println(message);
            throw new BuildException(message);
        }
    }
    
}
