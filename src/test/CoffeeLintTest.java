package test;

import org.apache.tools.ant.BuildFileTest;

public class CoffeeLintTest extends BuildFileTest {

    public CoffeeLintTest(String s) {
        super(s);
    }

    public void setUp() {
        // initialize Ant
        configureProject("test/test.xml");
    }

    public void testCoffeeLintFailedLint() {
        executeTarget("test-CoffeeScriptLint-bad");
        assertOutputContaining("should have error message", "message=");
        assertOutputContaining("should contain backtick text violation", "Backtick");
    }

    public void testCoffeeLintPassedLint() {
        executeTarget("test-CoffeeScriptLint-good");
        assertOutputNotContaining("should not have any error messages", "message=");
    }

    public void testCoffeeLintThrowsException() {
        try {
            executeTarget("test-CoffeeScriptLint-exception");
        } catch(RuntimeException e) {
            assertTrue(e.getCause().toString().contains("Backtick"));
        }
    }
}