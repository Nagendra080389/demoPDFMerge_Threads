package com.merge.pdf.demo.com.sfdc.pdfmerge.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestIFrame
 */
public class TestIFrame extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestIFrame() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sessionId = request.getParameter("SessionId");
		String instanceURL = "https://ap1.salesforce.com";
		String ids = request.getParameter("Attachids");
		String parentId = request.getParameter("parentId");
		String mergedDocName = request.getParameter("mergedDocName");
		
		System.out.println("Attachment Ids - "+ids);
		System.out.println("parentId - "+parentId);
		System.out.println("mergedDocName- "+mergedDocName);

		
		PrintWriter out = response.getWriter();
		
		if(sessionId == null)
		{			
			out.println("Error - Please pass the Session ID");
		}else{

			List<InputStream> pdfs = new ArrayList<InputStream>();
			ByteArrayOutputStream o = null;
			Salesforce sfdc = new Salesforce(instanceURL, sessionId);
			
			
			
			for (String id : ids.split(","))
			{
				pdfs.add(sfdc.getAttachment(id));
			}

			o = new ByteArrayOutputStream();
			MergePDF.concatPDFs(pdfs, o, false);
			
			sfdc.saveAttachment(parentId, mergedDocName, o, "application/pdf");
			
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
			
			response.setContentType("text/plain");
			response.getWriter().println("Documents merged successfully");
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
