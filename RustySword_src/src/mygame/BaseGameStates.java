package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

public class BaseGameStates extends AbstractAppState
{
    SimpleApplication app;
    Node rootNode = new Node(), guiNode;
    AssetManager assetManager;
    ViewPort viewPort;
    InputManager inputManager;
    Camera cam;
    FlyByCamera flyCam;

    @Override
    public void initialize(AppStateManager stateManager, Application application)
    {
        super.initialize(stateManager, application);
        app = (SimpleApplication) application;
        application.getViewPort().attachScene(rootNode);
        application.getStateManager().attach(this);

        assetManager = application.getAssetManager();
        viewPort = application.getViewPort();
        inputManager = application.getInputManager();
        cam = application.getCamera();
        flyCam = app.getFlyByCamera();
        guiNode = app.getGuiNode();
    }

    @Override
    public void update(float tpf)
    {
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

    }

    public void dispose()
    {
        guiNode.detachAllChildren();
        setEnabled(false);
        app.getViewPort().detachScene(rootNode);
        app.getStateManager().detach(this);
    }
}
