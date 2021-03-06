package fr.wisper.screens.gamescreen;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import fr.wisper.Game.WisperGame;
import fr.wisper.assets.WisperChooseAssets;
import fr.wisper.entities.AnimatedWisper;
import fr.wisper.animations.tween.TableAccessor;
import fr.wisper.entities.Wisper;
import fr.wisper.screens.loading.LoadingScreen;
import fr.wisper.utils.Config;
import fr.wisper.utils.Debug;
import fr.wisper.utils.ExtendedStage;

public class WisperChooseMenu implements FadingScreen {
    // Stage
    private ExtendedStage<WisperChooseMenu> stage;
    private Table table;
    private Skin skin;
    List<String> list;

    // Wisper
    private final String CHOSEN_WISPER = "chosen-wisper";
    private SpriteBatch batch;
    private AnimatedWisper wisper;

    // Tween
    private TweenManager tweenManager;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.95f, 0.95f, 0.95f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Manage camera
        WisperGame.Camera.update();

        // Stage
        stage.act(delta);
        stage.draw();

        // Wisper
        batch.setProjectionMatrix(WisperGame.Camera.combined);
        batch.begin();
        wisper.draw(batch, delta);
        batch.end();

        // Update animations
        tweenManager.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        WisperGame.Camera.zoom = 1f;
        WisperGame.Camera.updateViewport();

        ScalingViewport stageViewport = new ScalingViewport(
                Scaling.fit,
                WisperGame.VirtualViewport.getVirtualWidth(),
                WisperGame.VirtualViewport.getVirtualHeight(),
                WisperGame.Camera);

        stage.setViewport(stageViewport);
        stage.getViewport().update(width, height, true);
        table.invalidateHierarchy();
    }

    @Override
    public void show() {
        // Stage
        stage = new ExtendedStage(this, new MainMenu());
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);
        skin = WisperChooseAssets.manager.get(WisperChooseAssets.GlobalSkin);

        // Table
        table = new Table(skin);
        table.setFillParent(true);
        createTable();

        // Animations
        initAnimations();

        // Wisper
        batch = new SpriteBatch();
        if (WisperGame.preferences.getInteger(CHOSEN_WISPER) != Wisper.BLACK_WISPER) {
            list.setSelectedIndex(WisperGame.preferences.getInteger(CHOSEN_WISPER));
        } else {
            chooseWisper(Wisper.BLACK_WISPER);
        }
    }

    private void initAnimations() {
        tweenManager = new TweenManager();

        Tween.registerAccessor(Table.class, new TableAccessor());
        Tween.set(table, TableAccessor.ALPHA).target(0).start(tweenManager);
        Tween.to(table, TableAccessor.ALPHA, Config.ANIMATION_DURATION).target(1).start(tweenManager);

        tweenManager.update(Float.MIN_VALUE);
    }

    public void fadeTo(final FadingScreen screen) {
        wisper.stopDraw();

        Tween.set(table, TableAccessor.ALPHA).target(1).start(tweenManager);
        Tween.to(table, TableAccessor.ALPHA, Config.ANIMATION_DURATION / 3f).target(0).setCallback(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                LoadingScreen loader = ((WisperGame) Gdx.app.getApplicationListener()).getLoader();
                loader.setNextScreen(screen);
            }
        }).start(tweenManager);

        WisperGame.preferences.putInteger(CHOSEN_WISPER, list.getSelectedIndex());
        WisperGame.preferences.flush();
    }

    @Override
    public AssetManager getAssetManager() {
        return WisperChooseAssets.manager;
    }

    @Override
    public void load() {
        WisperChooseAssets.load();
    }

    private void createTable() {
        list = new List<String>(skin);
        list.setItems(new String[] {"Thanatos", "Spark", "Bloody"});
        list.addListener(new ListListener());

        ScrollPane scrollPane = new ScrollPane(list, skin);

        TextButton start = new TextButton("Start", skin, "medium");
        start.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                fadeTo(new GameScreen(list.getSelectedIndex()));
            }
        });
        start.pad(10);

        table.padTop(50);
        table.add(new Label("Select your Wisper", skin, "big-bold")).colspan(2).expandX().spaceBottom(125).row();
        table.add(scrollPane).padLeft(10).minWidth(250).top().left();
        table.add(wisper).uniformX().expandX().expandY().center().row();
        table.add(start).colspan(2).expandX().bottom();
        table.padBottom(50);

        stage.addActor(table);
    }

    public void setWisper(AnimatedWisper wisper) {
        if (this.wisper != null) {
            this.wisper.dispose();
        }

        this.wisper = wisper;
        wisper.animate(tweenManager);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        wisper.dispose();
        skin.dispose();
    }

    public void chooseWisper(int chosenWisper)  {
        switch (chosenWisper) {
            case Wisper.BLACK_WISPER:
                setWisper(new AnimatedWisper("particles/black-wisper-big-noadditive.p"));
                break;
            case Wisper.BLUE_WISPER:
                setWisper(new AnimatedWisper("particles/blue-wisper-big-noadditive.p"));
                break;
            case Wisper.RED_WISPER:
                setWisper(new AnimatedWisper("particles/red-wisper-big-noadditive.p"));
                break;
            default:
                break;
        }
    }

    private class ListListener extends ChangeListener {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            Debug.Log(list.getSelected().toString() + " selected");

            chooseWisper(list.getSelectedIndex());
        }
    }
}