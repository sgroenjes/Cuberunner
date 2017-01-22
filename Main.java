package mygame;
/*
 * High Score
 * 
 * 
 * */
import java.io.*;
import com.jme3.effect.ParticleMesh.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.lang.String;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Arrays;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Box;
import com.jme3.util.SkyFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends SimpleApplication implements ActionListener {

    private boolean left = false, right = false, up = false, down = false, pause = false;
    float r1;
    float r2;
    float rollLeft = 0;
    float rollRight = 0;
    int counter = 0;
    Node shipNode;
    Node boxNode;
    Spatial simpleShip;
    Geometry newSphere1;
    CameraNode camNode;
    Vector3f shipForward;
    Vector3f shipUp;
    Vector3f shipLocation;
    Vector3f boxForward;
    Vector3f boxUp;
    int numBoxes = 30;
    int score = 0;
    List<KeyPoint> keyPointsList = new ArrayList<KeyPoint>();
    float elapsedTime = 0;
    float minTime = 0;
    boolean[] markers = new boolean[80];
    boolean lvl2 = false;
    boolean gameOver = false;
    List<BoundingVolume> listBoxes = new ArrayList<BoundingVolume>();
    CollisionResults results = new CollisionResults();
    
    
            
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    private ChaseCamera chaseCam;
    private ParticleEmitter shockwave;
    private ParticleEmitter exhaust;
    private ParticleEmitter fire;
    private BitmapText elapsedTimeText;
    private BitmapText hitText;
    private String hitTextString = "";
    private float speed = 20f;
    boolean easy = true;
    Spatial ufo;
    private BulletAppState bulletAppState;
    private FileReader in = null;
    private FileWriter out = null;
    
    @Override
    public void simpleInitApp() {
        
        
        setUpKeys();
        setUpLight();
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        ufo = assetManager.loadModel("Models/ufo.obj");
        shipNode = new Node();
        
        ufo.setLocalScale(0.2f);
        shipNode.setLocalTranslation(new Vector3f(0,0, 0.5f));
        
        shipNode.attachChild(ufo);
        

        rootNode.attachChild(shipNode);
     

        Material material = new Material(assetManager, "Shaders/Particle.j3md");
        material.setTexture("Texture", assetManager.loadTexture("Shaders/flame.png"));
        material.setFloat("Softness", 3f);

        //Fire
        fire = new ParticleEmitter("Fire", ParticleMesh.Type.Triangle, 30);
        fire.setMaterial(material);
        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.1f));
        fire.setImagesX(2);
        fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f)); // red
        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fire.setStartSize(0.1f);
        fire.setEndSize(0.01f);
        fire.setGravity(0, 40, 0);
        fire.setLowLife(.01f);
        fire.setHighLife(.05f);
        fire.setLocalTranslation(-0.5f, 0, 0);
        fire.setParticlesPerSec(500);

        shipNode.attachChild(fire);
        



        // Disable the default flyby cam
        flyCam.setEnabled(false);
        //create the camera Node
        camNode = new CameraNode("Camera Node", cam);
        cam.setFrustumPerspective(60, 1.33f, 1, 100);
        
        
        //This mode means that camera copies the movements of the target:
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        //Attach the camNode to the target:
        shipNode.attachChild(camNode);
        //Move camNode, e.g. behind and above the target:
        camNode.setLocalTranslation(new Vector3f(-3, 1.5f, 0));
        //Rotate the camNode to look at the target:
        camNode.lookAt(shipNode.getLocalTranslation().add(new Vector3f(0, 1, 0)), Vector3f.UNIT_Y);
        //camNode.set(true);*/
        

        rootNode.attachChild(SkyFactory.createSky(
                assetManager,
                assetManager.loadTexture("Textures/horizon.jpg"),
                assetManager.loadTexture("Textures/horizon.jpg"),
                assetManager.loadTexture("Textures/horizon.jpg"),
                assetManager.loadTexture("Textures/horizon.jpg"),
                assetManager.loadTexture("Textures/horizon.jpg"),
                assetManager.loadTexture("Textures/horizon.jpg"),
                Vector3f.UNIT_XYZ));




        /*DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(.5f, .1f, -.5f));
        rootNode.addLight(sun);*/



        shipForward = new Vector3f(0, 0, 1);

        shipUp = new Vector3f(0, 1, 0);

        shipLocation = new Vector3f(0, 0, 0);



        setDisplayStatView(false);
        setDisplayFps(false);

            keyPointsList.add(new KeyPoint(20, 0, 0));
            keyPointsList.add(new KeyPoint(20, 5, 0));
            keyPointsList.add(new KeyPoint(0, 0, -20));
            keyPointsList.add(new KeyPoint(0, 0, 20));
            keyPointsList.add(new KeyPoint(0, 20, 0));
            keyPointsList.add(new KeyPoint(0, -20, 0));
            keyPointsList.add(new KeyPoint(20, 0, 5));
            keyPointsList.add(new KeyPoint(20, 5, 5));
            
        /*if (easy) {
            speed = 4;

        } else {
            speed = 10;
        }*/



      /*  for (KeyPoint kp : keyPointsList) {
            Geometry newSphere = new Geometry("sphere", new Sphere(16, 32, 1));
            newSphere.setLocalTranslation(kp.location);
            newSphere.setMaterial(new Material(assetManager, "Shaders/unlit.j3md"));
            newSphere.getMaterial().setColor("Color", ColorRGBA.Red);
            rootNode.attachChild(newSphere);
        }*/

        elapsedTimeText = new BitmapText(guiFont, false);
        elapsedTimeText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        elapsedTimeText.setColor(ColorRGBA.Green);                             // font color
        elapsedTimeText.setLocalTranslation(10, elapsedTimeText.getLineHeight(), 0); // position
        guiNode.attachChild(elapsedTimeText);

        hitText = new BitmapText(guiFont, false);
        hitText.setSize(28.0f);      // font size
        hitText.setColor(ColorRGBA.Magenta);                             // font color
        hitText.setLocalTranslation(200,440, 0); // position
        guiNode.attachChild(hitText);
        
        //shipNode.setModelBound(shipNode.getWorldBound().se;
    
       /* for(int i = 0; i < 10; i++)
        {
            genBoxes("Models/box1.obj", new Vector3f(0, 0, i*4));
        }
        //genBoxes("Models/box1.obj", new Vector3f(0, 0, 2));
        genBoxes("Models/box2.obj", new Vector3f(10, 0, 20));
        genBoxes("Models/box3.obj", new Vector3f(-10, 0, 10));
        genBoxes("Models/box4.obj", new Vector3f(-20, 0, 10));*/
    }

    private void setUpKeys() {
        System.out.println("Adding keys");
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
       inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
      //  inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));

        ///For the Dvorak users among us...
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_E));
      //  inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_COMMA));
      //  inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_O));


        inputManager.addListener(this, "Pause");
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
      //  inputManager.addListener(this, "Down");


    }

    public void onAction(String binding, boolean isPressed, float tpf) {

        if (binding.equals("Left")) {
            left = isPressed;
        } else if (binding.equals("Right")) {
            right = isPressed;
        } else if (binding.equals("Up")) {
            up = isPressed;
        } else if (binding.equals("Down")) {
            down = isPressed;
        } else if (binding.equals("Pause")) {
            pause = !pause;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(pause){
            speed = 0;
            counter--;
            elapsedTime-=tpf;
        } else if (lvl2)
            speed = 25f;
        else 
            speed = 20f;
        Arrays.fill(markers, false);
        counter++;
        ///Look for collisions
        //hitTextString = "";
        for(int i = 0; i< (Math.min(160, listBoxes.size()));i++)
            if(listBoxes.get(i).contains(new Vector3f(shipLocation.x,shipLocation.y,shipLocation.z)))//shipNode.getWorldBound().intersects(listBoxes.get(i)))
            {
                try{
                if(!gameOver){
                    score = openFiles((int)elapsedTime*100);
                }
                }
                catch(IOException ex){}
                gameOver = true;
                speed = 0;
                disableKeys();
                explode();
                //NumberFormat formatter = new DecimalFormatter("#.##");
                
                hitTextString = "GAME OVER!\nYOUR SCORE: "+String.valueOf((int)elapsedTime*100)+"\nHIGH SCORE: " + String.valueOf(score);
                //hitTextString = hitTextString.concat(" ")
            }
        
        
        
        shipLocation = shipLocation.add(shipForward.mult(tpf * speed));
        float z = shipLocation.getZ()+40f;
        
        
        
        //boolean missedAny = false;
        /*for (KeyPoint kp : keyPointsList) {
            if (!kp.hit && kp.location.distance(shipLocation) < 2) {
                kp.hit = true;
                Geometry newSphere = new Geometry("sphere", new Sphere(16, 32, 1.1f));
                newSphere.setLocalTranslation(kp.location);
                newSphere.setMaterial(new Material(assetManager, "Shaders/unlit.j3md"));
                newSphere.getMaterial().setColor("Color", ColorRGBA.Blue);
                rootNode.attachChild(newSphere);
            }

            hitTextString += "Sphere at " + kp.location + ", status: " + (kp.hit ? "hit" : "not hit") + "\n";

            if (!kp.hit) {
                missedAny = true;
            }
        }*/

        ///Update the corresponding global text.

        ///Let the user know how many they have hit
        hitText.setText(hitTextString);
        int zdiff =100;
        if(!gameOver)
            elapsedTime += tpf;
        elapsedTimeText.setText("Elapsed Time: " + elapsedTime + " s");
        if( elapsedTime > 10 && !gameOver )
        {
            
            if(!lvl2){
                lvl2 = true;
                rootNode.attachChild(SkyFactory.createSky(assetManager,
                assetManager.loadTexture("Textures/BlackF.jpg"),
                assetManager.loadTexture("Textures/BlackF.jpg"),
                assetManager.loadTexture("Textures/BlackF.jpg"),
                assetManager.loadTexture("Textures/BlackF.jpg"),
                assetManager.loadTexture("Textures/BlackF.jpg"),
                assetManager.loadTexture("Textures/BlackF.jpg"),
                Vector3f.UNIT_XYZ));
                speed = 25f;
            }
            numBoxes = 15;
            zdiff = 75;
        }
            if(counter % zdiff == 0)
            {
                
               // for(int i = 0; i < 30; i++)
                //{
                int i = 0;
                    do{
                        int rand = (int) Math.ceil(Math.random()*75.0f+2);
                        if(!(markers[rand-1] && markers[rand+1])&&!(markers[rand-2]&&
                                markers[rand-1])&&!(markers[rand+1]&&markers[rand+2]))
                        {    
                            markers[rand]=true;
                            i++;
                        }
                    }while(i<numBoxes );
                    for(int j = 0; j<80; j++)
                        if(markers[j])
                        {
                            if(lvl2)
                                genBoxes("Models/box4.obj", new Vector3f(shipLocation.getX()+j-40, 0 , z),4);
                            else{
                            switch((int)Math.ceil(Math.random()*3))
                            {
                                case 1:
                                    genBoxes("Models/box1.obj", new Vector3f(shipLocation.getX()+j-40, 0, z), 1);
                                    break;
                                case 2: 
                                    genBoxes("Models/box2.obj", new Vector3f(shipLocation.getX()+j-40, 0, z), 2);
                                    break;
                                case 3:
                                    genBoxes("Models/box3.obj", new Vector3f(shipLocation.getX()+j-40, 0, z), 3);
                                    break;
                            }
                            }
                        }
                  //  genBoxes("Models/box2.obj", new Vector3f(shipLocation.getX()+rand, 0, shipLocation.getZ()+40f));
                //}
              
            }
        
        //System.out.print(shipLocation.getZ() + " ");

        ///Update the ship's location
        /*if(elapsedTime < 60)
         * Code under this
         */
        ///Move the ship forward
        
        ///Update the render node's position for the ship
        
        /*else if(elapsedTime > 60)
         * {
         * speed = 8f;
         * shipLocation = shipLocation.add(shipForward.mult(tpf * speed));
         * }
         */
        
        

        ///Respond to commands
        if (left) {
            shipLocation = shipLocation.add(new Vector3f(tpf*15,0,0));
            //Keep track of amount of tilt
            if( rollLeft < .36){//~20degrees
            rollLeft += tpf;
            //Not responding
            cam.setAxes(new Quaternion().fromAngleAxis(tpf, cam.getDirection()).toRotationMatrix().mult(cam.getLeft()).normalize(), 
                    new Quaternion().fromAngleAxis(tpf, cam.getDirection()).toRotationMatrix().mult(cam.getUp()).normalize(), cam.getDirection().normalize());
            cam.update();
            }
        }
        else {
            if(rollLeft>0){
                rollLeft /= 2.0;
                cam.setAxes(new Quaternion().fromAngleAxis(-rollLeft, cam.getDirection()).toRotationMatrix().mult(cam.getLeft()).normalize(), 
                    new Quaternion().fromAngleAxis(-rollLeft, cam.getDirection()).toRotationMatrix().mult(cam.getUp()).normalize(), cam.getDirection().normalize());
                cam.update();
            }
        }
        if (right) {
            shipLocation = shipLocation.add(new Vector3f(-tpf*15,0,0));
        }
        shipNode.setLocalTranslation(shipLocation);
        Vector3f shipRight = shipForward.cross(shipUp);

        if (up) {
            //shipLocation = shipLocation.add(shipForward.mult(tpf * speed));
        }

        if (down) {
            shipForward = new Quaternion().fromAngleAxis(-tpf, shipRight).toRotationMatrix().mult(shipForward).normalize();
            shipUp = new Quaternion().fromAngleAxis(-tpf, shipRight).toRotationMatrix().mult(shipUp).normalize();
        }
            
        ///Assign this new basis to the render engine.
        Matrix3f newBasis = new Matrix3f(
                shipForward.x, shipUp.x, shipRight.x,
                shipForward.y, shipUp.y, shipRight.y,
                shipForward.z, shipUp.z, shipRight.z);

        shipNode.setLocalRotation(newBasis);
    }
    
    private void genBoxes(String filename, Vector3f location, int boxNum)
    {
        if(gameOver)
            return;
        boxForward = new Vector3f(0, 0, 1);
        boxUp = new Vector3f(0, 1, 0);
        Vector3f boxRight = boxForward.cross(boxUp);
        Spatial box = assetManager.loadModel(filename);
        box.setLocalTranslation(location.add(new Vector3f(0,0.4f,0)));
        if(boxNum == 2)
        {
            boxForward = new Quaternion().fromAngleAxis((float)Math.PI/2.0f*(float)(Math.floor(Math.random()*3.99)), boxRight).toRotationMatrix().mult(boxForward).normalize();
            boxUp = boxRight.cross(boxForward);
            boxForward = new Quaternion().fromAngleAxis((float)Math.PI/2.0f*(float)(Math.floor(Math.random()*3.99)), boxUp).toRotationMatrix().mult(boxForward).normalize();
            boxRight = boxForward.cross(boxUp);
            Matrix3f boxBasis = new Matrix3f(
                    boxForward.x, boxUp.x, boxRight.x,
                    boxForward.y, boxUp.y, boxRight.y,
                    boxForward.z, boxUp.z, boxRight.z);
            box.setLocalRotation(boxBasis);
        }
        box.setLocalScale(.5f);
        if(listBoxes.size()==270)
            listBoxes.remove(0);
        listBoxes.add(box.getWorldBound());
        rootNode.attachChild(box);
        
        
    }
     private void setUpLight() {
    // We add light so we see the scene
    AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(0.1f));
    rootNode.addLight(al);

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White.mult(1f));
    dl.setDirection(new Vector3f(1, 0, 0).normalizeLocal());
    rootNode.addLight(dl);
    
    dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White.mult(1f));
    dl.setDirection(new Vector3f(-1, 0, 0).normalizeLocal());
    rootNode.addLight(dl);
    /*
    dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White.mult(1f));
    dl.setDirection(new Vector3f(0, -10, -1).normalizeLocal());
    rootNode.addLight(dl);
    */
    
    
    
    dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White.mult(1f));
    dl.setDirection(new Vector3f(0f, -.2f, .8f).normalizeLocal());
    rootNode.addLight(dl);
  }
     private void disableKeys(){
         left = false;
         right = false;
     }
     private void explode(){
         shockwave = new ParticleEmitter("Shockwave", Type.Triangle, 1);
//        shockwave.setRandomAngle(true);
        shockwave.setFaceNormal(Vector3f.UNIT_Y);
        shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f )));
        shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));

        shockwave.setStartSize(0f);
        shockwave.setEndSize(7f);

        shockwave.setParticlesPerSec(0);
        shockwave.setGravity(0, 0, 0);
        shockwave.setLowLife(0.1f);
        shockwave.setHighLife(0.5f);
        shockwave.setInitialVelocity(new Vector3f(0, 0, 0));
        shockwave.setVelocityVariation(0f);
        shockwave.setImagesX(1);
        shockwave.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/shockwave.png"));
        shockwave.setMaterial(mat);
        shipNode.attachChild(shockwave);
        shockwave.emitAllParticles();
        shipNode.detachChild(ufo);
        shipNode.detachChild(fire);
         
     }
     
     private int openFiles(int score) throws IOException {
        int highScore = 0;
        try{
        in = new FileReader("highScore.txt");
        BufferedReader input = new BufferedReader(in);
        highScore = input.read();
        }catch(IOException ex){}
        if(highScore < score)
        {
            highScore = score;
            try{
            out = new FileWriter("highScore.txt");
            out.write(highScore);
            out.close();
            }catch(IOException ex){}
        }
        return highScore;
        
     }
     
}