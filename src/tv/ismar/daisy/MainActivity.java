package tv.ismar.daisy;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    @ViewById(R.id.myInput)
    EditText myInput;

    @ViewById(R.id.myTextView)
    TextView textView;

    @Click
    void myButton() {
         String name = myInput.getText().toString();
         textView.setText("Hello "+name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onBackPressed() {
        Toast.makeText(this, "Back key pressed!", Toast.LENGTH_SHORT).show();
    }


}

