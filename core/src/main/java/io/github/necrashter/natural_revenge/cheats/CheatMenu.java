package io.github.necrashter.natural_revenge.cheats;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.necrashter.natural_revenge.Main;

/**
 * CheatMenu - Floating mod menu UI
 * Provides toggle buttons for all cheats
 */
public class CheatMenu {
    private Stage stage;
    private Skin skin;
    private Window menuWindow;
    private ImageButton floatingButton;
    private boolean isMenuOpen = false;
    private CheatManager cheats;

    // Textures for UI
    private Texture buttonTexture;
    private Texture menuBgTexture;

    // Tab management
    private String currentTab = "Combat";
    private Table contentTable;

    public CheatMenu(Stage stage) {
        this.stage = stage;
        this.skin = Main.skin;
        this.cheats = CheatManager.getInstance();

        createTextures();
        createFloatingButton();
        createMenuWindow();
    }

    private void createTextures() {
        // Create floating button texture (circular gradient)
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.8f, 0.3f, 0.9f));
        pixmap.fillCircle(32, 32, 30);
        pixmap.setColor(new Color(0.3f, 1f, 0.4f, 1f));
        pixmap.fillCircle(32, 32, 25);
        pixmap.setColor(Color.WHITE);
        // Draw "M" for Menu
        pixmap.fillRectangle(20, 20, 5, 24);
        pixmap.fillRectangle(39, 20, 5, 24);
        pixmap.fillRectangle(25, 20, 5, 15);
        pixmap.fillRectangle(34, 20, 5, 15);
        buttonTexture = new Texture(pixmap);
        pixmap.dispose();

        // Create menu background
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(new Color(0.1f, 0.1f, 0.15f, 0.95f));
        bgPixmap.fill();
        menuBgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();
    }

    private void createFloatingButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(new TextureRegion(buttonTexture));

        floatingButton = new ImageButton(style);

        // Scale button size for mobile devices
        float buttonSize = Main.isMobile() ? 80 : 60;
        float uiScale = Main.getUiScale();
        buttonSize *= uiScale;

        floatingButton.setSize(buttonSize, buttonSize);
        updateButtonPosition();

        floatingButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleMenu();
            }
        });

        // Make sure the button is on top
        floatingButton.setZIndex(Integer.MAX_VALUE);
        stage.addActor(floatingButton);
    }

    private void createMenuWindow() {
        menuWindow = new Window("FROGUE MOD MENU v1.0", skin);
        menuWindow.setSize(500, 450);
        menuWindow.setPosition(
            (Gdx.graphics.getWidth() - 500) / 2f,
            (Gdx.graphics.getHeight() - 450) / 2f
        );
        menuWindow.setMovable(true);
        menuWindow.setVisible(false);

        // Tab buttons
        Table tabBar = new Table();
        String[] tabs = {"Combat", "Visual", "Movement", "Fun", "ESP/Aim"};

        for (String tab : tabs) {
            TextButton tabBtn = new TextButton(tab, skin);
            tabBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    switchTab(tab);
                }
            });
            tabBar.add(tabBtn).pad(2).expandX().fillX();
        }

        menuWindow.add(tabBar).expandX().fillX().row();

        // Content area with scroll
        contentTable = new Table();
        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);

        menuWindow.add(scrollPane).expand().fill().pad(5).row();

        // Bottom buttons
        Table bottomBar = new Table();

        TextButton resetBtn = new TextButton("Reset All", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cheats.resetAll();
                refreshContent();
            }
        });

        TextButton closeBtn = new TextButton("Close [ESC]", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleMenu();
            }
        });

        bottomBar.add(resetBtn).pad(5).expandX().fillX();
        bottomBar.add(closeBtn).pad(5).expandX().fillX();

        menuWindow.add(bottomBar).expandX().fillX();

        stage.addActor(menuWindow);

        // Initialize with first tab
        switchTab("Combat");
    }

    private void switchTab(String tab) {
        currentTab = tab;
        refreshContent();
    }

    private void refreshContent() {
        contentTable.clear();

        switch (currentTab) {
            case "Combat":
                addCombatCheats();
                break;
            case "Visual":
                addVisualCheats();
                break;
            case "Movement":
                addMovementCheats();
                break;
            case "Fun":
                addFunCheats();
                break;
            case "ESP/Aim":
                addESPAimbotCheats();
                break;
        }
    }

    private void addCombatCheats() {
        addToggle("God Mode", "Invincibility - Take no damage", cheats.godMode, () -> cheats.toggleGodMode());
        addToggle("Infinite Ammo", "Never run out of bullets", cheats.infiniteAmmo, () -> cheats.infiniteAmmo = !cheats.infiniteAmmo);
        addToggle("No Reload", "Instant reload", cheats.noReload, () -> cheats.noReload = !cheats.noReload);
        addToggle("One Hit Kill", "Kill enemies in one shot", cheats.oneHitKill, () -> cheats.oneHitKill = !cheats.oneHitKill);
        addToggle("Freeze Enemies", "Stop all enemy movement", cheats.freezeEnemies, () -> cheats.freezeEnemies = !cheats.freezeEnemies);
        addToggle("Invisibility", "Enemies can't see you", cheats.invisibility, () -> cheats.invisibility = !cheats.invisibility);
        addToggle("Explosive Shots", "Bullets explode on impact", cheats.explosiveShots, () -> cheats.explosiveShots = !cheats.explosiveShots);
        addToggle("Laser Beam", "Continuous laser instead of bullets", cheats.laserBeam, () -> cheats.laserBeam = !cheats.laserBeam);
        addToggle("Freeze Ray", "Freeze enemies on hit", cheats.freezeRay, () -> cheats.freezeRay = !cheats.freezeRay);
    }

    private void addVisualCheats() {
        addToggle("Rainbow World", "Everything becomes rainbow colored", cheats.rainbowWorld, () -> cheats.toggleRainbowWorld());
        addToggle("Spiral Weapon", "Weapon model spirals continuously", cheats.spiralWeapon, () -> cheats.toggleSpiralWeapon());
        addToggle("Matrix Mode", "Green tint + slow motion trails", cheats.matrixMode, () -> cheats.matrixMode = !cheats.matrixMode);
        addToggle("Disco Mode", "Flashing colorful lights", cheats.discoMode, () -> cheats.discoMode = !cheats.discoMode);
        addToggle("Inverted Colors", "Negative color effect", cheats.invertedColors, () -> cheats.invertedColors = !cheats.invertedColors);
        addToggle("Big Head Mode", "Enemies have giant heads", cheats.bigHead, () -> cheats.bigHead = !cheats.bigHead);
        addToggle("Tiny Enemies", "Shrink all enemies", cheats.tinyEnemies, () -> cheats.tinyEnemies = !cheats.tinyEnemies);
        addToggle("Giant Player", "Become a giant", cheats.giantPlayer, () -> cheats.giantPlayer = !cheats.giantPlayer);
        addToggle("Earthquake Mode", "Screen shakes constantly", cheats.earthquakeMode, () -> cheats.earthquakeMode = !cheats.earthquakeMode);
        addToggle("Drunk Mode", "Wobbly camera effect", cheats.drunkMode, () -> cheats.drunkMode = !cheats.drunkMode);
    }

    private void addMovementCheats() {
        addToggle("Super Speed", "Move 3x faster", cheats.superSpeed, () -> cheats.superSpeed = !cheats.superSpeed);
        addToggle("Super Jump", "Jump 2.5x higher", cheats.superJump, () -> cheats.superJump = !cheats.superJump);
        addToggle("No Clip", "Walk through walls", cheats.noClip, () -> cheats.noClip = !cheats.noClip);
        addToggle("Moon Gravity", "Low gravity like on the moon", cheats.moonGravity, () -> cheats.moonGravity = !cheats.moonGravity);
        addToggle("Slow Motion", "Everything moves in slow-mo", cheats.slowMotion, () -> { cheats.slowMotion = !cheats.slowMotion; if (cheats.slowMotion) cheats.hyperSpeed = false; });
        addToggle("Hyper Speed", "Everything moves faster", cheats.hyperSpeed, () -> { cheats.hyperSpeed = !cheats.hyperSpeed; if (cheats.hyperSpeed) cheats.slowMotion = false; });

        // Slider for slow motion factor
        addSlider("Slow-Mo Factor", 0.1f, 0.9f, cheats.slowMotionFactor, (val) -> cheats.slowMotionFactor = val);
        addSlider("Hyper Factor", 1.5f, 5f, cheats.hyperSpeedFactor, (val) -> cheats.hyperSpeedFactor = val);
    }

    private void addFunCheats() {
        addToggle("Confetti Kills", "Enemies explode into confetti", cheats.confettiKills, () -> cheats.confettiKills = !cheats.confettiKills);
        addToggle("Bounce Shots", "Bullets bounce off surfaces", cheats.bounceShots, () -> cheats.bounceShots = !cheats.bounceShots);
        addToggle("Gravity Gun", "Push/pull enemies with shots", cheats.gravityGun, () -> cheats.gravityGun = !cheats.gravityGun);

        // Spawn buttons
        contentTable.add(new Label("--- SPAWN COMMANDS ---", skin)).colspan(2).pad(10).row();

        addButton("Spawn Health Pack", () -> { /* Spawn health */ });
        addButton("Spawn Random Weapon", () -> { /* Spawn weapon */ });
        addButton("Kill All Enemies", () -> { /* Kill all */ });
        addButton("Complete Level", () -> { /* Win */ });
        addButton("Spawn 10 Zombies", () -> { /* Spawn zombies */ });
        addButton("Spawn Boss", () -> { /* Spawn boss */ });
    }

    private void addESPAimbotCheats() {
        contentTable.add(new Label("=== ESP (WALLHACK) ===", skin)).colspan(2).pad(5).row();

        addToggle("Enemy ESP", "See enemies through walls", cheats.enemyESP, () -> cheats.toggleEnemyESP());
        addToggle("Show Health", "Display enemy health bars", cheats.espShowHealth, () -> cheats.espShowHealth = !cheats.espShowHealth);
        addToggle("Show Distance", "Display distance to enemies", cheats.espShowDistance, () -> cheats.espShowDistance = !cheats.espShowDistance);
        addToggle("ESP Boxes", "Draw boxes around enemies", cheats.espBoxes, () -> cheats.espBoxes = !cheats.espBoxes);
        addToggle("ESP Lines", "Draw lines to enemies", cheats.espLines, () -> cheats.espLines = !cheats.espLines);
        addToggle("Extended View", "See further than normal", cheats.extendedView, () -> cheats.extendedView = !cheats.extendedView);

        addSlider("View Distance", 25f, 200f, cheats.extendedViewDistance, (val) -> cheats.extendedViewDistance = val);

        contentTable.add(new Label("=== AIMBOT ===", skin)).colspan(2).pad(5).row();

        addToggle("Aimbot", "Auto-aim at enemies", cheats.aimbot, () -> cheats.toggleAimbot());
        addToggle("Silent Aimbot", "Hit enemies without moving camera", cheats.silentAimbot, () -> cheats.toggleSilentAimbot());
        addToggle("Visible Only", "Only aim at visible enemies", cheats.aimbotVisibleOnly, () -> cheats.aimbotVisibleOnly = !cheats.aimbotVisibleOnly);

        addSlider("Aimbot FOV", 10f, 180f, cheats.aimbotFOV, (val) -> cheats.aimbotFOV = val);
        addSlider("Aim Smoothing", 1f, 20f, cheats.aimbotSmoothing, (val) -> cheats.aimbotSmoothing = val);
    }

    private void addToggle(String name, String description, boolean currentValue, Runnable onToggle) {
        Table row = new Table();

        Label nameLabel = new Label(name, skin);
        nameLabel.setColor(currentValue ? Color.GREEN : Color.WHITE);

        CheckBox checkBox = new CheckBox("", skin);
        checkBox.setChecked(currentValue);
        checkBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onToggle.run();
                nameLabel.setColor(checkBox.isChecked() ? Color.GREEN : Color.WHITE);
            }
        });

        row.add(checkBox).padRight(10);
        row.add(nameLabel).expandX().left();

        contentTable.add(row).expandX().fillX().pad(3).row();

        // Add description as smaller text
        Label descLabel = new Label(description, skin);
        descLabel.setFontScale(0.8f);
        descLabel.setColor(Color.GRAY);
        contentTable.add(descLabel).expandX().left().padLeft(30).padBottom(5).row();
    }

    private void addSlider(String name, float min, float max, float currentValue, SliderCallback onChange) {
        Table row = new Table();

        Label nameLabel = new Label(name + ": ", skin);
        Label valueLabel = new Label(formatFloat(currentValue), skin);

        Slider slider = new Slider(min, max, (max - min) / 100f, false, skin);
        slider.setValue(currentValue);
        slider.addListener(event -> {
            float val = slider.getValue();
            valueLabel.setText(formatFloat(val));
            onChange.accept(val);
            return false;
        });

        row.add(nameLabel).padRight(5);
        row.add(slider).width(150).padRight(5);
        row.add(valueLabel).width(40);

        contentTable.add(row).expandX().fillX().pad(3).row();
    }

    // GWT-compatible callback interface (replaces java.util.function.Consumer)
    private interface SliderCallback {
        void accept(float value);
    }

    // GWT-compatible float formatting
    private String formatFloat(float value) {
        int intPart = (int) value;
        int decimalPart = Math.abs((int) ((value - intPart) * 10));
        return intPart + "." + decimalPart;
    }

    private void addButton(String name, Runnable onClick) {
        TextButton button = new TextButton(name, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
        });
        contentTable.add(button).expandX().fillX().pad(3).row();
    }

    public void toggleMenu() {
        isMenuOpen = !isMenuOpen;
        menuWindow.setVisible(isMenuOpen);

        if (isMenuOpen) {
            refreshContent();
            // Unlock cursor for menu interaction
            Gdx.input.setCursorCatched(false);
        }
    }

    public boolean isOpen() {
        return isMenuOpen;
    }

    public void updateButtonPosition() {
        if (floatingButton == null) return;

        // Use viewport dimensions for proper positioning
        float viewportWidth = stage.getViewport().getWorldWidth();
        float viewportHeight = stage.getViewport().getWorldHeight();

        float buttonWidth = floatingButton.getWidth();
        float buttonHeight = floatingButton.getHeight();

        // Position in top-left corner for mobile (away from other controls)
        // Position in top-right for desktop
        float padding = 20;
        if (Main.isMobile()) {
            // Top-left for mobile to avoid conflict with other buttons
            floatingButton.setPosition(padding, viewportHeight - buttonHeight - padding);
        } else {
            // Top-right for desktop
            floatingButton.setPosition(viewportWidth - buttonWidth - padding, viewportHeight - buttonHeight - padding);
        }
    }

    public void dispose() {
        if (buttonTexture != null) buttonTexture.dispose();
        if (menuBgTexture != null) menuBgTexture.dispose();
    }
}
