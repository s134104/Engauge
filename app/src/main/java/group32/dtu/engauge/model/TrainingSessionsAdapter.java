package group32.dtu.engauge.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import group32.dtu.engauge.R;

/**
 * Created by oskar on 17.03.17.
 */

public class TrainingSessionsAdapter extends ArrayAdapter<TrainingSession> {
    public TrainingSessionsAdapter(Context context, ArrayList<TrainingSession> sessions) {
        super(context, 0, sessions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TrainingSession session = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_trainingsession, parent, false);
        }

        TextView sessionNameView = (TextView) convertView.findViewById(R.id.sessionName);
        TextView sessionInfoView = (TextView) convertView.findViewById(R.id.sessionInfo);

        sessionNameView.setText("Session");

        Date startDateTime = new Date(session.getStartTimestamp());
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String startFormatted = formatter.format(startDateTime);

        sessionInfoView.setText(" with start " + startFormatted);

        return convertView;
    }
}
