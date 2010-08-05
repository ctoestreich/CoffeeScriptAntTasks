/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

/**
 * 
 */
public class Main {
    
    /**
     * 
     */
    static public void main(String[] args) throws Exception {
        Properties versions = loadVersions();
        
        PrintStream os = System.out;
        
        os.println("CoffeeScriptAntTasks:");
        os.println("");
        os.println("This jar file contains Ant tasks to run JavaScript and CoffeeScript.");
        os.println("Add the following lines to your ant script to enable the tasks:");
        os.println("");
        os.println("   <path id=\"cp\"><pathelement path=\"csat.jar\"/></path>");
        os.println("   <taskdef name=\"JavaScript\"    classname=\"csat.JavaScriptTask\"    classpathref=\"cp\"/>");
        os.println("   <taskdef name=\"CoffeeScript\"  classname=\"csat.CoffeeScriptTask\"  classpathref=\"cp\"/>");
        os.println("   <taskdef name=\"CoffeeScriptC\" classname=\"csat.CoffeeScriptCTask\" classpathref=\"cp\"/>");
        os.println("");
        os.println("versions:");
        os.println("   CoffeeScriptAntTasks: " + versions.getProperty("CSAT-VERSION"));
        os.println("   CoffeeScript:         " + versions.getProperty("COFFEESCRIPT-VERSION"));
        os.println("   Rhino:                " + versions.getProperty("RHINO-VERSION"));
        os.println("   Apache Commons CLI:   " + versions.getProperty("CLI-VERSION"));
        os.println("");
        os.println("home:");
        os.println("   http://github.com/pmuellr/CoffeeScriptAntTasks");
    }

    /**
     * 
     */
    static private Properties loadVersions() throws IOException {
        Properties properties = new Properties();
        
        properties.load(Main.class.getResourceAsStream("versions.properties"));
        
        return properties;
    }

}
