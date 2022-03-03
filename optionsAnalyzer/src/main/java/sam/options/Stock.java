package sam.options;


public class Stock {
	public double[] priceHistory = new double[261];
	private String name;
	
	public Stock(String n) {
		name = n;
	}
	
	public Stock() {
		this("");
	}
	
	public void print() {
		System.out.println("Stock: " + name);
		for(int i = 0; i < 100; i++) {
			System.out.println(priceHistory[i]);
		}
	}
	
	public String getName() {
		return name;
	}
	
}
