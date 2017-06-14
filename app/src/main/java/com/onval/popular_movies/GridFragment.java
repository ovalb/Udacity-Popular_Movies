package com.onval.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.onval.popular_movies.Utilities.FetchUtilities;

import org.json.JSONObject;

import java.util.ArrayList;

public class GridFragment extends Fragment implements RecyclerAdapter.ItemClickInterface,
        Response.Listener<JSONObject>, Response.ErrorListener, RequestQueue.RequestFinishedListener<JSONObject> {

    private final int COL_SHOWN_IN_PORTRAIT = 3;
    private final int COL_SHOWN_IN_LANDSCAPE = 5;

    private Context mContext;
    private final String LOG_TAG = GridFragment.class.getSimpleName();

    private ArrayList<MovieDetail> mMovieDetailsArray = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mRecyclerAdapter;
    private  GridLayoutManager layoutManager;

    private int mPageToFetch = 0; //TODO: should this be really here?


    public GridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            layoutManager.setSpanCount(COL_SHOWN_IN_PORTRAIT);
        else
            layoutManager.setSpanCount(COL_SHOWN_IN_LANDSCAPE);

        if (mRecyclerAdapter != null) { //TODO: This shouldn't be here either
            FetchUtilities.sortMovies(mMovieDetailsArray, mContext);
            mRecyclerAdapter.notifyDataSetChanged();

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.grid_view);
        layoutManager = new GridLayoutManager(mContext, COL_SHOWN_IN_PORTRAIT);

        mRecyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        fetchNextPage(); //TODO: this shouldn't be here

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNextPage();
            }
        });

        return rootView;
    }

    // Use volley library to fetch movie data in JSON format
    // TODO: this definitely shouldn't be here
    private void fetchNextPage() {
        final RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.addRequestFinishedListener(this);

        ++mPageToFetch;

        String singlePageURL = FetchUtilities.createMoviesUri(mPageToFetch).toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                singlePageURL,  // string url
                null,           // optional JSONObject parameter (?)
                this,           //onResponse
                this            //onError
        );
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onResponse(JSONObject response) {
        FetchUtilities.addMoviesFromJSON(response, mMovieDetailsArray);
        FetchUtilities.sortMovies(mMovieDetailsArray, mContext);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, "onErrorResponse: an error has occurred: " + error.getMessage());
    }

    @Override
    public void onRequestFinished(Request<JSONObject> request) {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new RecyclerAdapter(mContext, mMovieDetailsArray, this);
        }

        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onItemClick(RecyclerAdapter.MoviePosterHolder holder) {
        int position = holder.getAdapterPosition();
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(MovieDetail.MOVIE_DETAILS_ID, mMovieDetailsArray.get(position));
        startActivity(intent);
    }
}

