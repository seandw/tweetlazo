package org.cognoseed.tweetlazo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for GoalCounter utilities.
 */
public class GoalCounterTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GoalCounterTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(GoalCounterTest.class);
    }

    /**
     * Tests a couple common cases for {@link org.cognoseed.tweetlazo.GoalCounter#stripDuplicateAdjacents(String)}
     */
    public void testStrippingDuplicates() {
        assertEquals("goal", GoalCounter.stripDuplicateAdjacents("gooooooooooooaaaaaaaaaaaaaaaallllllllllllllll"));
        assertEquals("nothing you can do for this one", GoalCounter.stripDuplicateAdjacents("nothing you can do for this one"));
        assertEquals("gol", GoalCounter.stripDuplicateAdjacents("GOOOOOooooooOOOOOOOooooooooOOOOOOOOOOLLLLLLLLLLLLLL"));
    }

    /**
     * Tests common cases for {@link org.cognoseed.tweetlazo.GoalCounter#stripPunctuation(String)}
     */
    public void testStrippingUnicodePunctuation() {
        assertEquals("GOLAZO", GoalCounter.stripPunctuation("¡GO-LA-ZO!"));
        assertEquals("Mal", GoalCounter.stripPunctuation("Mål!"));
    }
}
