var result = []

stdout.println("in " + __FILE__)
stdout.println("argv.length: " + argv.length)

for (var i=0; i<argv.length; i++) {
	stdout.println("argv[" + i + "]: " + argv[i])
	result.push(argv[i]) 
}

result = result.join(", ")
stdout.println("the result from running in the script is '" + result + "'")

result