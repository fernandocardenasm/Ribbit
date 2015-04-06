package com.example.usuario.ribbit.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.usuario.ribbit.R;
import com.example.usuario.ribbit.adapters.MessageAdapter;
import com.example.usuario.ribbit.utilities.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usuario on 19/03/2015.
 */
public class InboxFragment extends ListFragment {

    private List<ParseObject> mMessages;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    ProgressBar mInboxProgressBar;
    TextView mEmptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefresherListener);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.swipeRefresh1,
                R.color.swipeRefresh2,
                R.color.swipeRefresh3,
                R.color.swipeRefresh4
        );

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        LoadInboxMessages();
    }

    private void LoadInboxMessages() {


        mInboxProgressBar = (ProgressBar) getView().findViewById(R.id.inboxProgressBar);
        mEmptyTextView = (TextView) getView().findViewById(android.R.id.empty);

        //Loading Progress Bar
        mInboxProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setText(getString(R.string.loading_inbox_messages));


        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {


                //Validating if the SwipeRefresher is being used

                if (mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                mInboxProgressBar.setVisibility(View.INVISIBLE);
                mEmptyTextView.setText(R.string.empty_inbox_label);
                
                if (e == null){
                    //We find messages
                    mMessages = messages;

                    String[] usernames = new String[mMessages.size()];

                    int i = 0;

                    for (ParseObject message : mMessages) {
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }
                    if (getListView().getAdapter() == null) {

                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(),
                                mMessages);
                        setListAdapter(adapter);
                    }
                    else{
                        //refill the adapter
                        ((MessageAdapter)getListView().getAdapter()).refill(mMessages);
                    }

                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        //Get the Uri of the file that is in the back-end
        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);

        Uri fileUri = Uri.parse(file.getUrl());

        if (messageType.equals(ParseConstants.TYPE_IMAGE)){
            //View the image
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }
        else{
            //View the video
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.setDataAndType(fileUri, "video/*");
            startActivity(intent);
        }

        //Delete the message
        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);

        if (ids.size() == 1){
            //Last recipient - delete the whole thing, it means that´s the last person to see the message
            //So we can delete the whole message in the Parse
            message.deleteInBackground();
        }
        else{
            //remove the recipient and save

            //ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);

            message.saveInBackground();

        }
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefresherListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            LoadInboxMessages();
        }
    };
}
