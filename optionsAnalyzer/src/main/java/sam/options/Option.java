package sam.options;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.math3.*;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.regression.*;
import org.apache.commons.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Option {
	private double strike;
	private float prem;
	private float last;
	private float bid;
	private float ask;
	private int expiry;
	private Stock ul;
	private double spotPrice;
	private double zScore;
	private double zScoreProb;
	private double stdErrorD;
	private double firstECDTerm;
	private double secondECDTerm;
	private double ECD;
	private double extVal;
	private double expAnnRet;
	private String symbol;
	private static ErrorReporter errep;
	private static FileWriter logWriter;
	private float delta;
	
	

	

	public Option(float st, float b, float a, float la, float l, int d, Stock s, String symbol, float del) {
		strike = st;
		bid = b;
		ask = a;
		last = la;
		prem = l;
		expiry = d;
		ul = s;
		this.symbol = symbol;
		delta = del;
	}
	
	/*public Option(float st, float l) {
		this(st, 0, 0, 0, l, 0, new Stock(), "");
	}
	
	public Option(float st, float l, Stock s) {
		this(st, 0, 0, 0, l, 0, s);
	}*/
	
	public double calcExpVal() throws IOException {
		
		spotPrice = ul.priceHistory[260];
		double sum = 0;
		for(int t = 1; t < ul.priceHistory.length; t++) {
			double logDiff = Math.log(ul.priceHistory[t]/ul.priceHistory[t-1]);
			sum += Math.pow(logDiff, 2);
		}
		double varOfError = sum/260; //number of e terms (260)
		double stdError1 = Math.sqrt(varOfError);
		stdErrorD = stdError1 * Math.sqrt(expiry);
		//sigma = Math.sqrt(stdError);
		NormalDistribution std = new NormalDistribution(0, 1);
		zScore = Math.log(strike / spotPrice) / stdErrorD;
		zScoreProb = std.cumulativeProbability(Math.log(strike / spotPrice) / stdErrorD);
		firstECDTerm = std.cumulativeProbability((Math.log(strike / spotPrice) - (expiry * varOfError / 2)) / stdErrorD);
		secondECDTerm = std.cumulativeProbability((Math.log(strike / spotPrice) / stdErrorD));
		ECD = spotPrice * Math.exp(varOfError * expiry) * firstECDTerm / secondECDTerm;
		double pi = 1 + (prem + zScoreProb*(ECD - strike))/strike;
		expAnnRet = Math.pow(pi, (260/expiry)) - 1;
		double intVal;
		if (strike > spotPrice) {
			intVal = strike - spotPrice;
		} else {
			intVal = 0;
		}
		extVal = prem - intVal;
		for(int q = 0; q < 261; q++) {
			logWriter.write("ph: " + ul.priceHistory[q]);
			logWriter.write(" ");
		}
		logWriter.write('\n');
		logWriter.write("option " + symbol + " calculated" + '\n');
		logWriter.write("zScore: " + zScore + '\n');
		logWriter.write("zScoreProb: " + zScoreProb + '\n');
		logWriter.write("std Error D: " + stdErrorD + '\n');
		logWriter.write("ECD: " + ECD + '\n');
		logWriter.write("expected annual return: " + expAnnRet + '\n');
		/*System.out.println(ul.getName());
		System.out.print("Premium: 
		System.out.println(prem);
		System.out.print("Premium: ");
		System.out.println(prem);
		System.out.print("Premium: ");
		System.out.println(prem);
		System.out.print("days to expiry: ");
		System.out.println(expiry);
		System.out.print("Strike: ");
		System.out.println(strike);
		System.out.print("Extrinsic Value: ");
		System.out.println(prem-(strike-spotPrice));
		System.out.print("spotPrice: ");
		System.out.println(spotPrice);
		System.out.print("zScore: ");
		System.out.println(zScore);
		System.out.print("zScore prob: ");
		System.out.println(std.density(firstzScore));
		System.out.print("zScore prob: ");
		System.out.println(std.cumulativeProbability(secondzScore));
		System.out.print("Sigma: ");
		System.out.println(sigma);
		System.out.print("E[Cd|Cd < S]: ");
		System.out.println(ECD);
		System.out.print("Expected Annual Return: ");
		System.out.println(expAnnRet);*/
		
		return expAnnRet;
	}

	public double getStrike() {
		return strike;
	}

	public void setStrike(double strike) {
		this.strike = strike;
	}

	public float getPrem() {
		return prem;
	}

	public void setPrem(float prem) {
		this.prem = prem;
	}

	public int getExpiry() {
		return expiry;
	}

	public void setExpiry(int expiry) {
		this.expiry = expiry;
	}

	public Stock getUl() {
		return ul;
	}

	public void setUl(Stock ul) {
		this.ul = ul;
	}

	public double getSpotPrice() {
		return spotPrice;
	}

	public void setSpotPrice(double spotPrice) {
		this.spotPrice = spotPrice;
	}

	public double getzScore() {
		return zScore;
	}

	public void setzScore(double zScore) {
		this.zScore = zScore;
	}

	public double getzScoreProb() {
		return zScoreProb;
	}

	public void setzScoreProb(double zScoreProb) {
		this.zScoreProb = zScoreProb;
	}

	public double getECD() {
		return ECD;
	}

	public void setECD(double eCD) {
		ECD = eCD;
	}

	public double getExtVal() {
		return extVal;
	}

	public void setExtVal(double extVal) {
		this.extVal = extVal;
	}

	public double getExpAnnRet() {
		return expAnnRet;
	}

	public void setExpAnnRet(double expAnnRet) {
		this.expAnnRet = expAnnRet;
	}
	
	public String getTicker() {
		return ul.getName();
	}
	
	public double getFirstECDTerm() {
		return firstECDTerm;
	}
	
	public double getSecondECDTerm() {
		return secondECDTerm;
	}
	
	public float getBid() {
		return bid;
	}
	
	public float getAsk() {
		return ask;
	}
	
	public float getLast() {
		return last;
	}
	
	public String getSymbol() {
		return symbol;
	}

	public static void setErrep(ErrorReporter errep) {
		Option.errep = errep;
	}

	public static void setLogWriter(FileWriter logWriter) {
		Option.logWriter = logWriter;
	}
	
	public double getStdErrorD() {
		return stdErrorD;
	}
	
	public float getDelta() {
		return delta;
	}

	public void setDelta(float delta) {
		this.delta = delta;
	}
}
