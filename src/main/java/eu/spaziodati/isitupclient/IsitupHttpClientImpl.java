package eu.spaziodati.isitupclient;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class IsitupHttpClientImpl {
	private final CloseableHttpClient fHttpClient;
	private final String fUrl;

	public IsitupHttpClientImpl() throws MalformedURLException {
		fHttpClient = HttpClients.createDefault();
		fUrl = "http://isitup.spaziodati.eu/";
	}

	public void checkLinks(File inputFile, int numberOfLines)
			throws IOException {
		if (inputFile == null) {
			System.err.println("Can't acces input file");
			return;
		}
		try {
			List<JSONObject> requestList = prepareRequests(inputFile,
					numberOfLines);

			for (int i = 0; i < requestList.size(); i++) {
				performRequest(requestList.get(i));
			}
		} catch (Exception e) {

		}
	}

	public void performRequest(JSONObject request) throws IOException {
		CloseableHttpResponse response = null;
		try {
			HttpPost post = new HttpPost(new URIBuilder(fUrl).setPath(
					"/reconcile").build());

			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("queries", request
					.toString()));
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			long startTime = System.nanoTime();
			response = fHttpClient.execute(post);
			long stopTime = System.nanoTime();
			System.out.println("\nTime executing: " + (stopTime - startTime)
					/ 1000000 + "ms\n");
			int status = response.getStatusLine().getStatusCode();

			if (status != 200) {
				System.err.println("\nRequest Failed, response code: " + status
						+ "\n");
				for (int i = 0; i < request.length(); i++) {
					System.err.printf("%-80s\t%-10s\n",
							request.getJSONObject("q" + i).get("query"),
							"failed");
				}
				return;
			}

			JSONObject jsonResponse = decode(response);
			if (request.length() != jsonResponse.length()) {
				throw new Exception(
						"Response and Request doesn't Match in Size");
			}

			for (int i = 0; i < jsonResponse.length(); i++) {
				JSONObject result = jsonResponse.getJSONObject("q" + i)
						.getJSONArray("result").optJSONObject(0);
				if (result != null) {
					System.out.printf("%-80s\t%-10d\n", result.getString("id")
							+ "\t", 1);
				} else {
					System.out.printf("%-80s\t%-10d\n",
							request.getJSONObject("q" + i).get("query"), 0);
				}
			}

		} catch (Exception e) {
			System.err.println("Error performing request" + e);
			return;
		} finally {
			Utils.safeClose(response, false);
		}
	}

	public List<JSONObject> prepareRequests(File urls, int numberPerRequest) {
		List<JSONObject> requests = Utils.readUrlsToJSONRequests(urls);
		List<JSONObject> chunkedRequests = new ArrayList<JSONObject>();
		if (numberPerRequest == 0) {
			numberPerRequest = requests.size();
		}
		while (!requests.isEmpty()) {
			JSONObject chunk = new JSONObject();
			for (int i = 0; i < numberPerRequest && !requests.isEmpty(); i++) {
				chunk.put("q" + i, requests.remove(0));
			}
			chunkedRequests.add(chunk);
		}
		return chunkedRequests;
	}

	private JSONObject decode(CloseableHttpResponse response)
			throws IOException {
		try {
			return new JSONObject(IOUtils.toString(response.getEntity()
					.getContent()));
		} catch (JSONException ex) {
			System.err.println("Error decoding server response: " + ex);
			return null;
		}
	}

}
