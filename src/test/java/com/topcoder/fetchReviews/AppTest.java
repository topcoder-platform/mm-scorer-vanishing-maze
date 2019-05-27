package com.topcoder.fetchReviews;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.topcoder.ReviewHelper.ReviewHelper;

/**
 * Unit test for simple App.
 */
public class AppTest 
	extends TestCase
{
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( AppTest.class );
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp()
	{
		String submissionId = "523f2f22-695c-4e45-832c-597cf712bbac";
		String reviewerId = "da5876e2-58c9-40e5-862e-f8016041c160";
		String typeId = "68c5a381-c8ab-48af-92a7-7a869a4ee6c3";
		String scoreCardId = "30001850";
		String challengeId = "30055011";
		String testPhase = "provisional";
		List<Map<String,Object>> testScores = new ArrayList();
		HashMap<String,Object> testScore = new HashMap<String,Object>();
		testScore.put("score",20);
		testScore.put("testcase",1);
		testScores.add(testScore);
		testScore = new HashMap<String,Object>();
		testScore.put("score",20);
		testScore.put("testcase",2);
		testScores.add(testScore);

		ReviewHelper reviewHelper = new ReviewHelper();

		try {
			String res = reviewHelper.postReview(
				challengeId,
				testPhase,
				submissionId,
				reviewerId,
				typeId,
				scoreCardId,
				testScores);

			System.out.println("res:");
			System.out.println(res);
		} catch (Exception e) {
			System.out.println(e);
		}
		assertTrue( true );
	}

}
