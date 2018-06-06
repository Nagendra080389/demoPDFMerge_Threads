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

	static EnterpriseConnection connection;

	/*public static void main(String[] args) {

		String file1Id = "0692F000000FQUW";
		String file2Id = "0692F000000FQVA";

		String parentId = "a0Z2F000000eFEVUA2";

		//mergeanduploadPDF(file1Id,file2Id, parentId, accessToken, instanceURL);

	}*/

	// queries and displays the 5 newest contacts
	public static void mergeanduploadPDF(String file1Id, String file2Id, String parentId, String accessToken, String instanceURL, boolean useSoap) {

		System.out.println("Querying for the mail request...");

		ConnectorConfig config = new ConnectorConfig();
		config.setSessionId(accessToken);
		if(useSoap) {
			config.setPassword(instanceURL + "/services/Soap/u/40.0");
		}else {
			config.setPassword(instanceURL + "/services/Soap/T/40.0");
		}

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
	
	// split 1 pdf file and get first page out of it
	public static void splitanduploadPDF(String documentId, String parentId, String accessToken, String instanceURL, boolean useSoap) {

		try {

			System.out.println("Querying for the mail request...");

			ConnectorConfig config = new ConnectorConfig();
			config.setSessionId(accessToken);
			if(useSoap) {
				config.setPassword(instanceURL + "/services/Soap/u/40.0");
			}else {
				config.setPassword(instanceURL + "/services/Soap/T/40.0");
			}
			connection = Connector.newConnection(config);

			// query for the attachment data
			QueryResult queryResults = connection.query(
					"Select Id,VersionData from ContentVersion where Id IN(Select LatestPublishedVersionId from ContentDocument where Id = '"
							+ documentId + "')");
			System.out.println("in here.." + queryResults.getSize());
			File tempFile = File.createTempFile("test_", ".pdf", null);
			for (int i = 0; i < queryResults.getSize(); i++) {
				ContentVersion contentData = (ContentVersion) queryResults.getRecords()[i];
				System.out.println(i + "..file size.." + contentData.getVersionData().length + "    "
						+ contentData.getVersionData());
				FileOutputStream fos = new FileOutputStream(tempFile);
				fos.write(contentData.getVersionData());
				fos.close();
			}
			PdfReader Split_PDF_Document = new PdfReader(tempFile.toString());
			Document document;
			PdfCopy copy;

			document = new Document();
			String FileName = "File" + 1 + ".pdf";
			copy = new PdfCopy(document, new FileOutputStream(FileName));
			document.open();
			copy.addPage(copy.getImportedPage(Split_PDF_Document, 1));
			document.close();

			File splitFile = new File(FileName);
			splitFile.createNewFile();

			// File mergedFile = File .createTempFile( "CombinedPDFDocument", ".pdf", null);

			System.out.println("Creating ContentVersion record...");
			ContentVersion[] record = new ContentVersion[1];
			ContentVersion splitContentData = new ContentVersion();

			splitContentData.setVersionData(Files.readAllBytes(splitFile.toPath()));
			splitContentData.setFirstPublishLocationId(parentId);
			splitContentData.setTitle("Split Document");
			splitContentData.setPathOnClient(FileName);

			record[0] = splitContentData;

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
