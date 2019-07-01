package android.marc.uebung3;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


class SessionConnection extends AsyncTask<String, Void, Void> {
    public int SessionID;

    protected int CreateNewSession() throws Exception
    {
        String urlstring = "http://pi-bo.dd-dns.de:8080/ContextAware/api/v1/session";

        JSONObject obj = new JSONObject();
        obj.put("name", "Carrerabahn");
        obj.put("beschreibung", "Fahrer");
        obj.put("tid", 45);

        String requestBody = obj.toString();
        URL url;
        InputStream inputStream;

        try{
            url = new URL(urlstring);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");

            OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"utf-8"));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            outputStream.close();

            if(conn.getResponseCode() <HttpURLConnection.HTTP_BAD_REQUEST) {
                return GetNewestSession();
            } else {
                throw new Exception("error while creating new session");
            }

        }

        catch (IOException e) {
            e.printStackTrace();
        }

        throw new Exception("error while creating new session");
    }
    protected int GetNewestSession() throws Exception
    {
        String urlstring = "http://pi-bo.dd-dns.de:8080/ContextAware/api/v1/session?tid=45";

        URL url;
        InputStream inputStream;

        try {
            url = new URL(urlstring);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }
            conn.disconnect();


            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder everything = new StringBuilder();
            String line;
            while( (line = bufferedReader.readLine()) != null) {
                everything.append(line);
            }
            String result = everything.toString();


            JSONArray jarr = new JSONArray(result);
            JSONObject jobj = jarr.getJSONObject(jarr.length() - 1);

            return jobj.getInt("sid");
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    protected Void doInBackground(String... params) {
        try{
            this.SessionID = CreateNewSession();
        }
        catch(Exception e){}
        return null;
    }
}

public class Connection extends AsyncTask<String, Void, Void> {

    protected Void doInBackground(String... params) {

        String urlstring = "http://pi-bo.dd-dns.de:8080/ContextAware/api/v1/data";
        String requestBody = params[0];
        URL url;
        InputStream inputStream;

        try{
            url = new URL(urlstring);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");

            OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"utf-8"));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            outputStream.close();

            if(conn.getResponseCode() <HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }
            conn.disconnect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                Log.d( "Ergebnis: ",temp);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
