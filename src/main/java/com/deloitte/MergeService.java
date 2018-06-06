package com.deloitte;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RestController
public class MergeService {


    @RequestMapping(value = "/MergeNSplitService/merge", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public static String mergeUsers(@RequestParam("file1Id") String file1Id,
                                    @RequestParam("file2Id") String file2Id,
                                    @RequestParam("parentId") String parentId,
                                    @RequestParam("accessToken") String accessToken,
                                    @RequestParam("instanceURL") String instanceURL,
                                    @RequestParam("useSoap")boolean useSoap) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        MergeAndUploadPDF.mergeanduploadPDF(file1Id, file2Id, parentId,accessToken,instanceURL,useSoap );
        return gson.toJson("Merge PDF SUCCESS");

    }

    @RequestMapping(value = "/MergeNSplitService/split", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public static String splitPDFs(@RequestParam("file1Id") String file1Id,
                                   @RequestParam("parentId") String parentId,
                                   @RequestParam("accessToken") String accessToken,
                                   @RequestParam("instanceURL") String instanceURL,
                                   @RequestParam("useSoap")boolean useSoap) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        MergeAndUploadPDF.splitanduploadPDF(file1Id, parentId,accessToken,instanceURL,useSoap );
        return gson.toJson("SPLIT PDF SUCCESS");

    }
}
     
