package twistBioScience;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

public class driverClass {
	private StringBuffer sBuffer = new StringBuffer("");
	private LinkedHashMap<String, String> globalMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<Integer, Character> sequenceMapNormalCharacter = new LinkedHashMap<Integer, Character>();
	private LinkedHashMap<Integer, Character> sequenceMapSpecialCharacter = new LinkedHashMap<Integer, Character>();
	private LinkedHashMap<String, String> finalResults = new LinkedHashMap<String, String>();
	private String inputFile;
	private String dnaSeqFile;
	private String resultFolder;
	private String outputFileName;
	private long numberOfResultsToExtract;
	private String excelWorkSheetName;
	private String column1;
	private String column2;
	private String column3;
	private String column4;


	// Call constructor to initialize variables
	public driverClass() {
		try {
			this.inputFile = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.inputFileName);
			this.dnaSeqFile = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.dnaSeqFileName);
			this.resultFolder = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.resultFolder);
			this.outputFileName = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.outputFileName);
			this.numberOfResultsToExtract= Long.parseLong(ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.numberOfResultsToExtract));
			this.excelWorkSheetName=ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.excelWorkSheetName);
			this.column1=ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.column1);
			this.column2=ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.column2);
			this.column3=ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.column3);
			this.column4=ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.column4);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Load the translation table in a hashmap(key,value)
	@Test(enabled = true, priority = 0)
	public void loadCodesIntoHashMap() throws IOException {

		try {
			globalMap = commonFunctions.readFile(dnaSeqFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Convert the 3 character DNA into AA codes as per given specification
	@Test(enabled = true, priority = 1)
	public void convertDNAToAA() throws IOException {
		String dnaString = commonFunctions.readFileIntoString(inputFile);

		String temp;
		String converted;

		for (int i = 0; i < dnaString.length() - 2; i = i + 3) {
			temp = "";
			converted = "";
			temp = dnaString.substring(i, i + 3).trim();
			converted = temp.replaceAll(temp, globalMap.get(temp));
			sBuffer = sBuffer.append(converted);

		}

		//System.out.println("Translated DNA codes:" + sBuffer.toString());
		commonFunctions.writeToFile(this.outputFileName, sBuffer.toString());

	}

	// Find all occurances of 'M' start of DNA sequence & '*' end of DNA Sequence &
	// load into a global Hash map to be used later
	@Test(enabled = true, priority = 2)
	public void returnUniqueDNASequence() throws IOException {
		String dnaSeq = sBuffer.toString();

		for (int i = 0; i < dnaSeq.length(); i++) {
			if (dnaSeq.charAt(i) == 'M') {
				sequenceMapNormalCharacter.put(i, 'M');

			} else if (dnaSeq.charAt(i) == '*') {
				sequenceMapSpecialCharacter.put(i, '*');

			} else {
				continue;
			}
		}

	}

	// Get Position of 'M' character such that successive appearances are more than
	@Test(enabled = true, priority = 3)
	public void getUniqueSeq() throws IOException {
		int numberOfTotalSeq = 0;
		int numberOfNonUnique = 0;

		String getTranslatedValue = "";
		String concanateString;
		String aaSeq ="";
		int begainIndex=0;
		int endIndex=0;

		// Loop through and remove unwanted data which is not as per required criteria
		for (int key1 : sequenceMapNormalCharacter.keySet()) {
			
			for (int key2 : sequenceMapSpecialCharacter.keySet()) {

				if ((key1 >= key2) || ((key2 - key1) < 20)) {
					continue;
				}
				// valid value so process it
				else
				{
					aaSeq = sBuffer.toString().substring(key1, key2 + 1);
					String s = commonFunctions.readFileIntoString(inputFile);
					begainIndex = sBuffer.toString().indexOf(aaSeq)*3;
					endIndex = begainIndex+ aaSeq.length()*3;
					getTranslatedValue=s.substring(begainIndex, endIndex);	
					concanateString = aaSeq + ":" + getTranslatedValue;

					if (finalResults.containsValue(concanateString)) {
						System.out
								.println("DNA Sequence already found:" + concanateString);
						continue;
					}
					//finalMap.put(begainIndex, aaSeq);
					if (getTranslatedValue.length() % 3 == 0) {
						
						//only list the first 3000
						if (numberOfTotalSeq<=numberOfResultsToExtract)
						{
						finalResults.put(begainIndex+"-"+key1+":"+key2, concanateString);
	
						}
			
						numberOfTotalSeq++;						

					} else {
						numberOfNonUnique=numberOfNonUnique+1;
						continue;
					}
				
				}

			}

		
		}

		System.out.println("Number of unique DNA sequence:" + numberOfTotalSeq);
	}

	

	// Excel report to generate the output
	@Test(enabled = true, priority = 6)
	public void writeToExcelTwistDNAResult() throws FileNotFoundException {
		//int limitRecordDisplay = 0;

		System.out.println(
				"Generating the output excel.Due to large data set please be patience..\nExcel results will be generated in Output folder within the project workspace");

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(excelWorkSheetName);
		XSSFRow header = sheet.createRow(0);

		XSSFCellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		headerStyle.setFont(font);

		XSSFCell headerCell = header.createCell(0);
		headerCell.setCellValue(column1);
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(1);
		headerCell.setCellValue(column2);
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(2);
		headerCell.setCellValue(column3);
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(3);
		headerCell.setCellValue(column4);
		headerCell.setCellStyle(headerStyle);
		
		headerCell = header.createCell(4);
		headerCell.setCellValue("Start-End AA Seq");
		headerCell.setCellStyle(headerStyle);

		// get the list for exchange rate list and write to excel

		XSSFCellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		int i = 1;

		for (String key : finalResults.keySet()) {

			/*if (limitRecordDisplay >= 27703) {
				System.out.println("linit:"+limitRecordDisplay);
				break;
			}*/
			
			String[] aaToDna = finalResults.get(key).split(":");
			XSSFRow row = sheet.createRow(i);

			// AA Sequence
			XSSFCell cell = row.createCell(0);
			cell.setCellValue(aaToDna[0]);
			cell.setCellStyle(style);

			// DNA Sequence
			cell = row.createCell(1);
			cell.setCellValue(aaToDna[1]);
			cell.setCellStyle(style);

			// Start Index - DNA Seq
			cell = row.createCell(2);
			cell.setCellValue(key.substring(0, key.indexOf("-")));
			cell.setCellStyle(style);

			// Length
			cell = row.createCell(3);
			cell.setCellValue(aaToDna[1].length());
			cell.setCellStyle(style);
			// sheet.autoSizeColumn(2);

			// Start Index - AA Seq
			cell = row.createCell(4);
			cell.setCellValue(key.substring( key.indexOf("-")+1, key.length()));
			cell.setCellStyle(style);


			//limitRecordDisplay++;
			i++;
		}
		
		//if we auto resize during the excel operations then
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);

		String fileName = commonFunctions.returnUniqueFileName();
		String fileLocation = resultFolder + fileName;

		FileOutputStream outputStream = new FileOutputStream(fileLocation);
		try {
			workbook.write(outputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
