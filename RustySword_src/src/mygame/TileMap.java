package mygame;

import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import mygame.path.GridLocation;
import mygame.path.GridMap;
import mygame.path.GridPath;
import mygame.path.GridPathfinding;

public class TileMap
{
    public int AREASIZE = 32;
    public String[] files =
    {
        "wall.j3o", // A
        "obj1.j3o", // B
        "obj2.j3o",
        "tree.j3o",
        "obj3.j3o"
    };
    private static boolean _init = false;
    private byte map[][];
    private GridMap gmap;
    private GridPathfinding path = new GridPathfinding();
    private Vector3f playerPos = new Vector3f();

    public TileMap()
    {
        if (!_init)
        {
            Main.app.getAssetManager().registerLoader(TextLoader.class, "map");
            _init = true;
        }

    }

    public Node load(String fileName)
    {
        String mapStr = (String) Main.app.getAssetManager().loadAsset(fileName);
        return createTileMap(mapStr);
    }

    Node createTileMap(String mapstr)
    {
        Node mapNode = new Node("map");
        String lines[] = mapstr.split("\n");
        map = new byte[lines.length][lines[0].length()];
        gmap = new GridMap(lines[0].length(), lines.length);

        for (int y = 0; y < lines.length; y++)
        {
            for (int x = 0; x < lines[0].length() - 1; x++)
            {
                if (lines[y].charAt(x) == ' ')
                {
                    map[y][x] = -1;
                    gmap.set(x, y, 1);
                } else if (lines[y].charAt(x) == (char) 'Q')
                {
                    map[y][x] = -1;
                    gmap.set(x, y, 1);
                    playerPos.set(x * 2, 0, y * 2);
                } else
                {
                    map[y][x] = (byte) (lines[y].charAt(x) - 'A');
                    gmap.set(x, y, GridMap.WALL);

                    //Spatial obj = (Spatial) Main.app.getAssetManager().loadModel("Models/" + files[map[y][x]]);
                    //obj.setLocalTranslation(x * 2, 0, y * 2);
                    //mapNode.attachChild(obj); // TAPA 1
                }
            }
        }
        //jme3tools.optimize.GeometryBatchFactory.optimize(mapNode); // TAPA 1


        //--
        for (int y = 0; y < lines.length; y += AREASIZE)
        {
            for (int x = 0; x < lines[0].length(); x += AREASIZE)
            {
                mapNode.attachChild(createArea(x, y, AREASIZE));
            }
        }
        mapNode.setShadowMode(ShadowMode.CastAndReceive);

        return mapNode;
    }

    Node createArea(int x, int y, int size)
    {
        Node areaNode = new Node();
        for (int yy = y; yy < y + size; yy++)
        {
            if (yy >= map.length)
            {
                break;
            }
            for (int xx = x; xx < x + size; xx++)
            {
                if (xx >= map[0].length)
                {
                    break;
                }

                int id = map[yy][xx];
                if (id == -1)
                {
                    continue;
                }
                Spatial obj = (Spatial) Main.app.getAssetManager().loadModel("Models/" + files[id]);
                obj.setLocalTranslation(xx * 2, 0, yy * 2);
                if (id == 3)
                {
                    obj.setLocalScale(3); // tree
                }
                // seinät skaalataan, kulmia ei
                if (id == 0) // 0==wall
                {
                    try
                    {
                        if (map[yy][xx - 1] == 0 && map[yy][xx + 1] == 0
                                && map[yy - 1][xx] != 0 && map[yy + 1][xx] != 0)
                        {
                            obj.setLocalScale(1, 1, 0.5f);
                        } else if (map[yy][xx - 1] != 0 && map[yy][xx + 1] != 0
                                && map[yy - 1][xx] == 0 && map[yy + 1][xx] == 0)
                        {
                            obj.setLocalScale(0.5f, 1, 1);
                        }


                    } catch (Exception e)
                    {
                    }
                }

                areaNode.attachChild(obj);
            }
        }
        jme3tools.optimize.GeometryBatchFactory.optimize(areaNode);
        return areaNode;
    }

    public Vector3f getPlayerPos()
    {
        return playerPos;
    }

    public byte getObjAt(float px, float py)
    {
        int xx = (int) (px / 2);
        int yy = (int) (py / 2);
        if (xx <= 0 || yy <= 0 || xx >= map[0].length || yy >= map.length)
        {
            return 1;
        }
        return map[yy][xx];
    }

    public byte getObjAt(Character c)
    {
        return getObjAt(c.spatial.getLocalTranslation().x, c.spatial.getLocalTranslation().z);
    }

    public Vector3f[] getPath(Vector3f start, Vector3f end)
    {
        GridLocation st = new GridLocation((int) start.x / 2, (int) start.z / 2, false);
        GridLocation en = new GridLocation((int) end.x / 2, (int) end.z / 2, false);;

        GridPath gp = path.getPath(en, st, gmap);
        if (gp == null)
        {
            return null;
        }
        ArrayList<GridLocation> list = gp.getList();

        Vector3f[] vp = new Vector3f[list.size()];
        int c = 0;
        for (GridLocation l : list)
        {
            vp[c++] = new Vector3f(l.getX() * 2, 0, l.getY() * 2);
        }
        return vp;
    }
}
