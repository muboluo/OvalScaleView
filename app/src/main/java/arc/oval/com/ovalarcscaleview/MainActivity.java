package arc.oval.com.ovalarcscaleview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import arc.oval.com.widget.OvalScaleView;

public class MainActivity extends AppCompatActivity implements OvalScaleView.OnRotateListener {

    private OvalScaleView oval_scale_view;
    private TextView text_view_scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {

        text_view_scale = findViewById(R.id.text_view_scale);
        oval_scale_view = findViewById(R.id.oval_scale_view);

        int currentScale = 100;
        text_view_scale.setText(currentScale + "");

        oval_scale_view.setRotateListener(this);
        oval_scale_view.setCurrentScale(currentScale);
    }

    @Override
    public void onRotateScroll(int scale) {

        text_view_scale.setText(scale + "");
    }
}
