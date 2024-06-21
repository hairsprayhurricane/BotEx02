import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitPrice {
    public static String getPrice(){
        String url = "https://api.binance.com/api/v3/avgPrice?symbol=BTCUSDT";

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response2 = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response2.append(inputLine);
            }
            in.close();

            JSONObject myResponse = new JSONObject(response2.toString());
            String price = myResponse.getString("price");

            return price;

        } catch (Exception e) {
            e.printStackTrace();
            return "ОШИБКА";
        }
    }
}
