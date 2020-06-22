package com.example.popularmovies2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popularmovies2.data.Review;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    Review[] reviews;
    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        int posterLayout=R.layout.movie_review;
        LayoutInflater inflater= LayoutInflater.from(context);
        View posterView=inflater.inflate(posterLayout, parent,false);

        return new ReviewAdapter.ReviewViewHolder(posterView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        holder.reviewAuthorTextView.setText(reviews[position].getAuthor());
        holder.reviewContentTextView.setText(reviews[position].getContent());
    }

    @Override
    public int getItemCount() {
        if(reviews!=null)
            return reviews.length;
        else
            return 0;
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView reviewAuthorTextView;
        TextView reviewContentTextView;
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewAuthorTextView=itemView.findViewById(R.id.tv_review_author);
            reviewContentTextView=itemView.findViewById(R.id.tv_review_content);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
    public void setReviewsData(String jsonData) {
        Gson gson = new Gson();
        //JSONObject json=new JSONObject(jsonData);
        JsonObject jsonObject = new Gson().fromJson(jsonData, JsonObject.class);
        JsonElement results = jsonObject.get("results");
        Log.i("ReviewAdapter",results.toString());

        reviews = gson.fromJson(results.toString(), Review[].class);
        Log.i("Review Results", results.toString());
        notifyDataSetChanged();
    }
    public void getReviewsData(Review[] reviews){
        this.reviews = reviews;
    }
}
