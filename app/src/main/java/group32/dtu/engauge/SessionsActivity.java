package group32.dtu.engauge;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import group32.dtu.engauge.model.TrainingSession;
import group32.dtu.engauge.model.TrainingSessionsAdapter;
import group32.dtu.engauge.persistence.StorageUtils;


public class SessionsActivity extends ListActivity {
    private final String TAG = "SESSIONS";

    String[] listItems = {"item 1", "item 2 ", "list", "android", "item 3", "foobar", "bar", };

    private Button compareButton;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);
        context = this.getApplicationContext();

        /*
        TrainingSession s1 = new TrainingSession("session1");
        TrainingSession s2 = new TrainingSession("session2");
        TrainingSession s3 = new TrainingSession("session3");
        TrainingSession s4 = new TrainingSession("session4");

        ArrayList<TrainingSession> sessions = new ArrayList<>();
        sessions.add(s1);
        sessions.add(s2);
        sessions.add(s3);
        sessions.add(s4);
        */

        ArrayList<TrainingSession> sessions = StorageUtils.getTrainingSessions(context);

        // android.R.layout.simple_list_item_1
        //ArrayAdapter<TrainingSession> testAdapter = new ArrayAdapter(this,  R.layout.item_trainingsession, sessions);
        TrainingSessionsAdapter sessionsAdapter = new TrainingSessionsAdapter(this, sessions);

        setListAdapter(sessionsAdapter);
        compareButton = (Button)findViewById(R.id.compareButton);

        compareButton.setOnClickListener(new compareButtonListener());
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        TrainingSession session = (TrainingSession) lv.getAdapter().getItem(position);
        Log.d(TAG, "ITEM CLICKED" + v.toString());
        Log.d(TAG, session.getSessionName());

        if (!session.isActiveInView()){
            session.activate();
            v.setBackgroundColor(Color.RED);
        } else{
            session.disable();
            v.setBackgroundColor(Color.WHITE);
        }
    }

    private class compareButtonListener implements  Button.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SessionsActivity.this, MapsActivity.class);

            //EditText editText = (EditText) findViewById(R.id.editText);
            //String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);

            startActivity(intent);

        }
    }

}
