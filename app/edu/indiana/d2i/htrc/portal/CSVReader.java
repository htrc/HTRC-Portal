package edu.indiana.d2i.htrc.portal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;

public class CSVReader {
    public static Map<String,Integer> readAndSaveInstDomains(String filePath){
        try {

            CsvReader instList = new CsvReader(filePath);

            instList.readHeaders();
            Map<String,Integer> instDomains = new HashMap<>();

            while (instList.readRecord())
            {
                String instWebAddress = instList.get("Institution_Web_Address");
                String instDomainName = instWebAddress.replaceFirst("www.","");
                if(instDomainName.contains("/")){
                    instDomainName = instDomainName.substring(0,instDomainName.indexOf("/"));
                }
                if(!instDomains.containsKey(instDomainName)){
                    instDomains.put(instDomainName,1);
                }
            }

            instList.close();
            return instDomains;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String,Integer> readAndSaveApprovedEmails(String filePath){
        CsvReader approvedEmailList = null;
        try {
            approvedEmailList = new CsvReader(filePath);
            approvedEmailList.readHeaders();
            Map<String,Integer> approvedEmails = new HashMap<>();

            while (approvedEmailList.readRecord())
            {
                String approvedEmail = approvedEmailList.get("Approved_Emails");

                if(!approvedEmails.containsKey(approvedEmail)){
                    approvedEmails.put(approvedEmail,1);
                }
            }

            approvedEmailList.close();
            return approvedEmails;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
