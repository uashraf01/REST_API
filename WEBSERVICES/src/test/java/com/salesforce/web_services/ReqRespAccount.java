package com.salesforce.web_services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;

public class ReqRespAccount {
	static final String USERNAME = "ulfat.a.ashraf1@gmail.com";
	static final String PASSWORD = "ssw85926056";
	static final String LOGINURL = "https://login.salesforce.com";
	static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	static final String CLIENTID = "3MVG9szVa2RxsqBYpxeMrlnEUu5rOuSIdmMgUKiiQ2sGyD6KFCyyGxAxUsdIr6xd94KHcqLaS67lLADkefKaD";
	static final String CLIENTSECRET = "4626093501901612035";
	//static final String CLIENTSECRET = "4626093501901612036";
	
	
	private static String REST_ENDPOINT = "/services/data";
	private static String API_VERSION = "/v32.0";  //Where exactly in API doc does it come from
	private static String baseUri;  // Uniform Resource Identifier. The most common form of URI is the Uniform Resource Locator (URL), frequently referred to informally as a web address
	private static Header oauthHeader;  //this is an object of Header
	private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
	private static String accountId;
	private static String accountName;
	//private static String leadLastName;
	//private static String leadCompany;

	public static void main(String[] args) {

		HttpClient httpclient = HttpClientBuilder.create().build();

		// Assemble the login request URL
		String loginURL = LOGINURL +
                GRANTSERVICE +
                "&client_id=" + CLIENTID +
                "&client_secret=" + CLIENTSECRET +
                "&username=" + USERNAME +
                "&password=" + PASSWORD;

		// Login requests must be POSTs
		HttpPost httpPost = new HttpPost(loginURL);
		HttpResponse response = null;

		try {
			// Execute the login POST request
			response = httpclient.execute(httpPost);
		} catch (ClientProtocolException cpException) {
			cpException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		// verify response is HTTP OK
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			System.out.println("Error authenticating to Force.com: " + statusCode);
			// Error is in EntityUtils.toString(response.getEntity())
			return;
		}

		String getResult = null;
		try {
			getResult = EntityUtils.toString(response.getEntity());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		JSONObject jsonObject = null;
		String loginAccessToken = null;
		String loginInstanceUrl = null;

		try {
			jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
			loginAccessToken = jsonObject.getString("access_token");
			loginInstanceUrl = jsonObject.getString("instance_url");
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}

		baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION;
		oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken);
		System.out.println("oauthHeader1: " + oauthHeader);
		System.out.println("\n" + response.getStatusLine());
		System.out.println("Successful login");
		System.out.println("instance URL: " + loginInstanceUrl);
		System.out.println("access token/session ID: " + loginAccessToken);
		System.out.println("baseUri: " + baseUri);

		// Run codes to query, insert, update and delete records in Salesforce
		// using REST API
		queryAccounts();
		//createAccounts();
		//updateAccounts();
		//deleteAccounts();
		
		// release connection
		httpPost.releaseConnection();
	}

/*	private static void createAccounts() {
		// TODO Auto-generated method stub
		
	}*/

	// Query Accounts using REST HttpGet
	public static void queryAccounts() {
	        System.out.println("\n_______________ Accounts QUERY to find top 5 Accounts _______________");
	        try {
	 
	            //Set up the HTTP objects needed to make the request.
	            HttpClient httpClient = HttpClientBuilder.create().build();
	 
	            String uri = baseUri + "/query?q=Select+Id+,+name+From+Account";
	          //  String uri = baseUri + "/query?q=Select+Id+,+name+From+Account+Limit+5";
	            System.out.println("Query URL: " + uri);
	            HttpGet httpGet = new HttpGet(uri);
	            System.out.println("oauthHeader2: " + oauthHeader);
	            httpGet.addHeader(oauthHeader);
	            httpGet.addHeader(prettyPrintHeader);
	 
	            // Make the request.
	            HttpResponse response = httpClient.execute(httpGet);
	 
	            // Process the result
	            int statusCode = response.getStatusLine().getStatusCode();
	            if (statusCode == 200) {
	                String response_string = EntityUtils.toString(response.getEntity());
	                try {
	                    JSONObject json = new JSONObject(response_string);
	                    System.out.println("JSON result of Query:\n" + json.toString(1));
	                    JSONArray j = json.getJSONArray("records");
	                    for (int i = 0; i<j.length(); i++){
	                        accountId = json.getJSONArray("records").getJSONObject(i).getString("Id");
	                        accountName = json.getJSONArray("records").getJSONObject(i).getString("Name");
	                      //  leadLastName = json.getJSONArray("records").getJSONObject(i).getString("LastName");
	                      //  leadCompany = json.getJSONArray("records").getJSONObject(i).getString("Company");
	                        System.out.println("Account record is: " + i + ". " + accountId + " " + accountName );
	                    }
	                } catch (JSONException je) {
	                    je.printStackTrace();
	                }
	            } else {
	                System.out.println("Query was unsuccessful. Status code returned is " + statusCode);
	                System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
	                System.out.println(getBody(response.getEntity().getContent()));
	                System.exit(-1);
	            }
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        } catch (NullPointerException npe) {
	            npe.printStackTrace();
	        }
	    }

	// Create Leads using REST HttpPost
	public static void createAccounts() {
		System.out.println("\n_______________ Accounts INSERT _______________");

		String uri = baseUri + "/sobjects/Account/";
		try {

			// create the JSON object containing the new lead details.
			JSONObject account = new JSONObject();
			account.put("Account Name", "SW Inc");
/*			account.put("parent account", "Sam Inc");
			account.put("industry", "Banking");
*/
			System.out.println("JSON for account record to be inserted:\n" + account.toString(1));

			// Construct the objects needed for the request
			HttpClient httpClient = HttpClientBuilder.create().build();

			HttpPost httpPost = new HttpPost(uri);
			httpPost.addHeader(oauthHeader);
			httpPost.addHeader(prettyPrintHeader);
			// The message we are going to post
			StringEntity body = new StringEntity(account.toString(1));
			body.setContentType("application/json");
			httpPost.setEntity(body);

			// Make the request
			HttpResponse response = httpClient.execute(httpPost);

			// Process the results
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 201) {
				String response_string = EntityUtils.toString(response.getEntity());
				JSONObject json = new JSONObject(response_string);
				// Store the retrieved lead id to use when we update the lead.
				accountName = json.getString("id");
				System.out.println("New Account Name from response: " + accountName);
			} else {
				System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
			}
		} catch (JSONException e) {
			System.out.println("Issue creating JSON or processing results");
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	// Update Leads using REST HttpPatch. We have to create the HTTPPatch, as it
	// does not exist in the standard library
	// Since the PATCH method was only recently standardized and is not yet
	// implemented in Apache HttpClient
	public static void updateAccount() {
		System.out.println("\n_______________ Account UPDATE _______________");

		// Notice, the id for the record to update is part of the URI, not part
		// of the JSON
		String uri = baseUri + "/sobjects/Account/" + accountName;
		try {
			// Create the JSON object containing the updated lead last name
			// and the id of the lead we are updating.
			JSONObject lead = new JSONObject();
			lead.put("LastName", "IamDeleting");
			System.out.println("JSON for update of lead record:\n" + lead.toString(1));

			// Set up the objects necessary to make the request.
			// DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpClient httpClient = HttpClientBuilder.create().build();

			HttpPatch httpPatch = new HttpPatch(uri);
			httpPatch.addHeader(oauthHeader);
			httpPatch.addHeader(prettyPrintHeader);
			StringEntity body = new StringEntity(lead.toString(1));
			body.setContentType("application/json");
			httpPatch.setEntity(body);

			// Make the request
			HttpResponse response = httpClient.execute(httpPatch);

			// Process the response
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 204) {
				System.out.println("Updated the lead successfully.");
			} else {
				System.out.println("Lead update NOT successfully. Status code is " + statusCode);
			}
		} catch (JSONException e) {
			System.out.println("Issue creating JSON or processing results");
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	// Extend the Apache HttpPost method to implement an HttpPatch
	private static class HttpPatch extends HttpPost {
		public HttpPatch(String uri) {
			super(uri);
		}

		public String getMethod() {
			return "PATCH";
		}
	}

	// Update Leads using REST HttpDelete (We have to create the HTTPDelete, as
	// it does not exist in the standard library.)
	public static void deleteLeads() {
		System.out.println("\n_______________ Lead DELETE _______________");

		// Notice, the id for the record to update is part of the URI, not part
		// of the JSON
		String uri = baseUri + "/sobjects/Lead/" + accountId;
		try {
			// Set up the objects necessary to make the request.
			HttpClient httpClient = HttpClientBuilder.create().build();

			HttpDelete httpDelete = new HttpDelete(uri);
			httpDelete.addHeader(oauthHeader);
			httpDelete.addHeader(prettyPrintHeader);

			// Make the request
			HttpResponse response = httpClient.execute(httpDelete);

			// Process the response
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 204) {
				System.out.println("Deleted the lead successfully.");
			} else {
				System.out.println("Lead delete NOT successful. Status code is " + statusCode);
			}
		} catch (JSONException e) {
			System.out.println("Issue creating JSON or processing results");
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	private static String getBody(InputStream inputStream) {
		String result = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				result += inputLine;
				result += "\n";
			}
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return result;
	
	}
}
