package bnp.tests.mit_gr104;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

public class MBTAShellApp {
    public static void OutputResults(Map<String, Object> stopData, Map<String, Object> timeData) {
        System.out.println("The nearest arrival time is at " + timeData.get("arrival_time").toString());
        System.out.println("The nearest arrival time is in " + timeData.get("minutes") + " minutes");
        System.out.println("The name of the spot is " + stopData.get("name"));
        if (stopData.containsKey("platform_name") &&(stopData.get("platform_name")!=null)) {
            System.out.println("This is a subway stop on the  " + stopData.get("platform_name"));
        }
        if (stopData.containsKey("address") &&(stopData.get("address")!=null)) {
            System.out.println("This is a address is at  " + stopData.get("address"));
        }
          String isWheelchair =null;
          isWheelchair =stopData.get("wheelchair_boarding").toString();
          if (isWheelchair.equals("1")) {
              System.out.println("It is wheelchair accessable");
          }else {
              System.out.println("It is not wheelchair accessable");
          }
          
    }
    
    public static void main(String[] args) {
        String latitude = null;
        String longitude = null;
        MBTAData newData = new MBTAData();
        if (args.length<2){
            System.out.println("Missing paramaters");
            System.out.println("Add the latitude and longitude in that order");
            latitude = "42.2";
            longitude = "-71.18";
            
        }else {
         latitude = args[0].toString();
         longitude = args[1].toString();
        }
        System.out.println("Using the values latitude:" + latitude + " and longitude:" + longitude);
        Map<String, Object> stopData = null;
        try {
            newData.startConnection("https://api-v3.mbta.com");
            stopData = newData.acquireStopNearLatLong(latitude, longitude);
            Map<String, Object> curTimeResultAtributesMap = newData
                    .acquireNearestTimeAtStop(stopData.get("id").toString(), LocalDateTime.now().toString());
            OutputResults(stopData, curTimeResultAtributesMap);
            newData.closeConnection();
        } catch (ParseException e) {
            System.err.println("Badly formed JSON returned");
        } catch (URISyntaxException e) {
            System.err.println("bad coordinates");
        }
        catch (ClientProtocolException e) {
            System.err.println("Unable to connect");
        }catch (IOException e) {
            System.err.println("unknown error");
        } 

    }

}
