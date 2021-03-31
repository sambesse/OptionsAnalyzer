package sam.bullshit;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Put extends Option {

	public Put(float st, float b, float a, float la, float l, int d, Stock s, String symbol, float del) {
		super(st, b, a, la, l, d, s, symbol, del);
	}
	
	@Override
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
	
	@Override
	public double getPriceIndex() {
		if (strike >= spotPrice && Math.log(strike/spotPrice) <= (3 * stdErrorD)) {
			return zScoreProb * (strike - ECD);
		} else {
			return -42069;
		}
	}

}
