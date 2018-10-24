package bnp.tests.mit_gr104;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.http.ParseException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        if (args.length<2){
            System.out.println("Missing paramaters");
        }
        String latitude = args[0].toString();
        String longitude = args[1].toString();
        MBTAData newData = new MBTAData();
        String stopId = null;
        Map<String, Object> stopData = null;
        try {
            newData.startConnection("https://api-v3.mbta.com");
            stopData = newData.acquireStopNearLatLong(latitude, longitude);
            Map<String, Object> curTimeResultAtributesMap = newData
                    .acquireNearestTimeAtStop(stopData.get("id").toString(), LocalDateTime.now().toString());
            System.out.println(curTimeResultAtributesMap.get("arrival_time"));
            System.out.println(stopData.get("id"));
            System.out.println(stopData.get("name"));
            System.out.println(curTimeResultAtributesMap.get("description"));
        } catch (IOException e) {
          
           
        } catch (ParseException e) {
           
        } catch (URISyntaxException e) {
        
        }

    }
}
