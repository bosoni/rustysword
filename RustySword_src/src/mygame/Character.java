package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Character
{
    Spatial spatial;
    private Vector3f rotation = new Vector3f();
    private Vector3f direction = new Vector3f();
    AnimChannel animationChannel;
    AnimControl animationControl;
    float blendTime = 0.5f;
    int energy = 100;
    float walkSpeed = 4.5f;

    public enum Mode
    {
        IDLE, WALK_RANDOM, WALK_PATH, WALK_NEAR, ATTACK
    };
    public Mode mode = Mode.IDLE;

    public Spatial load(String name)
    {
        spatial = (Spatial) Main.app.getAssetManager().loadModel(name);
        animationControl = spatial.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();
        animationChannel.setLoopMode(LoopMode.Loop);
        animationChannel.setTime(Main.rnd.nextFloat() * 100);
        animationChannel.setSpeed(Main.rnd.nextFloat() + 0.5f);
        wake();
        return spatial;
    }

    void wake()
    {
        energy = 100 + Main.rnd.nextInt(50);
        walkSpeed = 4 + Main.rnd.nextFloat() * 1;
        setAnimation("Idle1");
        mode = Mode.IDLE;
    }

    public void setAnimation(String name)
    {
        if (name.equals(animationChannel.getAnimationName()))
        {
            return;
        }
        animationChannel.setAnim(name, blendTime);
    }

    public Vector3f getDirection(float scale)
    {
        scale = -scale;
        direction.x = scale * ((float) Math.sin(rotation.y) * (float) Math.cos(-rotation.x));
        direction.y = scale * ((float) Math.sin(-rotation.x));
        direction.z = scale * ((float) Math.cos(rotation.y) * (float) Math.cos(-rotation.x));
        return direction;
    }

    public void lookAt(Vector3f pos)
    {
        spatial.lookAt(pos, Vector3f.UNIT_Y);
        spatial.rotate(0, 180 * FastMath.DEG_TO_RAD, 0);
        float[] ang = spatial.getLocalRotation().toAngles(null);
        rotation.set(ang[0], ang[1], ang[2]);
    }

    public void rotateAdd(float x, float y, float z)
    {
        x *= FastMath.DEG_TO_RAD;
        y *= FastMath.DEG_TO_RAD;
        z *= FastMath.DEG_TO_RAD;
        rotation = rotation.add(x, y, z);
        spatial.rotate(x, y, z);
    }

    public void moveForward(float f)
    {
        spatial.move(getDirection(f));
    }

    public boolean tryMoveForward(float f)
    {
        boolean ok = true;

        // tarkistetaan x ja z suunnassa voiko liikkua
        Vector3f check = new Vector3f(getDirection(f * 10));
        getDirection(f);

        if (GameState.map.getObjAt(spatial.getLocalTranslation().x + 1, spatial.getLocalTranslation().z + check.z + 1) == -1
                && GameState.map.getObjAt(spatial.getLocalTranslation().x + 1, spatial.getLocalTranslation().z + direction.z + 1) == -1)
        {
            spatial.move(0, 0, direction.z);
        } else
        {
            ok = false;
        }

        if (GameState.map.getObjAt(spatial.getLocalTranslation().x + check.x + 1, spatial.getLocalTranslation().z + 1) == -1
                && GameState.map.getObjAt(spatial.getLocalTranslation().x + direction.x + 1, spatial.getLocalTranslation().z + 1) == -1)
        {
            spatial.move(direction.x, 0, 0);
        } else
        {
            ok = false;
        }

        return ok;
    }

    public void updateMaterial()
    {
        updateMaterial(spatial);
    }

    private void updateMaterial(Spatial spatial)
    {
        try
        {
            if (spatial instanceof Node)
            {
                Node node = (Node) spatial;
                for (int i = 0; i < node.getQuantity(); i++)
                {
                    updateMaterial(node.getChild(i));
                }
            } else if (spatial instanceof Geometry)
            {
                Geometry geo = (Geometry) spatial;
                geo.setShadowMode(ShadowMode.CastAndReceive);
                Material material = geo.getMaterial();

                material.setColor("Ambient", ColorRGBA.randomColor());
                material.setColor("Diffuse", ColorRGBA.randomColor());
                material.setBoolean("UseMaterialColors", true);
            }
        } catch (Exception err)
        {
            Main.printError(err.toString());
        }
    }
}
