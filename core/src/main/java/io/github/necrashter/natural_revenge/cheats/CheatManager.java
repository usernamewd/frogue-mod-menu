package io.github.necrashter.natural_revenge.cheats;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.player.Player;

/**
 * CheatManager - Core cheat system for Frogue
 * Manages all cheats and their states
 */
public class CheatManager {
    private static CheatManager instance;

    // Cheat states
    public boolean godMode = false;
    public boolean infiniteAmmo = false;
    public boolean noReload = false;
    public boolean oneHitKill = false;
    public boolean superSpeed = false;
    public boolean superJump = false;
    public boolean noClip = false;
    public boolean freezeEnemies = false;
    public boolean invisibility = false;

    // ESP & Aimbot
    public boolean enemyESP = false;
    public boolean espShowHealth = true;
    public boolean espShowDistance = true;
    public boolean espBoxes = true;
    public boolean espLines = true;
    public boolean aimbot = false;
    public boolean silentAimbot = false;
    public float aimbotFOV = 90f;
    public float aimbotSmoothing = 5f;
    public boolean aimbotVisibleOnly = true;

    // Visual cheats
    public boolean extendedView = false;
    public float extendedViewDistance = 100f;
    public boolean rainbowWorld = false;
    public boolean spiralWeapon = false;
    public boolean matrixMode = false;
    public boolean discoMode = false;
    public boolean invertedColors = false;
    public boolean bigHead = false;
    public boolean tinyEnemies = false;
    public boolean giantPlayer = false;
    public boolean slowMotion = false;
    public float slowMotionFactor = 0.25f;
    public boolean hyperSpeed = false;
    public float hyperSpeedFactor = 3f;

    // Fun cheats
    public boolean explosiveShots = false;
    public boolean laserBeam = false;
    public boolean bounceShots = false;
    public boolean gravityGun = false;
    public boolean freezeRay = false;
    public boolean confettiKills = false;
    public boolean moonGravity = false;
    public boolean earthquakeMode = false;
    public boolean drunkMode = false;

    // Rendering
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;

    // Animation timers
    public float globalTimer = 0f;
    public float rainbowHue = 0f;
    public float spiralAngle = 0f;
    public float shakeOffset = 0f;

    // Target for aimbot
    public GameEntity aimbotTarget = null;
    public Vector3 silentAimDirection = new Vector3();

    private CheatManager() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    public static CheatManager getInstance() {
        if (instance == null) {
            instance = new CheatManager();
        }
        return instance;
    }

    public void update(float delta, GameWorld world) {
        globalTimer += delta;
        rainbowHue = (rainbowHue + delta * 0.5f) % 1f;
        spiralAngle = (spiralAngle + delta * 360f) % 360f;

        if (earthquakeMode) {
            shakeOffset = MathUtils.sin(globalTimer * 30f) * 0.1f;
        } else {
            shakeOffset = 0f;
        }

        if (world == null || world.player == null) return;

        Player player = world.player;

        // God Mode
        if (godMode) {
            player.health = player.maxHealth;
        }

        // Super Speed
        if (superSpeed) {
            player.movementSpeed = 12f;
        } else {
            player.movementSpeed = 4f;
        }

        // Super Jump
        if (superJump) {
            player.jumpVelocity = 15f;
        } else {
            player.jumpVelocity = 6f;
        }

        // Moon Gravity
        if (moonGravity && player.hitBox != null) {
            // Reduced gravity effect applied in physics
        }

        // Aimbot logic
        if (aimbot || silentAimbot) {
            updateAimbot(world);
        }

        // Freeze enemies
        if (freezeEnemies) {
            freezeAllEnemies(world);
        }

        // Drunk mode camera wobble
        if (drunkMode && world.cam != null) {
            float wobble = MathUtils.sin(globalTimer * 2f) * 3f;
            world.cam.rotate(Vector3.Z, wobble * delta);
        }
    }

    private void updateAimbot(GameWorld world) {
        if (world.player == null || world.octree == null) return;

        GameEntity closestEnemy = null;
        float closestAngle = aimbotFOV;
        Vector3 playerPos = world.player.hitBox.position;
        Vector3 playerDir = world.cam.direction;

        // Find closest enemy within FOV
        Array<GameEntity> entities = world.octree.getAllEntities();
        for (GameEntity entity : entities) {
            if (entity == world.player || entity.dead) continue;

            Vector3 toEnemy = new Vector3(entity.hitBox.position).sub(playerPos);
            float distance = toEnemy.len();

            if (distance > world.viewDistance) continue;

            toEnemy.nor();
            float angle = (float) Math.toDegrees(Math.acos(playerDir.dot(toEnemy)));

            if (angle < closestAngle) {
                closestAngle = angle;
                closestEnemy = entity;
            }
        }

        aimbotTarget = closestEnemy;

        if (closestEnemy != null) {
            Vector3 targetPos = new Vector3(closestEnemy.hitBox.position);
            targetPos.y += closestEnemy.hitBox.height * 0.7f; // Aim at upper body

            if (silentAimbot) {
                // Silent aimbot - modify bullet direction without moving camera
                silentAimDirection.set(targetPos).sub(playerPos).nor();
            } else if (aimbot) {
                // Regular aimbot - smoothly move camera to target
                Vector3 toTarget = new Vector3(targetPos).sub(world.cam.position).nor();

                float lerpFactor = Math.min(1f, aimbotSmoothing * Gdx.graphics.getDeltaTime());

                // Update player forward direction (horizontal)
                Vector3 horizontalDir = new Vector3(toTarget.x, 0, toTarget.z).nor();
                world.player.forward.lerp(horizontalDir, lerpFactor);

                // Update player pitch (vertical angle)
                float targetPitch = (float) Math.toDegrees(Math.asin(MathUtils.clamp(toTarget.y, -1f, 1f)));
                world.player.pitch = MathUtils.lerp(world.player.pitch, MathUtils.clamp(targetPitch, -90f, 90f), lerpFactor);
            }
        }
    }

    private void freezeAllEnemies(GameWorld world) {
        if (world.octree == null) return;
        Array<GameEntity> entities = world.octree.getAllEntities();
        for (GameEntity entity : entities) {
            if (entity != world.player && !entity.dead) {
                entity.hitBox.velocity.setZero();
            }
        }
    }

    public void renderESP(GameWorld world) {
        if (!enemyESP || world == null || world.player == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(world.cam.combined);

        Array<GameEntity> entities = world.octree.getAllEntities();
        Vector3 playerPos = world.player.hitBox.position;

        for (GameEntity entity : entities) {
            if (entity == world.player || entity.dead) continue;

            Vector3 enemyPos = entity.hitBox.position;
            float distance = playerPos.dst(enemyPos);

            if (distance > extendedViewDistance) continue;

            // Color based on health
            float healthRatio = entity.health / entity.maxHealth;
            Color espColor = new Color(1f - healthRatio, healthRatio, 0f, 0.8f);

            if (espBoxes) {
                // Draw 3D box around enemy
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(espColor);

                float w = entity.hitBox.radius;
                float h = entity.hitBox.height;

                // Bottom square
                shapeRenderer.line(enemyPos.x - w, enemyPos.y, enemyPos.z - w,
                                  enemyPos.x + w, enemyPos.y, enemyPos.z - w);
                shapeRenderer.line(enemyPos.x + w, enemyPos.y, enemyPos.z - w,
                                  enemyPos.x + w, enemyPos.y, enemyPos.z + w);
                shapeRenderer.line(enemyPos.x + w, enemyPos.y, enemyPos.z + w,
                                  enemyPos.x - w, enemyPos.y, enemyPos.z + w);
                shapeRenderer.line(enemyPos.x - w, enemyPos.y, enemyPos.z + w,
                                  enemyPos.x - w, enemyPos.y, enemyPos.z - w);

                // Top square
                shapeRenderer.line(enemyPos.x - w, enemyPos.y + h, enemyPos.z - w,
                                  enemyPos.x + w, enemyPos.y + h, enemyPos.z - w);
                shapeRenderer.line(enemyPos.x + w, enemyPos.y + h, enemyPos.z - w,
                                  enemyPos.x + w, enemyPos.y + h, enemyPos.z + w);
                shapeRenderer.line(enemyPos.x + w, enemyPos.y + h, enemyPos.z + w,
                                  enemyPos.x - w, enemyPos.y + h, enemyPos.z + w);
                shapeRenderer.line(enemyPos.x - w, enemyPos.y + h, enemyPos.z + w,
                                  enemyPos.x - w, enemyPos.y + h, enemyPos.z - w);

                // Vertical lines
                shapeRenderer.line(enemyPos.x - w, enemyPos.y, enemyPos.z - w,
                                  enemyPos.x - w, enemyPos.y + h, enemyPos.z - w);
                shapeRenderer.line(enemyPos.x + w, enemyPos.y, enemyPos.z - w,
                                  enemyPos.x + w, enemyPos.y + h, enemyPos.z - w);
                shapeRenderer.line(enemyPos.x + w, enemyPos.y, enemyPos.z + w,
                                  enemyPos.x + w, enemyPos.y + h, enemyPos.z + w);
                shapeRenderer.line(enemyPos.x - w, enemyPos.y, enemyPos.z + w,
                                  enemyPos.x - w, enemyPos.y + h, enemyPos.z + w);

                shapeRenderer.end();
            }

            if (espLines) {
                // Draw line from player to enemy
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(espColor);
                shapeRenderer.line(playerPos.x, playerPos.y + 1f, playerPos.z,
                                  enemyPos.x, enemyPos.y + entity.hitBox.height / 2, enemyPos.z);
                shapeRenderer.end();
            }
        }

        // Draw 2D overlay info
        spriteBatch.begin();
        for (GameEntity entity : entities) {
            if (entity == world.player || entity.dead) continue;

            Vector3 screenPos = world.cam.project(new Vector3(entity.hitBox.position).add(0, entity.hitBox.height + 0.5f, 0));

            if (screenPos.z > 0 && screenPos.z < 1) {
                float distance = playerPos.dst(entity.hitBox.position);

                StringBuilder info = new StringBuilder();
                if (espShowHealth) {
                    info.append("HP: ").append((int)entity.health).append("/").append((int)entity.maxHealth);
                }
                if (espShowDistance) {
                    if (info.length() > 0) info.append(" | ");
                    // GWT-compatible float formatting
                    info.append((int)distance).append(".").append((int)((distance - (int)distance) * 10)).append("m");
                }

                font.draw(spriteBatch, info.toString(), screenPos.x, screenPos.y);
            }
        }
        spriteBatch.end();
    }

    public Color getRainbowColor() {
        return new Color().fromHsv(rainbowHue * 360f, 0.8f, 1f).add(0, 0, 0, 1);
    }

    public float getTimeScale() {
        if (slowMotion) return slowMotionFactor;
        if (hyperSpeed) return hyperSpeedFactor;
        return 1f;
    }

    public float getGravityMultiplier() {
        if (moonGravity) return 0.2f;
        return 1f;
    }

    public float modifyDamage(float damage, boolean isPlayer) {
        if (isPlayer && oneHitKill) {
            return 99999f;
        }
        if (!isPlayer && godMode) {
            return 0f;
        }
        return damage;
    }

    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
    }

    // Quick toggle methods
    public void toggleGodMode() { godMode = !godMode; }
    public void toggleInfiniteAmmo() { infiniteAmmo = !infiniteAmmo; }
    public void toggleEnemyESP() { enemyESP = !enemyESP; }
    public void toggleAimbot() { aimbot = !aimbot; if (aimbot) silentAimbot = false; }
    public void toggleSilentAimbot() { silentAimbot = !silentAimbot; if (silentAimbot) aimbot = false; }
    public void toggleRainbowWorld() { rainbowWorld = !rainbowWorld; }
    public void toggleSpiralWeapon() { spiralWeapon = !spiralWeapon; }

    public void resetAll() {
        godMode = false;
        infiniteAmmo = false;
        noReload = false;
        oneHitKill = false;
        superSpeed = false;
        superJump = false;
        noClip = false;
        freezeEnemies = false;
        invisibility = false;
        enemyESP = false;
        aimbot = false;
        silentAimbot = false;
        extendedView = false;
        rainbowWorld = false;
        spiralWeapon = false;
        matrixMode = false;
        discoMode = false;
        slowMotion = false;
        hyperSpeed = false;
        moonGravity = false;
        earthquakeMode = false;
        drunkMode = false;
    }
}
