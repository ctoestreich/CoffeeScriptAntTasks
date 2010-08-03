/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;

import csat.JavaScriptRunner.Result;

/**
 * 
 */
public class JavaScriptTask extends Task {
    private File      iFile;
    private String    taskText;
    private String    outProperty;
    private List<Arg> argsList;

    /**
     * 
     */
    public JavaScriptTask() {
        super();

        argsList = new ArrayList<Arg>();
    }

    /**
     * 
     */
    public void setSrc(File value) {
        iFile = value;
    }

    /**
     * 
     */
    public void setOut(String value) {
        outProperty = value;
    }

    /**
     * 
     */
    public void addText(String value) {
        taskText = value;
        
        if (0 == taskText.trim().length())
            taskText = null;
    }

    /**
     * Adds a command-line argument.
     */
    public Arg createArg() {
        Arg arg = new Arg();

        argsList.add(arg);

        return arg;
    }

    /**
     * 
     */
    public String getSource() throws BuildException {
        String source = taskText;

        if ((null == iFile) && (null == source)) {
            throw new BuildException("must use either the 'src' attribute or embedded text", getLocation());
        }

        if ((null != iFile) && (null != source)) {
            throw new BuildException("cannot use both the 'src' attribute and embedded text", getLocation());
        }

        if (null != iFile) {
            try {
                source = Util.readFile(iFile);
            } 
            catch (IOException e) {
                throw new BuildException("error reading '" + iFile.getAbsolutePath() + "'", e);
            }
        }

        return source;
    }

    /**
     * 
     */
    public void execute() {
        String source = getSource();

        String[] args = new String[argsList.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = argsList.get(i).getValue();
        }

        String iFileName = null;
        if (null != iFile)
            iFileName = iFile.getAbsolutePath();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("task", this);

        Result result = JavaScriptRunner.run(source, iFileName, args, properties);

        if (result.jsErrorClass != null) {
            String msg = result.jsErrorClass + ": " + result.jsErrorMessage;
            throw new BuildException(msg, getLocation());
        }
        
        if (result.jsException != null) {
            processException(result.jsException);
        }

        if ((null != outProperty) && (null != result)) {
            getProject().setProperty(outProperty, result.resultString);
        }

    }

    /**
     * process exception
     */
    private void processException(RuntimeException e) {
        if (e instanceof RhinoException) processRhinoException((RhinoException) e);
        
        String msg ="exception: " + e.getClass().getName() + ": " + e;

        throw new BuildException(msg, e, getLocation());
    }

    /**
     * process exception
     */
    private void processRhinoException(RhinoException e) {
        if (e instanceof JavaScriptException) processJavaScriptException((JavaScriptException) e);
        
        String msg = "exception: " + e.getClass().getName() + ": " + e.details();
        
        throw new BuildException(msg, e, getLocation());
    }

    /**
     * process exception
     */
    private void processJavaScriptException(JavaScriptException e) {
        Object eValue = e.getValue();
        
        if (eValue instanceof NativeJavaObject) {
            eValue = ((NativeJavaObject) eValue).unwrap();
        }

        boolean isBuildException = eValue instanceof BuildException;
        if (isBuildException) {
            BuildException be = (BuildException) eValue;
            be.setLocation(getLocation());
            throw be;
        }
    }

    /**
     * maps to the arg element in the ant task
     */
    public static class Arg {
        private String arg;

        public Arg() {
            super();
        }

        public void setValue(String value) {
            arg = value;
        }

        public String getValue() {
            return arg;
        }
    }

}
