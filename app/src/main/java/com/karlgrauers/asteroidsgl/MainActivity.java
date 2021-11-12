package com.karlgrauers.asteroidsgl;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.karlgrauers.asteroidsgl.input.InputManager;
import com.karlgrauers.asteroidsgl.input.TouchController;
import static com.karlgrauers.asteroidsgl.glEntities.GLEntity._game;

public class MainActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Game _game = findViewById(R.id.game);
        InputManager controls = new TouchController(findViewById(R.id.gamepad));
        _game.setControls(controls);
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            onPause();
            return;
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

    }

    @Override
    protected void onDestroy() {
        _game.onDestroy();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        _game.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        _game.onPause();
        super.onPause();
    }
}
