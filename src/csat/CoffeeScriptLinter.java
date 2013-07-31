/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import org.mozilla.javascript.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 *
 */
public class CoffeeScriptLinter {

    static private final ScriptableObject CompilerScope = getCompilerScope();
    static private final ScriptableObject LinterScope = getLinterScope();
    private static final String CONFIGURATION = "configuration";
    private static final String SOURCE = "source";

    /**
     *
     */
    static public void main(String[] args) {
        if(args.length < 1) {
            throw new RuntimeException("argument required (literal CoffeeScript source)");
        }

        Map<String, Object> options = new HashMap<String, Object>();
        System.out.println(lint(args[0], options));
    }

    /**
     *
     */
    static private String getCoffeeScriptSource() {
        InputStream coffeeStream = JavaScriptTask.class.getResourceAsStream("coffee-script.js");
        Reader coffeeReader = new InputStreamReader(coffeeStream);

        try {
            String result = Util.readStream(coffeeReader, 8096);
            coffeeReader.close();
            return result;
        } catch(IOException e) {
            throw new RuntimeException("error reading coffee-script.js resource", e);
        }
    }

    static private String getCoffeeScriptLintSource() {
        InputStream lintStream = JavaScriptTask.class.getResourceAsStream("coffeelint.js");
        Reader lintReader = new InputStreamReader(lintStream);

        try {
            String result = Util.readStream(lintReader, 8096);
            lintReader.close();
            return result;
        } catch(IOException e) {
            throw new RuntimeException("error reading coffee-script.js resource", e);
        }
    }

    /**
     *
     */
    static private ScriptableObject getCompilerScope() {
        Context context = Context.enter();
        ScriptableObject scope = context.initStandardObjects();

        context.setOptimizationLevel(-1);
        context.evaluateString(scope, getCoffeeScriptSource(), "coffee-script.js", 1, null);

        Context.exit();
        return scope;
    }

    static private ScriptableObject getLinterScope() {
        Context context = Context.enter();
        ScriptableObject scope = context.initStandardObjects();

        context.setOptimizationLevel(-1);
        context.evaluateString(scope, getCoffeeScriptSource(), "coffee-script.js", 1, null);
        context.evaluateString(scope, getCoffeeScriptLintSource(), "coffeelint.js", 1, null);

        Context.exit();
        return scope;
    }

    /**
     *
     */
    static public String compile(String source) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(SOURCE, "<unnamed>");
        return lint(source, options);
    }

    /**
     *
     */
    static public String lint(String source, Map<String, Object> options) {
        Context context = Context.enter();
        List<String> lints = new ArrayList<String>();

        try {
            context.setOptimizationLevel(-1);
            Scriptable scope = context.newObject(LinterScope);
            scope.setParentScope(null);
            scope.setPrototype(LinterScope);

            for(Map.Entry<String, Object> entry : options.entrySet()) {
                ScriptableObject.putProperty(scope, entry.getKey(), entry.getValue());
            }

            ScriptableObject.putProperty(scope, SOURCE, Context.javaToJS(source, scope));

            try {
                Object result = context.evaluateString(
                        scope,
                        "coffeelint.lint(source, configuration)",
                        "<coffeelint lint>", 1,
                        null);

                NativeArray arr = (NativeArray) result;
                Object[] array = new Object[(int) arr.getLength()];

                for(Object o : arr.getIds()) {
                    int index = (Integer) o;
                    String lint = unwrapValue(arr.get(index, null)).toString();
                    lints.add(lint);
                }
            } catch(RuntimeException e) {
                if(!(e instanceof JavaScriptException)) {
                    throw e;
                }

                Object eValue = ((JavaScriptException) e).getValue();
                if(!(eValue instanceof ScriptableObject)) {
                    throw e;
                }

                ScriptableObject eObject = (ScriptableObject) eValue;

                String jsErrorClass = eObject.getClassName();
                String jsErrorMessage = "<undefined>";

                Object message = ScriptableObject.getProperty(eObject, "message");
                if(message instanceof String) {
                    jsErrorMessage = (String) message;
                }

                String msg = "linting error: " + e.getMessage();
                System.err.println(msg);

                throw new RuntimeException(jsErrorClass + ": " + jsErrorMessage);
            }

            return Context.toString(lints);
        } finally {
            Context.exit();
        }
    }

    public static Object unwrapValue(Object value) {
        if(value == null) {
            return null;
        } else if(value instanceof Wrapper) {
            // unwrap a Java object from a JavaScript wrapper
            // recursively call this method to convert the unwrapped value
            value = unwrapValue(((Wrapper) value).unwrap());
        } else if(value instanceof IdScriptableObject) {
            // check for special case Native object wrappers
            String className = ((IdScriptableObject) value).getClassName();
            // check for special case of the String object
            if("String".equals(className)) {
                value = Context.jsToJava(value, String.class);
            }
            // check for special case of a Date object
            else if("Date".equals(className)) {
                value = Context.jsToJava(value, Date.class);
            } else {
                // a scriptable object will probably indicate a multi-value property set
                // set using a JavaScript associative Array object
                Scriptable values = (Scriptable) value;
                Object[] propIds = values.getIds();

                // is it a JavaScript associative Array object using Integer indexes?
                if(values instanceof NativeArray && isArray(propIds)) {
                    // convert JavaScript array of values to a List of Serializable objects
                    List<Object> propValues = new ArrayList<Object>(propIds.length);
                    for(Object propId1 : propIds) {
                        // work on each key in turn
                        Integer propId = (Integer) propId1;

                        // we are only interested in keys that indicate a list of values
                        if(propId instanceof Integer) {
                            // get the value out for the specified key
                            Object val = values.get(propId, values);
                            // recursively call this method to convert the value
                            propValues.add(unwrapValue(val));
                        }
                    }

                    value = propValues;
                } else {
                    // any other JavaScript object that supports properties - convert to a Map of objects
                    Map<String, Object> propValues = new HashMap<String, Object>(propIds.length);
                    for(Object propId : propIds) {
                        // work on each key in turn
                        // we are only interested in keys that indicate a list of values
                        if(propId instanceof String) {
                            // get the value out for the specified key
                            Object val = values.get((String) propId, values);
                            // recursively call this method to convert the value
                            propValues.put((String) propId, unwrapValue(val));
                        }
                    }
                    value = propValues;
                }
            }
        } else if(value instanceof Object[]) {
            // convert back a list Object Java values
            Object[] array = (Object[]) value;
            ArrayList<Object> list = new ArrayList<Object>(array.length);
            for(Object anArray : array) {
                list.add(unwrapValue(anArray));
            }
            value = list;
        } else if(value instanceof Map) {
            // ensure each value in the Map is unwrapped (which may have been an unwrapped NativeMap!)
            Map<Object, Object> map = (Map<Object, Object>) value;
            Map<Object, Object> copyMap = new HashMap<Object, Object>(map.size());
            for(Object key : map.keySet()) {
                copyMap.put(key, unwrapValue(map.get(key)));
            }
            value = copyMap;
        }
        return value;
    }

    /**
     * Look at the id's of a native array and try to determine whether it's actually an Array or a Hashmap
     *
     * @param ids id's of the native array
     * @return boolean  true if it's an array, false otherwise (ie it's a map)
     */
    private static boolean isArray(final Object[] ids) {
        boolean result = true;
        for(Object id : ids) {
            if(!(id instanceof Integer)) {
                result = false;
                break;
            }
        }
        return result;
    }

}
