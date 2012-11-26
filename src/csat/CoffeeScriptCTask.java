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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CoffeeScriptCTask extends Task {

    static private final Pattern NoExtPattern = Pattern.compile("(.*)\\..*");

    Vector<FileSet> filesets = new Vector<FileSet>();

    private boolean noWrap;
    private File destDir;
    private boolean verbose;
    private boolean nesting;
    private boolean nodes;
    private boolean print;
    private boolean showVersion;

    /**
     *
     */
    public CoffeeScriptCTask() {
        super();

        noWrap = false;
        nesting = false;
        nodes = false;
        print = false;
        showVersion = false;
    }

    /**
     *
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void execute() throws BuildException {
        if(null == destDir) {
            destDir = getProject().getBaseDir();
        }

        //FileSet fileSet = getImplicitFileSet();
        //fileSet.setDir(getProject().getBaseDir());
        //Iterator iter = fileSet.iterator();

//        DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
        //verbose(ds.getBasedir().getAbsolutePath());
//        String[] includedFiles = ds.getIncludedFiles();
//        FileSelector[] selectors = fileSet.getSelectors(getProject());
//        for(FileSelector selector:selectors){
//            verbose(selector.toString());
//        }
//
//        String [] dirs = ds.getIncludedDirectories();
//        for(String dir : dirs){
//            verbose(dir);
//        }
//
//        String[] includedFiles = ds.getIncludedFiles();
//        String foundLocation = null;
//        for(String includedFile : includedFiles) {
//            verbose(includedFile);
//            String filename = includedFile.replace('\\', '/');           // 4
//            filename = filename.substring(filename.lastIndexOf("/") + 1);
//
//            File base = ds.getBasedir();
//            verbose(base.toString());
//            File found = new File(base, includedFile);
//            verbose(found.getAbsolutePath());
//
//        }

        for(FileSet fileset : filesets) {
            String filesetSrcPath = fileset.getDir().getPath();
            DirectoryScanner ds = fileset.getDirectoryScanner();
            File dir = ds.getBasedir();
            String[] filesInSet = ds.getIncludedFiles();
            for(String filename : filesInSet) {
                FileResource fileResource = new FileResource(dir, filename);
                File iFile = fileResource.getFile();

                File oFile = getOutputFile(destDir, filesetSrcPath, iFile);

                if(iFile.getAbsoluteFile() == oFile.getAbsoluteFile()) {
                    throw new BuildException("input and output files the same for " + iFile);
                }

                if(iFile.lastModified() < oFile.lastModified()) {
                    verbose("skipping:  " + iFile + " since it's older than " + oFile);
                    continue;
                }

                Map<String, Object> compileOptions = new HashMap<String, Object>();
                compileOptions.put("source", iFile.getAbsoluteFile());
                compileOptions.put("bare", noWrap);
                compileOptions.put("nodes", nodes);
                compileOptions.put("version", showVersion);
                compileOptions.put("print", print);

                String source;
                try {
                    source = Util.readFile(iFile);
                } catch(IOException e) {
                    throw new BuildException("error reading file '" + iFile + "'", e);
                }

                verbose("compiling: " + iFile + " to " + oFile);
                String compiled;
                try {
                    compiled = CoffeeScriptCompiler.compile(source, compileOptions);
                } catch(RuntimeException e) {
                    throw new BuildException("compile error", e);
                }

                try {
                    Util.writeFile(oFile, compiled);
                } catch(IOException e) {
                    throw new BuildException("error writing file '" + oFile + "'", e);
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

        return new File(destDir, baseName + ".js");
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
            subDir = iFile.getAbsolutePath().replace(srcDir, "").replace(iFile.getName(),"");
            destDir = new File(destDir.getAbsolutePath() + subDir);
        }

        if(!destDir.exists()){
            destDir.mkdir();
        }

        return new File(destDir, baseName + ".js");
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
