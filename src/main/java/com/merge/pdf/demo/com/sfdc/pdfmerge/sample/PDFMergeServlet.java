package com.merge.pdf.demo.com.sfdc.pdfmerge.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class PDFMergeServlet extends HttpServlet {
	private String accessToken;
	private String instanceURL;
	
	private static final Logger log = Logger.getLogger(PDFMergeServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	    log.severe("PDF Merge Servlet Called");
		String sfdcInstance = req.getParameter("instanceURL");
		
		accessToken = (String)req.getSession().getAttribute(StartServet.ACCESS_TOKEN);
		instanceURL = (String)req.getSession().getAttribute(StartServet.INSTANCE_URL);
		
		
		if (accessToken == null)
			return;
		
		String ids = req.getParameter("ids");
		String parentId = req.getParameter("parentId");
		String mergedDocName = req.getParameter("mergedDocName");
		
		List<InputStream> pdfs = new ArrayList<InputStream>();
		ByteArrayOutputStream o = null;
		if (accessToken != null && ids != null && parentId != null)
		{
			Salesforce sfdc = new Salesforce(instanceURL, accessToken);
			for (String id : ids.split(","))
			{
				pdfs.add(sfdc.getAttachment(id));
			}

			o = new ByteArrayOutputStream();
			MergePDF.concatPDFs(pdfs, o, false);
			
			sfdc.saveAttachment(parentId, mergedDocName, o, "application/pdf");
		}
		
		try
		{
			if (o != null)
			{	
				o.flush();
				o.close();
			}
			
			for (InputStream pdf : pdfs)
			{
				pdf.close();
			}		
		}
		catch (Exception e){e.printStackTrace();}
		
		resp.setContentType("text/plain");
		resp.getWriter().println("Documents merged successfully");
	}
	
}
