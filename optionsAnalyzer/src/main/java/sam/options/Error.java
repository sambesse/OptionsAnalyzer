package sam.options;

public class Error {
	private Exception ex;
	private String er;
	
	Error(Exception e, String s) {
		ex = e;
		er = s;
	}
	
	Error(String s) {
		ex = null;
		er = s;
	}
	
	public String toString() {
		if(ex != null) {
			return ex.toString() + '\n' + er;
		} else {
			return er;
		}
	}
}
