package com.example.project.Fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.project.Adapter.PostAdapter;
import com.example.project.Adapter.StoryAdapter;
import com.example.project.Model.PostModel;
import com.example.project.Model.StoryModel;
import com.example.project.Model.UserStories;
import com.example.project.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
RecyclerView storyRv;
ShimmerRecyclerView dashbordRV;
ArrayList<StoryModel> list;
ArrayList<PostModel> postModel;
FirebaseAuth auth;
FirebaseDatabase database;
FirebaseStorage storage;
        ProgressDialog dialog;

    CircleImageView  storyImage;
ActivityResultLauncher<String> galleryLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
         dialog = new ProgressDialog(getContext());
       storyImage = view.findViewById(R.id.StoryImage);
        dashbordRV = view.findViewById(R.id.dashbordRV);
        dashbordRV.showShimmerAdapter();

dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
dialog.setTitle("Story Uploading");
dialog.setCancelable(false);
        //Story
        storyRv = view.findViewById(R.id.storyRV);
        list = new ArrayList<>();

        StoryAdapter adapter = new StoryAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        storyRv.setLayoutManager(linearLayoutManager);
        storyRv.setNestedScrollingEnabled(false);
        storyRv.setAdapter(adapter);

       database.getReference()
               .child("stories").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   list.clear();
                   for (DataSnapshot storySnapshot : snapshot.getChildren()){
                       StoryModel storyModel = new StoryModel();
                       storyModel.setStoryBy(storySnapshot.getKey());
                       storyModel.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                       ArrayList<UserStories> stories = new ArrayList<>();
                       for (DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()){
                           UserStories userStories = snapshot1.getValue(UserStories.class);
                           stories.add(userStories);

                       }
                       storyModel.setStories(stories);
                       list.add(storyModel);
                   }
                   adapter.notifyDataSetChanged();
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
        // Dashboard

        postModel = new ArrayList<>();


        PostAdapter postAdapter = new PostAdapter(postModel,getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        dashbordRV.setLayoutManager(linearLayoutManager1);
        dashbordRV.setNestedScrollingEnabled(false);

        database.getReference()
                .child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postModel.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    PostModel post = dataSnapshot.getValue(PostModel.class);
                    post.setPostId(dataSnapshot.getKey());
                    postModel.add(post);

                }
                dashbordRV.setAdapter(postAdapter);
                dashbordRV.hideShimmerAdapter();
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        storyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryLauncher.launch("image/*");
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                storyImage.setImageURI(result);
                dialog.show();
                final StorageReference reference = storage.getReference()
                        .child("stories")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child(new Date().getTime()+"");
                reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override


                            public void onSuccess(Uri uri) {
                                StoryModel storyModel = new StoryModel();
                                storyModel.setStoryAt(new Date().getTime());
                                database.getReference()
                                        .child("stories")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .child("postedBy")
                                        .setValue(storyModel.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        UserStories userStories = new UserStories(uri.toString(), storyModel.getStoryAt());
                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("userStories")
                                                .push()
                                                .setValue(userStories).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                              dialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        return view;
    }
}