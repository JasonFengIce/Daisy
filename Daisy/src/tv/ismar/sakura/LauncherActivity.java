package tv.ismar.sakura;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import tv.ismar.daisy.R;
import tv.ismar.sakura.core.FeedbackProblem;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.data.http.ProblemEntity;
import tv.ismar.sakura.ui.activity.HomeActivity;

import java.util.List;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_IRIS_TVXIO;

public class LauncherActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LauncherActivity";


    private ImageView indicatorNode;
    private ImageView indicatorFeedback;
    private ImageView indicatorHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_launch);
        initViews();
        fetchProblems();
    }

    private void initViews() {
        indicatorNode = (ImageView) findViewById(R.id.indicator_node_image);
        indicatorFeedback = (ImageView) findViewById(R.id.indicator_feedback_image);
        indicatorHelp = (ImageView) findViewById(R.id.indicator_help_image);

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


    /**
     * fetch tv problems from http server
     */
    private void fetchProblems() {
        SakuraClientAPI.Problems client = restAdapter_IRIS_TVXIO.create(SakuraClientAPI.Problems.class);

        client.excute().enqueue(new Callback<List<ProblemEntity>>() {
            @Override
            public void onResponse(Response<List<ProblemEntity>> response, Retrofit retrofit) {
                FeedbackProblem feedbackProblem = FeedbackProblem.getInstance();
                feedbackProblem.saveCache(response.body());
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "fetchProblems error!!!");
            }
        });

    }

}
