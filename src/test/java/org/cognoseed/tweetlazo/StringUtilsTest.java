package org.cognoseed.tweetlazo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for {@link StringUtils} utilities.
 */
public class StringUtilsTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public StringUtilsTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(StringUtilsTest.class);
    }

    /**
     * Tests a couple common cases for {@link StringUtils#stripDuplicateAdjacents(String)}
     */
    public void testStrippingDuplicates() {
        assertEquals("goal", StringUtils.stripDuplicateAdjacents("gooooooooooooaaaaaaaaaaaaaaaallllllllllllllll"));
        assertEquals("nothing you can do for this one", StringUtils.stripDuplicateAdjacents("nothing you can do for this one"));
        assertEquals("gol", StringUtils.stripDuplicateAdjacents("GOOOOOooooooOOOOOOOooooooooOOOOOOOOOOLLLLLLLLLLLLLL"));
    }

    /**
     * Tests common cases for {@link StringUtils#stripPunctuation(String)}
     */
    public void testStrippingUnicodePunctuation() {
        assertEquals("GOLAZO", StringUtils.stripPunctuation("¡GO-LA-ZO!"));
        assertEquals("Mal", StringUtils.stripPunctuation("Mål!"));
    }
}
