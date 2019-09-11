package nl.dionsegijn.steppertouchdemo;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import nl.dionsegijn.steppertouch.ShaderBtnView;

public class MainActivity extends AppCompatActivity {

    ShaderBtnView btn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_view);
//        btn = findViewById(R.id.shadowbtn);
//
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("lake", "onClick: ");
//            }
//        });
    }

}
