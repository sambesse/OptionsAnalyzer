package sam.options;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ErrorReporter {
	private List<Error> errorList;
	private int errCnt;
	private FileWriter reportWriter;
	
	ErrorReporter() throws IOException {
		errorList = new LinkedList<Error>();
		errCnt = 0;
		reportWriter = new FileWriter("errorReport.txt");
	}
	
	public void reportError(String s) throws IOException {
		Error er = new Error(s);
		errorList.add(er);
		errCnt++;
		reportWriter.write(er.toString() + '\n');
	}
	
	public void reportError(Exception e, String s) throws IOException {
		Error er = new Error(e, s);
		errorList.add(er);
		errCnt++;
		reportWriter.write(er.toString() + '\n');
	}
	
	public void reportError(Error e) throws IOException {
		errorList.add(e);
		errCnt++;
		reportWriter.write(e.toString() + '\n');
	}
	
	public void generateReport() throws IOException {
		reportWriter.write(errCnt + " errors reported.");
		reportWriter.close();
	}
}
