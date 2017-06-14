package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.ui.Picture;

public class MenuState extends BaseGameStates implements ActionListener
{
    float col = 0;
    int selected = 0;
    BitmapFont font;
    BitmapText txt, selectedText;
    int screen = 0;
    static String[] m1 =
    {
        "Start", "Options", "Help", "Credits", "Exit"
    };
    static String[] m2 =
    {
        "Shadows Enabled", "Sounds Enabled", "Back"
    };
    String[] menu = m1;

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        if (super.isInitialized())
            return;
        super.initialize(stateManager, app);

        Picture p = new Picture("back");
        p.move(0, 0, -1); // make it appear behind stats view
        p.setPosition(0, 0);
        p.setWidth(Settings.Width);
        p.setHeight(Settings.Height);
        p.setImage(assetManager, "Textures/rusty.jpg", false);
        guiNode.attachChild(p);        // attach geometry to orthoNode

        inputManager.clearMappings();
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Enter", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Up", "Down", "Enter");

        font = assetManager.loadFont("Interface/Fonts/Impact.fnt");
        txt = new BitmapText(font, false);
        txt.setBox(new Rectangle(0, 0, Settings.Width, Settings.Height));
        txt.setSize(font.getPreferredSize() * 3);
        txt.setLineWrapMode(LineWrapMode.Word);
        txt.setAlignment(BitmapFont.Align.Center);
        txt.setLocalTranslation(0, 400, 0);
        txt.setColor(ColorRGBA.White);
        guiNode.attachChild(txt);

        selectedText = new BitmapText(font, false);
        selectedText.setBox(new Rectangle(0, 0, Settings.Width, Settings.Height));
        selectedText.setSize(font.getPreferredSize() * 3);
        selectedText.setLineWrapMode(LineWrapMode.Word);
        selectedText.setAlignment(BitmapFont.Align.Center);
        selectedText.setLocalTranslation(0, 400, 0);
        selectedText.setColor(ColorRGBA.Green);
        guiNode.attachChild(selectedText);

        flyCam.setEnabled(false);
    }

    @Override
    public void update(float tpf)
    {
        try
        {
            Thread.sleep(10);
        }
        catch (Exception e)
        {
        }

        if (screen == 0 || screen == 1) // menu
        {
            String tex = "", seltex = "";
            for (int q = 0; q < menu.length; q++)
            {
                if (q == selected)
                {
                    seltex += menu[q] + "\n";
                    tex += "\n";
                }
                else
                {
                    seltex += "\n";
                    tex += menu[q] + "\n";
                }
            }
            txt.setText(tex);
            selectedText.setText(seltex);
        }
        else if (screen == 2) // help
        {
            txt.setText("Keys:\nArrow keys to move player.\nQ         :  kick\nSPACE :  attack");
            selectedText.setText("\n\n\n\nBack");
        }
        else if (screen == 3) // credits
        {
            txt.setText("Programming by mjt\n3D models by Psionic and mjt\nFree sounds from the internet\nPowered by jMonkeyEngine3");
            selectedText.setText("\n\n\n\nBack");
        }

        float s = ((float) Math.sin(col) + 1) * 0.5f;
        selectedText.setColor(new ColorRGBA(0, 0.5f, s, 1));
        col += tpf * 2;

        super.update(tpf);
    }

    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (name.contains("Up") && isPressed)
        {
            if (selected > 0)
                selected--;
        }
        if (name.contains("Down") && isPressed)
        {
            if (selected < menu.length - 1)
                selected++;
        }
        if (name.contains("Enter") && isPressed)
        {
            if (screen == 0)
            {
                // "Start", "Options", "Help", "Credits", "Exit"
                if (selected == 0)
                {
                    dispose();
                    inputManager.clearMappings();
                    new GameState().initialize(null, app);
                }
                if (selected == 1) // options
                {
                    menu = m2;
                    screen = 1;
                    selected = 0;
                }
                if (selected == 2) // help
                    screen = 2;
                if (selected == 3) // credits
                    screen = 3;
                if (selected == 4) // exit
                    app.stop();
            }
            else if (screen == 1)
            {
                if (selected == menu.length - 1)
                {
                    menu = m1;
                    screen = 0;
                    selected = 1;
                    return;
                }
                if (menu[selected].contains("Shadows"))
                    if (menu[selected].contains("Enabled"))
                    {
                        menu[selected] = "Shadows Disabled";
                        Settings.useShadows = false;
                    }
                    else
                    {
                        menu[selected] = "Shadows Enabled";
                        Settings.useShadows = true;
                    }

                if (menu[selected].contains("Sounds"))
                    if (menu[selected].contains("Enabled"))
                    {
                        menu[selected] = "Sounds Disabled";
                        Settings.useSounds = false;
                    }
                    else
                    {
                        menu[selected] = "Sounds Enabled";
                        Settings.useSounds = true;
                    }
            }
            else
                screen = 0; // menu
        }
    }
}
