package sam.bullshit;

import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import kong.unirest.*;
import java.time.*;
import org.json.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.math3.*;
import org.apache.commons.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
	final static String host = "https://api.tradier.com/v1/";
	final static String charset = "UTF-8";
	final static String api_key = "LSBpzvGd9DCWt2m646UwTkaCv9ym";
	static String[] tckr;
	static String tckrs = "";
	final static String[] expiryDates = {"2021-03-05", "2021-03-12", "2021-03-19", "2021-03-26", "2021-04-16", "2021-05-21"};
	final static int[] tDates = {4, 9, 13, 18, 33, 58};
	final static String histDate = "2020-08-24";
	static List<Option> optionList;
	static Stock currStock;
	static ErrorReporter errep;
	static FileWriter logWriter;
	static int reqCnt = 0;
	final static String[] HEADERS = {"Name", "Bid", "Ask", "Last", "Premium", "Days to Expiry", "Strike", "Spot Price", "Z-Score", "Z-Score Prob.", "Std Error D", "E[Cd|Cd < S]", "Delta", "Extrensic Value", "Expected Annual Return", 
			"F Score", "F Score numerator", "F Score Denominator", "z Mean 0", "z Mean total", "z Mean 1", "new RMSE", "old RMSE"};
	
    public static void main( String[] args ) throws Exception
    {
    	
    	long start = System.currentTimeMillis();
    	errep = new ErrorReporter();
    	logWriter = new FileWriter("log.txt");
    	Option.setLogWriter(logWriter);
    	Option.setErrep(errep);
    	File tckrFile = new File("OptionsTckrList.txt");
    	Scanner sc = new Scanner(tckrFile);
    	sc.useDelimiter("|");
    	List<String> tckrList = new LinkedList<String>();
    	for(;sc.hasNextLine();) {
    		String newTckr = new String();
    		String newLine = sc.nextLine();
    		boolean skip = false;
    		if(newLine == null) {
    			continue;
    		}
    		for(int p = 0;  newLine.charAt(p) != '|'; p++) {
    			newTckr += newLine.charAt(p);
    			if (newLine.charAt(p) == '$')
    				skip = true;
    		}
    		if (skip) {
    			
    		} else {
    			tckrList.add(newTckr);
    		}
    	}
    	tckr = new String[tckrList.size()];
    	for(int q = 0; q < tckrList.size(); q++) {
    		tckr[q] = tckrList.get(q);
    	}
    	
    	FileWriter out = new FileWriter("stonks.csv");
    	CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS));
    	optionList = new ArrayList<Option>();
    	for (int i = 0; i < tckr.length; i++) {
    		HttpResponse <JsonNode> histResponse = Unirest.get(host + "markets/history")
    				.header("Authorization", "Bearer " + api_key)
    				.header("Accept", "application/json")
    				.queryString("symbol", tckr[i])
    				.queryString("start", histDate)
    				.asJson();
    		reqCnt++;
    		logWriter.write("hist req for " + tckr[i] + ": " +  histResponse.getStatus() + '\n');
    		if(histResponse.getStatus() != 200) {
				continue;
			}
    		//System.out.println(histResponse.getBody().toPrettyString());
    		currStock = new Stock(tckr[i]);
    		JSONObject rootHistObj = new JSONObject(histResponse.getBody().toString());
    		//System.out.println(histResponse.getHeaders().toString());
    		//System.out.println(histResponse.getBody().toString());
    		JSONArray histArray;
    		try { 
    			histArray = rootHistObj.getJSONObject("history").getJSONArray("day");
    		} catch (Exception e) {
    			errep.reportError(e, "single day of history for: " + tckr[i]);
    			continue;
    		}
    		System.out.println(histArray.length());
    		if(histArray.length() != 130) { 
    			errep.reportError("price history less than 130 days for " + tckr[i]);
    			logWriter.write("tossed " + tckr[i] + " for price history too short" + '\n');
    			continue;
    		}
    		for(int j = 0; j < histArray.length(); j++) {
    			JSONObject dataPoint = histArray.getJSONObject(j);
    			currStock.priceHistory[2*j] = dataPoint.getFloat("open");
    			currStock.priceHistory[(2*j)+1] = dataPoint.getFloat("close");
    		}
    		HttpResponse <JsonNode> priceResponse = Unirest.get(host + "markets/quotes")
    				.header("Authorization", "Bearer " + api_key)
    				.header("Accept", "application/json")
    				.queryString("symbols", tckr[i])
    				.asJson();
    		reqCnt++;
    		logWriter.write("hist req for " + tckr[i] + ": " +  priceResponse.getStatus() + '\n');
    		if(priceResponse.getStatus() != 200) {
				errep.reportError("failed to get spot price for " + tckr[i]);
				continue;
			}
    		
    		//System.out.println(priceResponse.getBody().toPrettyString());
    		JSONObject priceObj = new JSONObject(priceResponse.getBody().toString());
    		currStock.priceHistory[260] = priceObj.getJSONObject("quotes").getJSONObject("quote").getFloat("last");
    		logWriter.write("added spot price for " + tckr[i] + '\n');
    		//currStock.print();
    		for(int e = 0; e < expiryDates.length; e++) {
    			HttpResponse <JsonNode> optResponse = Unirest.get(host + "markets/options/chains")
    				.header("Authorization", "Bearer " + api_key)
    				.header("Accept", "application/json")
    				.queryString("symbol", tckr[i])
    				.queryString("expiration", expiryDates[e])
    				.queryString("greeks", true)
    				.asJson();
    			reqCnt++;
    			logWriter.write("opt req for " + tckr[i] + "on: "+ expiryDates[e] + ": " +  optResponse.getStatus() + '\n');
    			if(optResponse.getStatus() != 200) {
    				continue;
    			}
    			//System.out.println(optResponse.getBody().toPrettyString());
    			JSONObject rootOptObj = new JSONObject(optResponse.getBody().toString());
    			String failString = "{\"options\":null}";
    			if(optResponse.getBody().toString().equals(failString)) {
    				errep.reportError(new Error("no options for: " + tckr[i]));
    				continue;
    			}
    			JSONArray optArray = rootOptObj.getJSONObject("options").getJSONArray("option");
    			for(int j = 0; j < optArray.length(); j++) {
    				JSONObject opt = optArray.getJSONObject(j);
    				float delta;
    				try {
    					JSONObject greeks = opt.getJSONObject("greeks");
    					delta = greeks.getFloat("delta");
    				} catch(Exception exc) {
    					errep.reportError(exc, "greeks req failed");
    					delta = -420;
    				}
    				String bungusTest = opt.getString("symbol");
    				int dateLength = 0;
    				
    				for(int g = 0; g < bungusTest.length(); g++) {
    					if(isDigit(bungusTest.charAt(g)))
    						dateLength++;
    					if(bungusTest.charAt(g) == 'P')
    						break;
    				}
    				if(dateLength > 6) {
    					continue;
    				}
    				
    				if(opt.getString("option_type").equals("call") || !opt.getString("expiration_type").equals("standard")) 
    					continue;
    				if(opt.get("last").equals(null) || opt.getFloat("bid") == 0) {
    					continue;
    				} else if((opt.getFloat("bid") + opt.getFloat("ask"))/2 > opt.getFloat("last")) {
    					optionList.add(new Option(opt.getFloat("strike"), opt.getFloat("bid"), opt.getFloat("ask"), opt.getFloat("last"), opt.getFloat("last"), tDates[e], currStock, opt.getString("symbol"), delta)); 				
    					logWriter.write("added option: " + opt.getString("symbol") + '\n');
    					System.out.print("added option: " + opt.getString("symbol") + '\n');
    				} else {
    					optionList.add(new Option(opt.getFloat("strike"), opt.getFloat("bid"), opt.getFloat("ask"), opt.getFloat("last"), (opt.getFloat("bid") + opt.getFloat("ask"))/2, tDates[e], currStock, opt.getString("symbol"), delta));
    					logWriter.write("added option: " + opt.getString("symbol") + '\n');
    					System.out.print("added option: " + opt.getString("symbol") + '\n');
    				}
    			}
    		}
    	}
    	
    	for(int i = 0; i < optionList.size(); i++) {
    		optionList.get(i).calcExpVal();
    		if(Math.abs(optionList.get(i).getzScore()) > 4) {
    			continue;
    		}
    		printer.printRecord(optionList.get(i).getSymbol(), optionList.get(i).getBid(), optionList.get(i).getAsk(), optionList.get(i).getLast(), optionList.get(i).getPrem(), optionList.get(i).getExpiry(), optionList.get(i).getStrike(), optionList.get(i).getSpotPrice()
    		, optionList.get(i).getzScore(), optionList.get(i).getzScoreProb(), optionList.get(i).getStdErrorD(), optionList.get(i).getECD(), optionList.get(i).getDelta(), optionList.get(i).getExtVal(), optionList.get(i).getExpAnnRet(), optionList.get(i).getfScore(), optionList.get(i).getfScoreNum(), optionList.get(i).getfScoreDen(), optionList.get(i).getzMean0()
    		, optionList.get(i).getzMean1(), optionList.get(i).getzMean(), optionList.get(i).geteBananas1(), optionList.get(i).geteBananas0());
    	}
    	
    	long end = System.currentTimeMillis();
		System.out.print("runtime: ");
		System.out.println(end - start);
		logWriter.write("runtime: " + (end - start) + '\n');
		System.out.println("requests made: " + reqCnt);
		logWriter.write("requests made: " + reqCnt + '\n');
		logWriter.write("options tossed: " + Option.getTossed());
		errep.generateReport();
		System.out.println("Error Report Generated");
		logWriter.close();
	
    }
    
    private static boolean isDigit(char c) {
    	return (c=='0') || (c=='1') || (c=='2') || (c=='3') || (c=='4') || (c=='5') || (c=='6') || (c=='7') || (c=='8') || (c=='9');
    }
}

