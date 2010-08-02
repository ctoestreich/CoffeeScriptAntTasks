/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 */
public class CoffeeScriptCompiler {

    static private final ScriptableObject CompilerScope = getCompilerScope();

    /**
     * 
     */
    static public void main(String[] args) {
        if (args.length < 1) throw new RuntimeException("argument required (literal CoffeeScript source)");
        
        Map<String,Object> options = new HashMap<String,Object>();
        options.put("noWrap", true);
        System.out.println(compile(args[0], options));
    }

    /**
     * 
     */
    static private String getCoffeeScriptSource() {
        InputStream iStream = JavaScriptAntTask.class.getResourceAsStream("coffee-script.js");
        Reader isReader = new InputStreamReader(iStream);
        
        try {
            String result = Util.readStream(isReader, 8096);
            isReader.close();
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException("error reading coffee-script.js resource", e);
        }
    }
    
    /**
     * 
     */
    static private ScriptableObject getCompilerScope() {
        Context          context = Context.enter();
        ScriptableObject scope   = context.initStandardObjects();
            
        context.setOptimizationLevel(-1);
        context.evaluateString(scope, getCoffeeScriptSource(), "coffee-script.js", 1, null);
            
        Context.exit();
        return scope;
    }
    
    /**
     * 
     */
    static public String compile(String source) {
        Map<String, Object> options = new HashMap<String, Object>();
        
        options.put("source", "<unnamed>");
        return compile(source, options);
    }

    /**
     * 
     */
    static public String compile(String source, Map<String,Object> options) {
        Context context = Context.enter();

        try {
            context.setOptimizationLevel(-1);
            
            Scriptable scope = context.newObject(CompilerScope);
            scope.setParentScope(null);
            scope.setPrototype(CompilerScope);
            
            Scriptable opts = context.newObject(scope);
            for (Map.Entry<String,Object> entry : options.entrySet()) {
                ScriptableObject.putProperty(opts, entry.getKey(), entry.getValue());
            }
            
            ScriptableObject.putProperty(scope, "options", opts);
            ScriptableObject.putProperty(scope, "source",  Context.javaToJS(source, scope));
            
            Object result = null;
            try {
                result = context.evaluateString(
                        scope, 
                        "CoffeeScript.compile(source, options)",
                        "<coffee-script compilation>", 1, 
                        null);
            }
            catch (RuntimeException e) {
                if (!(e instanceof JavaScriptException)) throw e;

                Object eValue = ((JavaScriptException) e).getValue();
                if (!(eValue instanceof ScriptableObject)) throw e;

                ScriptableObject eObject = (ScriptableObject) eValue;

                String jsErrorClass   = eObject.getClassName();
                String jsErrorMessage = "<undefined>";

                Object message = ScriptableObject.getProperty(eObject, "message");
                if (message instanceof String) {
                    jsErrorMessage = (String) message;
                }

                String msg = "compile error: " + e.getMessage();
                System.err.println(msg);
                
                throw new RuntimeException(jsErrorClass + ": " + jsErrorMessage);
            }
            
            return Context.toString(result);
        }
        finally {
            Context.exit();
        }
    }

}
