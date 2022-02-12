package com.example.project.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.example.project.Model.StoryModel;
import com.example.project.Model.UserData;
import com.example.project.Model.UserStories;
import com.example.project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder> {

    ArrayList<StoryModel> list;
    Context context;

    public StoryAdapter(ArrayList<StoryModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_rv, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        StoryModel storyModel = list.get(position);
        if (storyModel.getStories().size() > 0) {
            UserStories lastStory = storyModel.getStories().get(storyModel.getStories().size() - 1);
            Picasso.get()
                    .load(lastStory.getImage())
                    .into(holder.profileImage);
            holder.imageBorder.setPortionsCount(storyModel.getStories().size());
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(storyModel.getStoryBy()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData userData = snapshot.getValue(UserData.class);

                    holder.name.setText(userData.getName());


                    holder.profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<MyStory> myStories = new ArrayList<MyStory>();

                            for (UserStories stories : storyModel.getStories()) {
                                myStories.add(new MyStory(
                                        stories.getImage()
                                ));
                            }
                            new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                                    .setStoriesList(myStories) // Required
                                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                    .setTitleText(userData.getName()) // Default is Hidden
                                    .setSubtitleText("") // Default is Hidden
                                    .setTitleLogoUrl(userData.getProfilePhoto()) // Default is Hidden
                                    .setStoryClickListeners(new StoryClickListeners() {
                                        @Override
                                        public void onDescriptionClickListener(int position) {
                                            //your action
                                        }

                                        @Override
                                        public void onTitleIconClickListener(int position) {
                                            //your action
                                        }
                                    }) // Optional Listeners
                                    .build() // Must be called before calling show method
                                    .show();
                        }
//
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        CircularStatusView imageBorder;
        ImageView profileImage;
        TextView name;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageBorder = itemView.findViewById(R.id.storyImageBorder);
            profileImage = itemView.findViewById(R.id.storyImage);
            name = itemView.findViewById(R.id.name);
        }
    }
}
