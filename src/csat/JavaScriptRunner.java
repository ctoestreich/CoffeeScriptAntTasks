/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.util.optional.JavaxScriptRunner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 */
public class JavaScriptRunner {

    private static final String JSONsrc = getJSONsrc();
    
    private String             source;
    private String             sourceURL;
    private String[]           args;
    private Map<String,Object> properties;
    private ScriptableObject   prototype;

    /**
     * 
     */
    static public Result run(String source, String sourceURL, String[] args, Map<String,Object> properties) {
        return run(source, sourceURL, args, properties, null);
    }

    /**
     * 
     */
    static public Result run(String source, String sourceURL, String[] args, Map<String,Object> properties, ScriptableObject prototype) {
        return new JavaScriptRunner(source, sourceURL, args, properties, prototype).run();
    }

    /**
     * 
     */
    static private String getJSONsrc() {
        InputStream iStream = JavaScriptRunner.class.getResourceAsStream("json2.js");
        Reader isReader = new InputStreamReader(iStream);
        
        try {
            String result = Util.readStream(isReader, 8096);
            isReader.close();
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException("error reading json2.js resource", e);
        }
    }
    
    /**
     * 
     */
    private JavaScriptRunner(String source, String sourceURL, String[] args, Map<String,Object> properties, ScriptableObject prototype ) {
        super();
        
        if (null == source) throw new RuntimeException("source was not set");
        
        if (null == args)       args       = new String[0];
        if (null == properties) properties = new HashMap<String,Object>();
        
        this.source     = source;
        this.sourceURL  = sourceURL;
        this.args       = Arrays.copyOf(args, args.length);
        this.properties = new HashMap<String,Object>();
        this.prototype  = prototype;
        
        this.properties.putAll(properties);
    }
    
    /**
     * 
     */
    private Result run() {
        Context context = Context.enter();
        Result  result  = new Result();
        
        try {
            Scriptable scope  = new ImporterTopLevel(context);
            if (null != prototype) scope.setPrototype(prototype);
            
            ScriptableObject.putProperty(scope, "stdout",   Context.javaToJS(System.out, scope));
            ScriptableObject.putProperty(scope, "stderr",   Context.javaToJS(System.err, scope));
            ScriptableObject.putProperty(scope, "__FILE__", Context.javaToJS(sourceURL,  scope));
            ScriptableObject.putProperty(scope, "argv",     Context.javaToJS(args,       scope));

            for (Map.Entry<String, Object> entry: properties.entrySet()) {
                ScriptableObject.putProperty(scope, entry.getKey(), Context.javaToJS(entry.getValue(), scope));
            }
            
            if (null == sourceURL) sourceURL  = "<unnamed>";
            
            try {
                context.evaluateString(scope, JSONsrc, "json2.js", 1, null);
                result.result = context.evaluateString(scope, source, sourceURL, 1, null);
            }
            catch (RuntimeException e) {
                processException(e, result);
            }
            
            result.resultString = Context.toString(result.result);
        }
        finally {
            Context.exit();
        }
        
        return result;
    }

    /**
     * 
     */
    private void processException(RuntimeException e, Result result) {
        result.jsException = e;
        
        if (e instanceof EvaluatorException) {
            processEvaluatorException((EvaluatorException) e, result);
        }
        
        if (!(e instanceof JavaScriptException)) return;
        
        Object eValue = ((JavaScriptException) e).getValue();
        if (!(eValue instanceof ScriptableObject)) return;
        
        ScriptableObject eObject = (ScriptableObject) eValue;

        result.jsErrorClass   = eObject.getClassName();
        result.jsErrorMessage = "<undefined>";
        
        Object message = ScriptableObject.getProperty(eObject, "message");
        if (message instanceof String) {
            result.jsErrorMessage = (String) message;
        }
    }
    
    /**
     * 
     */
    private void processEvaluatorException(EvaluatorException e, Result result) {
        System.err.println("error: " + e.sourceName() + ":" + e.lineNumber() + ": " + e.details());
    }
    
    /**
     * 
     */
    public static class Result {
        public Object           result;
        public String           resultString;
        public RuntimeException jsException;
        public String           jsErrorClass;
        public String           jsErrorMessage;
        
        public Result() { super(); }
        
    }
}
