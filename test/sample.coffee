result: []

stderr.println "argv.length: ${argv.length}"

result: for i in [0...argv.length]
    stderr.println "argv${i}: ${argv[i]}"
    argv[i]

result: result.join ", "
stdout.println "the result from running in the script is ${result}"

result