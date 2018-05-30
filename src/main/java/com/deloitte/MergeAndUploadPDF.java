package com.deloitte;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.ContentVersion;
import com.sforce.ws.ConnectorConfig;

public class MergeAndUploadPDF {

	static final String USERNAME = "amsane@deloitte.com.elc.elcgccdev1";
	static final String PASSWORD = "start1234";
	static EnterpriseConnection connection;

	public static void main(String[] args) {

		String file1Id = "0692F000000FPX5";
		String file2Id = "0692F000000FPHb";

		String parentId = "a0Z2F000000eFEVUA2";

		mergeanduploadPDF(file1Id,file2Id, parentId);

	}

	// queries and displays the 5 newest contacts
	public static void mergeanduploadPDF(String file1Id, String file2Id, String parentId) {

		System.out.println("Querying for the mail request...");

		ConnectorConfig config = new ConnectorConfig();
		config.setUsername(USERNAME);
		config.setPassword(PASSWORD);
		// config.setTraceMessage(true);

		// display some current settings
		System.out.println("Auth EndPoint: " + config.getAuthEndpoint());
		System.out.println("Service EndPoint: " + config.getServiceEndpoint());
		System.out.println("Username: " + config.getUsername());
		System.out.println("SessionId: " + config.getSessionId());

		//String[] contentDocIds = { "0692F000000FPX5", "0692F000000FPHb" };
		List<String> contentDocIds = new ArrayList<String>();
		contentDocIds.add(file1Id);
		contentDocIds.add(file2Id);

		List<File> inputFiles = new ArrayList<File>();

		try {

			connection = Connector.newConnection(config);

			for (String contentDocId : contentDocIds) {
				// query for the attachment data
				QueryResult queryResults = connection.query(
						"Select Id,VersionData from ContentVersion where Id IN(Select LatestPublishedVersionId from ContentDocument where Id = '"
								+ contentDocId + "')");
				if (queryResults.getSize() > 0) {
					System.out.println("in here.." + queryResults.getSize());
					for (int i = 0; i < queryResults.getSize(); i++) {
						// cast the SObject to a strongly-typed Mail_Request__c
						ContentVersion contentData = (ContentVersion) queryResults.getRecords()[i];
						System.out.println(i + "..file size.." + contentData.getVersionData().length + "    "
								+ contentData.getVersionData());
						File tempFile = File.createTempFile("test_", ".pdf", null);
						FileOutputStream fos = new FileOutputStream(tempFile);
						fos.write(contentData.getVersionData());
						fos.close();
						inputFiles.add(tempFile);
					}
				}
			}

			Document PDFCombineUsingJava = new Document();
			PdfCopy copy = new PdfCopy(PDFCombineUsingJava, new FileOutputStream("CombinedPDFDocument.pdf"));
			PDFCombineUsingJava.open();
			PdfReader ReadInputPDF;
			int number_of_pages;
			for (File inputFile : inputFiles) {
				ReadInputPDF = new PdfReader(inputFile.toString());
				number_of_pages = ReadInputPDF.getNumberOfPages();
				for (int page = 0; page < number_of_pages;) {
					copy.addPage(copy.getImportedPage(ReadInputPDF, ++page));
				}
			}
			PDFCombineUsingJava.close();

			File mergedFile = new File("CombinedPDFDocument" + ".pdf");
			mergedFile.createNewFile();

			//File mergedFile = File .createTempFile( "CombinedPDFDocument", ".pdf", null);

			System.out.println("Creating ContentVersion record...");
			ContentVersion[] record = new ContentVersion[1];
			ContentVersion mergedContentData = new ContentVersion();

			mergedContentData.setVersionData(Files.readAllBytes(mergedFile.toPath()));
			//mergedContentData.setFirstPublishLocationId("a0Z2F000000eFEVUA2");
			mergedContentData.setFirstPublishLocationId(parentId);
			mergedContentData.setTitle("Merged Document");
			mergedContentData.setPathOnClient("/CombinedPDFDocument.pdf");

			record[0] = mergedContentData;

			//creating attachment
			/*System.out.println("Creating attachment record...");
			Attachment[] record = new Attachment[1];
			Attachment a = new Attachment();

			a.setParentId("a0Z2F000000eFEVUA2");
			a.setContentType("application/pdf");
			a.setBody(Files.readAllBytes(mergedFile.toPath()));
			a.setName("Merged Document");

			record[0] = a;*/

			// create the records in Salesforce.com
			SaveResult[] saveResults = connection.create(record);

			// check the returned results for any errors
			for (int i = 0; i < saveResults.length; i++) {
				if (saveResults[i].isSuccess()) {
					System.out.println(i + ". Successfully created record - Id: " + saveResults[i].getId());
				} else {
					Error[] errors = saveResults[i].getErrors();
					for (int j = 0; j < errors.length; j++) {
						System.out.println("ERROR creating record: " + errors[j].getMessage());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}