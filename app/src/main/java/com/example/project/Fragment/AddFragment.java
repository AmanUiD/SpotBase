package com.example.project.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.project.Model.PostModel;
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

import java.util.Date;

public class AddFragment extends Fragment {
    ImageView profilePic, postImage, picture;
    TextView Name;
    EditText postText;
    Button Post;
    Uri uri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        profilePic = view.findViewById(R.id.profilePic);
        postImage = view.findViewById(R.id.imagePost);
        picture = view.findViewById(R.id.picture);
        Name = view.findViewById(R.id.name);
        postText = view.findViewById(R.id.postText);
        Post = view.findViewById(R.id.postBtn);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Please Wait..");
        dialog.setTitle("Uploading");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //user profile and name
        database.getReference().child("Users")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserData user = snapshot.getValue(UserData.class);
                    Picasso.get()
                            .load(user.getProfilePhoto())
                            .placeholder(R.drawable.male)
                            .into(profilePic);
                    Name.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //text watcher
        postText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String description = postText.getText().toString();
                if (!description.isEmpty()) {
                    Post.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn));
                    Post.setTextColor(getContext().getResources().getColor(R.color.white));
                    Post.setEnabled(true);
                } else {
                    Post.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.following_btn));
                    Post.setTextColor(getContext().getResources().getColor(R.color.darker_grey));
                    Post.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // gallery
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });
        // Post Activity
        Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             dialog.show();
//                // image store in storage
                final StorageReference reference = storage.getReference()
                        .child("posts")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child(new Date().getTime() + "");
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // storage copy on database
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                PostModel post = new PostModel();
                                post.setPostImage(uri.toString());
                                post.setPostedBy(FirebaseAuth.getInstance().getUid());
                                post.setPostDescription(postText.getText().toString());
                                post.setPostedAt(new Date().getTime());
                                database.getReference()
                                        .child("posts")
                                        .push()
                                        .setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(getContext(), HomeFragment.class);
                                        getContext().startActivity(intent);
                                        Toast.makeText(getContext(), "Posted Successfully", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null) {
            uri = data.getData();
            postImage.setImageURI(uri);
            postImage.setVisibility(View.VISIBLE);
            Post.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn));
            Post.setTextColor(getContext().getResources().getColor(R.color.white));
            Post.setEnabled(true);
        }
    }
}