package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadFromFile {

	private final String FILE_NAME = "data.txt";
	private final int MAX_ARRAY_LENGTH = 10000;
	private Double xArray[];
	private Double yArray[];

	public ReadFromFile() {

	}

	public void read() {

		xArray = new Double[MAX_ARRAY_LENGTH];
		yArray = new Double[MAX_ARRAY_LENGTH];

		File file = new File(FILE_NAME);
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (sc != null) {
			int count = 0;
			sc.useDelimiter(";");
			while (sc.hasNext()) {
				System.out.println("Read count: " + count);
				String current = sc.next();
				String[] stringArray = current.split(",");
				Double x = Double.parseDouble(stringArray[0]);
				Double y = Double.parseDouble(stringArray[1]);
				xArray[count] = x;
				yArray[count] = y;
				count ++;
			}
		}

	}

	public Double[] getxArray() {
		return xArray;
	}

	public void setxArray(Double[] xArray) {
		this.xArray = xArray;
	}

	public Double[] getyArray() {
		return yArray;
	}

	public void setyArray(Double[] yArray) {
		this.yArray = yArray;
	}

	public String getFILE_NAME() {
		return FILE_NAME;
	}

	public int getMAX_ARRAY_LENGTH() {
		return MAX_ARRAY_LENGTH;
	}
	
	

}
