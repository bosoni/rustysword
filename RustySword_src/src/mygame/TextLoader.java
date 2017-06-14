package mygame;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;
import java.util.Scanner;

public class TextLoader implements AssetLoader
{
    public Object load(AssetInfo assetInfo) throws IOException
    {
        Scanner scan = new Scanner(assetInfo.openStream());
        StringBuilder sb = new StringBuilder();
        try
        {
            while (scan.hasNextLine())
            {
                sb.append(scan.nextLine()).append('\n');
            }
        }
        finally
        {
            scan.close();
        }
        return sb.toString();
    }
}
