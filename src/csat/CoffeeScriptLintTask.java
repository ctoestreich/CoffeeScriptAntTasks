/*
 * Copyright (c) 2010 Patrick Mueller
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package csat;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CoffeeScriptLintTask extends Task {

    static private final Pattern NoExtPattern = Pattern.compile("(.*)\\..*");
    private static final String CONFIGURATION = "configuration";
    private static final String SOURCE = "source";
    private static final String EMPTY_LITS = "[]";
    private static final String DOT_JS = ".js";

    Vector<FileSet> filesets = new Vector<FileSet>();

    private boolean verbose;
    private boolean nesting;
    private boolean nodes;
    private boolean print;
    private boolean showVersion;
    private boolean overwriteNewer;
    private boolean failOnLint;
    private File configFile;

    /**
     *
     */
    public CoffeeScriptLintTask() {
        super();
        failOnLint = false;
        nesting = false;
        nodes = false;
        print = false;
        showVersion = false;
        overwriteNewer = false;
    }

    /**
     *
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void execute() throws BuildException {
        String lintConfig = readFile(configFile);

        for(FileSet fileset : filesets) {
            String filesetSrcPath = fileset.getDir().getPath();
            DirectoryScanner ds = fileset.getDirectoryScanner();
            File dir = ds.getBasedir();
            String[] filesInSet = ds.getIncludedFiles();
            for(String filename : filesInSet) {
                FileResource fileResource = new FileResource(dir, filename);
                File iFile = fileResource.getFile();

                Map<String, Object> compileOptions = new HashMap<String, Object>();
                compileOptions.put(CONFIGURATION, lintConfig);

                String source;
                try {
                    source = Util.readFile(iFile);
                } catch(IOException e) {
                    throw new BuildException("error reading file '" + iFile + "'", e);
                }

                verbose("linting: " + iFile);
                String lintResult;

                try {
                    lintResult = CoffeeScriptLinter.lint(source, compileOptions);
                    if(failOnLint && !lintResult.equals(EMPTY_LITS)) {
                        throw new BuildException(lintResult);
                    }
                } catch(RuntimeException e) {
                    throw new BuildException("CoffeeLint violation", e);
                }

                try {
                    verbose(lintResult);
                } catch(Exception e) {
                    throw new BuildException("Error linting file '" + iFile + "'", e);
                }
            }
        }
    }

    private File getOutputFile(File destDir, File iFile) {
        String newDir = "";
        String baseName = iFile.getName();

        Matcher matcher = NoExtPattern.matcher(baseName);
        if(matcher.matches()) {
            baseName = matcher.group(1);
        }

        if(nesting) {
            destDir = new File(destDir.getPath() + newDir);
        }

        return new File(destDir, baseName + DOT_JS);
    }

    /**
     *
     */
    private File getOutputFile(File destDir, String srcDir, File iFile) {
        String subDir = "";
        String baseName = iFile.getName();

        Matcher matcher = NoExtPattern.matcher(baseName);
        if(matcher.matches()) {
            baseName = matcher.group(1);
        }

        if(nesting) {
            subDir = iFile.getAbsolutePath().replace(srcDir, "").replace(iFile.getName(), "");
            destDir = new File(destDir.getAbsolutePath() + subDir);
        }

        if(!destDir.exists()) {
            destDir.mkdirs();
        }

        return new File(destDir, baseName + DOT_JS);
    }

    protected String readFile(File file) {
        String content = "{}";
        if(file != null) {
            try {
                FileReader reader = new FileReader(file);
                char[] chars = new char[(int) file.length()];
                reader.read(chars);
                content = new String(chars);
                reader.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public void addFileSet(FileSet fileset) {
        if(!filesets.contains(fileset)) {
            filesets.add(fileset);
        }
    }

    public void setNesting(boolean value) {
        nesting = value;
    }

    public void setPrint(boolean value) {
        print = value;
    }

    public void setNodes(boolean nodes) {
        this.nodes = nodes;
    }

    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }

    /**
     *
     */
    public void setVerbose(boolean value) {
        verbose = value;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    /**
     *
     */
    public void setOverwriteNewer(boolean value) {
        overwriteNewer = value;
    }

    public void setFailOnLint(boolean failOnLint) {
        this.failOnLint = failOnLint;
    }

    /**
     *
     */
    private void verbose(String... args) {
        if(!verbose) {
            return;
        }

        for(String arg : args) {
            System.out.print(arg);
        }
        System.out.println();
    }
}
