package eu.spaziodati.isitupclient.cli;

import java.io.File;
import java.io.IOException;

import eu.spaziodati.isitupclient.IsitupHttpClientImpl;

public class IsitupClient {
	
	
	public static void main(String[] args) throws IOException{
		 
		if (args.length < 1) {printUsage(); return;}
		
		IsitupHttpClientImpl client = new IsitupHttpClientImpl();
		int numberOfRequests = (args.length == 2) ? Integer.parseInt(args[1]) : 0;
		client.checkLinks(checkExists(args[0]), numberOfRequests);
	}
	
	private static void printUsage(){
		System.out.println("usage: IsitupClient filename [-n #ofRequestInBatch]");
	}
	
	
	
	private static File checkExists(String name) {
		File file = new File(name);
		if (!file.exists()) {
			return null;
		}
		return file;
	}


}
