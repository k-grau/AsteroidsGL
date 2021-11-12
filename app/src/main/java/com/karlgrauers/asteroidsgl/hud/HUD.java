package com.karlgrauers.asteroidsgl.hud;


import com.karlgrauers.asteroidsgl.glEntities.Text;

import java.util.ArrayList;

public class HUD {

    private final static String TAG = "HUD";
    private ArrayList<Text> _textCopy = new ArrayList<Text>();


    public HUD(){}


    public void updateHUD(final ArrayList<Text> texts, String frameCount, final String score, final String life, final String level){
        for(Text t : texts) {
            final String[] checkString = t.getString().split(":");

            if (frameCount.contains(checkString[0])) {
                t.setString(frameCount);
            } else if((score.contains(checkString[0]))) {
                t.setString(score);
            } else if (life.contains(checkString[0])) {
                t.setString(life);
            } else if(level.contains(checkString[0])) {
                t.setString(level);
            }
        }
        _textCopy = new ArrayList<>(texts);
    }


    public void renderLevelComplete(final float[] viewportMatrix, final String levelComplete, final boolean levelIsCompleted){
        if(levelIsCompleted) {
            for (Text t : _textCopy) {
                if (levelComplete.contains(t.getString())) {
                    t.render(viewportMatrix);
                }
            }
        }
    }


    public void renderTeleport(final float[] viewportMatrix, final String teleporting, final boolean isTeleporting){
        if(isTeleporting) {
            for (Text t : _textCopy) {
                if (teleporting.contains(t.getString())) {
                    t.render(viewportMatrix);
                }
            }
        }
    }

    public void renderGameOver(final float[] viewportMatrix, final String gameOverString, final boolean gameOver){
        if(gameOver) {
            for (Text t : _textCopy) {
                if (gameOverString.contains(t.getString())) {
                    t.render(viewportMatrix);
                }
            }
        }
    }


    public void renderActiveGame(final float[] viewportMatrix, final String levelComplete, final String teleporting, final String gameOver){
        for(final Text t : _textCopy){
            if(t.getString().contains(levelComplete)){ continue; }
            if(t.getString().contains(teleporting)){ continue; }
            if(t.getString().contains(gameOver)){ continue; }
            t.render(viewportMatrix);
        }
    }
}




