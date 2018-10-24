package bnp.tests.mit_gr104;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MBTAData implements MBTADataInterface {
    protected String errorMessage = "None";
    protected String baseRequestUrl = null;
    protected CloseableHttpClient httpclient;

    /*
     * This function only adds a user-agent but could also add more
     */
    protected void setupHeaders(HttpGet httpget) {
        httpget.addHeader("User-Agent", "Mozilla/5.0");
    }

    /*
     * This function gets the JSONObject named "data" from the return code
     */
    private JSONArray getDataElements(CloseableHttpResponse responseToUse) throws ParseException, IOException {
        // Initialize defaults
        JSONArray currentMBTAData = null;
        HttpEntity entity = responseToUse.getEntity();
        String content = EntityUtils.toString(entity);
        JSONObject jsonData = new JSONObject(content);
        currentMBTAData = jsonData.getJSONArray("data");
        return currentMBTAData;

    }

    /*
     * This function returns a map of the attributes
     */
    private Map<String, Object> getAttributesMap(JSONObject curResults) {
        JSONObject curAttributes = curResults.getJSONObject("attributes");
        return curAttributes.toMap();
    }

    /*
     * This function returns the closest time from the given stop
     */
    public Map<String, Object> acquireNearestTimeAtStop(String stopId, String curTime)
            throws ClientProtocolException, URISyntaxException, ParseException, IOException {
        // Initialize defaults
        Map<String, Object> rtnValue = null;
        String arrivalTimeStr = null;
        Map<String, Object> curTimeResultAtributesMap = null;
        boolean notFound = false;

        LocalDateTime localDateTime = LocalDateTime.now();
        JSONArray currentMBTAData;
        // This next section adds the query parameters to the URI. This could be
        // broken out in a larger version
        URIBuilder uriRequest = new URIBuilder(baseRequestUrl + "/schedules");
        uriRequest.addParameter("filter[stop]", stopId);
        uriRequest.addParameter("sort", "arrival_time");
        HttpGet httpget = new HttpGet(uriRequest.build());
        setupHeaders(httpget);
        // make request
        CloseableHttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() != 200)
            throw new ClientProtocolException("Error returned from site");
        currentMBTAData = getDataElements(response);
        int idx = 0;
        while ((notFound == false) && (idx < currentMBTAData.length())) {
            JSONObject curTimeResult = currentMBTAData.getJSONObject(idx);
            curTimeResultAtributesMap = getAttributesMap(curTimeResult);
            arrivalTimeStr = (String) curTimeResultAtributesMap.get("arrival_time");
            //
            //  Very briefly, this will return the first time that is still in the future
            //
            LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            if (arrivalTime.isAfter(localDateTime)) {
                notFound = true;
                long diffInMinutes = java.time.Duration.between(localDateTime, arrivalTime).toMinutes();
                curTimeResultAtributesMap.put("minutes", Long.toString(diffInMinutes));
                rtnValue = curTimeResultAtributesMap;
            }
            idx++;
        }
        if (rtnValue == null)
            throw new ClientProtocolException("Unable to find stop");

        return rtnValue;
    }

    //
    //  Initialize a connection.  This could be used in the future to keep connections open
    //
    public void startConnection(String baseUrl) {
        baseRequestUrl = baseUrl;
        httpclient = HttpClients.createDefault();
    }
    //
    //   Close a connection.  
    //
    public void closeConnection() {
        try {
            httpclient.close();
        } catch (IOException e) {

        }
    }
    //
    //  Get map of attributes for closer stop
    //
    public Map<String, Object> acquireStopNearLatLong(String latitude, String longitude)
            throws URISyntaxException, ParseException, ClientProtocolException, IOException {
        //Set default values
        Map<String, Object> rtnValue = null;
        JSONObject curStopsResult = null;
        Map<String, Object> curStopsAtributesMap = null;

        JSONArray currentMBTAData;
        httpclient = HttpClients.createDefault();
        URIBuilder uriRequest = new URIBuilder(baseRequestUrl + "/stops");
        uriRequest.addParameter("filter[latitude]", latitude);
        uriRequest.addParameter("filter[longitude]", longitude);
        uriRequest.addParameter("filter[radius]", "0.2"); // this is a nice radius (2 miles)
        uriRequest.addParameter("page[limit]", "1"); // we only need the closest one
        HttpGet httpget = new HttpGet(uriRequest.build());
        setupHeaders(httpget);
        CloseableHttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() != 200)
            throw new ClientProtocolException("Error returned from site");
        currentMBTAData = getDataElements(response);
        if (currentMBTAData.length() > 0) {
            curStopsResult = currentMBTAData.getJSONObject(0);
            curStopsAtributesMap = getAttributesMap(curStopsResult);
            String stopId = curStopsResult.getString("id");
            curStopsAtributesMap.put("id", stopId);
            rtnValue = curStopsAtributesMap;

        }

        return rtnValue;
    }

}
