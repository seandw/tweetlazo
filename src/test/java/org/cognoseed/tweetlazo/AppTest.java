package org.cognoseed.tweetlazo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Tests a couple common cases of {@link org.cognoseed.tweetlazo.App#stripDuplicateAdjacents(String)}
     */
    public void testStripping() {
        assertEquals("goal", App.stripDuplicateAdjacents("gooooooooooooaaaaaaaaaaaaaaaallllllllllllllll"));
        assertEquals("nothing you can do for this one", App.stripDuplicateAdjacents("nothing you can do for this one"));
        assertEquals("gol", App.stripDuplicateAdjacents("GOOOOOooooooOOOOOOOooooooooOOOOOOOOOOLLLLLLLLLLLLLL"));
    }
}
