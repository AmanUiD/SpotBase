package com.example.project.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Adapter.FollowersAdapter;
import com.example.project.LoginActivity;
import com.example.project.Model.Follow;
import com.example.project.Model.UserData;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    RecyclerView recyclerView;
    TextView followerCount;
    ArrayList<Follow> list;
    CircleImageView profilePic;
    TextView  Name, Profession;
    ImageView menuBtn;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.profilePic);
        recyclerView = view.findViewById(R.id.friendsView);
        followerCount = view.findViewById(R.id.followerCount);
        Name = view.findViewById(R.id.Name);
        Profession = view.findViewById(R.id.profession);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        menuBtn = view.findViewById(R.id.menuButton);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class );
                getContext().startActivity(intent);
            }
        });




        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()){
                UserData userData = snapshot.getValue(UserData.class);
                Picasso.get()
                        .load(userData.getProfilePhoto())

                        .into(profilePic);
                Name.setText(userData.getName());
                Profession.setText(userData.getProfession());
                followerCount.setText(userData.getFollowerCount()+"");

            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        list = new ArrayList<>();



        FollowersAdapter adapter = new FollowersAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        database.getReference().child("Users")
                .child(auth.getUid())
                .child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Follow follow = dataSnapshot.getValue(Follow.class);
                    list.add(follow);

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,11);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null){
            Uri uri = data.getData();
            profilePic.setImageURI(uri);

            final StorageReference storageReference = storage.getReference().child("profile_photo").child(FirebaseAuth.getInstance().getUid());
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                           database.getReference().child("Users").child(auth.getUid()).child("profilePhoto").setValue(uri.toString());
                        }
                    });
                }
            });
        }

    }
}
