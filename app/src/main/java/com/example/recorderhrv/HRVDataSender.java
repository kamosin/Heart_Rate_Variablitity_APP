package com.example.recorderhrv;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HRVDataSender{

    Long epochDate;
    Integer beatsPerMinute;
    Integer MSSD;
    Integer userId;

    RequestQueue requestQueue;

    String responseData;

    private JSONObject prepareJson(Long epochDate, Integer beatsPerMinute, Integer MSSD, Integer userId) throws JSONException {

        // Tworzenie obiektu JSON
        JSONObject jsonObject = new JSONObject();

        // Dodawanie danych do obiektu JSON
        jsonObject.put("epochDate", epochDate);
        jsonObject.put("beatsPerMinute", beatsPerMinute);
        jsonObject.put("MSSD", MSSD);
        jsonObject.put("userId", userId);

        // Zwracanie JSON jako string
        return jsonObject;
    }
    public void send() throws JSONException {
        JSONObject jsonToSend = prepareJson(111122233L, 33, 15, 4324);
        //sendRequest(context, jsonToSend);
        postDataUsingVolley(jsonToSend);
    }



//    private void sendRequest(Context context, JSONObject json){
//        String url = "http://192.168.50.121:8082/heartdeat-data";
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, json,
//                new Response.Listener<JSONObject>(){
//                    @Override
//                    public void onResponse
//                }
////                response -> Toast.makeText(context, "Success", Toast.LENGTH_LONG).show(),
////                error -> Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()){
////            //Add parameters to the request
////
////        };
//
//        requestQueue = Volley.newRequestQueue(context);
//        requestQueue.add(stringRequest);
//    }

    private void postDataUsingVolley(JSONObject jsonBody) {
        // on below line specifying the url at which we have to make a post request
        String url = "http://192.168.50.121:8082/heartdeat-data";
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.context);
        // making a string request on below line.
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // setting response to text view.
                responseData = ("Response from the API is :" + response);
                // displaying toast message.
                Toast.makeText(MainActivity.context, "Data posted succesfully..", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // handling error on below line.
                Toast.makeText(MainActivity.context, "Fail to get response..", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        // adding request to queue to post the data.
        queue.add(request);
    }


}
