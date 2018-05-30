package com.deloitte;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RestController
public class MergeService {

	   @RequestMapping(value = "/MergeService/merge", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
	   public static String mergeUsers(@RequestParam("file1Id") String file1Id,
			   @RequestParam("file2Id") String file2Id,
			   @RequestParam("parentId") String parentId){
		   Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		   
		   return gson.toJson("SUCCESS");
		   //MergeAndUploadPDF.mergeanduploadPDF(file1Id, file2Id, parentId);; 
		   
	   }	
}
     
