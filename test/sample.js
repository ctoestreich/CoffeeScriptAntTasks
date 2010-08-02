var result = []

stderr.println("argv.length: " + argv.length)

for (var i=0; i<argv.length; i++) {
	stderr.println("argv[" + i + "]: " + argv[i])
	result.push(argv[i]) 
}

result = result.join(", ")
stdout.println("the result from running in the script is " + result)

result