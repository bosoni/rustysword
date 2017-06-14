package mygame;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteLoader implements AssetLoader
{
    public static int SIZE = 64 * 64;

    public Object load(AssetInfo assetInfo) throws IOException
    {
        InputStream in = assetInfo.openStream();
        ByteBuffer bb = ByteBuffer.allocate(SIZE);
        byte[] data = new byte[SIZE];
        try
        {
            int read;
            do
            {
                read = in.read(data, 0, data.length);
                if (read > 0)
                {
                    bb.put(data, 0, read);
                }
            } while (read >= 0);
        }
        finally
        {
            in.close();
        }
        return bb;
    }
}
