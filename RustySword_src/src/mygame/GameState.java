// alphamap 128x128, boxsize 128x128, tilemapin koko 128x128
package mygame;

import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioNode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;

public class GameState extends BaseGameStates implements AnalogListener
{
    public static GameState game;
    final static float FLOOR_SIZE = 128;
    final static float BLOODSIZE = 4;
    final static int MAXBLOOD = 100;
    public static final int MAX_CHARACTERS = 100;
    public static final float SWORD_LEN = 3, KICK_LEN = 5, HIT_LEN = 3;
    final static float CAMY = 40, CAMZ = 30;
    public static TileMap map;
    static int _sndTime = 0;
    Geometry bloodSplash;
    int bloodCount = 0;
    BitmapFont font;
    BitmapText talk;
    DirectionalLight light = new DirectionalLight();
    PssmShadowRenderer pssmRenderer;
    Character characters[] = new Character[MAX_CHARACTERS];
    Character self;
    ParticleEmitter blood;
    boolean walk = false, turn = false, attack = false;
    float attackLen = HIT_LEN;
    Picture energyBar;
    int _killed = 0;
    boolean hasSword = false;
    final static String snds[] =
    {
        "punch.wav",
        "sword1.wav",
        "swordecho.wav",
        //-
        "chomp.wav",
        "dart.wav",
        "squish.wav",
        "squish2.wav",
        //--
        "scream.wav",
        "scream2.wav"
    };
    AudioNode snd[] = new AudioNode[snds.length];
    final static String txts[] =
    {
        "Now you die!",
        "You are stupid and ugly!",
        "Die you little sh*t!",
        "Feel the pain!",
        "You will suffer!",
        "I'll drink your blood!",
        "I'll cut your head off!",
        "Meet my sword!",
        "You are too ugly to live!",
        "Say hello to my little sword!",
        "I will eat your soul!",
        "I'll eat your brains!",
        "Suck my sword!",
        "You stink!",
        "Die now!",
        "You look stupid!",
        "I make you bleed!",
        "Nahkurin orsilla tavataan!",
        "I can smell your fear!",
        "I can smell your pants, you sick sh*t!",
        "Your soul is mine!",
        "Die!",
        "Dont run away, coward!",
        "No mercy!"
    };

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        if (super.isInitialized())
            return;
        super.initialize(stateManager, app);

        _killed = 0;
        this.game = this;

        if (Settings.useSounds)
            for (int q = 0; q < snds.length; q++)
            {
                snd[q] = new AudioNode(assetManager, "Audio/" + snds[q], false);
                snd[q].setLooping(false);
            }

        font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        talk = new BitmapText(font, false);
        talk.setBox(new Rectangle(0, 0, Settings.Width, Settings.Height));
        talk.setSize(font.getPreferredSize());
        guiNode.attachChild(talk);

        BitmapText txt = new BitmapText(font, false);
        txt.setBox(new Rectangle(0, 0, Settings.Width, Settings.Height));
        txt.setSize(font.getPreferredSize());
        txt.setLineWrapMode(LineWrapMode.Word);
        txt.setLocalTranslation(64 + 10, Settings.Height - 5, 0);
        txt.setText("Lord Drunkard");
        guiNode.attachChild(txt);

        Picture p = new Picture("profile");
        p.move(0, 0, -1); // make it appear behind stats view
        p.setPosition(5, Settings.Height - 64 - 5);
        p.setWidth(64);
        p.setHeight(64);
        p.setImage(assetManager, "Textures/profile.jpg", false);
        guiNode.attachChild(p);        // attach geometry to orthoNode

        energyBar = new Picture("energybar");
        energyBar.move(0, 0, -1);
        energyBar.setPosition(64 + 10, Settings.Height - 5 - 40);
        energyBar.setWidth(0);
        energyBar.setHeight(20);
        energyBar.setImage(assetManager, "Textures/energy.png", false);
        guiNode.attachChild(energyBar);

        map = new TileMap();
        rootNode.attachChild(map.load("Textures/map_128.map"));

        for (int q = 0; q < MAX_CHARACTERS; q++)
        {
            characters[q] = new Character();
            rootNode.attachChild(characters[q].load("Models/Ninja.j3o"));
            if (q > 0)
            {
                int x, z;
                while (true)
                {
                    x = Main.rnd.nextInt((int) FLOOR_SIZE) * 2;
                    z = Main.rnd.nextInt((int) FLOOR_SIZE) * 2;
                    if (map.getObjAt(x, z) == -1)
                    {
                        break;
                    }
                }
                characters[q].spatial.setLocalTranslation(x, 0, z);
                characters[q].updateMaterial();
                float s = 0.015f + Main.rnd.nextFloat() * 0.015f;
                characters[q].spatial.scale(s, 0.02f, s);

                AIControl ai = new AIControl(characters[q]);
                characters[q].spatial.addControl(ai);
            }
        }
        self = characters[0];

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture(new TextureKey("Models/Ninja3.jpg", false)));
        self.spatial.setMaterial(mat);
        self.spatial.setShadowMode(ShadowMode.CastAndReceive);
        self.spatial.setLocalTranslation(map.getPlayerPos());
        self.spatial.scale(0.02f);
        self.energy = 1000;
        Geometry n = (Geometry) (((Node) self.spatial).getChild("Ninja-geom-2"));
        n.setCullHint(CullHint.Always); // miekka piiloon


        light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
        rootNode.addLight(light);

        cam.setLocation(new Vector3f(0, CAMY, CAMZ));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        setupFloor();
        setupInput();
        setupPSSM();
        //setupSkyBox();
        //setupFog();

    }

    void setupFloor()
    {
        final float GRASS_SCALE = 10, DIRT_SCALE = 40, ROCK_SCALE = 80;

        Material floorMat = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

        // ALPHA map (for splat textures)
        floorMat.setTexture("Alpha", assetManager.loadTexture(new TextureKey("Textures/Terrain/alphamap.png", true)));

        // GRASS texture (RED)
        Texture grass = assetManager.loadTexture("Textures/Terrain/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        floorMat.setTexture("Tex1", grass);
        floorMat.setFloat("Tex1Scale", GRASS_SCALE);

        // DIRT texture (GREEN)
        Texture dirt = assetManager.loadTexture("Textures/Terrain/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        floorMat.setTexture("Tex2", dirt);
        floorMat.setFloat("Tex2Scale", DIRT_SCALE);

        // ROCK texture (BLUE)
        Texture rock = assetManager.loadTexture("Textures/Terrain/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        floorMat.setTexture("Tex3", rock);
        floorMat.setFloat("Tex3Scale", ROCK_SCALE);

        floorMat.getTextureParam("Tex1").getTextureValue().setWrap(WrapMode.MirroredRepeat);
        floorMat.getTextureParam("Tex2").getTextureValue().setWrap(WrapMode.MirroredRepeat);
        floorMat.getTextureParam("Tex3").getTextureValue().setWrap(WrapMode.MirroredRepeat);

        Box floorBox = new Box(FLOOR_SIZE, 1, FLOOR_SIZE);
        Geometry floorGeometry = new Geometry("floor", floorBox);
        floorGeometry.setMaterial(floorMat);

        floorGeometry.setLocalTranslation(FLOOR_SIZE, -1, FLOOR_SIZE);
        floorGeometry.rotate(0, -FastMath.DEG_TO_RAD * 90, 0);
        floorGeometry.setShadowMode(ShadowMode.Receive);
        rootNode.attachChild(floorGeometry);
    }

    void createBloodSplash()
    {
        if (Main.rnd.nextFloat() > 0.2)
            return;

        Quad bloodQuad = new Quad(BLOODSIZE, BLOODSIZE);
        bloodSplash = new Geometry("bloodQuad" + bloodCount, bloodQuad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/bloodsplat.png"));
        mat.setColor("Color", new ColorRGBA(1, 1, 1, 0.2f));

        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false);
        bloodSplash.setMaterial(mat);
        bloodSplash.setQueueBucket(Bucket.Transparent);
        bloodSplash.rotate(FastMath.DEG_TO_RAD * -90, Main.rnd.nextFloat(), 0);
        bloodSplash.setLocalTranslation(Main.rnd.nextFloat() + self.spatial.getLocalTranslation().x - BLOODSIZE / 3,
                0.2f,
                Main.rnd.nextFloat() + self.spatial.getLocalTranslation().z + BLOODSIZE / 2);

        rootNode.attachChild(bloodSplash);
        bloodCount++;

        if (bloodCount >= MAXBLOOD)
            rootNode.detachChildNamed("bloodQuad" + (bloodCount - MAXBLOOD));

    }

    void setupFog()
    {
        FilterPostProcessor fpp;
        FogFilter fog;
        fpp = new FilterPostProcessor(assetManager);
        fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        fog.setFogDistance(200);
        fog.setFogDensity(1.0f);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);
    }

    void talk(String txt, Vector3f pos, boolean forceSay)
    {
        if (talkTime > 0)
            return;

        if (Main.rnd.nextFloat() > 0.01f && forceSay == false)
            return;

        Vector3f v = cam.getScreenCoordinates(new Vector3f(pos.x - 2, pos.y + 5f, pos.z));
        talk.setLocalTranslation(v);
        talk.setText(txt);
        talkTime = (float) txt.length() * 0.1f;
        _pos = pos;
    }
    float talkTime = 0;
    Vector3f _pos;

    void talkUpdate(float tpf)
    {
        if (talkTime <= 0)
            talk.setText(" ");
        else
        {
            Vector3f v = cam.getScreenCoordinates(new Vector3f(_pos.x - 2, _pos.y + 5f, _pos.z));
            talk.setLocalTranslation(v);
            talkTime -= tpf;
        }
    }

    void setupPSSM()
    {
        if (Settings.useShadows == false)
            return;

        pssmRenderer = new PssmShadowRenderer(assetManager, 512, 2);
        pssmRenderer.setDirection(light.getDirection());
        pssmRenderer.setLambda(0.8f);
        pssmRenderer.setShadowIntensity(0.8f);
        pssmRenderer.setCompareMode(CompareMode.Software);
        pssmRenderer.setFilterMode(FilterMode.PCF8);
        viewPort.addProcessor(pssmRenderer);
    }

    public void setupBlood(int id)
    {
        if (rootNode.getChild("blood" + id) != null)
        {
            return;
        }
        if (Settings.useSounds)
            snd[3 + Main.rnd.nextInt(4)].playInstance();

        blood = new ParticleEmitter("blood" + id, ParticleMesh.Type.Triangle, 10);
        Material blood_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        blood_mat.setTexture("Texture", assetManager.loadTexture("Textures/blood.png"));
        blood_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        blood.setMaterial(blood_mat);
        blood.setImagesX(3);
        blood.setImagesY(3); // 3x3 texture animation
        blood.setRotateSpeed(4);
        blood.setSelectRandomImage(true);
        blood.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2 + Main.rnd.nextInt(2), 0));
        blood.setStartColor(new ColorRGBA(0.8f, 0f, 0f, 0.5f));
        blood.setEndColor(new ColorRGBA(0.5f, 0f, 0f, 0.2f));
        blood.setGravity(0f, 9f, 0f);
        blood.getParticleInfluencer().setVelocityVariation(.60f);
        blood.setParticlesPerSec(0);
        blood.setStartSize(0.2f);
        blood.setLowLife(0.1f);
        blood.setHighLife(1f);

        blood.setLocalTranslation(characters[id].spatial.getLocalTranslation().x,
                3 + Main.rnd.nextInt(2),
                characters[id].spatial.getLocalTranslation().z);

        rootNode.attachChild(blood);
        blood.emitAllParticles();

    }

    public void setupSkyBox()
    {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/BrightSky.dds", false));
    }

    void setupInput()
    {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping("Hit", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Kick", new KeyTrigger(KeyInput.KEY_Q));

        inputManager.addMapping("ESC", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this, "Up", "Down", "Left", "Right", "Hit", "Kick", "ESC");
    }

    void checkHits(int id)
    {
        if (characters[id].energy <= 0)
        {
            return;
        }

        Vector3f v1 = characters[id].spatial.getLocalTranslation().subtract(self.spatial.getLocalTranslation());
        float len = v1.length();
        if (len < attackLen)
        {
            v1 = v1.normalize();
            float ang = v1.dot(self.getDirection(1).normalize());
            if (ang > 0.9f)
            {
                if (Main.rnd.nextDouble() > 0.5)
                {
                    if (attackLen == SWORD_LEN && hasSword)
                    {
                        setupBlood(id); // roiskukoon veri
                        characters[id].energy -= 1 + Main.rnd.nextDouble() * 2;
                        characters[id].mode = Character.Mode.ATTACK;
                        talk(txts[Main.rnd.nextInt(txts.length)], characters[id].spatial.getLocalTranslation(), false);
                        createBloodSplash();
                    }
                    if (attackLen == KICK_LEN)
                    {
                        Vector3f nt = characters[id].spatial.getLocalTranslation().add(self.getDirection(1.0f));
                        characters[id].spatial.setLocalTranslation(nt);
                        characters[id].mode = Character.Mode.WALK_NEAR;
                    }
                    if (attackLen == HIT_LEN && !hasSword)
                    {
                        characters[id].energy -= 1 * Main.rnd.nextDouble();
                        characters[id].mode = Character.Mode.ATTACK;
                        if (Settings.useSounds)
                            if (_sndTime++ > 200)
                            {
                                _sndTime = 0;
                                snd[0].playInstance();
                            }
                    }

                    if (characters[id].energy <= 0)
                    {
                        _killed++;
                        talkTime = 0;


                        characters[id].setAnimation("Death" + (1 + Main.rnd.nextInt(2)));
                        characters[id].animationChannel.setLoopMode(LoopMode.DontLoop);
                        if (Settings.useSounds)
                            snd[7 + Main.rnd.nextInt(2)].playInstance();
                        if (hasSword == false)
                        {
                            Geometry n = (Geometry) (((Node) self.spatial).getChild("Ninja-geom-2"));
                            n.setCullHint(CullHint.Dynamic); // napataan kuolleelta miekka
                            hasSword = true;

                            talkTime = 0;
                            talk("Yeah, now I have a sword!", self.spatial.getLocalTranslation(), true);
                        }


                        int l = 0;
                        for (int c = 0; c < MAX_CHARACTERS; c++)
                        {
                            if (characters[c].energy > 0)
                                l++;
                        }
                        if (l == 1) // vain pelaaja on hengissä
                        {
                            talkTime = 0;
                            talk("Haaa, I killed them all! Now I can die with peace.", self.spatial.getLocalTranslation(), true);
                        }

                    }
                }
            }
        }
    }
    boolean playerTalk = true;

    @Override
    public void update(float tpf)
    {
//        flyCam.setMoveSpeed(100); flyCam.setEnabled(true);  //-- DEBUG CAM

        cam.setLocation(new Vector3f(self.spatial.getLocalTranslation().add(new Vector3f(0, CAMY, CAMZ))));

        if (playerTalk)
        {
            Vector3f v = new Vector3f(self.spatial.getLocalTranslation());
            v.x -= 3;
            v.y = 2;
            talkTime = 0;
            talk("Those bastards killed my family!\nI must kill them all!\n", v, true);
        } else
            talkUpdate(tpf);

        if (self.energy <= 0)
        {
            if (self.animationChannel.getAnimationName().contains("Death") == false)
            {
                energyBar.setWidth(0);
                self.setAnimation("Death" + (1 + Main.rnd.nextInt(2)));
                self.animationChannel.setLoopMode(LoopMode.DontLoop);
                if (Settings.useSounds)
                    snd[7 + Main.rnd.nextInt(2)].playInstance();
            }

            talkTime = 0;
            Vector3f v = new Vector3f(self.spatial.getLocalTranslation());
            v.x -= 2;
            v.y = 5;
            talk("--- You failed! You killed " + _killed + "! Press ESC ---", v, true);

        } else
        {
            energyBar.setWidth(self.energy / 10);
        }

        // poistetaan turhat parkikkelit
        for (int q = 0; q < MAX_CHARACTERS; q++)
        {
            ParticleEmitter pe = (ParticleEmitter) rootNode.getChild("blood" + q);
            if (pe != null)
            {
                if (pe.getNumVisibleParticles() <= 0)
                    rootNode.detachChild(pe);
            }
        }

        if (walk == false && turn == false && attack == false && self.energy > 0)
        {
            if (self.animationChannel.getAnimationName().contains("Idle") == false)
            {
                self.setAnimation("Idle" + (1 + Main.rnd.nextInt(3)));
            }
        }
        if (attack)
        {
            for (int q = 1; q < MAX_CHARACTERS; q++)
            {
                checkHits(q);
            }
        }

        walk = turn = attack = false;

        super.update(tpf);
    }

    public void onAnalog(String binding, float f, float tpf)
    {
        if (binding.equals("ESC"))
        {
            dispose();
            inputManager.clearMappings();
            new MenuState().initialize(null, app);
        }

        if (self.energy <= 0)
        {
            return;
        }

        if (!attack && binding.equals("Up"))
        {
            self.tryMoveForward(self.walkSpeed * tpf);
            self.setAnimation("Walk");
            walk = true;
        }
        if (!attack && binding.equals("Down"))
        {
            self.tryMoveForward(-self.walkSpeed * tpf);
            self.setAnimation("Backflip");
            walk = true;
        }
        if (binding.equals("Left"))
        {
            self.rotateAdd(0, tpf * 300, 0);
            turn = true;
        }
        if (binding.equals("Right"))
        {
            self.rotateAdd(0, -tpf * 300, 0);
            turn = true;
        }

        if (!walk && binding.equals("Hit"))
        {
            playerTalk = false;

            if (self.animationChannel.getAnimationName().contains("Attack") == false)
            {
                self.setAnimation("Attack" + (1 + Main.rnd.nextInt(3)));
            }
            attack = true;
            if (hasSword)
                attackLen = SWORD_LEN;
            else
                attackLen = HIT_LEN;
        }
        if (!walk && binding.equals("Kick"))
        {
            self.setAnimation("Kick");
            attack = true;
            attackLen = KICK_LEN;
        }
    }
}
