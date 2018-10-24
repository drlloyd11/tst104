package bnp.tests.mit_gr104;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

public interface MBTADataInterface {
  
    void startConnection(String baseUrl);

    void closeConnection();

    Map<String, Object> acquireStopNearLatLong(String latitude, String longitude)
            throws URISyntaxException, ParseException, ClientProtocolException, IOException;

    Map<String, Object> acquireNearestTimeAtStop(String stopId, String curTime)
            throws URISyntaxException, ParseException, IOException;

}
