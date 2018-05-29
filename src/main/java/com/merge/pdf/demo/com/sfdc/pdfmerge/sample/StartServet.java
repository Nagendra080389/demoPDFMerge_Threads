package com.merge.pdf.demo.com.sfdc.pdfmerge.sample;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Servlet implementation class TestOAuth
 */
public class StartServet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StartServet() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	public static final String INSTANCE_URL = "INSTANCE_URL";
	String oauthURL = "";


	private static final Logger log = Logger.getLogger(StartServet.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sfdcInstance = "https://cs91.salesforce.com";

		String accessToken = (String) request.getSession().getAttribute(ACCESS_TOKEN);
		String code = request.getParameter("code");
		System.out.println("code : "+request.getParameter("code"));

		String consumerKey = "3MVG9d3kx8wbPieHjOp9VVrZ9A5CJRa582aZvgXcfAQf0doVuX.CB2JTws_oBUvx.uyBlsHeE77MxlUpOLwEm";
		String consumerSecret = "3139176758014005887";
		String redirectUri = "https://pdfmergedemo.herokuapp.com/TestOAuth?isCallBack=1";

		oauthURL = sfdcInstance + "/services/oauth2/authorize?response_type=code&client_id=" + consumerKey + "&redirect_uri="
				+ URLEncoder.encode(redirectUri, "UTF-8");

		if (accessToken == null) {

			try {
				String isCallBack = request.getParameter("isCallBack");
				log.info("Starting OAuth");
				log.info("Is CallBack = "+isCallBack);
				System.out.println("Inside Try block : Starting Oauth :");
				System.out.println("Is CallBack = "+isCallBack);

				/**
				 * Initiate HTTP request to get the Authorization code
				 */
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(oauthURL);
				HttpResponse response1 = httpclient.execute(httpPost);

				/**
				 * If HTTP status code is 302, means Salesforce trying to redirect to authorize the Local System
				 */
				if(response1.getStatusLine().getStatusCode() == 302 && isCallBack == null)
				{
					httpclient.getConnectionManager().shutdown();
					log.info("Got 302 request from Salesforce so redirect it");
					response.sendRedirect(oauthURL);
					return;
				}
				else
				{
					log.info("Got Callback, Now get the Access Token");

					httpclient = new DefaultHttpClient();
					String tokenUrl = sfdcInstance + "/services/oauth2/token";

					log.info("Code - "+code);

					List<NameValuePair> qparams = new ArrayList<NameValuePair>();
					qparams.add(new BasicNameValuePair("code", code));
					qparams.add(new BasicNameValuePair("grant_type", "authorization_code"));
					qparams.add(new BasicNameValuePair("client_id", consumerKey));
					qparams.add(new BasicNameValuePair("client_secret", consumerSecret));
					qparams.add(new BasicNameValuePair("redirect_uri", redirectUri));

					HttpPost httpPost2 = new HttpPost(tokenUrl);
					httpPost2.setEntity(new UrlEncodedFormEntity(qparams));

					/**
					 * Always set this Header to explicitly to get Authrization code else following error will be raised
					 * unsupported_grant_type
					 */
					httpPost2.setHeader("Content-Type", "application/x-www-form-urlencoded");

					HttpResponse response2 = httpclient.execute(httpPost2);

					HttpEntity entity2 = response2.getEntity();
					System.out.println(entity2.getContent().toString());

					JSONObject authResponse = new JSONObject(new JSONTokener(new InputStreamReader(entity2.getContent())));
					System.out.println(authResponse.toString());
					accessToken = authResponse.getString("access_token");
					String instanceUrl = authResponse.getString("instance_url");

					request.getSession().setAttribute(ACCESS_TOKEN, accessToken);

					// We also get the instance URL from the OAuth response, so set it
					// in the session too
					request.getSession().setAttribute(INSTANCE_URL, instanceUrl);

					log.info("Response is : "+authResponse);
					log.info("Got access token: " + accessToken);

						/*
						PrintWriter out = response.getWriter();
						out.println("Auth successful - got callback");
						*/
					String pdfUrl = "/pdfmerge?ids=0692F000000FPHbQAO&parentId=a0Z2F000000eFEVUA2&mergedDocName=MergedDoc.pdf";
					response.sendRedirect(pdfUrl);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}

		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}
