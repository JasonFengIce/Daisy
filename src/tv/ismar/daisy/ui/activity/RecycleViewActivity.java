package tv.ismar.daisy.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.ui.widget.recycleview.widget.DefaultItemAnimator;
import tv.ismar.daisy.ui.widget.recycleview.widget.LinearLayoutManager;
import tv.ismar.daisy.ui.widget.recycleview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaijie on 8/1/15.
 */
public class RecycleViewActivity extends Activity {

    private RecyclerView recyclerView;
    private List<String> datas;
    private HomeAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycleview);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setStateListAnimator();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new HomeAdapter());

        initData();
    }


    protected void initData() {
        datas = new ArrayList<String>();
        for (int i = 'A'; i < 'z'; i++) {
            datas.add("" + (char) i);
        }
    }


    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(RecycleViewActivity.this).inflate(R.layout.item_recycler, viewGroup, false);

            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        AnimationSet animationSet = new AnimationSet(true);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.5f, 1, 1.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(200);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.setFillAfter(true);
                        v.startAnimation(animationSet);

                    } else {
                        AnimationSet animationSet = new AnimationSet(true);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f, 1.5f, 1f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(200);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.setFillAfter(true);
                        v.startAnimation(animationSet);
                    }
                }
            });

            view.bringToFront();
            MyViewHolder holder = new MyViewHolder(view);


            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
//            myViewHolder.textView.setText(datas.get(i));

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView textView;

            public MyViewHolder(View itemView) {
                super(itemView);

                textView = (ImageView) itemView.findViewById(R.id.id_number);
            }
        }

    }
}
