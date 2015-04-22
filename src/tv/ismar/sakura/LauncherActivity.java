package tv.ismar.sakura;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import tv.ismar.daisy.R;
import tv.ismar.sakura.ui.activity.HomeActivity;
import tv.ismar.sakura.ui.widget.SakuraImageView;

public class LauncherActivity extends Activity implements View.OnClickListener {


    private SakuraImageView indicatorNode;
    private SakuraImageView indicatorFeedback;
    private SakuraImageView indicatorHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_launch);

        initViews();
    }

    private void initViews() {
        indicatorNode = (SakuraImageView) findViewById(R.id.indicator_node_image);
        indicatorFeedback = (SakuraImageView) findViewById(R.id.indicator_feedback_image);
        indicatorHelp = (SakuraImageView) findViewById(R.id.indicator_help_image);

        indicatorNode.setOnClickListener(this);
        indicatorFeedback.setOnClickListener(this);
        indicatorHelp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this, HomeActivity.class);

        switch (view.getId()) {
            case R.id.indicator_node_image:
                intent.putExtra("position", 0);
                break;
            case R.id.indicator_feedback_image:
                intent.putExtra("position", 1);
                break;
            case R.id.indicator_help_image:
                intent.putExtra("position", 2);
                break;
        }
        startActivity(intent);
    }
}
