#---------------------------------------------------------------------
importClass java.util.Scanner
importClass java.io.File
importClass java.lang.System
importClass org.apache.tools.ant.BuildException

#---------------------------------------------------------------------
readFile = (file) ->
  String text = new Scanner(new File(file)).useDelimiter("\\A").next();
  return text.replaceAll("\\r\\n?", "\n")
    
#---------------------------------------------------------------------
file1 = new File(task.project.baseDir, argv[0])
file2 = new File(task.project.baseDir, argv[1])

contents1 = readFile file1
contents2 = readFile file2

if not contents1.equals contents2
    throw new BuildException "files do not match: " + file1 + ", " + file2