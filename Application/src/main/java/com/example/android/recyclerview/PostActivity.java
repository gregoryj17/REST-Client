package com.example.android.recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "RecyclerViewFragment";

    public String title = "";
    public String mess = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);
        initializeComponents();
        Intent intent = getIntent();


        //Toast.makeText(getApplicationContext(),Integer.toString(ID),Toast.LENGTH_LONG).show();

        ((EditText)findViewById(R.id.etNoteTitle)).setText(title);
        ((EditText)findViewById(R.id.etNoteContent)).setText(mess);

    }
    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.btnDelete).setOnClickListener(this);


    }

    //Listens for posting/cancelling
    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.btnSave:
                createNewNote();
                break;
            //If delete note, call deleteNote()
            case R.id.btnDelete:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            //This shouldn't happen
            default:
                break;
        }
    }


    //Create a new post request with info from fields
    void createNewNote(){
        String resp;
        try {
            String URL = "https://jsonplaceholder.typicode.com/posts";
            final JSONObject jsonBody = new JSONObject();

            jsonBody.put("title", ((EditText)findViewById(R.id.etNoteTitle)).getText());
            jsonBody.put("body", ((EditText)findViewById(R.id.etNoteContent)).getText());
            jsonBody.put("userId", "12");

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "onResponse: "+response.toString());
                    Toast.makeText(getApplicationContext(), "Response:  " + response.toString(), Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "onErrorResponse: ", error);
                }
            }) /*{
                @Override
                protected Map<String, String> getParams(){
                    Map<String, String> params = new HashMap<>();
                    params.put("title", title);
                    params.put("body", mess);
                    params.put("userId", "12");
                    return params;
                }
            }*/;
            Volley.newRequestQueue(this).add(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


}
