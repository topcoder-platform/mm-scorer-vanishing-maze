package com.topcoder.ReviewTest;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.topcoder.ReviewHelper.ReviewHelper;

/**
 * Hello world!
 *
 */
public class App {

    public static final String OUTPUT_DIR = "G:\\testOutput\\";

	public static void main(String[] args) {
		try {
			ReviewHelper reviewHelper = new ReviewHelper();

			// GET TOKEN
			// String token = reviewHelper.getToken();

			// GET REVIEWS
			// JSONArray reviews = reviewHelper.getReviews("30055011", token);
			// System.out.println(reviews);

			// JSONParser parser = new JSONParser();

			/**
			 * USE postReview method String res = postReview(String challengeId, String
			 * testPhase, String submissionId, String reviewerId, String typeId, String
			 * scoreCardId, List<Map<String,Object>> testScores)
			 */		

             String challengeId = "30091712";
             String testPhase = "provisional";
             String submissionId = "4a7564f3-ed3d-420d-b084-fe6ab67337c8";
             String reviewerId = "3ba1e475-b870-43ee-a64d-ae9365eda888";
             String typeId = "48c4296e-5d4a-4797-80b7-1c22f36ba698";
             String scoreCardId = "30001852";
             String exec = "java VanishingMaze";
             
             if (args.length >= 7) {
               String challengeId = args[0];
               String testPhase = args[1];
               String submissionId = args[2];
               String reviewerId =args[3];
               String typeId = args[4];
               String scoreCardId = args[5];
               String exec = args[6];
             }
            
             // 10 Provisional tests, 100 system tests
             long startSeed = 1;
             long endSeed = 10;
             if (testPhase.equals("final")) {
               startSeed = 101;
               endSeed = 150;
             }
             
             StringBuilder allOutput = new StringBuilder();
             List<Map<String, Object>> testScores = new ArrayList<Map<String, Object>>();
             // For now, try doing 10 tests and see how that works
             for (long i = startSeed; i <= endSeed; i++) {
               double score = VanishingMazeVis.main(new String[]{ "-exec", exec, "-seed", i + "", "-novis" });
               HashMap res = new HashMap<String, Object>();
               res.put("score", score);
               res.put("testcase", i);
               testScores.add(res);
               allOutput.append("Test Case #" + i + ":\n");
               allOutput.append("Score = " + score + "\n\n");
               allOutput.append(VanishingMazeVis.output.getOutput());
               allOutput.append("\n\n");
             }
             try {
               FileOutputStream os = new FileOutputStream(new File(OUTPUT_DIR + "output.txt"));
               os.write(allOutput.toString().getBytes());
             } catch (IOException ie) {
               ie.printStackTrace();
             }             
             ReviewHelper.postReview(challengeId, testPhase, submissionId, reviewerId, typeId, scoreCardId, testScores);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
