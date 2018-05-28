package com.merge.pdf.demo.com.sfdc.pdfmerge.sample;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class DemoREST extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String INSTANCE_URL = "INSTANCE_URL";

	private void showAccounts(String instanceUrl, String accessToken,
			PrintWriter writer) throws ServletException, IOException {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		HttpGet get = new HttpGet(instanceUrl
				+ "/services/data/v20.0/query");
		
		get.setHeader("Authorization", "OAuth " + accessToken);

		
		BasicHttpParams p = new BasicHttpParams();
		p.setParameter("q", "SELECT Name, Id from Account LIMIT 100");
		
		get.setParams(p);
		


		try {
			HttpResponse response2 =httpclient.execute(get);
			
				// Now lets use the standard java json classes to work with the
				// results
				try {
					
					HttpEntity entity2 = response2.getEntity();
					
					JSONObject response = new JSONObject(
							new JSONTokener(new InputStreamReader(
									entity2.getContent())));
					System.out.println("Query response: "
							+ response.toString(2));

					writer.write(response.getString("totalSize")
							+ " record(s) returned\n\n");

					JSONArray results = response.getJSONArray("records");

					for (int i = 0; i < results.length(); i++) {
						writer.write(results.getJSONObject(i).getString("Id")
								+ ", "
								+ results.getJSONObject(i).getString("Name")
								+ "\n");
					}
					writer.write("\n");
				} catch (JSONException e) {
					e.printStackTrace();
					throw new ServletException(e);
				}
			
		} finally {
			get.releaseConnection();
		}
	}

	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();

		String accessToken = (String) request.getSession().getAttribute(
				ACCESS_TOKEN);

		String instanceUrl = (String) request.getSession().getAttribute(
				INSTANCE_URL);

		if (accessToken == null) {
			writer.write("Error - no access token");
			return;
		}

		writer.write("We have an access token: " + accessToken + "\n"
				+ "Using instance " + instanceUrl + "\n\n");

		showAccounts(instanceUrl, accessToken, writer);


	}
}

