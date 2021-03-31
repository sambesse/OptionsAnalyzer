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
	protected double strike;
	protected float prem;
	protected float last;
	protected float bid;
	protected float ask;
	protected int expiry;
	protected Stock ul;
	protected double spotPrice;
	protected double zScore;
	protected double zScoreProb;
	protected double stdErrorD;
	protected double firstECDTerm;
	protected double secondECDTerm;
	protected double ECD;
	protected double extVal;
	protected double expAnnRet;
	protected String symbol;
	protected static ErrorReporter errep;
	protected static FileWriter logWriter;
	protected float delta;
	protected static int tossed;
	protected double zMean0, zMean1, zMean;
	

	protected double fScore, fScoreNum, fScoreDen;
	protected double eBananas0 = 0, eBananas1 = 0;
	
	protected double pi;
	
	

	

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
		double varOfError = ul.getVarOfError();
		double stdError1 = Math.sqrt(varOfError);
		stdErrorD = stdError1 * Math.sqrt(expiry);
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
		
		logWriter.write("option " + symbol + " calculated" + '\n');
		logWriter.write("zScore: " + zScore + '\n');
		logWriter.write("zScoreProb: " + zScoreProb + '\n');
		logWriter.write("std Error D: " + stdErrorD + '\n');
		logWriter.write("ECD: " + ECD + '\n');
		logWriter.write("expected annual return: " + expAnnRet + '\n');
		
		return expAnnRet;
	}
	
	public double getPriceIndex() {
		return 0;
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
