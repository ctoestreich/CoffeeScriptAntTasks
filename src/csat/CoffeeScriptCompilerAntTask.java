/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * 
 */
public class CoffeeScriptCompilerAntTask extends MatchingTask {
    static private final Pattern NoExtPattern = Pattern.compile("(.*)\\..*");

    private boolean noWrap;
    private File    destDir;
    private boolean verbose;

    /**
     * 
     */
    public CoffeeScriptCompilerAntTask() {
        super();
        
        noWrap  = false;
    }

    /**
     * 
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void execute() throws BuildException {
        if (null == destDir) destDir = getProject().getBaseDir();
        
        FileSet fileSet = getImplicitFileSet();
        fileSet.setDir(getProject().getBaseDir());
        
        Iterator iter = fileSet.iterator();
        while (iter.hasNext()) {
            FileResource fileResource = (FileResource) iter.next();
            
            File iFile = fileResource.getFile();
            File oFile = getOutputFile(destDir, iFile);
            
            if (iFile.getAbsoluteFile() == oFile.getAbsoluteFile()) {
                throw new BuildException("input and output files the same for " + iFile);
            }
            
            verbose();
            
            if (iFile.lastModified() < oFile.lastModified()) {
                verbose("skipping:  " + iFile + " since it's older than " + oFile);
                continue;
            }

            Map<String, Object> compileOptions = new HashMap<String, Object>();
            compileOptions.put("source", iFile.getAbsoluteFile());
            compileOptions.put("noWrap",  noWrap);
            
            String source;
            try {
                source = Util.readFile(iFile);
            }
            catch (IOException e) {
                throw new BuildException("error reading file '" + iFile + "'", e);
            }

            verbose("compiling: " + iFile + " to " + oFile);
            String compiled; 
            try {
                compiled = CoffeeScriptCompiler.compile(source, compileOptions);
            }
            catch (RuntimeException e) {
                throw new BuildException("compile error", e);
            }
            
            try {
                Util.writeFile(oFile, compiled);
            }
            catch (IOException e) {
                throw new BuildException("error writing file '" + oFile + "'", e);
            }
        }
    }

    /**
     * 
     */
    private File getOutputFile(File destDir, File iFile) {
        String baseName = iFile.getName();
        
        Matcher matcher = NoExtPattern.matcher(baseName);
        if (matcher.matches()) {
            baseName = matcher.group(1);
        }
        
        return new File(destDir, baseName + ".js");
    }
    
    /**
     * 
     */
    public void setNowrap(boolean value) {
        noWrap = value;
    }

    /**
     * 
     */
    public void setVerbose(boolean value) {
        verbose = value;
    }

    /**
     * 
     */
    public void setDestdir(File value) {
        destDir = value;
    }
    
    /**
     * 
     */
    private void verbose(String ...args) {
        if (!verbose) return;
        
        for (String arg: args) {
            System.out.print(arg);
        }
        System.out.println();
    }
}
