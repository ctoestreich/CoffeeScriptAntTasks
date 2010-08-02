csat - CoffeeScript Ant Tasks
=============================

This project contains Ant tasks to run CoffeeScript from 
Ant, and compile CoffeeScript files to JavaScript.  It uses
Rhino as the underlying JavaScript engine.

Installing
----------

I'm working on it.


Examples
----------

The example below writes a line to stdout and a line to stderr.

        <CoffeeScript>
            stdout.println "stdout: hello, world!"
            stderr.println "stderr: hello, world!"
        </CoffeeScript>

The example below will set the property `output` to the
value `"the result"`

        <CoffeeScript out="output">
            x: "the result"
            x
        </CoffeeScript>
        

When run after the previous command, this example will
print the value of previous output.

        <CoffeeScript>
            <arg value="${output}"/>
            stdout.println "the previous result was '" + argv[0] + "'"
        </CoffeeScript>


Here's an example that runs CoffeeScript stored in a file.

        <CoffeeScript src="test/sample.coffee" out="output">
            <arg value="arg #1"/>
            <arg value="the second arg"/>
            <arg value="finally, the third arg"/>
        </CoffeeScript>

This example compiles CoffeeScript into JavaScript.

        <CoffeeScriptC nowrap="true" destDir="test/out" verbose="true">
            <include name="test/src/**/*"/>
        </CoffeeScriptC>


Running
----------

Three Ant tasks are included:

### `JavaScript` ###

Runs JavaScript code.

The `JavaScript` task supports the following attributes:

* `src` - the name of a file that contains the JavaScript code
to run

* `out` - the name of a property to place the output of running
the JavaScript code in.  The result of the JavaScript code is
converted to a string and then placed in the property.

Instead of running a file of JavaScript code, you can place the
JavaScript code in the task as text instead.  JavaScript code
embedded directly in the task will **NOT** have typical Ant property
substitution done on it.

The `JavaScript` task supports the following nested elements:

* `<arg>` - contains an argument to pass to the JavaScript
code.  May be used multiple times.  Must contain an attribute
named `value` which is the value to pass to the JavaScript code.
(Note this is similar to the `Exec` task)

When the JavaScript code runs, the following variables will be
set:

* `argv` - an array of the values set by the `<arg>` elements
* `stdout` - set to `System.out`
* `stderr` - set to `System.err`
* `__FILE__` - set to the name of the script
* `task` - set to the Ant task that is being run

### `CoffeeScript` ###

Exactly the same as the `JavaScript` task, only the source is
treated as CoffeeScript instead of JavaScript.

### `CoffeeScriptC` ###

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

The `CoffeeScript` task supports the typical "fileSet" sort of
nested elements, as near as I can tell.  This is how you specify
input files.  To generate the name of the output file, the path
of the input file is completely stripped off, and a `".js"` suffix
is added to create the base name.  Those files are all written 
to the `destDir` directory.


Building
----------

I'm working on it.

