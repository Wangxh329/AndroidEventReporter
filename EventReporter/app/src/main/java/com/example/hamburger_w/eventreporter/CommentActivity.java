/**
 * Get specific event and its comments information from database, then pass to adapter
 */

package com.example.hamburger_w.eventreporter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseReference;
    private RecyclerView mRecyclerView;
    private EditText mEditTextComment;
    private CommentAdapter commentAdapter;
    private Button mCommentSubmitButton;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent(); // start by EventListAdapter
        final String eventId = intent.getStringExtra("EventID");
        mRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        mEditTextComment = (EditText) findViewById(R.id.comment_edittext);
        mCommentSubmitButton = (Button) findViewById(R.id.comment_submit);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        commentAdapter = new CommentAdapter(this);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mCommentSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment(eventId);
                getData(eventId, commentAdapter); // refresh
                mEditTextComment.setText("");
            }
        });

        getData(eventId, commentAdapter);
    }

    // get comments data in database, update database of commentNumber, and set adapter
    private void getData(final String eventId, final CommentAdapter commentAdapter) {
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // read data: read all comments of eventId
                // find all comments of eventId
                DataSnapshot commentSnapshot = dataSnapshot.child("comments");
                List<Comment> comments = new ArrayList<Comment>();
                for (DataSnapshot noteDataSnapshot : commentSnapshot.getChildren()) {
                    Comment comment = noteDataSnapshot.getValue(Comment.class);
                    if(comment.getEventId().equals(eventId)) {
                        comments.add(comment);
                    }
                }
                Collections.sort(comments, new Comparator<Comment>() {
                    @Override
                    public int compare(Comment c1, Comment c2) {
                        long time1 = c1.getTime();
                        long time2 = c2.getTime();
                        if (time1 == time2) {
                            return 0;
                        }
                        return time1 < time2 ? 1 : -1;
                    }
                });
                mDatabaseReference.getRef().child("events").child(eventId).child("commentNumber").setValue(comments.size()); // update data
                commentAdapter.setComments(comments);
                // find specific event
                DataSnapshot eventSnapshot = dataSnapshot.child("events");
                for (DataSnapshot noteDataSnapshot : eventSnapshot.getChildren()) {
                    Event event = noteDataSnapshot.getValue(Event.class);
                    if(event.getId().equals(eventId)) {
                        commentAdapter.setEvent(event);
                        break;
                    }
                }
                if (mRecyclerView.getAdapter() != null) { // update activity
                    commentAdapter.notifyDataSetChanged();
                } else { // render new activity
                    mRecyclerView.setAdapter(commentAdapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendComment(final String eventId) {
        String description = mEditTextComment.getText().toString();
        if (description.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(), "Do not input blank comment!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        Comment comment = new Comment();
        comment.setCommenter(Utils.username);
        comment.setEventId(eventId);
        comment.setDescription(description);
        comment.setTime(System.currentTimeMillis());
        String key = mDatabaseReference.child("comments").push().getKey(); // create a unique id and return
        comment.setCommentId(key);
        mDatabaseReference.child("comments").child(key).setValue(comment, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "The comment is failed," +
                            " please check you network status.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "The comment is reported.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
