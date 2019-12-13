/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class RecyclerViewFragment extends Fragment {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 100;
    protected JSONObject mjo;

    private static RecyclerViewFragment rvf;
    protected static RecyclerView rv;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    //Determines what mode of display the app is in
    protected enum DisplayMode{
        POSTS,
        POST,
        USER
    }

    protected static DisplayMode curmode = DisplayMode.POSTS;

    protected static int critnum = -1;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;


    //Creates the fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        initDataset();
    }


    //Creates the view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new CustomAdapter(mDataset);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)

        return rootView;
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(20);
        mRecyclerView.smoothScrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    //Resets the data to what it should now be
    public static void reset(){
        rvf.initDataset();
        if(rvf.mRecyclerView!=null&&rv==null)rv=rvf.mRecyclerView;
    }

    //Returns the current dataset
    public static String[] getDataset(){
        return rvf.mDataset;
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data comes
     * from a local content provider or remote server.
     */
    private void initDataset() {
        rvf=this;
        if(mRecyclerView!=null){
            mRecyclerView.smoothScrollToPosition(0);
        }
        // Access the RequestQueue through your singleton class.
        RequestQueue requestQueue=Volley.newRequestQueue(this.getContext());

        mDataset = new String[DATASET_COUNT];

        if(curmode == DisplayMode.POSTS) {
            String url = "https://jsonplaceholder.typicode.com/posts/";

            for (int i = 0; i < DATASET_COUNT; i++) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url + (i + 1), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                mjo = response;
                                try {
                                    mDataset[mjo.getInt("id") - 1] = response.toString();
                                } catch (JSONException e) {
                                    Log.e(TAG, "onResponse: ", e);
                                }
                                Log.d(TAG, "onResponse: " + response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.e(TAG, "onErrorResponse: ", error);
                            }
                        });
                requestQueue.add(jsonObjectRequest);
            }
        }
        else if(curmode == DisplayMode.POST){
            String url = "https://jsonplaceholder.typicode.com/comments/";

            for (int i = 0; i < 5; i++) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url + (((critnum-1)*5) + (i + 1)), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                mjo = response;
                                try {
                                    mDataset[(mjo.getInt("id")-1)%5+1] = response.toString();
                                } catch (JSONException e) {
                                    Log.e(TAG, "onResponse: ", e);
                                }
                                Log.d(TAG, "onResponse: " + response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.e(TAG, "onErrorResponse: ", error);
                            }
                        });
                requestQueue.add(jsonObjectRequest);
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, "https://jsonplaceholder.typicode.com/users/"+(((critnum-1)/10)+1), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            mjo = response;
                            mDataset[0] = response.toString();
                            Log.d(TAG, "onResponse: " + response.toString());
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Log.e(TAG, "onErrorResponse: ", error);
                        }
                    });
            requestQueue.add(jsonObjectRequest);

        }
        else if(curmode == DisplayMode.USER){
            String url = "https://jsonplaceholder.typicode.com/posts/";

            for (int i = 0; i < 10; i++) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url + (((critnum-1)*10) + (i + 1)), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                mjo = response;
                                try {
                                    mDataset[(mjo.getInt("id")-1)%10+1] = response.toString();
                                } catch (JSONException e) {
                                    Log.e(TAG, "onResponse: ", e);
                                }
                                Log.d(TAG, "onResponse: " + response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.e(TAG, "onErrorResponse: ", error);
                            }
                        });
                requestQueue.add(jsonObjectRequest);
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, "https://jsonplaceholder.typicode.com/users/"+critnum, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            mjo = response;
                            mDataset[0] = response.toString();
                            Log.d(TAG, "onResponse: " + response.toString());
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Log.e(TAG, "onErrorResponse: ", error);
                        }
                    });
            requestQueue.add(jsonObjectRequest);
        }

    }
}
