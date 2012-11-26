CoffeeScript Ant Tasks
=============================

This project contains Ant tasks to run 
[CoffeeScript](http://jashkenas.github.com/coffee-script/) 
from Ant, and compile CoffeeScript files to JavaScript.  
It uses Rhino as the underlying JavaScript engine.

Examples
========

The example below writes a line to stdout and a line to stderr.

        <CoffeeScript>
            stdout.println "stdout: hello, world!"
            stderr.println "stderr: hello, world!"
        </CoffeeScript>

The example below will set the property `theResult` to the
string `"the result"`

        <CoffeeScript result="theResult">
            "the result"
        </CoffeeScript>

When run after the previous command, this example will
print the value of previous output.

        <CoffeeScript>
            <arg value="${theResult}"/>
            stdout.println "the previous result was '${argv[0]}'"
        </CoffeeScript>


Here's an example that runs CoffeeScript stored in a file.

        <CoffeeScript src="test/sample.coffee" result="theResult2">
            <arg value="arg #1"/>
            <arg value="the second arg"/>
            <arg value="finally, the third arg"/>
        </CoffeeScript>

Here's some example CoffeeScript that could be stored in that file:

        result: []
        
        stdout.println "in $__FILE__"
        stdout.println "argv.length: ${argv.length}"
        
        result: for i in [0...argv.length]
            stdout.println "argv[${i}]: ${argv[i]}"
            argv[i]
        
        result: result.join ", "
        stdout.println "the result from running in the script is '${result}'"
        
        result

This example compiles CoffeeScript into JavaScript that puts all files at the test/out directory with no .call() wrapper.

        <CoffeeScriptC nowrap="true" destDir="test/out" verbose="true" nesting="false">
            <fileset dir="test/src" includes="**/*" />
        </CoffeeScriptC>

This example compiles CoffeeScript into JavaScript that puts all files under their child directories test/out/** with no .call() wrapper.

        <CoffeeScriptC nowrap="true" destDir="test/out" verbose="true" nesting="true">
            <fileset dir="test/src" includes="**/*" />
        </CoffeeScriptC>

Running
=======

Three Ant tasks are included:

`JavaScript`
------------

Runs JavaScript code.

The `JavaScript` task supports the following attributes:

* `src` - the name of a file that contains the JavaScript code
  to run

  Instead of running a file of JavaScript code, you can place the
  JavaScript code in the task as text instead.  JavaScript code
  embedded directly in the task will **NOT** have typical Ant property
  substitution done on it.

* `result` - the name of a property to place the result of running
  the JavaScript code in.  The result of the JavaScript code is
  converted to a string and then placed in the property.

The `JavaScript` task supports the following nested elements:

* `<arg>` - contains an argument to pass to the JavaScript
  code.  May be used multiple times.  Must contain an attribute
  named `value` which is the value to pass to the JavaScript code.
  Note that this is similar to the `Exec` task.  
  
  An `<arg>` element may specify an attribute named `type` whose
  value is `file`, in which case the argument is passed to 
  JavaScript as a `java.io.File` object.  The `value` attribute
  should contain a file name relative to the Project's `basedir`
  attribute. 

When the JavaScript code runs, the following variables will be set:

* `argv` - an array of the values set by the `<arg>` elements
* `stdout` - set to `System.out`
* `stderr` - set to `System.err`
* `__FILE__` - set to the name of the script
* `task` - set to the Ant task that is being run
* `util` - set to an object with some useful methods

### The `util` object ###

When your script runs with the `JavaScript` or `CoffeeScript` tasks,
an additional object is available to your code in the global property
name `util`.  This object has the following functions:

* `readFile(fileName)`
 
  This function will read the specified file and return the contents
  as a string.  The file name is relative to the project's `basedir`.

* `writeFile(fileName, contents)`

  This function will write the specified contents to the specified file.
  The file name is relative to the project's `basedir`.

`CoffeeScript`
--------------

Exactly the same as the `JavaScript` task, only the source is
treated as CoffeeScript instead of JavaScript.

`CoffeeScriptC`
---------------

Compiles CoffeeScript source to JavaScript.

The `CoffeeScript` task supports the following attributes:

* `destDir` - the output directory for the JavaScript files.
  If not specified, uses the "basedir" of the Ant project.

* `noWrap` - sets the "noWrap" compile option of the CoffeeScript
  compiler.  Setting to `true` will cause the function wrapper
  around the output to **not** be generated.  The default is `false`,
  which will cause the function wrapper to be generated.
  (yeah, I hate negative option names too, prolly will change this)

* `verbose` - prints a message to the console for every file processed.

* `nesting` - will inherit and maintin directory nested directory structure
 in the fileset base directories.

The `CoffeeScript` task supports the typical "fileSet" sort of
nested elements, as near as I can tell.  This is how you specify
input files.  To generate the name of the output file, the path
of the input file is completely stripped off, and a `".js"` suffix
is added to create the base name.  Those files are all written 
to the `destDir` directory.

Installing 
==========

You can run the jar file from the command line to print some
installation help and version information.

        java -jar csat.jar

for usage in Eclipse
--------------------

* Download a <tt>csat-{version}.jar</tt> file from 
  [http://github.com/pmuellr/CoffeeScriptAntTasks/downloads](http://github.com/pmuellr/CoffeeScriptAntTasks/downloads)

* Add it to a project in your Eclipse workspace

* For any Ant scripts you would like to use the tasks in, add
  the following lines:

        <path id="cp"><pathelement path="lib/csat.jar"/></path>
        <taskdef name="JavaScript"    classname="csat.JavaScriptTask"    classpathref="cp"/>
        <taskdef name="CoffeeScript"  classname="csat.CoffeeScriptTask"  classpathref="cp"/>
        <taskdef name="CoffeeScriptC" classname="csat.CoffeeScriptCTask" classpathref="cp"/>

for usage not in Eclipse
------------------------

Pretty much same as the Eclipse instructions, just don't use Eclipse.

Building
========

To rebuild this jar: 

* checkout the project at github as an Eclipse project

* run the <tt>build/get-libs.xml</tt> Ant script to load
  the required external code

* refresh the project

* update <tt>src/csat/versions.properties</tt> to give
  <tt>CSAT-VERSION</tt> a new version label

* run the <tt>build/build-jar.xml</tt> Ant script to build 
  the jar

Repository
==========

[http://github.com/pmuellr/CoffeeScriptAntTasks](http://github.com/pmuellr/CoffeeScriptAntTasks)

License
=======

MIT license: [http://www.opensource.org/licenses/mit-license.php](http://www.opensource.org/licenses/mit-license.php)

ChangeLog
=========

0.1.6 - 2012/11/26
-----------------
- Updated to CoffeeScript 1.4.0
- Changed Task for `CoffeeScriptC` to inherit Task instead of MatchingTask so we can
  add a boolean flag to inherit directory nesting.  The new flag is called `nesting`.
- Changed `noWrap` to the new `bare` param for coffee-script compiler.

0.1.5 - 2010/08/06
------------------
- `util.readFile()` was returning an empty string for non-existant files, now throws exception
- `util.readFile()` and `writeFile()` no longer take into account the task's basedir
- upgrade to CoffeeScript 0.9.0
- add `type` attribute to the `<arg>` element for `JavaScript` and `CoffeeScript` to 
  support passing Project basedir-resolved files into scripts.

0.1.4 - 2010/08/05
------------------
- add a JSON object, since this version of Rhino doesn't have one

0.1.3 - 2010/08/05
------------------
- make the util object available to scripts with some i/o functions
- fix the location of the versions.properties file in the ant scripts
- added Rhino's toolsrc to the rhino-src.zip file built

0.1.2 - 2010/08/03
------------------
- README fixes
- change the old `out` attribute of the `JavaScript` and `CoffeeScript`
  taks to `result`


0.1.1 - 2010/08/03
------------------

- initial buildable version
