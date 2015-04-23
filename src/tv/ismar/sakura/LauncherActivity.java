package tv.ismar.sakura;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.activeandroid.ActiveAndroid;
import retrofit.Callback;
import retrofit.RetrofitError;
import tv.ismar.daisy.R;
import tv.ismar.sakura.core.FeedbackProblem;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.data.http.ProblemEntity;
import tv.ismar.sakura.data.table.CityTable;
import tv.ismar.sakura.ui.activity.HomeActivity;
import tv.ismar.sakura.ui.widget.SakuraImageView;
import tv.ismar.sakura.utils.StringUtils;

import java.util.List;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_IRIS_TVXIO;

public class LauncherActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LauncherActivity";


    private SakuraImageView indicatorNode;
    private SakuraImageView indicatorFeedback;
    private SakuraImageView indicatorHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_launch);
        initViews();
        fetchProblems();
        initializeCityTable();
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


    /**
     * fetch tv problems from http server
     */
    private void fetchProblems() {
        SakuraClientAPI.Problems client = restAdapter_IRIS_TVXIO.create(SakuraClientAPI.Problems.class);
        client.excute(new Callback<List<ProblemEntity>>() {
            @Override
            public void success(List<ProblemEntity> problemEntities, retrofit.client.Response response) {
                FeedbackProblem feedbackProblem = FeedbackProblem.getInstance();
                feedbackProblem.saveCache(problemEntities);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, retrofitError.getMessage());
            }
        });
    }



    /**
     * insert data to city table
     */
    public void initializeCityTable() {
        String[] cities = getResources().getStringArray(R.array.citys);
        String[] cityNicks = getResources().getStringArray(R.array.city_nicks);
        int[] flags = getResources().getIntArray(R.array.city_flag);


        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < cities.length; ++i) {
                CityTable cityTable = new CityTable();
                cityTable.flag = flags[i];
                cityTable.name = cities[i];
                cityTable.nick = cityNicks[i];
                cityTable.areaName = StringUtils.getAreaNameByProvince(cities[i]);
                cityTable.areaFlag = StringUtils.getAreaCodeByProvince(cities[i]);
                cityTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
}
