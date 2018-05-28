package com.merge.pdf.demo.com.sfdc.pdfmerge.sample;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;



public class Salesforce {

	public static final String sfdcApiVersion = "21.0";
	public static final String sfdcOAuthURI = "/services/oauth2/token";
	
	private String instanceURL;
	private String accessToken;
	
	private static final Logger log = Logger.getLogger(PDFMergeServlet.class.getName());
	
	public Salesforce(String instanceURL, String accessToken)
	{
		this.instanceURL = instanceURL;
		this.accessToken = accessToken;
	}
	
	public InputStream getAttachment (String recordId)
	{
		if(recordId == null || recordId.trim().equals(""))
		{
			log.severe("No Attachment ID for processing");
			return null;
		}
		log.severe("Getting Attachment:"+ recordId);		
		String restURI = instanceURL + "/services/data/v" + sfdcApiVersion + 
						 "/sobjects/attachment/" + recordId + "/body";
		try
		{
			//URL url = new URL(restURI);
			
			DefaultHttpClient httpclient = new DefaultHttpClient();	
			
			HttpGet httpget = new HttpGet(restURI);
			httpget.setHeader("Authorization","OAuth "+ accessToken);
			
			HttpResponse response = httpclient.execute(httpget);
						
			log.severe("HTTP code:"+response.getStatusLine().getStatusCode());

			InputStream i = null;
			if (response.getStatusLine().getStatusCode() == 200)
	    	{
				HttpEntity ent = response.getEntity();
				
				i = ent.getContent();
	    	}
			return i;
	    } catch (Exception e) {
		    log.severe("Getting Attachment Exception:"+ e.getMessage());
	    	e.printStackTrace();
	    }			
	    return null;
	}
	
	public void saveAttachment (String parentId, String name, ByteArrayOutputStream body, String contentType)
	{
		log.severe("Saving Attachment:"+ name);		
		String restURI = instanceURL + "/services/data/v" + sfdcApiVersion + "/sobjects/attachment/";
		try
		{
			
			//URL url = new URL(restURI);
			
			DefaultHttpClient httpclient = new DefaultHttpClient();	
			
			HttpPost httpPost = new HttpPost(restURI);
			httpPost.setHeader("Authorization","OAuth "+ accessToken);
			httpPost.setHeader("Content-Type", "application/json");


			JSONObject attachment = new JSONObject();
			
			String mergedPdf = new String(Base64.encodeBase64(body.toByteArray()));

			attachment.put("parentId", parentId);
			attachment.put("name", (name == null || name.equals(""))? "MergedDoc.pdf":name);
			attachment.put("ContentType", contentType);
			attachment.put("body", mergedPdf);

			ByteArrayEntity ent = new ByteArrayEntity(attachment.toString().getBytes());
			httpPost.setEntity(ent);


			HttpResponse response = httpclient.execute(httpPost);
			
			
			log.severe("HTTP code After Saving the attachment:"+response.getStatusLine().getStatusCode());
			 
	    } catch (Exception e) {
		    log.severe("Saving Attachment Exception:"+ e.getMessage());
	    	e.printStackTrace();
	    }
	}
}
