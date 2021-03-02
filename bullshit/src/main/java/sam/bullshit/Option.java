package sam.bullshit;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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
	private static int tossed;
	private double zMean0, zMean1, zMean;
	

	private double fScore, fScoreNum, fScoreDen;
	private double eBananas0 = 0, eBananas1 = 0;
	
	private double pi;
	
	

	

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
		double[] logDiff0 = new double[230];
		double[] logDiff1 = new double[30];
		for(int t = 1; t < ul.priceHistory.length; t++) {
			double logDiff = Math.log(ul.priceHistory[t]/ul.priceHistory[t-1]);
			if (t < 231) {
				logDiff0[t-1] = logDiff; 
			} else {
				logDiff1[t-231] = logDiff;
			}
			sum += Math.pow(logDiff, 2);
		}
		//beginning of brown 4chan test
		Arrays.sort(logDiff0);
		Arrays.sort(logDiff1);
		double eMed0 = (logDiff0[115] + logDiff0[116]) / 2;
		double eMed1 = (logDiff1[15] + logDiff1[16]) / 2;
		double[] z0 = new double[230]; // declaring z terms for both groups
		double[] z1 = new double [30];
		
		for(int i = 0; i < 230; i++) {//defining z terms of group 0
			z0[i] = Math.abs(logDiff0[i] - eMed0);
		}
		for (int i = 0; i < 30; i++) {//defining z terms of group 1 
			z1[i] = Math.abs(logDiff1[i] - eMed1);
		}
		
		
		double sum0 = 0;
		double sum1 = 0;
		for (int i = 0; i < 230; i++) {
			sum0 += z0[i];
		}
		for(int i = 0; i < 30; i++) {
			sum1 += z1[i];
		}
		zMean0 = sum0/230;//calculating means
		zMean1 = sum1/30;
		zMean = (sum0 + sum1) / 260;
		
		fScoreNum = 230 * Math.pow(zMean0 - zMean, 2) + 30 * Math.pow(zMean1 - zMean, 2);
		//defining zScoreDen
		fScoreDen = 0;
		for(int i = 0; i < 230; i++) {
			fScoreDen+= Math.pow(z0[i] - zMean0, 2);
		}
		for(int i = 0; i < 30; i++) {
			fScoreDen+= Math.pow(z1[i] - zMean1, 2);
		}
		//combining terms to define fScore
		fScore = 258 * fScoreNum / fScoreDen;
		
		
		for(int i = 0; i < 230; i++) {
			eBananas0+= Math.pow(logDiff0[i], 2);
		}
		eBananas0 /= 230;
		for(int i = 0; i < 30; i++) {
			eBananas1+= Math.pow(logDiff1[i], 2);
		}
		eBananas1 /= 30;
		if(fScore > 6.7 && eBananas1 > eBananas0) {
			stdErrorD = -420;
			zScore = -420;
			zScoreProb = -420;
			ECD = -420;
			expAnnRet = -420;
			logWriter.write("threw out" + symbol + "\r\n");
			tossed++;
			return 0;
		}
		
		
		double varOfError = sum/260; //number of e terms (260)
		double stdError1 = Math.sqrt(varOfError);
		stdErrorD = stdError1 * Math.sqrt(expiry);
		//sigma = Math.sqrt(stdError);
		NormalDistribution std = new NormalDistribution(0, 1);
		zScore = Math.log(strike / spotPrice) / stdErrorD;
		zScoreProb = std.cumulativeProbability(Math.log(strike / spotPrice) / stdErrorD);
		firstECDTerm = std.cumulativeProbability((Math.log(strike / spotPrice) - (expiry * varOfError / 2)) / stdErrorD);
		secondECDTerm = std.cumulativeProbability((Math.log(strike / spotPrice) + (expiry * varOfError / 2)) / stdErrorD);
		ECD = spotPrice * firstECDTerm / secondECDTerm;
		pi = (prem + zScoreProb*(ECD - strike))/(strike-prem);
		expAnnRet = Math.pow((1 + pi), (260/expiry)) - 1;
		double intVal;
		if (strike > spotPrice) {
			intVal = strike - spotPrice;
		} else {
			intVal = 0;
		}
		extVal = prem - intVal;
		/*for(int q = 0; q < 261; q++) {//displaying price history in log for debugging purposes
			logWriter.write("ph: " + ul.priceHistory[q]);
			logWriter.write(" ");
		}
		logWriter.write('\n');*/
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

	public double getPi() {
		return pi;
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
	
	public static int getTossed() {
		return tossed;
	}
	
	public double getzMean0() {
		return zMean0;
	}

	public void setzMean0(double zMean0) {
		this.zMean0 = zMean0;
	}

	public double getzMean1() {
		return zMean1;
	}

	public void setzMean1(double zMean1) {
		this.zMean1 = zMean1;
	}

	public double getzMean() {
		return zMean;
	}

	public void setzMean(double zMean) {
		this.zMean = zMean;
	}

	public double getfScore() {
		return fScore;
	}

	public void setfScore(double fScore) {
		this.fScore = fScore;
	}

	public double getfScoreNum() {
		return fScoreNum;
	}

	public void setfScoreNum(double fScoreNum) {
		this.fScoreNum = fScoreNum;
	}

	public double getfScoreDen() {
		return fScoreDen;
	}

	public void setfScoreDen(double fScoreDen) {
		this.fScoreDen = fScoreDen;
	}

	public double geteBananas0() {
		return eBananas0;
	}

	public void seteBananas0(double eBananas0) {
		this.eBananas0 = eBananas0;
	}

	public double geteBananas1() {
		return eBananas1;
	}

	public void seteBananas1(double eBananas1) {
		this.eBananas1 = eBananas1;
	}
}
