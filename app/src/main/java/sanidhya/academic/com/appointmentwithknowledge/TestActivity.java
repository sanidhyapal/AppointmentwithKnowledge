package sanidhya.academic.com.appointmentwithknowledge;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ListView listView=(ListView)findViewById(R.id.test_list_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
           }

}
