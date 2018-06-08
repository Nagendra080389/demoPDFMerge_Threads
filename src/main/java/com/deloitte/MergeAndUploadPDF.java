package com.deloitte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.ContentVersion;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class MergeAndUploadPDF {

    static EnterpriseConnection connection;
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    // queries and displays the 5 newest contacts
    public static void mergeanduploadPDF(String file1Id, String file2Id, String parentId, String accessToken, String instanceURL, boolean useSoap) {

        System.out.println("Querying for the mail request...");

        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(accessToken);
        if (useSoap) {
            config.setServiceEndpoint(instanceURL + "/services/Soap/c/40.0");
        } else {
            config.setServiceEndpoint(instanceURL + "/services/Soap/T/40.0");
        }

        List<String> contentDocIds = new ArrayList<String>();
        contentDocIds.add(file1Id);
        contentDocIds.add(file2Id);

        List<File> inputFiles = new ArrayList<File>();

        try {

            THREADPOOL.execute(new Runnable() {
                @Override
                public void run() {

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
                                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                                        fos.write(contentData.getVersionData());
                                    }
                                    inputFiles.add(tempFile);
                                }
                            }
                        }

                        Document PDFCombineUsingJava = new Document();
                        PdfSmartCopy copy = new PdfSmartCopy(PDFCombineUsingJava, new FileOutputStream("CombinedPDFDocument.pdf"));
                        PDFCombineUsingJava.open();
                        PdfReader ReadInputPDF;
                        int number_of_pages;
                        for (File inputFile : inputFiles) {
                            ReadInputPDF = new PdfReader(inputFile.toString());
                            number_of_pages = ReadInputPDF.getNumberOfPages();
                            for (int page = 0; page < number_of_pages; ) {
                                copy.addPage(copy.getImportedPage(ReadInputPDF, ++page));
                            }
                            ReadInputPDF.close();
                        }
                        PDFCombineUsingJava.close();
                        copy.close();
                        File mergedFile = new File("CombinedPDFDocument" + ".pdf");
                        mergedFile.createNewFile();

                        System.out.println("Creating ContentVersion record...");
                        ContentVersion[] record = new ContentVersion[1];
                        ContentVersion mergedContentData = new ContentVersion();

                        mergedContentData.setVersionData(Files.readAllBytes(mergedFile.toPath()));
                        mergedContentData.setFirstPublishLocationId(parentId);
                        mergedContentData.setTitle("Merged Document");
                        mergedContentData.setPathOnClient("/CombinedPDFDocument.pdf");

                        record[0] = mergedContentData;


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
                    } catch (ConnectionException | IOException | DocumentException e) {
                        e.printStackTrace();
                    }
                }
            });


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
            if (useSoap) {
                config.setServiceEndpoint(instanceURL + "/services/Soap/c/40.0");
            } else {
                config.setServiceEndpoint(instanceURL + "/services/Soap/T/40.0");
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
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(contentData.getVersionData());
                }
            }
            PdfReader Split_PDF_Document = new PdfReader(tempFile.toString());
            Document document;
            document = new Document();
            String FileName = "File" + 1 + ".pdf";
            PdfSmartCopy copy = new PdfSmartCopy(document, new FileOutputStream(FileName));
            document.open();
            copy.addPage(copy.getImportedPage(Split_PDF_Document, 1));
            copy.close();
            document.close();
            Split_PDF_Document.close();
            File splitFile = new File(FileName);
            splitFile.createNewFile();

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
        } finally {


        }

    }

}
