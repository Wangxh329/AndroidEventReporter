/**
 * Get all events information from database and ads information, then pass to adapter
 */

package com.example.hamburger_w.eventreporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment; // this edition offers backward compatible to new edition's activity
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventsFragment extends Fragment {
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1072772517";
    private ImageView mImageViewAdd;
    private RecyclerView recyclerView;
    private EventListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager; // use listview
    private DatabaseReference database;
    private List<Event> events;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        mImageViewAdd = (ImageView) view.findViewById(R.id.img_event_add); // initialize mImageViewAdd

        mImageViewAdd.setOnClickListener(new View.OnClickListener() { // observer pattern
            @Override
            public void onClick(View view) {
                Intent eventReportIntent = new Intent(getActivity(), EventReportActivity.class); // explicit intent
                startActivity(eventReportIntent);
            }
        });

        // initialize
        recyclerView = (RecyclerView) view.findViewById(R.id.event_recycler_view);
        database = FirebaseDatabase.getInstance().getReference();
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        setAdapter();

        return view;
    }

    /**
     * get data from database and adapt to recyclerview
     */
    public void setAdapter() {
        events = new ArrayList<Event>();
        database.child("events").addListenerForSingleValueEvent(new ValueEventListener() { // read data
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) { // read all uploaded events
                    Event event = noteDataSnapshot.getValue(Event.class);
                    events.add(event);
                }
                // sorted the events by reported time
                Collections.sort(events, new Comparator<Event>() {
                    @Override
                    public int compare(Event e1, Event e2) {
                        long t1 = e1.getTime();
                        long t2 = e2.getTime();
                        if (t1 == t2) {
                            return 0;
                        }
                        return t1 < t2 ? 1 : -1;
                    }
                });
                mAdapter = new EventListAdapter(events, getActivity()); // set adapter
                if (recyclerView.getAdapter() != null) { // update activity
                    mAdapter.notifyDataSetChanged();
                } else { // render new activity
                    recyclerView.setAdapter(mAdapter);
                }
                setUpAndLoadNativeExpressAds(); // load ads
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO: do something
            }
        });
    }

    private void setUpAndLoadNativeExpressAds() { // lazy loading
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                final float scale = getActivity().getResources().getDisplayMetrics().density; // 获取手机屏幕相对密度
                // Set the ad size and ad unit ID for each Native Express ad in the items list.
                for (int i = 0; i < mAdapter.getEventList().size(); i ++) {
                    if (mAdapter.getMap().containsKey(i)) {
                        final NativeExpressAdView adView = mAdapter.getMap().get(i);
                        final CardView cardView = (CardView) getActivity().findViewById(R.id.ad_card_view);
                        final int adWidth = cardView.getWidth() - cardView.getPaddingLeft() - cardView.getPaddingRight();
                        AdSize adSize = new AdSize((int) (adWidth / scale), 150);
                        adView.setAdSize(adSize);
                        adView.setAdUnitId(AD_UNIT_ID);
                        loadNativeExpressAd(i, adView);
                    }
                }
            }
        });
    }

    // load native ads
    private void loadNativeExpressAd(final int index, NativeExpressAdView adView ) {
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("EventsFragment", "The ads failed to load, will refresh");
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }
}

