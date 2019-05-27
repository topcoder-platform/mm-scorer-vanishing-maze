package com.topcoder.ReviewHelper;

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

import com.topcoder.ReviewHelper.*;

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
		String submissionId = "4a7564f3-ed3d-420d-b084-fe6ab67337c8";
		String reviewerId = "3ba1e475-b870-43ee-a64d-ae9365eda888";
		String typeId = "48c4296e-5d4a-4797-80b7-1c22f36ba698";
		String scoreCardId = "30001852";
		String challengeId = "30091712";
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

		try {
			String res = ReviewHelper.postReview(
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
