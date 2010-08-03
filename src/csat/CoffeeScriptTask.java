/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;

/**
 * 
 */
public class CoffeeScriptTask extends JavaScriptTask {

    static private final boolean DEBUG = false;

    private Map<String, Object> compileOptions;
    
    /**
     * 
     */
    public CoffeeScriptTask() {
        super();
        
        compileOptions = new HashMap<String, Object>();
        compileOptions.put("noWrap",  true);
    }

    /**
     * 
     */
    @Override 
    public void setSrc(File value) {
        super.setSrc(value);
        
        compileOptions.put("source", value.getAbsoluteFile());
    }
    
    /**
     * 
     */
    @Override
    public String getSource() throws BuildException {
        if (DEBUG) {
            String source = super.getSource();
            System.out.print("CoffeeScript source:\n" + source);
            source = CoffeeScriptCompiler.compile(source, compileOptions);
            System.out.print("CoffeeScript genned:\n" + source);
            return source;
        }
        
        return CoffeeScriptCompiler.compile(super.getSource(), compileOptions);
    }
}
