package com.karlgrauers.asteroidsgl.soundfx;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;


import com.karlgrauers.asteroidsgl.GameEvent;
import com.karlgrauers.asteroidsgl.R;

import java.io.IOException;
import java.util.HashMap;


public class SoundFX {

    private final static String TAG = "SoundFX";
    private SoundPool _soundPool = null;
    private static int maxStreams = 0;

    private float defaultVolume = 0;
    private int defaultLoop = 0;
    private int eternalLoop = 0;
    private float defaultRate = 0;
    private int defaultPriority = 0;
    private MediaPlayer _musicPlayer = null;
    private boolean _soundEnabled = true;
    private boolean _musicEnabled = true;
    private Context _context = null;
    private static final String SOUNDS_PREF_KEY = "soundsEnabled";
    private static final String MUSIC_PREF_KEY = "musicEnabled";
    private HashMap<GameEvent, Integer> _soundMap;



    public SoundFX(final Context context) {
        _context = context;
        defaultVolume = Float.parseFloat(context.getResources().getString(R.string.default_volume));
        defaultLoop = context.getResources().getInteger(R.integer.default_loop);
        eternalLoop = context.getResources().getInteger(R.integer.eternal_loop);
        defaultRate = Float.parseFloat(context.getResources().getString(R.string.default_rate));
        defaultPriority = context.getResources().getInteger(R.integer.priority);
        maxStreams = context.getResources().getInteger(R.integer.max_streams);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        _soundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true);
        _musicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true);

        loadIfNeeded();
    }

    private void loadIfNeeded(){
        final String backGroundThemeFile = "sounds/background-theme.wav";

        if(_soundEnabled){
            loadSounds();
        }
        if(_musicEnabled) {
            loadMusic(backGroundThemeFile);
        }
    }

    private void createSoundPool() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            _soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        } else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            _soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(maxStreams).build();
        }
    }


    private void loadSound(GameEvent event, final String fileName){
        try {
            AssetFileDescriptor assetFileDescriptor = _context.getAssets().openFd(fileName);
            int soundId = _soundPool.load(assetFileDescriptor , 1);
            _soundMap.put(event, soundId);
        }catch(IOException e){
            Log.e(TAG, "loadSound: error loading sound " + e.toString());
        }
    }



    private void loadSounds() {
        final String asteroidHit = "sounds/asteroid_hit.wav";
        final String shot = "sounds/shot.wav";
        final String teleport = "sounds/teleport.wav";
        final String playerHit = "sounds/player_hit.wav";
        final String shipBoost = "sounds/ship_boost.wav";
        final String gameOver = "sounds/game-over.wav";
        final String levelComplete = "sounds/level-complete.wav";

        createSoundPool();
        _soundMap = new HashMap<>();
        loadSound(GameEvent.AsteroidHit, asteroidHit);
        loadSound(GameEvent.Shot, shot);
        loadSound(GameEvent.Teleport, teleport);
        loadSound(GameEvent.PlayerHit, playerHit);
        loadSound(GameEvent.ShipBoost, shipBoost);
        loadSound(GameEvent.GameOver, gameOver);
        loadSound(GameEvent.LevelComplete, levelComplete);
    }


    private void loadMusic(final String fileName){

        try{
            _musicPlayer = new MediaPlayer();
            AssetFileDescriptor assetManager = _context
                    .getAssets().openFd(fileName);
            _musicPlayer.setDataSource(
                    assetManager.getFileDescriptor(),
                    assetManager.getStartOffset(),
                    assetManager.getLength());
            _musicPlayer.setLooping(true);
            _musicPlayer.setVolume(defaultVolume, defaultVolume);
            _musicPlayer.prepare();
        }catch(IOException e){
            _musicPlayer = null;
            _musicEnabled = false;
            e.printStackTrace();
        }
    }


    public void playSound(final GameEvent event){
        if(!_soundEnabled){return;}
        final float leftVolume = defaultVolume;
        final float rightVolume = defaultVolume;
        final int priority = defaultPriority;
        final int loop = defaultLoop; //-1 loop forever, 0 play once
        final float rate = defaultRate;
        final Integer soundID = _soundMap.get(event);
        if(soundID != null){
            _soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }


    public void playSound(final GameEvent event, final float lowerVolume){
        if(!_soundEnabled){return;}
        final float leftVolume = defaultVolume - lowerVolume;
        final float rightVolume = defaultVolume - lowerVolume;
        final int priority = defaultPriority;
        final int loop = defaultLoop; //-1 loop forever, 0 play once
        final float rate = defaultRate;
        final Integer soundID = _soundMap.get(event);
        if(soundID != null){
            _soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }


    public void toggleSoundStatus(){
        _soundEnabled = !_soundEnabled;
        if(_soundEnabled){
            loadSounds();
        }else{
            unloadSounds();
        }
        PreferenceManager
                .getDefaultSharedPreferences(_context)
                .edit()
                .putBoolean(SOUNDS_PREF_KEY, _soundEnabled)
                .commit();
    }

    public void toggleMusicStatus(final String fileName){
        _musicEnabled = !_musicEnabled;
        if(_musicEnabled){
            loadMusic(fileName);
        }else{
            unloadMusic();
        }
        PreferenceManager
                .getDefaultSharedPreferences(_context)
                .edit()
                .putBoolean(MUSIC_PREF_KEY, _musicEnabled)
                .commit();
    }


    private void unloadMusic(){
        if(_musicPlayer != null) {
            _musicPlayer.stop();
            _musicPlayer.release();
        }
    }

    public void pauseMusic(){
        if(_musicEnabled){
            _musicPlayer.pause();
        }
    }
    public void resumeMusic(){
        if(_musicEnabled){
            _musicPlayer.start();
        }
    }

    public void stopMusic(){
        if(_musicEnabled){
            _musicPlayer.stop();
        }
    }


    public void stop(int streamId) {
        _soundPool.stop(streamId);
     }

    public void pause(int streamId) {
        _soundPool.pause(streamId);
    }

    private void unloadSounds() {
        _soundPool.release();
        _soundPool = null;
    }


    public void destroy() {
        unloadSounds();
        unloadMusic();
    }

    public void onPause() {
        pauseMusic();
        _soundPool.autoPause();
    }

    public void onResume() {
        resumeMusic();
        _soundPool.autoResume();
    }

}
