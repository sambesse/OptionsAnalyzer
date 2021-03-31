package sam.bullshit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.CSVPrinter;

public class Stock {
	public double[] priceHistory = new double[261];
	private String name;
	private double varOfError = -1;
	private ArrayList<Call> callList = new ArrayList<>();
	private ArrayList<Put> putList = new ArrayList<>();
	private static FileWriter logWriter;
	private double minCallInd, maxCallInd, minPutInd, maxPutInd;
	private double skew = -69;
	
	public Stock(String n) {
		name = n;
	}
	
	public Stock() {
		this("");
	}
	
	public void print() {
		System.out.println("Stock: " + name);
		for(int i = 0; i < priceHistory.length; i++) {
			System.out.println(priceHistory[i]);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public double getVarOfError() {
		if(varOfError != -1) {
			return varOfError;
		} else {
			double sum = 0;
			for(int t = 1; t < priceHistory.length; t++) {
				double logDiff = Math.log(priceHistory[t]/priceHistory[t-1]);
				sum += Math.pow(logDiff, 2);
			}
			varOfError = sum/260; //number of e terms (260)
			return varOfError;
		}
	}
	
	public void addCall(Call c) throws IOException {
		callList.add(c);
		c.calcExpVal();
	}
	
	public void addPut (Put p) throws IOException {
		putList.add(p);
		p.calcExpVal();
	}
	
	public double getSkew() throws IOException {
		if (skew != -69) {
			return skew;
		} else {
			double sumC = 0, sumP = 0;
			int cntC = 0, cntP = 0;
			if(callList.get(0).getPriceIndex() != -42069)
				maxCallInd = minCallInd = callList.get(0).getPriceIndex();
			for(int i = 0; i < callList.size(); i++) {
				if(callList.get(i).getPriceIndex() != -42069) {
					sumC += callList.get(i).getPriceIndex();
					cntC++;
				}
				if(callList.get(i).getPriceIndex() > maxCallInd && callList.get(i).getPriceIndex() != -42069)
					maxCallInd = callList.get(i).getPriceIndex();
				if((minCallInd == 0 || callList.get(i).getPriceIndex() < minCallInd) && callList.get(i).getPriceIndex() != -42069)
					minCallInd = callList.get(i).getPriceIndex();
			}
			if(putList.get(0).getPriceIndex() != -42069)
				maxPutInd = minPutInd = putList.get(0).getPriceIndex();
			for(int i = 0; i < putList.size(); i++) {
				if(putList.get(i).getPriceIndex() != -42069) {
					sumP += putList.get(i).getPriceIndex();
					cntP++;
				}
				if(putList.get(i).getPriceIndex() > maxPutInd && putList.get(i).getPriceIndex() != -42069)
					maxPutInd = putList.get(i).getPriceIndex();
				if((minPutInd == 0 || putList.get(i).getPriceIndex() < minPutInd) && putList.get(i).getPriceIndex() != -42069)
					minPutInd = putList.get(i).getPriceIndex();
			}
			logWriter.write("call price index: " + (sumC / cntC) + '\n' + "put price index: " + (sumP / cntP) + '\n');
			logWriter.write("call range: [" + minCallInd + ", " + maxCallInd + "]\nput range: [" + minPutInd + ", " + maxPutInd + "]\n");
			return (sumC / cntC) / (sumP / cntP);
		}
	}
	
	public void setLogWriter(FileWriter logWriter) {
		Stock.logWriter = logWriter;
	}
	
	public void printOptions (CSVPrinter printer) throws IOException {
		for (int i = 0; i < putList.size(); i++) {
    		if(Math.abs(putList.get(i).getzScore()) > 4) {
    			continue;
    		}
    		printer.printRecord(putList.get(i).getSymbol(), putList.get(i).getBid(), putList.get(i).getAsk(), putList.get(i).getLast(), putList.get(i).getPrem(), putList.get(i).getExpiry(), putList.get(i).getStrike(), putList.get(i).getSpotPrice()
    		, putList.get(i).getzScore(), putList.get(i).getzScoreProb(), putList.get(i).getStdErrorD(), putList.get(i).getECD(), putList.get(i).getDelta(), putList.get(i).getExtVal(), getSkew(), putList.get(i).getExpAnnRet());
		}
		
		for (int i = 0; i < callList.size(); i++) {
    		if(Math.abs(callList.get(i).getzScore()) > 4) {
    			continue;
    		}
    		printer.printRecord(callList.get(i).getSymbol(), callList.get(i).getBid(), callList.get(i).getAsk(), callList.get(i).getLast(), callList.get(i).getPrem(), callList.get(i).getExpiry(), callList.get(i).getStrike(), callList.get(i).getSpotPrice()
    		, callList.get(i).getzScore(), callList.get(i).getzScoreProb(), callList.get(i).getStdErrorD(), callList.get(i).getECD(), callList.get(i).getDelta(), callList.get(i).getExtVal(), getSkew(), callList.get(i).getExpAnnRet());
    	
		}
	}
	
	public int getOptionCnt() {
		return callList.size() + putList.size();
	}
	
}
