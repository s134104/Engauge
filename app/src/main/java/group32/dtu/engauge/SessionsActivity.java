package group32.dtu.engauge;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;


public class SessionsActivity extends ListActivity {

    String[] listItems = {"item 1", "item 2 ", "list", "android", "item 3", "foobar", "bar", };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sessions);
        ArrayAdapter<String> testAdapter = new ArrayAdapter(this,  android.R.layout.simple_list_item_1, listItems);


        setListAdapter(testAdapter);

    }
}
