package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.project.Adapter.CommentAdapter;
import com.example.project.Adapter.CommentAdapter;
import com.example.project.Model.Comment;
import com.example.project.Model.NotificationModel;
import com.example.project.Model.PostModel;
import com.example.project.Model.UserData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class CommentActivity extends AppCompatActivity {
    ImageView postImage, profilePic, commentBtn,  comment, send;
    TextView name, like;
    EditText coment;
    Intent intent;
    String postId;
    String postedBy;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Comment> list = new ArrayList<>();
    RecyclerView commentRV;
    Toolbar toolbar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        postImage = findViewById(R.id.postImage);
        commentRV = findViewById(R.id.commentRV);
        profilePic = findViewById(R.id.profilePic);
        commentBtn = findViewById(R.id.commentBtn);
        name = findViewById(R.id.name);
       toolbar2 = findViewById(R.id.toolbar2);
        like = findViewById(R.id.like);
        comment = findViewById(R.id.comment);
        send = findViewById(R.id.send);
        coment = findViewById(R.id.coment);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        intent = getIntent();
        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");
       setSupportActionBar(toolbar2);
       CommentActivity.this.setTitle("Comments");
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database.getReference()
                .child("posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostModel post = snapshot.getValue(PostModel.class);
                Picasso.get()
                        .load(post.getPostImage())
                        .placeholder(R.drawable.male)
                        .into(postImage);


                like.setText(post.getPostLike()+"");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
      database.getReference()
              .child("Users")
              .child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              UserData user = snapshot.getValue(UserData.class);
              Picasso.get()
                      .load(user.getProfilePhoto())
                      .into(profilePic);
              name.setText(user.getName());
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      });
           commentBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                   Comment comment = new Comment();
                   comment.setCommentBody(coment.getText().toString());
                   comment.setCommentedAt(new Date().getTime());
                   comment.setCommentedBy(FirebaseAuth.getInstance().getUid());

                   database.getReference()
                           .child("posts")
                           .child(postId)
                           .child("comments")
                           .push()
                           .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void unused) {

                           coment.setText("");
                           Toast.makeText(CommentActivity.this, "Commented", Toast.LENGTH_SHORT).show();
                           NotificationModel notification = new NotificationModel();
                           notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                           notification.setNotificationAt(new Date().getTime());
                           notification.setPostId(postId);
                           notification.setPostedBy(postedBy);
                           notification.setType("comment");

                           FirebaseDatabase.getInstance().getReference()
                                   .child("notification")
                                   .child(postedBy)
                                   .push()
                                   .setValue(notification);
                       }
                   });
               }
           });




        CommentAdapter adapter = new CommentAdapter(this, list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentRV.setLayoutManager(linearLayoutManager);
        commentRV.setAdapter(adapter);
        database.getReference()
                .child("posts")
                .child(postId)
                .child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    list.add(comment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}