package com.gnb.dev.goliathnationalbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gnb.dev.goliathnationalbank.objects.Conversion;
import com.gnb.dev.goliathnationalbank.objects.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    final String RATES_URL = "http://gnb.dev.airtouchmedia.com/rates.json";
    final String TRANSACATIONS_URL = "http://gnb.dev.airtouchmedia.com/transactions.json";

    String ratesJsonString, transactionsJsonString;

    ArrayList<Conversion> conversionArray;
    HashMap<String, ArrayList<Transaction>> transactionHashMap; // Key - Product Name, Value - ArrayList with the product's transactions

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // check network availabilty
        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // download data
            new DownloadJsonData().execute();

        } else {
            Toast.makeText(getContext(), "No internet connectivity", Toast.LENGTH_LONG).show();
        }

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private class DownloadJsonData extends AsyncTask<Void, Void, String[]> {

        ProgressDialog progressDialog;
        String[] jsonStrings;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading...");
            progressDialog.setMessage("Downloading data from server...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                jsonStrings = new String[]{downloadUrl(RATES_URL), downloadUrl(TRANSACATIONS_URL)};
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonStrings;
        }

        @Override
        protected void onPostExecute(String[] jsonStrings) {

            if (jsonStrings[0] != null && jsonStrings[1] != null) {
                ratesJsonString = jsonStrings[0];
                transactionsJsonString = jsonStrings[1];
//                ratesJsonString = "[ { \"from\": \"EUR\", \"to\": \"USD\", \"rate\": \"1.359\" }, { \"from\": \"CAD\", \"to\": \"EUR\", \"rate\": \"0.732\" }, {\n" +
//                        "\"from\": \"USD\", \"to\": \"EUR\", \"rate\": \"0.736\" }, { \"from\": \"EUR\", \"to\": \"CAD\", \"rate\": \"1.366\" }, " +
//                        "{ \"from\": \"AUD\", \"to\": \"USD\", \"rate\": \"2.351\" } ] ";
//                transactionsJsonString = "[ { \"sku\": \"T2006\", \"amount\": \"10.00\", \"currency\": \"AUD\" }, { \"sku\": \"M2007\", \"amount\": \"34.57\",\n" +
//                        "\"currency\": \"CAD\" }, { \"sku\": \"R2008\", \"amount\": \"17.95\", \"currency\": \"USD\" }, { \"sku\": \"T2006\",\n" +
//                        "\"amount\": \"7.63\", \"currency\": \"EUR\" }, { \"sku\": \"B2009\", \"amount\": \"21.23\", \"currency\": \"USD\" } ] ";
            } else {
                Toast.makeText(getContext(), "There was a problem downloading the data", Toast.LENGTH_LONG).show();
            }


            try {

                // parse conversion rates JSON
                JSONArray jsonArray = new JSONArray(ratesJsonString);
                conversionArray = new ArrayList<Conversion>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String from = jsonObject.getString("from");
                    String to = jsonObject.getString("to");
                    String rate = jsonObject.getString("rate");

                    // save parsed conversion rate to an ArrayList
                    Conversion conversion = new Conversion(from, to, rate);
                    if (!conversionArray.contains(conversion)) {
                        conversionArray.add(conversion);
                    }
                }

                // parse transactions JSON
                jsonArray = new JSONArray(transactionsJsonString);
                transactionHashMap = new HashMap<String, ArrayList<Transaction>>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String product = jsonObject.getString("sku");
                    String amount = jsonObject.getString("amount");
                    String currency = jsonObject.getString("currency");

                    // save parsed transactions to a HashMap for fast access
                    Transaction transaction = new Transaction(product, amount, currency);
                    if (transactionHashMap.containsKey(product)) {
                        ArrayList<Transaction> auxArrayList = transactionHashMap.get(product);
                        auxArrayList.add(transaction);
                        transactionHashMap.put(product, auxArrayList);
                    } else { // product not existent in HashMap, add new entry
                        ArrayList<Transaction> auxArrayList = new ArrayList<Transaction>();
                        auxArrayList.add(transaction);
                        transactionHashMap.put(product, auxArrayList);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // set ListView contents after keys stored in the HashMap (Products)
            String[] keyArray = transactionHashMap.keySet().toArray(new String[transactionHashMap.keySet().size()]);
            ArrayAdapter<String> productsAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item_product, keyArray);
            ListView productListView = (ListView) getActivity().findViewById(R.id.listview_products);

            productListView.setAdapter(productsAdapter);

            // create new Activity for detailed view of the Product
            productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String product = parent.getItemAtPosition(position).toString();
                    Intent intent = new Intent(getActivity(), ProductActivity.class);
                    intent.putExtra("product", product);
                    intent.putExtra("transactions", transactionHashMap.get(product));
                    intent.putExtra("rates", conversionArray);
                    startActivity(intent);

                }
            });

            progressDialog.dismiss();
        }

        String downloadUrl(String url) throws IOException {
            InputStream is = null;
            String JsonString = null;
            try {
                URL targetUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.addRequestProperty("Accept", "application/json");
                conn.connect();

                int response = conn.getResponseCode();
                if (response == 200) {
                    is = conn.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader reader = new BufferedReader(new InputStreamReader((is)));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + '\n');
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    JsonString = buffer.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                is.close();
            }

            return JsonString;
        }
    }

}
