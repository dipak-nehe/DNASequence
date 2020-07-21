package twistBioScience;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

public class commonFunctions {
	public static String dnaSeqFileName="dnaSeqFile";
	public static String inputFileName="inputFile";
	public static String resultFolder="resultFolder";
	private static String UniqueDNASequence="UniqueDNASequence";


	
	//read file and load into hashmap
	public static LinkedHashMap<String,String> readFile(String filePath) throws IOException 
	{
		LinkedHashMap<String,String> mapForDNA = new LinkedHashMap<String,String>();
		String []arrayForCodes;
		BufferedReader br = new BufferedReader(new FileReader(filePath));
	    String line="";

		try {
		    while ((line = br.readLine()) != null) 
		    {
		    	arrayForCodes = line.split(":");
		    	// iterating over an array 
		        for (int i = 1; i < arrayForCodes.length; i++) 
		        { 
		  
		        	mapForDNA.put(arrayForCodes[i], arrayForCodes[0]);
		        } 

		    }
		} finally {
		    br.close();
		}
		return  mapForDNA;
	}
	
	//read file into a string
	public static String readFileIntoString(String filePath) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filePath));
	    String line,content = "";

		try {
		    while ((line = br.readLine()) != null) {
		       // process the line
		    	//System.out.println(content);
		    	content = content + line;
		    	
		    }
		} finally {
		    br.close();
		}
		return content;
	}

	
	//generate unqiue ResultFileName
	public static String returnUniqueFileName()
	{
		return commonFunctions.UniqueDNASequence + "_" + System.currentTimeMillis()/1000+".xlsx";
		
	}
}
