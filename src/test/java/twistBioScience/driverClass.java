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
	private LinkedHashMap<String, String> finalMap = new LinkedHashMap<String, String>();
	private String inputFile;
	private String dnaSeqFile;
	private String resultFolder;

	// Call constructor to initialize variables
	public driverClass() {
		try {
			this.inputFile = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.inputFileName);
			this.dnaSeqFile = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.dnaSeqFileName);
			this.resultFolder = ReadPropertyFile.readPropFileAndReturnPropertyValue(commonFunctions.resultFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//Load the translation table in a hashmap(key,value)
	@Test(enabled = true, priority = 0)
	public void loadCodesIntoHashMap() throws IOException {

		try {
			globalMap = commonFunctions.readFile(dnaSeqFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Convert the 3 character DNA into AA codes as per given specification
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

		System.out.println("Translated DNA codes:"+sBuffer.toString());

	}

	//Find all occurances of 'M' start of DNA sequence and '*' end of DNA Sequence and load into a global Hashmap to be used later
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

	//Get Position of 'M' character such that successive appearances are more than 21
	@Test(enabled = true, priority = 3)
	public void getUniqueSeq() throws IOException {

		int prev = 0;
		for (int key : sequenceMapNormalCharacter.keySet()) {
			if (key - prev < 21) {
				prev = key;
				sequenceMapNormalCharacter.put(key, null);
				continue;
			} else {
				prev = key;

			}

		}

		int temp = 0;
		
		//Loop through and remove unwanted data which is not as per required criteria

		for (int key1 : sequenceMapNormalCharacter.keySet()) {

			for (int key2 : sequenceMapSpecialCharacter.keySet()) {

				//If current 'M' character position is less than that of previous '*' ignore it as it is already covered in previous DNA sequence
				if (key1 < temp) {
					continue;
				}

				//from previous processing NULL means not valid for current processing so ignore it
				else if (sequenceMapNormalCharacter.get(key1) == null) {
					continue;
				} 
				
				//from previous processing NULL means not valid for current processing so ignore it
				else if (sequenceMapSpecialCharacter.get(key2) == null) {
					continue;
				}
                
				//if the difference between 'M' and '*' character position is less than 20 then ignore it
				else if ((key1 >= key2) || ((key2 - key1) < 20)) {
					// System.out.println("1: "+key1+":"+key2);
					continue;
				} 
				//valid value so process it
				else if ((key2 + 1 - key1) % 3 == 0) {
					finalMap.put(key1 + "-" + key2, sBuffer.toString().substring(key1, key2 + 1));
					//System.out.println("2: " + key1 + ":" + key2);
					//System.out.println(sBuffer.toString().substring(key1, key2 + 1));
					sequenceMapSpecialCharacter.put(key2, null);
					sequenceMapNormalCharacter.put(key1, null);
					temp = key2;
					break;
				}
				
				//Everything else goes here
				else {

					continue;
				}

			}

		}


	}

	// Excel report to generate the output
	@Test(enabled = true, priority = 4)
	public void writeToExcelRate() throws FileNotFoundException {
		XSSFWorkbook workbook = new XSSFWorkbook();

		XSSFSheet sheet = workbook.createSheet("ExchangeRate");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 6000);

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
		headerCell.setCellValue("Start:End");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(1);
		headerCell.setCellValue("UniqSeq");
		headerCell.setCellStyle(headerStyle);
		
		headerCell = header.createCell(2);
		headerCell.setCellValue("Len");
		headerCell.setCellStyle(headerStyle);

		// get the list for exchange rate list and write to excel

		XSSFCellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		int i = 1;

		for (String key : finalMap.keySet()) {

			// Start-End Position
			XSSFRow row = sheet.createRow(i);
			XSSFCell cell = row.createCell(0);
			cell.setCellValue(key);
			cell.setCellStyle(style);
			sheet.autoSizeColumn(0);

			// Unique DNA
			cell = row.createCell(1);
			cell.setCellValue(finalMap.get(key));
			cell.setCellStyle(style);
			//sheet.autoSizeColumn(1);


			// Length
			cell = row.createCell(2);
			cell.setCellValue(finalMap.get(key).length());
			cell.setCellStyle(style);
			sheet.autoSizeColumn(2);


			i++;
		}

		// File currDir = new File(".");
		// String path = currDir.getAbsolutePath();
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
