package io.github.necrashter.natural_revenge.cheats;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.player.PlayerWeapon;

/**
 * CheatEffects - Visual effects for cheat system
 * Handles post-processing and model modifications
 */
public class CheatEffects {
    private static CheatEffects instance;

    private SpriteBatch batch;
    private ShaderProgram rainbowShader;
    private ShaderProgram matrixShader;
    private ShaderProgram invertShader;
    private ShaderProgram spiralShader;
    private FrameBuffer frameBuffer;

    private CheatManager cheats;

    // Cached colors for effects
    private Color currentRainbowColor = new Color();
    private float effectTimer = 0f;

    private CheatEffects() {
        batch = new SpriteBatch();
        cheats = CheatManager.getInstance();

        createShaders();
    }

    public static CheatEffects getInstance() {
        if (instance == null) {
            instance = new CheatEffects();
        }
        return instance;
    }

    private void createShaders() {
        // Rainbow shader
        String rainbowVert = "attribute vec4 a_position;\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "    v_color = a_color;\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}";

        String rainbowFrag = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform float u_time;\n" +
            "vec3 hsv2rgb(vec3 c) {\n" +
            "    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);\n" +
            "    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
            "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
            "}\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(u_texture, v_texCoords);\n" +
            "    float hue = fract(u_time * 0.2 + v_texCoords.x * 0.5 + v_texCoords.y * 0.5);\n" +
            "    vec3 rainbow = hsv2rgb(vec3(hue, 0.7, 1.0));\n" +
            "    color.rgb = mix(color.rgb, rainbow, 0.5);\n" +
            "    gl_FragColor = color * v_color;\n" +
            "}";

        rainbowShader = new ShaderProgram(rainbowVert, rainbowFrag);
        if (!rainbowShader.isCompiled()) {
            Gdx.app.error("Shader", "Rainbow shader compilation failed: " + rainbowShader.getLog());
        }

        // Matrix shader (green tint)
        String matrixFrag = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(u_texture, v_texCoords);\n" +
            "    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));\n" +
            "    color.rgb = vec3(gray * 0.2, gray * 1.0, gray * 0.2);\n" +
            "    gl_FragColor = color * v_color;\n" +
            "}";

        matrixShader = new ShaderProgram(rainbowVert, matrixFrag);

        // Invert shader
        String invertFrag = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(u_texture, v_texCoords);\n" +
            "    color.rgb = 1.0 - color.rgb;\n" +
            "    gl_FragColor = color * v_color;\n" +
            "}";

        invertShader = new ShaderProgram(rainbowVert, invertFrag);

        // Spiral shader for weapon
        String spiralFrag = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform float u_time;\n" +
            "void main() {\n" +
            "    vec2 center = vec2(0.5, 0.5);\n" +
            "    vec2 uv = v_texCoords - center;\n" +
            "    float angle = atan(uv.y, uv.x) + u_time * 2.0;\n" +
            "    float radius = length(uv);\n" +
            "    uv = vec2(cos(angle), sin(angle)) * radius + center;\n" +
            "    vec4 color = texture2D(u_texture, uv);\n" +
            "    gl_FragColor = color * v_color;\n" +
            "}";

        spiralShader = new ShaderProgram(rainbowVert, spiralFrag);
    }

    public void update(float delta) {
        effectTimer += delta;

        // Update rainbow color
        float hue = (effectTimer * 0.3f) % 1f;
        currentRainbowColor.fromHsv(hue * 360f, 0.8f, 1f);
        currentRainbowColor.a = 1f;
    }

    /**
     * Apply visual effects to weapon model
     */
    public void applyWeaponEffects(PlayerWeapon weapon, float delta) {
        if (weapon == null || weapon.viewModel == null) return;

        ModelInstance model = weapon.viewModel;

        // Spiral effect
        if (cheats.spiralWeapon) {
            float spiralAngle = cheats.spiralAngle;
            model.transform.rotate(Vector3.Z, spiralAngle * delta * 2f);
            model.transform.rotate(Vector3.Y, spiralAngle * delta);
        }

        // Rainbow color effect on weapon
        if (cheats.rainbowWorld) {
            // Apply rainbow tint to model materials
            for (int i = 0; i < model.materials.size; i++) {
                model.materials.get(i).set(ColorAttribute.createDiffuse(currentRainbowColor));
            }
        }

        // Big weapon effect
        if (cheats.giantPlayer) {
            model.transform.scale(1.5f, 1.5f, 1.5f);
        }
    }

    /**
     * Apply post-processing effects to the screen
     */
    public void beginPostProcess() {
        if (needsPostProcess()) {
            if (frameBuffer == null || frameBuffer.getWidth() != Gdx.graphics.getWidth()) {
                if (frameBuffer != null) frameBuffer.dispose();
                frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
                    Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            }
            frameBuffer.begin();
        }
    }

    public void endPostProcess() {
        if (needsPostProcess() && frameBuffer != null) {
            frameBuffer.end();

            // Choose shader based on active effect
            ShaderProgram shader = null;
            if (cheats.rainbowWorld && rainbowShader.isCompiled()) {
                shader = rainbowShader;
            } else if (cheats.matrixMode && matrixShader.isCompiled()) {
                shader = matrixShader;
            } else if (cheats.invertedColors && invertShader.isCompiled()) {
                shader = invertShader;
            }

            batch.begin();
            if (shader != null) {
                batch.setShader(shader);
                if (shader == rainbowShader) {
                    shader.setUniformf("u_time", effectTimer);
                }
            }

            // Draw the framebuffer texture
            Texture texture = frameBuffer.getColorBufferTexture();
            batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                0, 0, texture.getWidth(), texture.getHeight(), false, true);

            batch.setShader(null);
            batch.end();
        }

        // Disco mode overlay
        if (cheats.discoMode) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            batch.begin();
            batch.setColor(currentRainbowColor.r, currentRainbowColor.g, currentRainbowColor.b, 0.1f);
            // Would draw a colored overlay here
            batch.end();

            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private boolean needsPostProcess() {
        return cheats.rainbowWorld || cheats.matrixMode || cheats.invertedColors;
    }

    /**
     * Modify environment colors for visual effects
     */
    public void applyEnvironmentEffects(GameWorld world) {
        if (world == null || world.environment == null) return;

        if (cheats.rainbowWorld) {
            // Rainbow fog
            Color fogColor = new Color().fromHsv(cheats.rainbowHue * 360f, 0.5f, 0.8f);
            fogColor.a = 1f;
            world.environment.set(new ColorAttribute(ColorAttribute.Fog,
                fogColor.r, fogColor.g, fogColor.b, 1f));
        }

        if (cheats.matrixMode) {
            // Green matrix fog
            world.environment.set(new ColorAttribute(ColorAttribute.Fog, 0f, 0.1f, 0f, 1f));
            world.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0f, 0.3f, 0f, 1f));
        }
    }

    /**
     * Apply view distance modifications
     */
    public void applyViewDistance(GameWorld world) {
        if (world == null || world.cam == null) return;

        if (cheats.extendedView) {
            world.cam.far = cheats.extendedViewDistance;
            world.viewDistance = cheats.extendedViewDistance;
        } else {
            world.cam.far = 25f;
            world.viewDistance = 25f;
        }
        world.cam.update();
    }

    /**
     * Apply camera shake for earthquake mode
     */
    public Vector3 getCameraShakeOffset() {
        if (!cheats.earthquakeMode) return Vector3.Zero;

        float intensity = 0.15f;
        return new Vector3(
            MathUtils.sin(effectTimer * 25f) * intensity,
            MathUtils.cos(effectTimer * 30f) * intensity * 0.5f,
            MathUtils.sin(effectTimer * 20f) * intensity * 0.3f
        );
    }

    public Color getCurrentRainbowColor() {
        return currentRainbowColor;
    }

    public void dispose() {
        if (batch != null) batch.dispose();
        if (rainbowShader != null) rainbowShader.dispose();
        if (matrixShader != null) matrixShader.dispose();
        if (invertShader != null) invertShader.dispose();
        if (spiralShader != null) spiralShader.dispose();
        if (frameBuffer != null) frameBuffer.dispose();
    }
}
