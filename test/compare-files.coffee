#---------------------------------------------------------------------
importClass java.lang.Character
importClass java.lang.StringBuilder
importClass java.io.FileReader
importClass java.io.File
importClass org.apache.tools.ant.BuildException

#---------------------------------------------------------------------
charArray = (size) -> java.lang.reflect.Array.newInstance(Character.TYPE, size)

#---------------------------------------------------------------------
readFile = (file) ->
    fr     = new FileReader file
    buffer = charArray 256*256
    result = new StringBuilder()

    while (read = fr.read buffer) > 0
        result.append(buffer,0, read)

    fr.close()

    return result.toString()
    
#---------------------------------------------------------------------
file1 = new File(task.project.baseDir, argv[0])
file2 = new File(task.project.baseDir, argv[1])

contents1 = readFile file1
contents2 = readFile file2

if not contents1.equals contents2
    throw new BuildException "files do not match: '#file1', '#file2'"