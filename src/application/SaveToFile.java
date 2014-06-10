package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SaveToFile {
	
	private final String FILE_NAME = "data.txt";

	public SaveToFile() {

	}

	public void save(Double x[], Double y[]) {
		File file = new File(FILE_NAME);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (pw != null) {
			for (int j = 0; j < x.length; j ++) {
				pw.print(x[j]);
				pw.print(",");
				pw.print(y[j]);
				pw.print(";");
			}
			pw.close();
		}

	}

}
