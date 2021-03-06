package fr.wisper.utils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import fr.wisper.screens.gamescreen.FadingScreen;

public class ExtendedStage<T extends FadingScreen> extends Stage {
    private T screen;
    private boolean fading = false;
    private FadingScreen nextScreen;

    public ExtendedStage(T screen, FadingScreen nextScreen) {
        super();

        this.nextScreen = nextScreen;
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keyCode) {
        if(keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK){
            if (!fading) {
                screen.fadeTo(nextScreen);
                fading = true;
            }
        }

        return super.keyDown(keyCode);
    }
}
