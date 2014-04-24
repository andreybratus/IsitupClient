package eu.spaziodati.isitupclient;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class Utils {
	public static void safeClose(Closeable closeable, boolean rethrow)
			throws IOException {

		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ex) {
				if (rethrow) {
					throw ex;
				} else {
					ex.printStackTrace();
				}
			}
		}
	}

	public static List<JSONObject> readUrlsToJSONRequests(File urls) {
		List<JSONObject> requests = null;
		try {
			List<String> urlList = FileUtils.readLines(urls, "UTF-8");
			requests = new ArrayList<JSONObject>();

			for (String line : urlList) {
				JSONObject request = new JSONObject();
				request.put("query", line.trim());
				requests.add(request);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading from File: " + e);
		}
		return requests;
	}

}
