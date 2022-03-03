package sam.options;
import java.util.Date;

public class Record {
	private float open;
	private float close;
	private float high;
	private float low;
	private String date;
	
	public Record(String rd, int o, int h, int l, int c) {
		open = o;
		close = c;
		high = h;
		low = l;
		date = rd;
	}
	
	public Record() {
		this("", 0, 0 , 0 , 0);
	}

	public float getOpen() {
		return open;
	}

	public void setOpen(float open) {
		this.open = open;
	}

	public float getClose() {
		return close;
	}

	public void setClose(float close) {
		this.close = close;
	}

	public float getHigh() {
		return high;
	}

	public void setHigh(float high) {
		this.high = high;
	}

	public float getLow() {
		return low;
	}

	public void setLow(float low) {
		this.low = low;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public void print() {
		System.out.println(date + " " + open + " " + high + " " + low + " " + close);
	}
	
	
	
	
}
