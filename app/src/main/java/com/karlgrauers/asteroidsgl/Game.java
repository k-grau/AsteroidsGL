package com.karlgrauers.asteroidsgl;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import com.karlgrauers.asteroidsgl.glEntities.Asteroid;
import com.karlgrauers.asteroidsgl.glEntities.Border;
import com.karlgrauers.asteroidsgl.glEntities.Bullet;
import com.karlgrauers.asteroidsgl.glEntities.Flame;
import com.karlgrauers.asteroidsgl.glEntities.GLEntity;
import com.karlgrauers.asteroidsgl.glEntities.Player;
import com.karlgrauers.asteroidsgl.glEntities.Star;
import com.karlgrauers.asteroidsgl.glEntities.Text;
import com.karlgrauers.asteroidsgl.glTools.GLManager;
import com.karlgrauers.asteroidsgl.hud.HUD;
import com.karlgrauers.asteroidsgl.input.InputManager;
import com.karlgrauers.asteroidsgl.soundfx.SoundFX;
import com.karlgrauers.asteroidsgl.utils.Utils;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



public class Game extends GLSurfaceView implements GLSurfaceView.Renderer {

    final public Resources resources = getResources();
    private final static String TAG = "Game";

    private static final long SECOND_IN_NANOSECONDS = 1000000000;
   /* private static final long MILLISECOND_IN_NANOSECONDS = 1000000;*/
    private long teleportTime = 0;
    private long levelCompletedTime = 0;
    private long gameOverTime = 0;
    private long startTime = 0;

    private static final float[] BG_COLOR =  {0 / 255f, 0 / 255f, 0 / 255f, 0.9f};


    private final float WORLD_WIDTH = Float.parseFloat(resources.getString(R.string.world_width));
    private final float WORLD_HEIGHT = Float.parseFloat(resources.getString(R.string.world_height));
    private final float METERS_TO_SHOW_X = Float.parseFloat(resources.getString(R.string.meters_to_show_x));
    private final float METERS_TO_SHOW_Y = Float.parseFloat(resources.getString(R.string.meters_to_show_y));
   /* public static float NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS;*/
    private static final float NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS;
    private final float right = METERS_TO_SHOW_X;
    private final float bottom = METERS_TO_SHOW_Y;

    private double msPerFrame = 0;
    private double accumulator = 0.0;
    private double currentTime = System.nanoTime()*NANOSECONDS_TO_SECONDS;
    private double lastTime = System.nanoTime()*NANOSECONDS_TO_SECONDS;


    private final int MAX_LEVELS = resources.getInteger(R.integer.max_levels);
    private int countFrame = 0;
    private int score = 0;
    private int lives = resources.getInteger(R.integer.start_lifes);
    private int level = resources.getInteger(R.integer.start_level);
    private final int MOVE_OFF_SCREEN = resources.getInteger(R.integer.move_off_screen);
    private final int ASTEROID_SIZE_BOUND = resources.getInteger(R.integer.asteroid_size_bound);
    private final int ASTEROID_SHAPE_BOUND = resources.getInteger(R.integer.asteroid_shape_bound);
    private int asteroidCount = resources.getInteger(R.integer.asteroid_count);
    private final int ASTEROID_START_POSITION_X = resources.getInteger(R.integer.asteroid_start_position);
    private final int ASTEROID_START_POSITION_Y = resources.getInteger(R.integer.asteroid_start_position);
    private final int MIN_SCORE = resources.getInteger(R.integer.min_score);
    private final int MED_SCORE = resources.getInteger(R.integer.med_score);
    private final int MAX_SCORE = resources.getInteger(R.integer.max_score);
    private final int STAR_COUNT = resources.getInteger(R.integer.star_count);
    private final int BULLET_COUNT = (int)(Bullet.TIME_TO_LIVE/Float.parseFloat(resources.getString(R.string.time_between_shots))+1);

    private boolean levelIsCompleted = false;
    private boolean isTeleporting = false;
    private boolean gameOver = false;


    private String scoreText = resources.getString(R.string.score_text, score);
    private String lifeText = resources.getString(R.string.life_text, lives);
    private String levelText = resources.getString(R.string.level_text, 1);
    private String fpsText = resources.getString(R.string.fps_text, msPerFrame);
    private final String LEVEL_COMPLETE_TEXT = resources.getString(R.string.level_complete_text);
    private final String GAMEOVER_TEXT = resources.getString(R.string.gameover_text);
    private final String TELEPORTING_TEXT = resources.getString(R.string.teleporting_text);

    private final Bullet[] _bullets = new Bullet[BULLET_COUNT];
    private final float[] _viewportMatrix = new float[4*4]; //In essence, it is our our Camera

    private final ArrayList<Star> _stars = new ArrayList();
    private final ArrayList<Asteroid> _asteroids = new ArrayList();
    private final  ArrayList<Text> _texts = new ArrayList<>();

    public InputManager _inputs = new InputManager(); //empty but valid default
    // Create the projection Matrix. This is used to project the scene onto a 2D viewport.
    private Border _border;
    private Player _player;
    private Flame _flame;
    private HUD _hud;
    private SoundFX _soundFX = null;


    public void setControls(final InputManager input){
        _inputs = input;
    }

    public InputManager getControls() {
        return _inputs;
    }

    public boolean getIsTeleporting() {
        return isTeleporting;
    }

    public float getWorldWidth() {
        return WORLD_WIDTH;
    }

    public float getWorldHeight() {
        return WORLD_HEIGHT;
    }





    public Game(Context context) {
        super(context);
        init(context);
    }

    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        GLEntity._game = this;
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        _hud = new HUD();
        _soundFX = new SoundFX(context);
        _soundFX.resumeMusic();
        createEntities();
        setRenderer(this);

    }

    public boolean maybeFireBullet(final GLEntity source){
        for(final Bullet b : _bullets) {
            if(b.isDead()) {
                b.fireFrom(source);
                onGameEvent(GameEvent.Shot);
                return true;
            }
        }
        return false;
    }



    private void checkBoost() {
        if(getControls()._pressingB && !_player.isImmortal && !isTeleporting && !levelIsCompleted) {
            onGameEvent(GameEvent.ShipBoost, Float.parseFloat(resources.getString(R.string.lower_boost_vol)));
        }
    }


    private void onGameEvent(GameEvent gameEvent) {
        _soundFX.playSound(gameEvent);
    }

    private void onGameEvent(GameEvent gameEvent, final float adjustVolume) {
        _soundFX.playSound(gameEvent, adjustVolume);
    }


    public void updateFlamePosition(GLEntity source){
        if(!isTeleporting) {
            _flame.flameFrom(source);
        }
    }


    private void createEntities() {
        if(_asteroids.isEmpty()) {
            for (int i = 0; i < asteroidCount; i++) {
                _asteroids.add(new Asteroid(ASTEROID_START_POSITION_X, ASTEROID_START_POSITION_Y,
                        Utils.RNG.nextInt(ASTEROID_SHAPE_BOUND), Utils.RNG.nextInt(ASTEROID_SIZE_BOUND)));
            }
        }

        if(_stars.isEmpty()) {
            for(int i = 0; i < STAR_COUNT; i++){
                _stars.add(new Star(Utils.RNG.nextInt((int)WORLD_WIDTH), Utils.RNG.nextInt((int)WORLD_HEIGHT)));
            }
        }

        if(_border == null) {
            _border = new Border(WORLD_WIDTH/2, WORLD_HEIGHT/2, WORLD_WIDTH, WORLD_HEIGHT);
        }

        createPlayerEntities(_player, _bullets, _flame);
        createTextEntities();
    }

    private void createPlayerEntities(final Player player, final Bullet[] bullets, final Flame flame) {
        if (player == null) {
            _player = new Player(WORLD_WIDTH/2, WORLD_HEIGHT/2);
        }

        if (flame == null) {
            _flame = new Flame();
        }

        if(bullets[0] == null) {
            for (int i = 0; i < BULLET_COUNT; i++) {
                _bullets[i] = new Bullet();
            }
        }

    }


    private void createTextEntities() {
        if(_texts.isEmpty()) {
            _texts.add(new Text(fpsText,  METERS_TO_SHOW_X-METERS_TO_SHOW_X+3, METERS_TO_SHOW_Y - 7));
            _texts.add(new Text(scoreText,  METERS_TO_SHOW_X-METERS_TO_SHOW_X+3, METERS_TO_SHOW_Y-METERS_TO_SHOW_Y + 3));
            _texts.add(new Text(lifeText,  METERS_TO_SHOW_X-METERS_TO_SHOW_X+3, METERS_TO_SHOW_Y-METERS_TO_SHOW_Y + 9));
            _texts.add(new Text(levelText,  METERS_TO_SHOW_X-29, METERS_TO_SHOW_Y-METERS_TO_SHOW_Y + 3));
            _texts.add(new Text(LEVEL_COMPLETE_TEXT,  METERS_TO_SHOW_X-METERS_TO_SHOW_X+8, METERS_TO_SHOW_Y/3));
            _texts.add(new Text(TELEPORTING_TEXT,  METERS_TO_SHOW_X/2-20, METERS_TO_SHOW_Y/3));
            _texts.add(new Text(GAMEOVER_TEXT,  METERS_TO_SHOW_X/2-55, METERS_TO_SHOW_Y/3));
        }

    }



    private void checkBulletCollision(){
        for(final Bullet b : _bullets) {
            if(b.isDead()){ continue; } //skip dead bullets
            for(final Asteroid a : _asteroids) {
                if(b.isColliding(a)){
                    if(a.isDead()){continue;}
                    b.onCollision(a); //notify each entity so they can decide what to do
                    a.onCollision(b);
                    onGameEvent(GameEvent.AsteroidHit);
                    if(a.getRadius() >= a.getSmallAsteroidRadius()
                        && a.getRadius() < a.getMediumAsteroidRadius()) {
                        score += MAX_SCORE;
                    } else if(a.getRadius() >= a.getMediumAsteroidRadius()
                            && a.getRadius() < a.getLargeAsteroidRadius()) {
                        score += MED_SCORE;
                    } else {
                        score += MIN_SCORE;

                    }
                }
            }
        }
    }



    private void checkPlayerCollision() {
        for(final Asteroid a : _asteroids) {
            if(a.isDead()){continue;}
            if(_player.isColliding(a)){
                _player.onCollision(a);
                a.onCollision(_player);
                onGameEvent(GameEvent.PlayerHit);
                if(!_player.isImmortal) {
                    lives--;
                }
            }

        }
    }


    private void checkTeleport() {
        if(getControls()._pressingUp && !isTeleporting && !levelIsCompleted) {
            _player.setBottom(MOVE_OFF_SCREEN);
            isTeleporting = true;
            teleportTime = Utils.activateTimeStamp();
            onGameEvent(GameEvent.Teleport);
        }

        if(isTeleporting) {
            if(System.currentTimeMillis() > teleportTime + 1000) {
                _player.setCustom((Utils.RNG.nextInt((int) WORLD_WIDTH)-4), Utils.RNG.nextInt((int) WORLD_HEIGHT)-4);
                isTeleporting = false;
            }
        }
    }


    private void removeDeadEntities(){
        Asteroid temp;
        final int count = _asteroids.size();
        for(int i = count-1; i >= 0; i--){
            temp = _asteroids.get(i);
            if(temp.isDead()){
                _asteroids.remove(i);
            }
        }
    }

    private void updateTextEntities() {
        fpsText = resources.getString(R.string.fps_text, msPerFrame);
        scoreText = resources.getString(R.string.score_text, score);
        lifeText = resources.getString(R.string.life_text, lives);
        levelText = resources.getString(R.string.level_text, level);
        _hud.updateHUD(_texts, fpsText, scoreText, lifeText, levelText);
    }


    private void checkNextLevel() {
        if(_asteroids.size() <= 0 && !levelIsCompleted) {
            levelCompletedTime = Utils.activateTimeStamp();
            _soundFX.pauseMusic();
            onGameEvent(GameEvent.LevelComplete);
            levelIsCompleted = true;
        }

        if(System.currentTimeMillis() > levelCompletedTime + 3000 && levelIsCompleted) {
            if(level < MAX_LEVELS) {
                level++;
                asteroidCount++;
                lives = resources.getInteger(R.integer.start_lifes);

            } else {
                _asteroids.clear();
                restartGame();
            }
            _soundFX.resumeMusic();
            createEntities();
            levelIsCompleted = false;
        }
    }

    private void checkGameOver() {
        if(lives < 1 && !gameOver) {
            gameOverTime = Utils.activateTimeStamp();
            isTeleporting = false;
            gameOver = true;
            _player = null;
            _asteroids.clear();
            _soundFX.pauseMusic();
            onGameEvent(GameEvent.GameOver);

        }
        if(System.currentTimeMillis() > gameOverTime + 2000 && gameOver) {
            if (getControls()._pressingB && gameOver) {
                restartGame();
            }
        }
    }


    private void restartGame() {
        asteroidCount = resources.getInteger(R.integer.asteroid_count);
        lives = resources.getInteger(R.integer.start_lifes);
        score = 0;
        level = resources.getInteger(R.integer.start_level);
        createEntities();
        _soundFX.resumeMusic();
        gameOver = false;
    }



    private void update(){
        final double newTime = System.nanoTime()*NANOSECONDS_TO_SECONDS;
        final double frameTime = newTime - currentTime;
        currentTime = newTime;
        accumulator += frameTime;

        double dt = 0.01;
        while(accumulator >= dt){

            for(final Bullet b : _bullets){
                if(b.isDead()){ continue; } //skip
                b.update(dt);
            }

            for(final Asteroid a : _asteroids) {
                if(a.isDead()){continue;}
                a.update(dt);
            }

            updateTextEntities();
            removeDeadEntities();
            checkTeleport();
            checkGameOver();
            if(!gameOver) {
                checkBulletCollision();
                checkPlayerCollision();
                checkNextLevel();
                checkBoost();
                if(!isTeleporting) {
                    _player.update(dt);
                }
            }
            accumulator -= dt;
        }
    }




    private void render(){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); //clear buffer to background color
        int offset = 0;
        float far = 1f;
        float near = 0f;
        float top = 0;
        float left = 0;
        Matrix.orthoM(_viewportMatrix, offset, left, right, bottom, top, near, far);

        for (final Star s : _stars) {
            s.render(_viewportMatrix);
        }
        for (final Asteroid a : _asteroids) {
            a.render(_viewportMatrix);
        }

        for (final Bullet b : _bullets) {
            if (b.isDead()) {
                continue;
            }
            b.render(_viewportMatrix);
        }

        if (getControls()._pressingB && !gameOver && !isTeleporting) {
            _flame.render(_viewportMatrix);
        }

        if (!gameOver) {
            _player.render(_viewportMatrix);
        }
        _hud.renderTeleport(_viewportMatrix, TELEPORTING_TEXT, isTeleporting);
        _hud.renderLevelComplete(_viewportMatrix, LEVEL_COMPLETE_TEXT, levelIsCompleted);
        _hud.renderGameOver(_viewportMatrix, GAMEOVER_TEXT, gameOver);
        _hud.renderActiveGame(_viewportMatrix, LEVEL_COMPLETE_TEXT, TELEPORTING_TEXT, GAMEOVER_TEXT);
        _border.render(_viewportMatrix);
    }



    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLManager.buildProgram(resources); //compile, link and upload our GL program
        GLES20.glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], BG_COLOR[3]); //set clear color

    }


    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);
    }


    @Override
    public void onDrawFrame(final GL10 unused) {
        double nowTime = System.nanoTime() * NANOSECONDS_TO_SECONDS;
        countFrame++;

        if (nowTime - lastTime >= 1.0) {
            msPerFrame = 1000.0 / countFrame;
            countFrame = 0;
            lastTime += 1.0;
        }
        update();
        render();
    }


    void onDestroy() {
        if(_soundFX != null) {
            _soundFX.destroy();
        }
        if(_inputs != null) {
            _inputs = null;
        }
    }


    public void onResume() {
        _inputs.onResume();
        _soundFX.onResume();
    }


    public void onPause() {
        _inputs.onPause();
        _soundFX.onPause();
    }

}
