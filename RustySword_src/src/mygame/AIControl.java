package mygame;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mygame.Character.Mode;

public class AIControl extends AbstractControl
{
    Character c;
    Vector3f path[] = null;
    float posOnPath = 0;

    public AIControl(Character character)
    {
        c = character;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        if (c.energy <= 0)
        {
            path = null;
            posOnPath = 0;
            return;
        }

        Node n=(Node)c.spatial;
        n.updateModelBound();

        Vector3f v1 = spatial.getLocalTranslation().subtract(GameState.game.self.spatial.getLocalTranslation());
        float len = v1.length();

        if (len > 100)
            return;


        for (int q = 1; q < GameState.game.characters.length; q++)
        {
            if (spatial.equals(GameState.game.characters[q].spatial) || c.mode == Mode.ATTACK || c.energy <= 0)
                continue;

            Vector3f v2 = spatial.getLocalTranslation().subtract(GameState.game.characters[q].spatial.getLocalTranslation());
            float len2 = v2.length();

            if (len2 < 3)
            {
                c.mode = Mode.WALK_RANDOM;
            }
        }

        switch (c.mode)
        {
            case IDLE:
                c.setAnimation("Idle1");
                if (Main.rnd.nextFloat() > 0.99f)
                {
                    c.mode = Mode.WALK_RANDOM; // lähtee käppäilemään
                }
                if (Main.rnd.nextFloat() > 0.99f)
                {
                    c.mode = Mode.WALK_NEAR; // lähtee hyökkäämään
                }
                break;

            case WALK_PATH:
                if (path == null)
                {
                    int x, z;
                    x = Main.rnd.nextInt((int) GameState.FLOOR_SIZE) * 2;
                    z = Main.rnd.nextInt((int) GameState.FLOOR_SIZE) * 2;
                    if (GameState.map.getObjAt(x, z) == -1)
                    {
                        path = GameState.map.getPath(spatial.getLocalTranslation(), new Vector3f(x, 0, z));
                        if (path == null)
                        {
                            c.mode = Mode.IDLE;
                        }
                        else
                        {
                            c.setAnimation("Walk");
                            c.lookAt(path[1]);
                        }

                    }
                    else
                        c.mode = Mode.IDLE;

                    return;
                }
                else
                {
                    int oldpos = (int) posOnPath;
                    posOnPath += tpf * c.walkSpeed / 2;

                    int newpos = (int) posOnPath;
                    if (newpos >= path.length - 1)
                    {
                        c.mode = Mode.IDLE;
                        posOnPath = 0;
                        path = null;
                        return;
                    }

                    if (oldpos != newpos) // lasketaan uus asento vain kun tarvitsee
                    {
                        newpos++;
                        c.lookAt(path[newpos]);
                        spatial.setLocalTranslation(path[oldpos + 1]);
                        return;
                    }

                    newpos++;
                    Vector3f vec = path[newpos].subtract(path[oldpos]);
                    Vector3f np = path[oldpos].add(vec.mult(posOnPath - (int) posOnPath));
                    spatial.setLocalTranslation(np);
                }

                break;

            case WALK_RANDOM:
                c.setAnimation("Walk");
                if (c.tryMoveForward(tpf * c.walkSpeed) == false)
                {
                    c.rotateAdd(0, 180, 0);
                }

                if (Main.rnd.nextFloat() > 0.999f)
                {
                    c.mode = Mode.IDLE;
                    return;
                }

                if (Main.rnd.nextFloat() > 0.999)
                {
                    if (((int) c.spatial.getLocalTranslation().x / 2) == 0
                            && ((int) c.spatial.getLocalTranslation().y / 2) == 0)
                        c.mode = Mode.WALK_PATH;
                    return;
                }

                if (Main.rnd.nextFloat() > 0.99f)
                {
                    if (Main.rnd.nextFloat() > 0.5f)
                    {
                        c.rotateAdd(0, 100 * tpf, 0);
                    }
                    else
                    {
                        c.rotateAdd(0, -100 * tpf, 0);
                    }

                    if (Main.rnd.nextFloat() > 0.7f)
                    {
                        c.rotateAdd(0, Main.rnd.nextFloat() * 100, 0);
                    }
                }
                break;

            case ATTACK:
                if (GameState.game.self.animationChannel.getAnimationName().contains("Death")) // jos ollaan kuoltu, ei tarvii tulla tänne
                {
                    c.mode = Mode.WALK_RANDOM;
                    return;
                }

                if (c.animationChannel.getAnimationName().contains("Attack") == false)
                {
                    c.setAnimation("Attack" + (1 + Main.rnd.nextInt(3)));
                }

                if (c.energy < 30 && Main.rnd.nextFloat() > 0.99f)
                {
                    c.mode = Mode.WALK_RANDOM; // pakenee paikalta
                }
                else
                {
                    // vihollisen kääntyy pelaajaan päin
                    c.lookAt(GameState.game.self.spatial.getLocalTranslation());

                    if (len > GameState.SWORD_LEN)
                    {
                        c.mode = Mode.WALK_NEAR;
                    }
                    else
                    {
                        if (Settings.useSounds)
                            if (GameState._sndTime++ > 100 + Main.rnd.nextInt(200))
                            {
                                if (GameState.game.hasSword)
                                    GameState.game.snd[Main.rnd.nextInt(3)].playInstance();

                                GameState._sndTime = 0;
                            }

                        if (Main.rnd.nextFloat() < 0.2f)
                        {
                            GameState.game.self.energy -= Main.rnd.nextFloat() * 2 + 1;
                            GameState.game.setupBlood(0);
                            GameState.game.createBloodSplash();
                        }
                    }
                }

                break;

            case WALK_NEAR:
                if (len > 30)
                {
                    c.mode = Mode.WALK_RANDOM;
                    return;
                }

                // vihollisen kääntyy pelaajaan päin
                c.lookAt(GameState.game.self.spatial.getLocalTranslation());
                GameState.game.talk(GameState.txts[Main.rnd.nextInt(GameState.txts.length)], spatial.getLocalTranslation(), false);
                if (len < GameState.SWORD_LEN)
                {
                    if (GameState.game.attackLen != GameState.KICK_LEN)
                    {
                        c.mode = Mode.ATTACK;
                    }
                }
                else
                {
                    c.setAnimation("Walk");
                    if (c.tryMoveForward(tpf * c.walkSpeed) == false)
                    {
                        c.rotateAdd(0, 180, 0);
                        c.mode = Mode.WALK_RANDOM;
                        GameState.game.talkTime = 0;
                    }
                }
                break;

        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }

    public Control cloneForSpatial(Spatial spatial)
    {
        return null;
    }
}
