package com.topcoder.ReviewHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.appirio.tech.core.api.v3.util.jwt.JWTTokenGenerator;

public class ReviewHelper {

	String clientId = "";
	String clientSecret = "";
	String audience = "";
	String m2mAuthDomain = "";
	String tcDomain = "";

	private static Properties loadPropertyFile() throws Exception {
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("token.properties");
		props.load(in);
		in.close();

		return props;
	}

	// String clientId, String clientSecret, String audience, String m2mAuthDomain
	public static String getToken() throws Exception {
		Properties props = loadPropertyFile();

		JWTTokenGenerator jwtTokenGenerator = JWTTokenGenerator.getInstance(props.getProperty("clientId"),
				props.getProperty("clientSecret"), props.getProperty("audience"), props.getProperty("m2mAuthDomain"),
				30, null);
		String token = jwtTokenGenerator.getMachineToken();
		return token;
	}

	public static String generateReview(JSONObject postJSON, String testPhase, String token) throws Exception {

		Properties props = loadPropertyFile();
		Client client = ClientBuilder.newClient();

		Response response;
		if (testPhase == "system") {
			WebTarget webTarget = client.target(props.getProperty("tcDomain") + "/reviewSummations");

			response = webTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + token).post(Entity.json(postJSON));
		} else {
			WebTarget webTarget = client.target(props.getProperty("tcDomain") + "/reviews");

			response = webTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + token).post(Entity.json(postJSON));

		}

		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}

		return response.readEntity(String.class);

	}

	public static JSONArray getReviews(String challengeId, String token) throws Exception {

		Properties props = loadPropertyFile();
		JSONArray reviews = new JSONArray();
		JSONObject memberReviews = new JSONObject();

		try {

			Client client = ClientBuilder.newClient();
			WebTarget webTarget = client.target(
					props.getProperty("tcDomain") + "/submissions/?challengeId=" + challengeId + "&perPage=100");

			Response response = webTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + token).get();

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String submissions = response.readEntity(String.class);
			JSONParser parser = new JSONParser();
			Object jsonObj = parser.parse(submissions);
			JSONArray jsonArray = (JSONArray) jsonObj;

			for (Object submission : jsonArray) {
				JSONObject jsonSub = (JSONObject) submission;
				JSONArray reviewArray = (JSONArray) jsonSub.get("review");
				Long memberId = (Long) jsonSub.get("memberId");

				if (!memberReviews.containsKey(memberId)) {
					memberReviews.put(memberId, new JSONArray());
				}

				JSONArray tempJSONArray = new JSONArray();

				for (Object review : reviewArray) {
					JSONObject jsonReview = (JSONObject) review;
					if (!jsonReview.get("typeId").equals(props.getProperty("avScanTypeId"))) {
						tempJSONArray = (JSONArray) memberReviews.get(memberId);
						tempJSONArray.add(review);
					}

				}

				if (tempJSONArray.size() > 0) {
					memberReviews.put(memberId, tempJSONArray);
				}
			}

			for (Object memberId : memberReviews.keySet()) {
				JSONArray tempReviews = (JSONArray) memberReviews.get(memberId);
				reviews.add(tempReviews.get(0));
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		return reviews;
	}

	public static JSONArray list2JSON(List<Map<String,Object>> list) {
		JSONArray res = new JSONArray();
		for(int i = 0; i < list.size(); i++) {
			JSONObject obj = new JSONObject();
			for(Map.Entry<String,Object> entry : list.get(i).entrySet()) {
				obj.put(entry.getKey(),entry.getValue());
			}
			res.add(obj);
		}
		return res;
	}

	public static String postReview(
			String challengeId,
			String testPhase,
			String submissionId,
			String reviewerId,
			String typeId,
			String scoreCardId,
			List<Map<String,Object>> testScores
		) throws Exception {

		String postResult = "";
		try {
			// Get Token
			String token = getToken();
	
			// Get Reviews
			JSONArray reviews = getReviews(challengeId, token);
			System.out.println("getReviews:");
			System.out.println(reviews);

			// Calc Max Scores for each test case
			Map<String,Double> maxScores = new HashMap<String,Double>();

			for (Object obj : reviews) {
				JSONObject reviewObj = (JSONObject) obj;
				reviewObj = (JSONObject) reviewObj.get("metadata");
				if( null == reviewObj ) continue;
				JSONArray scoObjs = (JSONArray) reviewObj.get("testScores");
				if( null == scoObjs ) continue;

				for (Object scoObj : scoObjs) {
					JSONObject scoreObj = (JSONObject) scoObj;
					String testcase = scoreObj.get("testcase").toString();
					Double scoreCase = Double.parseDouble(scoreObj.get("score").toString());
					Double maxScore = maxScores.get(testcase);
					if(maxScore == null || maxScore < scoreCase) {
						maxScores.put(testcase,scoreCase);
					}
				}
			}

			JSONObject metadata = new JSONObject();
			metadata.put("testType",testPhase);
			metadata.put("testScores",list2JSON(testScores));

			double score = 0.0;
			int tot = 0;
			// Calc Relative Scores
			for (Map<String,Object> scoreObj : testScores) {
				String testcase  = scoreObj.get("testcase").toString();
				Double scoreCase = Double.parseDouble(scoreObj.get("score").toString());
				Double maxScore  = maxScores.get(testcase);
				if ( null == maxScore ) {
					scoreCase = 1.0;
				} else if( 0.0 == maxScore ) {
					if( 0.0 < scoreCase ) {
						scoreCase = 1.0;
					} else {
						scoreCase = 0.0;
					}
				} else {
					scoreCase = scoreCase/maxScore;
				}
				scoreCase *= 100.0;
				score += scoreCase;
				tot ++;
				scoreObj.put("score",scoreCase);
			}

			if(tot>0) score /= tot;

			// add relativeScores
			metadata.put("relativeScores",list2JSON(testScores));

			JSONObject reviewJSON = new JSONObject();
			reviewJSON.put("submissionId",submissionId);
			reviewJSON.put("reviewerId",reviewerId);
			reviewJSON.put("typeId",typeId);
			reviewJSON.put("score",score);
			reviewJSON.put("scoreCardId",scoreCardId);
			reviewJSON.put("metadata",metadata);


			System.out.println("generate reviewJSON:");
			System.out.println(reviewJSON);

			postResult = generateReview(reviewJSON, "provisional", token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return postResult;
	}

}
