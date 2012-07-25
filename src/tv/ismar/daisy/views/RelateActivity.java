package tv.ismar.daisy.views;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.googlecode.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_channel)
public class RelateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onBackPressed() {
        Toast.makeText(this, "Back key pressed!", Toast.LENGTH_SHORT).show();
    }


}

