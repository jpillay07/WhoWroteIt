package com.codelabs.whowroteit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = findViewById(R.id.bookInput);
        mTitleText = findViewById(R.id.titleText);
        mAuthorText = findViewById(R.id.authorText);
    }

    public void searchBooks(View view) {
        // Get the search string from the input field.
        String queryString = mBookInput.getText().toString();

        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if(inputManager != null){
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;

        if(connManager != null){
            networkInfo = connManager.getActiveNetworkInfo();
        }

        if(networkInfo != null && networkInfo.isConnected() && queryString.length() != 0){
            new FetchBook(mTitleText, mAuthorText).execute(queryString);

            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);
        }
        else{
            if(queryString.length() == 0){
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_search_term);
            }
            else{
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_internet);
            }
        }


    }

    public class FetchBook extends AsyncTask<String, Void, String>{

        private WeakReference<TextView> mTitleText;
        private WeakReference<TextView> mAuthorText;

        public FetchBook(TextView titleText, TextView authorText){
            this.mTitleText = new WeakReference<>(titleText);
            this.mAuthorText = new WeakReference<>(authorText);
        }

        @Override
        protected String doInBackground(String... strings) {
            return NetworkUtils.getBookInfo(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                JSONObject jsonObject = new JSONObject(s);
                JSONArray itemsArray = jsonObject.getJSONArray("items");

                int i =0;
                String title = null;
                String authors = null;

                while(i < itemsArray.length() && (title == null && authors == null)){
                    //Get the current item information
                    JSONObject book = itemsArray.getJSONObject(i);
                    JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                    //try to get the author and title, catch if either field is empty and move on

                    try{
                        title = volumeInfo.getString("title");
                        authors = volumeInfo.getString("authors");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    i++;
                }

                if(title !=null && authors != null){
                    mTitleText.get().setText(title);
                    mAuthorText.get().setText(authors);
                }
                else{
                    mTitleText.get().setText(R.string.no_results);
                    mAuthorText.get().setText("");
                }

            }
            catch(JSONException e){
                // If onPostExecute does not receive a proper JSON string,
                // update the UI to show failed results.
                mTitleText.get().setText(R.string.no_results);
                mAuthorText.get().setText("");
                e.printStackTrace();
            }
        }
    }
}
