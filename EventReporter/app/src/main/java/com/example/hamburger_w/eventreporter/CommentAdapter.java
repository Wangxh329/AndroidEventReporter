/**
 * Receive specific event and its comments information from CommentActivity, read data and assignment with layout xml,
 * and update data with listener
 */

package com.example.hamburger_w.eventreporter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private Context context;
    private final static int TYPE_EVENT = 0;
    private final static int TYPE_COMMENT = 1;
    private List<Comment> commentList;
    private Event event;
    private DatabaseReference databaseReference;
    private LayoutInflater inflater; // layout.xml -> java object

    // constructor: initialize
    public CommentAdapter(Context context) {
        this.context = context;
        commentList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setEvent(final Event event) {
        this.event = event;
    }

    public void setComments(final List<Comment> comments) {
        this.commentList = comments;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_EVENT : TYPE_COMMENT;
    }

    @Override
    public int getItemCount() {
        return commentList.size() + 1;
    }

    // create a viewholder to hold event
    public class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView eventUser;
        public TextView eventTitle;
        public TextView eventLocation;
        public TextView eventDescription;
        public TextView eventTime;
        public ImageView eventImgView;
        public ImageView eventImgViewGood;
        public ImageView eventImgViewComment;
        public TextView eventLikeNumber;
        public TextView eventCommentNumber;
        public View layout;
        // constructor and initialize
        public EventViewHolder(View v) {
            super(v);
            layout = v;
            eventUser = (TextView) v.findViewById(R.id.comment_main_user);
            eventTitle = (TextView) v.findViewById(R.id.comment_main_title);
            eventLocation = (TextView) v.findViewById(R.id.comment_main_location);
            eventDescription = (TextView) v.findViewById(R.id.comment_main_description);
            eventTime = (TextView) v.findViewById(R.id.comment_main_time);
            eventImgView = (ImageView) v.findViewById(R.id.comment_main_image);
            eventImgViewGood = (ImageView) v.findViewById(R.id.comment_main_like_img);
            eventImgViewComment = (ImageView) v.findViewById(R.id.comment_main_comment_img);
            eventLikeNumber = (TextView) v.findViewById(R.id.comment_main_like_number);
            eventCommentNumber = (TextView) v.findViewById(R.id.comment_main_comment_number);
        }
    }

    // create a viewholder to hold comments
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView commentUser; // children view
        public TextView commentDescription;
        public TextView commentTime;
        public ImageView imgviewGood;
        public TextView goodNumber;
        public View layout; // parent view
        // constructor and initialize
        public CommentViewHolder(View v) {
            super(v);
            layout = v;
            commentUser = (TextView) v.findViewById(R.id.comment_item_user);
            commentDescription = (TextView) v.findViewById(R.id.comment_item_description);
            commentTime = (TextView) v.findViewById(R.id.comment_item_time);
            imgviewGood = (ImageView) v.findViewById(R.id.comment_item_good);
            goodNumber = (TextView) v.findViewById(R.id.comment_item_good_number);
        }
    }

    // create all types of viewholders(one instance of each) and initialize them
    // called getItemCount() times
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType) {
            case TYPE_EVENT:
                v = inflater.inflate(R.layout.comment_main, parent, false);
                viewHolder = new EventViewHolder(v);
                break;
            case TYPE_COMMENT:
                v = inflater.inflate(R.layout.comment_item, parent, false);
                viewHolder = new CommentViewHolder(v);
                break;
        }
        return viewHolder;
    }

    // show views(items) in recyclerview
    // render viewholder: instance -> frontend
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case TYPE_EVENT:
                EventViewHolder viewHolderEvent = (EventViewHolder) holder;
                configureEventView(viewHolderEvent); // render
                break;
            case TYPE_COMMENT:
                CommentViewHolder viewHolderComment = (CommentViewHolder) holder;
                configureCommentView(/*curEvent, */viewHolderComment, position);
                break;
        }
    }

    // render views differently
    // set parameter's value according to what passed by activity/fragment, set listener, and presents in frontend
    private void configureEventView(final EventViewHolder holder) {
        holder.eventUser.setText(event.getUsername());
        holder.eventTitle.setText(event.getTitle());
        String[] locations = event.getAddress().split(",");
        holder.eventLocation.setText(locations[1] + "," + locations[2]);
        holder.eventDescription.setText(event.getDescription());
        holder.eventTime.setText(Utils.timeTransformer(event.getTime()));
        holder.eventCommentNumber.setText(String.valueOf(event.getCommentNumber()));
        holder.eventLikeNumber.setText(String.valueOf(event.getLike()));
        if (event.getImgUri() != null) {
            final String url = event.getImgUri();
            holder.eventImgView.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Bitmap>(){ // about network operations, so pay attention to ANR -> do in background thread
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return Utils.getBitmapFromURL(url);
                }
                @Override
                protected void onPostExecute(Bitmap bitmap) { // in UI thread
                    holder.eventImgView.setImageBitmap(bitmap);
                }
            }.execute();
        } else {
            holder.eventImgView.setVisibility(View.GONE); // not reserve space
        }
        // listen to "like" of event
        holder.eventImgViewGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) { // update data
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Event recordedEvent = snapshot.getValue(Event.class);
                            if (recordedEvent.getId().equals(event.getId())) {
                                int number = recordedEvent.getLike();
                                holder.eventLikeNumber.setText(String.valueOf(number + 1)); // update frontend
                                snapshot.getRef().child("like").setValue(number + 1); // update database
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    private void configureCommentView(/*final EventViewHolder curEvent, */final CommentViewHolder holder, final int position) {
        final Comment comment = commentList.get(position - 1);
        holder.commentUser.setText(comment.getCommenter());
        holder.commentDescription.setText(comment.getDescription());
        holder.commentTime.setText(Utils.timeTransformer(comment.getTime()));
        holder.goodNumber.setText(String.valueOf(comment.getGood()));

        // listen to "like" of comment
        holder.imgviewGood.setOnClickListener(new View.OnClickListener() { // update data
            @Override
            public void onClick(View v) {
                databaseReference.child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Comment recordedComment = snapshot.getValue(Comment.class);
                            if (recordedComment.getCommentId().equals(comment.getCommentId())) {
                                int number = recordedComment.getGood();
                                holder.goodNumber.setText(String.valueOf(number + 1));
                                snapshot.getRef().child("good").setValue(number + 1);
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }
}
