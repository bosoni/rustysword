package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends SimpleApplication
{
    public static Main app;
    public static final Random rnd = new Random();

    public static void main(String[] args)
    {
        System.out.println("Rusty Sword\n(c) mjt, 2012 [mixut@hotmail.com]\n");
        app = new Main();
    }

    public static void printError(String str)
    {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, str, str);
    }

    public static void println(String str)
    {
        System.out.println(str);
    }

    public Main()
    {
        Settings.DEBUG = false;
        try
        {
            FileHandler handler = new FileHandler("log.txt");
            if (Settings.DEBUG)
                Logger.getLogger(Main.class.getName()).setLevel(Level.WARNING);
            else
                Logger.getLogger(Main.class.getName()).setLevel(Level.SEVERE);

            Logger.getLogger(Main.class.getName()).addHandler(handler);
        } catch (Exception ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (false)
        {
            setShowSettings(false);
            AppSettings settings = new AppSettings(true);
            settings.put("Width", 1024);
            settings.put("Height", 768);
            settings.put("Title", "Rusty Sword");
            //settings.put("VSync", true);
            settings.put("Samples", 0);
            setSettings(settings);
        } else
        {
            AppSettings settings = new AppSettings(false);
            settings.put("Width", 1024);
            settings.put("Height", 768);
            settings.put("Title", "Rusty Sword");
            settings.setSettingsDialogImage("Textures/rustys.png");
            setShowSettings(true);
            setSettings(settings);
        }

        start();
    }

    @Override
    public void simpleInitApp()
    {
        if (Settings.DEBUG == false)
        {
            setDisplayFps(false); // to hide the FPS
            setDisplayStatView(false); // to hide the statistics
        }
        mouseInput.setCursorVisible(false);

        Settings.Width = settings.getWidth();
        Settings.Height = settings.getHeight();

        new MenuState().initialize(null, this);
    }
}
