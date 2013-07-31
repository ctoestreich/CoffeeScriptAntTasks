/*
 CoffeeLint

 Copyright (c) 2011 Matthew Perpick.
 CoffeeLint is freely distributable under the MIT license.
 */


(function() {
    var ASTLinter, CoffeeScript, ERROR, IGNORE, LexicalLinter, LineLinter, RULES, WARN, block_config, coffeelint, createError, defaults, extend, mergeDefaultConfig, regexes,
            __slice = [].slice,
            __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

    coffeelint = {};

    if (typeof exports !== "undefined" && exports !== null) {
        coffeelint = exports;
        CoffeeScript = require('coffee-script');
    } else {
        this.coffeelint = coffeelint;
        CoffeeScript = this.CoffeeScript;
    }

    coffeelint.VERSION = "0.5.6";

    ERROR = 'error';

    WARN = 'warn';

    IGNORE = 'ignore';

    coffeelint.RULES = RULES = {
        no_tabs: {
            level: ERROR,
            message: 'Line contains tab indentation',
            description: "This rule forbids tabs in indentation. Enough said. It is enabled by\ndefault."
        },
        no_trailing_whitespace: {
            level: ERROR,
            message: 'Line ends with trailing whitespace',
            allowed_in_comments: false,
            description: "This rule forbids trailing whitespace in your code, since it is\nneedless cruft. It is enabled by default."
        },
        max_line_length: {
            value: 80,
            level: ERROR,
            message: 'Line exceeds maximum allowed length',
            description: "This rule imposes a maximum line length on your code. <a\nhref=\"http://www.python.org/dev/peps/pep-0008/\">Python's style\nguide</a> does a good job explaining why you might want to limit the\nlength of your lines, though this is a matter of taste.\n\nLines can be no longer than eighty characters by default."
        },
        camel_case_classes: {
            level: ERROR,
            message: 'Class names should be camel cased',
            description: "This rule mandates that all class names are camel cased. Camel\ncasing class names is a generally accepted way of distinguishing\nconstructor functions - which require the 'new' prefix to behave\nproperly - from plain old functions.\n<pre>\n<code># Good!\nclass BoaConstrictor\n\n# Bad!\nclass boaConstrictor\n</code>\n</pre>\nThis rule is enabled by default."
        },
        indentation: {
            value: 2,
            level: ERROR,
            message: 'Line contains inconsistent indentation',
            description: "This rule imposes a standard number of spaces to be used for\nindentation. Since whitespace is significant in CoffeeScript, it's\ncritical that a project chooses a standard indentation format and\nstays consistent. Other roads lead to darkness. <pre> <code>#\nEnabling this option will prevent this ugly\n# but otherwise valid CoffeeScript.\ntwoSpaces = () ->\n  fourSpaces = () ->\n      eightSpaces = () ->\n            'this is valid CoffeeScript'\n\n</code>\n</pre>\nTwo space indentation is enabled by default."
        },
        no_implicit_braces: {
            level: IGNORE,
            message: 'Implicit braces are forbidden',
            description: "This rule prohibits implicit braces when declaring object literals.\nImplicit braces can make code more difficult to understand,\nespecially when used in combination with optional parenthesis.\n<pre>\n<code># Do you find this code ambiguous? Is it a\n# function call with three arguments or four?\nmyFunction a, b, 1:2, 3:4\n\n# While the same code written in a more\n# explicit manner has no ambiguity.\nmyFunction(a, b, {1:2, 3:4})\n</code>\n</pre>\nImplicit braces are permitted by default, since their use is\nidiomatic CoffeeScript."
        },
        no_trailing_semicolons: {
            level: ERROR,
            message: 'Line contains a trailing semicolon',
            description: "This rule prohibits trailing semicolons, since they are needless\ncruft in CoffeeScript.\n<pre>\n<code># This semicolon is meaningful.\nx = '1234'; console.log(x)\n\n# This semicolon is redundant.\nalert('end of line');\n</code>\n</pre>\nTrailing semicolons are forbidden by default."
        },
        no_plusplus: {
            level: IGNORE,
            message: 'The increment and decrement operators are forbidden',
            description: "This rule forbids the increment and decrement arithmetic operators.\nSome people believe the <tt>++</tt> and <tt>--</tt> to be cryptic\nand the cause of bugs due to misunderstandings of their precedence\nrules.\nThis rule is disabled by default."
        },
        no_throwing_strings: {
            level: ERROR,
            message: 'Throwing strings is forbidden',
            description: "This rule forbids throwing string literals or interpolations. While\nJavaScript (and CoffeeScript by extension) allow any expression to\nbe thrown, it is best to only throw <a\nhref=\"https://developer.mozilla.org\n/en/JavaScript/Reference/Global_Objects/Error\"> Error</a> objects,\nbecause they contain valuable debugging information like the stack\ntrace. Because of JavaScript's dynamic nature, CoffeeLint cannot\nensure you are always throwing instances of <tt>Error</tt>. It will\nonly catch the simple but real case of throwing literal strings.\n<pre>\n<code># CoffeeLint will catch this:\nthrow \"i made a boo boo\"\n\n# ... but not this:\nthrow getSomeString()\n</code>\n</pre>\nThis rule is enabled by default."
        },
        cyclomatic_complexity: {
            value: 10,
            level: IGNORE,
            message: 'The cyclomatic complexity is too damn high'
        },
        no_backticks: {
            level: ERROR,
            message: 'Backticks are forbidden',
            description: "Backticks allow snippets of JavaScript to be embedded in\nCoffeeScript. While some folks consider backticks useful in a few\nniche circumstances, they should be avoided because so none of\nJavaScript's \"bad parts\", like <tt>with</tt> and <tt>eval</tt>,\nsneak into CoffeeScript.\nThis rule is enabled by default."
        },
        line_endings: {
            level: IGNORE,
            value: 'unix',
            message: 'Line contains incorrect line endings',
            description: "This rule ensures your project uses only <tt>windows</tt> or\n<tt>unix</tt> line endings. This rule is disabled by default."
        },
        no_implicit_parens: {
            level: IGNORE,
            message: 'Implicit parens are forbidden',
            description: "This rule prohibits implicit parens on function calls.\n<pre>\n<code># Some folks don't like this style of coding.\nmyFunction a, b, c\n\n# And would rather it always be written like this:\nmyFunction(a, b, c)\n</code>\n</pre>\nImplicit parens are permitted by default, since their use is\nidiomatic CoffeeScript."
        },
        empty_constructor_needs_parens: {
            level: IGNORE,
            message: 'Invoking a constructor without parens and without arguments'
        },
        non_empty_constructor_needs_parens: {
            level: IGNORE,
            message: 'Invoking a constructor without parens and with arguments'
        },
        no_empty_param_list: {
            level: IGNORE,
            message: 'Empty parameter list is forbidden',
            description: "This rule prohibits empty parameter lists in function definitions.\n<pre>\n<code># The empty parameter list in here is unnecessary:\nmyFunction = () -&gt;\n\n# We might favor this instead:\nmyFunction = -&gt;\n</code>\n</pre>\nEmpty parameter lists are permitted by default."
        },
        space_operators: {
            level: IGNORE,
            message: 'Operators must be spaced properly'
        },
        duplicate_key: {
            level: ERROR,
            message: 'Duplicate key defined in object or class'
        },
        newlines_after_classes: {
            value: 3,
            level: IGNORE,
            message: 'Wrong count of newlines between a class and other code'
        },
        no_stand_alone_at: {
            level: IGNORE,
            message: '@ must not be used stand alone',
            description: "This rule checks that no stand alone @ are in use, they are\ndiscouraged. Further information in CoffeScript issue <a\nhref=\"https://github.com/jashkenas/coffee-script/issues/1601\">\n#1601</a>"
        },
        arrow_spacing: {
            level: IGNORE,
            message: 'Function arrow (->) must be spaced properly',
            description: "<p>This rule checks to see that there is spacing before and after\nthe arrow operator that declares a function. This rule is disabled\nby default.</p> <p>Note that if arrow_spacing is enabled, and you\npass an empty function as a parameter, arrow_spacing will accept\neither a space or no space in-between the arrow operator and the\nparenthesis</p>\n<pre><code># Both of this will not trigger an error,\n# even with arrow_spacing enabled.\nx(-> 3)\nx( -> 3)\n\n# However, this will trigger an error\nx((a,b)-> 3)\n</code>\n</pre>"
        },
        coffeescript_error: {
            level: ERROR,
            message: ''
        }
    };

    regexes = {
        trailingWhitespace: /[^\s]+[\t ]+\r?$/,
        lineHasComment: /^\s*[^\#]*\#/,
        indentation: /\S/,
        longUrlComment: /^\s*\#\s*http[^\s]+$/,
        camelCase: /^[A-Z][a-zA-Z\d]*$/,
        trailingSemicolon: /;\r?$/,
        configStatement: /coffeelint:\s*(disable|enable)(?:=([\w\s,]*))?/
    };

    extend = function() {
        var destination, k, source, sources, v, _i, _len;
        destination = arguments[0], sources = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
        for (_i = 0, _len = sources.length; _i < _len; _i++) {
            source = sources[_i];
            for (k in source) {
                v = source[k];
                destination[k] = v;
            }
        }
        return destination;
    };

    defaults = function(source, defaults) {
        return extend({}, defaults, source);
    };

    createError = function(rule, attrs) {
        var level;
        if (attrs == null) {
            attrs = {};
        }
        level = attrs.level;
        if (level !== IGNORE && level !== WARN && level !== ERROR) {
            throw new Error("unknown level " + level);
        }
        if (level === ERROR || level === WARN) {
            attrs.rule = rule;
            return defaults(attrs, RULES[rule]);
        } else {
            return null;
        }
    };

    block_config = {
        enable: {},
        disable: {}
    };

    LineLinter = (function() {

        function LineLinter(source, config, tokensByLine) {
            this.source = source;
            this.config = config;
            this.line = null;
            this.lineNumber = 0;
            this.tokensByLine = tokensByLine;
            this.lines = this.source.split('\n');
            this.lineCount = this.lines.length;
            this.context = {
                "class": {
                    inClass: false,
                    lastUnemptyLineInClass: null,
                    classIndents: null
                }
            };
        }

        LineLinter.prototype.lint = function() {
            var error, errors, line, lineNumber, _i, _len, _ref;
            errors = [];
            _ref = this.lines;
            for (lineNumber = _i = 0, _len = _ref.length; _i < _len; lineNumber = ++_i) {
                line = _ref[lineNumber];
                this.lineNumber = lineNumber;
                this.line = line;
                this.maintainClassContext();
                error = this.lintLine();
                if (error) {
                    errors.push(error);
                }
            }
            return errors;
        };

        LineLinter.prototype.lintLine = function() {
            return this.checkTabs() || this.checkTrailingWhitespace() || this.checkLineLength() || this.checkTrailingSemicolon() || this.checkLineEndings() || this.checkComments() || this.checkNewlinesAfterClasses();
        };

        LineLinter.prototype.checkTabs = function() {
            var indentation;
            indentation = this.line.split(regexes.indentation)[0];
            if (this.lineHasToken() && __indexOf.call(indentation, '\t') >= 0) {
                return this.createLineError('no_tabs');
            } else {
                return null;
            }
        };

        LineLinter.prototype.checkTrailingWhitespace = function() {
            var line, str, token, tokens, _i, _len, _ref, _ref1;
            if (regexes.trailingWhitespace.test(this.line)) {
                if (!((_ref = this.config['no_trailing_whitespace']) != null ? _ref.allowed_in_comments : void 0)) {
                    return this.createLineError('no_trailing_whitespace');
                }
                line = this.line;
                tokens = this.tokensByLine[this.lineNumber];
                if (!tokens) {
                    return null;
                }
                _ref1 = (function() {
                    var _j, _len, _results;
                    _results = [];
                    for (_j = 0, _len = tokens.length; _j < _len; _j++) {
                        token = tokens[_j];
                        if (token[0] === 'STRING') {
                            _results.push(token[1]);
                        }
                    }
                    return _results;
                })();
                for (_i = 0, _len = _ref1.length; _i < _len; _i++) {
                    str = _ref1[_i];
                    line = line.replace(str, 'STRING');
                }
                if (!regexes.lineHasComment.test(line)) {
                    return this.createLineError('no_trailing_whitespace');
                } else {
                    return null;
                }
            } else {
                return null;
            }
        };

        LineLinter.prototype.checkLineLength = function() {
            var max, rule, _ref;
            rule = 'max_line_length';
            max = (_ref = this.config[rule]) != null ? _ref.value : void 0;
            if (max && max < this.line.length) {
                if (!regexes.longUrlComment.test(this.line)) {
                    return this.createLineError(rule);
                }
            } else {
                return null;
            }
        };

        LineLinter.prototype.checkTrailingSemicolon = function() {
            var first, hasNewLine, hasSemicolon, last, _i, _ref;
            hasSemicolon = regexes.trailingSemicolon.test(this.line);
            _ref = this.getLineTokens(), first = 2 <= _ref.length ? __slice.call(_ref, 0, _i = _ref.length - 1) : (_i = 0, []), last = _ref[_i++];
            hasNewLine = last && (last.newLine != null);
            if (hasSemicolon && !hasNewLine && this.lineHasToken()) {
                return this.createLineError('no_trailing_semicolons');
            } else {
                return null;
            }
        };

        LineLinter.prototype.checkLineEndings = function() {
            var ending, lastChar, rule, valid, _ref;
            rule = 'line_endings';
            ending = (_ref = this.config[rule]) != null ? _ref.value : void 0;
            if (!ending || this.isLastLine() || !this.line) {
                return null;
            }
            lastChar = this.line[this.line.length - 1];
            valid = (function() {
                if (ending === 'windows') {
                    return lastChar === '\r';
                } else if (ending === 'unix') {
                    return lastChar !== '\r';
                } else {
                    throw new Error("unknown line ending type: " + ending);
                }
            })();
            if (!valid) {
                return this.createLineError(rule, {
                    context: "Expected " + ending
                });
            } else {
                return null;
            }
        };

        LineLinter.prototype.checkComments = function() {
            var cmd, r, result, rules, _i, _len, _ref;
            result = regexes.configStatement.exec(this.line);
            if (result != null) {
                cmd = result[1];
                rules = [];
                if (result[2] != null) {
                    _ref = result[2].split(',');
                    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                        r = _ref[_i];
                        rules.push(r.replace(/^\s+|\s+$/g, ""));
                    }
                }
                block_config[cmd][this.lineNumber] = rules;
            }
            return null;
        };

        LineLinter.prototype.checkNewlinesAfterClasses = function() {
            var ending, got, rule;
            rule = 'newlines_after_classes';
            ending = this.config[rule].value;
            if (!ending || this.isLastLine()) {
                return null;
            }
            if (!this.context["class"].inClass && (this.context["class"].lastUnemptyLineInClass != null) && ((this.lineNumber - 1) - this.context["class"].lastUnemptyLineInClass) !== ending) {
                got = (this.lineNumber - 1) - this.context["class"].lastUnemptyLineInClass;
                return this.createLineError(rule, {
                    context: "Expected " + ending + " got " + got
                });
            }
            return null;
        };

        LineLinter.prototype.createLineError = function(rule, attrs) {
            var _ref;
            if (attrs == null) {
                attrs = {};
            }
            attrs.lineNumber = this.lineNumber + 1;
            attrs.level = (_ref = this.config[rule]) != null ? _ref.level : void 0;
            return createError(rule, attrs);
        };

        LineLinter.prototype.isLastLine = function() {
            return this.lineNumber === this.lineCount - 1;
        };

        LineLinter.prototype.lineHasToken = function(tokenType, lineNumber) {
            var token, tokens, _i, _len;
            if (tokenType == null) {
                tokenType = null;
            }
            if (lineNumber == null) {
                lineNumber = null;
            }
            lineNumber = lineNumber != null ? lineNumber : this.lineNumber;
            if (tokenType == null) {
                return this.tokensByLine[lineNumber] != null;
            } else {
                tokens = this.tokensByLine[lineNumber];
                if (tokens == null) {
                    return null;
                }
                for (_i = 0, _len = tokens.length; _i < _len; _i++) {
                    token = tokens[_i];
                    if (token[0] === tokenType) {
                        return true;
                    }
                }
                return false;
            }
        };

        LineLinter.prototype.getLineTokens = function() {
            return this.tokensByLine[this.lineNumber] || [];
        };

        LineLinter.prototype.maintainClassContext = function() {
            if (this.context["class"].inClass) {
                if (this.lineHasToken('INDENT')) {
                    this.context["class"].classIndents++;
                } else if (this.lineHasToken('OUTDENT')) {
                    this.context["class"].classIndents--;
                    if (this.context["class"].classIndents === 0) {
                        this.context["class"].inClass = false;
                        this.context["class"].classIndents = null;
                    }
                }
                if (this.context["class"].inClass && !this.line.match(/^\s*$/)) {
                    this.context["class"].lastUnemptyLineInClass = this.lineNumber;
                }
            } else {
                if (!this.line.match(/\\s*/)) {
                    this.context["class"].lastUnemptyLineInClass = null;
                }
                if (this.lineHasToken('CLASS')) {
                    this.context["class"].inClass = true;
                    this.context["class"].lastUnemptyLineInClass = this.lineNumber;
                    this.context["class"].classIndents = 0;
                }
            }
            return null;
        };

        return LineLinter;

    })();

    LexicalLinter = (function() {

        function LexicalLinter(source, config) {
            this.source = source;
            this.tokens = CoffeeScript.tokens(source);
            this.config = config;
            this.i = 0;
            this.tokensByLine = {};
            this.arrayTokens = [];
            this.parenTokens = [];
            this.callTokens = [];
            this.lines = source.split('\n');
            this.braceScopes = [];
        }

        LexicalLinter.prototype.lint = function() {
            var error, errors, i, token, _i, _len, _ref;
            errors = [];
            _ref = this.tokens;
            for (i = _i = 0, _len = _ref.length; _i < _len; i = ++_i) {
                token = _ref[i];
                this.i = i;
                error = this.lintToken(token);
                if (error) {
                    errors.push(error);
                }
            }
            return errors;
        };

        LexicalLinter.prototype.lintToken = function(token) {
            var lineNumber, type, value, _base, _ref;
            type = token[0], value = token[1], lineNumber = token[2];
            if (typeof lineNumber === "object") {
                if (type === 'OUTDENT' || type === 'INDENT') {
                    lineNumber = lineNumber.last_line;
                } else {
                    lineNumber = lineNumber.first_line;
                }
            }
            if ((_ref = (_base = this.tokensByLine)[lineNumber]) == null) {
                _base[lineNumber] = [];
            }
            this.tokensByLine[lineNumber].push(token);
            this.lineNumber = lineNumber || this.lineNumber || 0;
            switch (type) {
                case "->":
                    return this.lintArrowSpacing(token);
                case "INDENT":
                    return this.lintIndentation(token);
                case "CLASS":
                    return this.lintClass(token);
                case "UNARY":
                    return this.lintUnary(token);
                case "{":
                case "}":
                    return this.lintBrace(token);
                case "IDENTIFIER":
                    return this.lintIdentifier(token);
                case "++":
                case "--":
                    return this.lintIncrement(token);
                case "THROW":
                    return this.lintThrow(token);
                case "[":
                case "]":
                    return this.lintArray(token);
                case "(":
                case ")":
                    return this.lintParens(token);
                case "JS":
                    return this.lintJavascript(token);
                case "CALL_START":
                case "CALL_END":
                    return this.lintCall(token);
                case "PARAM_START":
                    return this.lintParam(token);
                case "@":
                    return this.lintStandaloneAt(token);
                case "+":
                case "-":
                    return this.lintPlus(token);
                case "=":
                case "MATH":
                case "COMPARE":
                case "LOGIC":
                case "COMPOUND_ASSIGN":
                    return this.lintMath(token);
                default:
                    return null;
            }
        };

        LexicalLinter.prototype.lintUnary = function(token) {
            var expectedCallStart, expectedIdentifier, identifierIndex;
            if (token[1] === 'new') {
                identifierIndex = 1;
                while (true) {
                    expectedIdentifier = this.peek(identifierIndex);
                    expectedCallStart = this.peek(identifierIndex + 1);
                    if ((expectedIdentifier != null ? expectedIdentifier[0] : void 0) === 'IDENTIFIER') {
                        if ((expectedCallStart != null ? expectedCallStart[0] : void 0) === '.') {
                            identifierIndex += 2;
                            continue;
                        }
                    }
                    break;
                }
                if ((expectedIdentifier != null ? expectedIdentifier[0] : void 0) === 'IDENTIFIER' && (expectedCallStart != null)) {
                    if (expectedCallStart[0] === 'CALL_START') {
                        if (expectedCallStart.generated) {
                            return this.createLexError('non_empty_constructor_needs_parens');
                        }
                    } else {
                        return this.createLexError('empty_constructor_needs_parens');
                    }
                }
            }
        };

        LexicalLinter.prototype.lintArray = function(token) {
            if (token[0] === '[') {
                this.arrayTokens.push(token);
            } else if (token[0] === ']') {
                this.arrayTokens.pop();
            }
            return null;
        };

        LexicalLinter.prototype.lintParens = function(token) {
            var i, n1, n2, p1;
            if (token[0] === '(') {
                p1 = this.peek(-1);
                n1 = this.peek(1);
                n2 = this.peek(2);
                i = n1 && n2 && n1[0] === 'STRING' && n2[0] === '+';
                token.isInterpolation = i;
                this.parenTokens.push(token);
            } else {
                this.parenTokens.pop();
            }
            return null;
        };

        LexicalLinter.prototype.isInInterpolation = function() {
            var t, _i, _len, _ref;
            _ref = this.parenTokens;
            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                t = _ref[_i];
                if (t.isInterpolation) {
                    return true;
                }
            }
            return false;
        };

        LexicalLinter.prototype.isInExtendedRegex = function() {
            var t, _i, _len, _ref;
            _ref = this.callTokens;
            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                t = _ref[_i];
                if (t.isRegex) {
                    return true;
                }
            }
            return false;
        };

        LexicalLinter.prototype.lintPlus = function(token) {
            var isUnary, p, unaries, _ref;
            if (this.isInInterpolation() || this.isInExtendedRegex()) {
                return null;
            }
            p = this.peek(-1);
            unaries = ['TERMINATOR', '(', '=', '-', '+', ',', 'CALL_START', 'INDEX_START', '..', '...', 'COMPARE', 'IF', 'THROW', 'LOGIC', 'POST_IF', ':', '[', 'INDENT', 'COMPOUND_ASSIGN'];
            isUnary = !p ? false : (_ref = p[0], __indexOf.call(unaries, _ref) >= 0);
            if ((isUnary && token.spaced) || (!isUnary && !token.spaced && !token.newLine)) {
                return this.createLexError('space_operators', {
                    context: token[1]
                });
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.lintMath = function(token) {
            if (!token.spaced && !token.newLine) {
                return this.createLexError('space_operators', {
                    context: token[1]
                });
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.lintCall = function(token) {
            var p;
            if (token[0] === 'CALL_START') {
                p = this.peek(-1);
                token.isRegex = p && p[0] === 'IDENTIFIER' && p[1] === 'RegExp';
                this.callTokens.push(token);
                if (token.generated) {
                    return this.createLexError('no_implicit_parens');
                } else {
                    return null;
                }
            } else {
                this.callTokens.pop();
                return null;
            }
        };

        LexicalLinter.prototype.lintParam = function(token) {
            var nextType;
            nextType = this.peek()[0];
            if (nextType === 'PARAM_END') {
                return this.createLexError('no_empty_param_list');
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.lintIdentifier = function(token) {
            var key, nextToken, previousToken;
            key = token[1];
            if (!(this.currentScope != null)) {
                return null;
            }
            nextToken = this.peek(1);
            if (nextToken[1] !== ':') {
                return null;
            }
            previousToken = this.peek(-1);
            if (previousToken[0] === '@') {
                key = "@" + key;
            }
            key = "identifier-" + key;
            if (this.currentScope[key]) {
                return this.createLexError('duplicate_key');
            } else {
                this.currentScope[key] = token;
                return null;
            }
        };

        LexicalLinter.prototype.lintBrace = function(token) {
            var i, t;
            if (token[0] === '{') {
                if (this.currentScope != null) {
                    this.braceScopes.push(this.currentScope);
                }
                this.currentScope = {};
            } else {
                this.currentScope = this.braceScopes.pop();
            }
            if (token.generated && token[0] === '{') {
                i = -1;
                while (true) {
                    t = this.peek(i);
                    if (!(t != null) || t[0] === 'TERMINATOR') {
                        return this.createLexError('no_implicit_braces');
                    }
                    if (t[0] === 'CLASS') {
                        return null;
                    }
                    i -= 1;
                }
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.lintJavascript = function(token) {
            return this.createLexError('no_backticks');
        };

        LexicalLinter.prototype.lintThrow = function(token) {
            var n1, n2, nextIsString, _ref;
            _ref = [this.peek(), this.peek(2)], n1 = _ref[0], n2 = _ref[1];
            nextIsString = n1[0] === 'STRING' || (n1[0] === '(' && n2[0] === 'STRING');
            if (nextIsString) {
                return this.createLexError('no_throwing_strings');
            }
        };

        LexicalLinter.prototype.lintIncrement = function(token) {
            var attrs;
            attrs = {
                context: "found '" + token[0] + "'"
            };
            return this.createLexError('no_plusplus', attrs);
        };

        LexicalLinter.prototype.lintStandaloneAt = function(token) {
            var isDot, isIdentifier, isIndexStart, isValidProtoProperty, nextToken, protoProperty, spaced;
            nextToken = this.peek();
            spaced = token.spaced;
            isIdentifier = nextToken[0] === 'IDENTIFIER';
            isIndexStart = nextToken[0] === 'INDEX_START';
            isDot = nextToken[0] === '.';
            if (nextToken[0] === '::') {
                protoProperty = this.peek(2);
                isValidProtoProperty = protoProperty[0] === 'IDENTIFIER';
            }
            if (spaced || (!isIdentifier && !isIndexStart && !isDot && !isValidProtoProperty)) {
                return this.createLexError('no_stand_alone_at');
            }
        };

        LexicalLinter.prototype.lintIndentation = function(token) {
            var context, currentLine, expected, ignoreIndent, isArrayIndent, isInterpIndent, isMultiline, lineNumber, numIndents, previous, previousIndentation, previousLine, previousSymbol, type, _ref;
            type = token[0], numIndents = token[1], lineNumber = token[2];
            if (token.generated != null) {
                return null;
            }
            previous = this.peek(-2);
            isInterpIndent = previous && previous[0] === '+';
            previous = this.peek(-1);
            isArrayIndent = this.inArray() && (previous != null ? previous.newLine : void 0);
            previousSymbol = (_ref = this.peek(-1)) != null ? _ref[0] : void 0;
            isMultiline = previousSymbol === '=' || previousSymbol === ',';
            ignoreIndent = isInterpIndent || isArrayIndent || isMultiline;
            if (this.isChainedCall()) {
                currentLine = this.lines[this.lineNumber];
                previousLine = this.lines[this.lineNumber - 1];
                previousIndentation = previousLine.match(/^(\s*)/)[1].length;
                numIndents = currentLine.match(/^(\s*)/)[1].length;
                numIndents -= previousIndentation;
            }
            expected = this.config['indentation'].value;
            if (!ignoreIndent && numIndents !== expected) {
                context = ("Expected " + expected + " ") + ("got " + numIndents);
                return this.createLexError('indentation', {
                    context: context
                });
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.lintClass = function(token) {
            var attrs, className, offset, _ref, _ref1, _ref2;
            if ((token.newLine != null) || ((_ref = this.peek()[0]) === 'INDENT' || _ref === 'EXTENDS')) {
                return null;
            }
            className = null;
            offset = 1;
            while (!className) {
                if (((_ref1 = this.peek(offset + 1)) != null ? _ref1[0] : void 0) === '.') {
                    offset += 2;
                } else if (((_ref2 = this.peek(offset)) != null ? _ref2[0] : void 0) === '@') {
                    offset += 1;
                } else {
                    className = this.peek(offset)[1];
                }
            }
            if (!regexes.camelCase.test(className)) {
                attrs = {
                    context: "class name: " + className
                };
                return this.createLexError('camel_case_classes', attrs);
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.lintArrowSpacing = function(token) {
            if (!(((token.spaced != null) || (token.newLine != null)) && ((this.peek(-1).spaced != null) || (this.peek(-1).generated != null) || this.peek(-1)[0] === "INDENT" || (this.peek(-1)[0] === "CALL_START" && !(this.peek(-1).generated != null))))) {
                return this.createLexError('arrow_spacing');
            } else {
                return null;
            }
        };

        LexicalLinter.prototype.createLexError = function(rule, attrs) {
            if (attrs == null) {
                attrs = {};
            }
            attrs.lineNumber = this.lineNumber + 1;
            attrs.level = this.config[rule].level;
            attrs.line = this.lines[this.lineNumber];
            return createError(rule, attrs);
        };

        LexicalLinter.prototype.peek = function(n) {
            if (n == null) {
                n = 1;
            }
            return this.tokens[this.i + n] || null;
        };

        LexicalLinter.prototype.inArray = function() {
            return this.arrayTokens.length > 0;
        };

        LexicalLinter.prototype.isChainedCall = function() {
            var i, lastNewLineIndex, lines, t, token, tokens;
            lines = (function() {
                var _i, _len, _ref, _results;
                _ref = this.tokens.slice(0, +this.i + 1 || 9e9);
                _results = [];
                for (i = _i = 0, _len = _ref.length; _i < _len; i = ++_i) {
                    token = _ref[i];
                    if (token.newLine != null) {
                        _results.push(i);
                    }
                }
                return _results;
            }).call(this);
            lastNewLineIndex = lines ? lines[lines.length - 2] : null;
            if (!(lastNewLineIndex != null)) {
                return false;
            }
            tokens = [this.tokens[lastNewLineIndex], this.tokens[lastNewLineIndex + 1]];
            return !!((function() {
                var _i, _len, _results;
                _results = [];
                for (_i = 0, _len = tokens.length; _i < _len; _i++) {
                    t = tokens[_i];
                    if (t && t[0] === '.') {
                        _results.push(t);
                    }
                }
                return _results;
            })()).length;
        };

        return LexicalLinter;

    })();

    ASTLinter = (function() {

        function ASTLinter(source, config) {
            this.source = source;
            this.config = config;
            this.errors = [];
        }

        ASTLinter.prototype.lint = function() {
            try {
                this.node = CoffeeScript.nodes(this.source);
            } catch (coffeeError) {
                this.errors.push(this._parseCoffeeScriptError(coffeeError));
                return this.errors;
            }
            this.lintNode(this.node);
            return this.errors;
        };

        ASTLinter.prototype.lintNode = function(node) {
            var attrs, complexity, error, name, rule, _ref,
                    _this = this;
            name = node.constructor.name;
            complexity = name === 'If' || name === 'While' || name === 'For' || name === 'Try' ? 1 : name === 'Op' && ((_ref = node.operator) === '&&' || _ref === '||') ? 1 : name === 'Switch' ? node.cases.length : 0;
            node.eachChild(function(childNode) {
                if (!childNode) {
                    return false;
                }
                complexity += _this.lintNode(childNode);
                return true;
            });
            rule = this.config.cyclomatic_complexity;
            if (name === 'Code' && complexity >= rule.value) {
                attrs = {
                    context: complexity + 1,
                    level: rule.level,
                    line: 0
                };
                error = createError('cyclomatic_complexity', attrs);
                if (error) {
                    this.errors.push(error);
                }
            }
            return complexity;
        };

        ASTLinter.prototype._parseCoffeeScriptError = function(coffeeError) {
            var attrs, lineNumber, match, message, rule;
            rule = RULES['coffeescript_error'];
            message = coffeeError.toString();
            lineNumber = -1;
            if (coffeeError.location != null) {
                lineNumber = coffeeError.location.first_line + 1;
            } else {
                match = /line (\d+)/.exec(message);
                if ((match != null ? match.length : void 0) > 1) {
                    lineNumber = parseInt(match[1], 10);
                }
            }
            attrs = {
                message: message,
                level: rule.level,
                lineNumber: lineNumber
            };
            return createError('coffeescript_error', attrs);
        };

        return ASTLinter;

    })();

    mergeDefaultConfig = function(userConfig) {
        var config, rule, ruleConfig;
        config = {};
        for (rule in RULES) {
            ruleConfig = RULES[rule];
            config[rule] = defaults(userConfig[rule], ruleConfig);
        }
        return config;
    };

    coffeelint.lint = function(source, userConfig) {
        var all_errors, astErrors, cmd, config, difference, disabled, disabled_initially, e, errors, i, l, lexErrors, lexicalLinter, lineErrors, lineLinter, next_line, r, rules, s, tokensByLine, _i, _j, _k, _len, _len1, _ref, _ref1, _ref2, _ref3, _ref4;
        if (userConfig == null) {
            userConfig = {};
        }
        config = mergeDefaultConfig(userConfig);
        disabled_initially = [];
        _ref = source.split('\n');
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            l = _ref[_i];
            s = regexes.configStatement.exec(l);
            if ((s != null) && s.length > 2 && __indexOf.call(s, 'enable') >= 0) {
                _ref1 = s.slice(1);
                for (_j = 0, _len1 = _ref1.length; _j < _len1; _j++) {
                    r = _ref1[_j];
                    if (r !== 'enable' && r !== 'disable') {
                        if (!(r in config && ((_ref2 = config[r].level) === 'warn' || _ref2 === 'error'))) {
                            disabled_initially.push(r);
                            config[r] = {
                                level: 'error'
                            };
                        }
                    }
                }
            }
        }
        astErrors = new ASTLinter(source, config).lint();
        lexicalLinter = new LexicalLinter(source, config);
        lexErrors = lexicalLinter.lint();
        tokensByLine = lexicalLinter.tokensByLine;
        lineLinter = new LineLinter(source, config, tokensByLine);
        lineErrors = lineLinter.lint();
        errors = lexErrors.concat(lineErrors, astErrors);
        errors.sort(function(a, b) {
            return a.lineNumber - b.lineNumber;
        });
        difference = function(a, b) {
            var j, _ref3, _results;
            j = 0;
            _results = [];
            while (j < a.length) {
                if (_ref3 = a[j], __indexOf.call(b, _ref3) >= 0) {
                    _results.push(a.splice(j, 1));
                } else {
                    _results.push(j++);
                }
            }
            return _results;
        };
        all_errors = errors;
        errors = [];
        disabled = disabled_initially;
        next_line = 0;
        for (i = _k = 0, _ref3 = source.split('\n').length; 0 <= _ref3 ? _k < _ref3 : _k > _ref3; i = 0 <= _ref3 ? ++_k : --_k) {
            for (cmd in block_config) {
                rules = block_config[cmd][i];
                if (rules != null) {
                    ({
                        'disable': function() {
                            return disabled = disabled.concat(rules);
                        },
                        'enable': function() {
                            difference(disabled, rules);
                            if (rules.length === 0) {
                                return disabled = disabled_initially;
                            }
                        }
                    })[cmd]();
                }
            }
            while (next_line === i && all_errors.length > 0) {
                next_line = all_errors[0].lineNumber - 1;
                e = all_errors[0];
                if (e.lineNumber === i + 1 || !(e.lineNumber != null)) {
                    e = all_errors.shift();
                    if (_ref4 = e.rule, __indexOf.call(disabled, _ref4) < 0) {
                        errors.push(e);
                    }
                }
            }
        }
        block_config = {
            'enable': {},
            'disable': {}
        };
        return errors;
    };

}).call(this);
