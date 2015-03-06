//package b_object3D_collision;
package c;

import javafx.collections.ObservableFloatArray;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

//import b_object3D_collision.Triangle3D.Pair3D;
import c.Triangle3D.Pair3D;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

import org.fxyz.shapes.composites.PolyLine3D;

// This code is originally based on code from:
// http://stackoverflow.com/questions/19462571/how-to-create-3d-shape-from-stl-in-javafx-8

// The current (1/14/2015) architecture is a kind of a View-Controller. I did not separate 
//   out Model from View because the Model information is stored in MeshView class that is 
//   part of the View. However, any changes to the Model are then immediately reflected in 
//   the view. It probably makes sense to just put MeshView in Model and and treat it 
//   simply as such. (Interestingly, then, model does not even need to notify view in case 
//   of a change. The notice happens behind the scenes; no pun intended. That type of 
//   functionality is likely intentional in JavaFX.) At present I am leaving taking model 
//   out of view for later.  
//   
// (GK 2/19/2015) Methods added below for naive octree recursive separation (still achieves
//   significant speedup). The new methods are based on code from Noah.

public class ViewSTL {

  private Controller controller;
  long sumOfDurations       = 0;
  long sumOfDurationsStage2 = 0;
    
  private static final String MESH_DIRECTORY =
    //"C:/Users/Gary/workspaceEclipse4.4/downloads/stlFiles/";
		  "C:/Users/Roberto/Desktop/Java/TIA Supplementary files/c/stlFiles";

  private static final double MODEL_SCALE_FACTOR = 4;
  private static final double MODEL_X_OFFSET = 0; // standard
  private static final double MODEL_Y_OFFSET = 0; // standard

  private static final int VIEWPORT_SIZE = 800;

  private static final Color   lightColor = Color.rgb(244, 255, 250);//Color.rgb(  0,   0,   0);//
  private static final Color  lightDColor = Color.rgb( 80,  80,  80);//Color.rgb(  0,   0,   0);//Darker version of color//
  private static final Color ambientColor = Color.rgb( 80,  80,  80,   0);
  private static final Color  objectColor = Color.rgb(180, 170, 122);//Color.rgb(  0,   0,   0);//
  private static final Color objectDColor = Color.rgb( 60,  60,  40);//Color.rgb(  0,   0,   0);//Darker version of color//
  private static final Color  collsnColor = Color.rgb(255,  20,  20);
  private static final Color   sceneColor = Color.rgb( 10,  10,  40);

  private Stage primaryStage;
  private Scene scene3D;
  private Scene scene2D; // We want to be able to switch easily between the two.
  private Group path3D;
  private Group rootAll;
  private Group rootMeshAll; // Contains all/only displayed meshes, used only for translation
  private Group rootMeshRotates; // Contains all/only displayed meshes, used only for rotation
  private Group path2D;
  private   PointLight   pointLight1;
  private   PointLight   pointLight2;
  private   PointLight   pointLight3;
  private AmbientLight ambientLight ;

  ViewSTL() {
      rootMeshRotates = new Group();
      path3D          = new Group();
  }

  ViewSTL(Controller controller) {
      this();
      this.controller = controller;
      this.controller.setView(this);
  }

  MeshView loadMeshView() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory( new File(MESH_DIRECTORY) );
      fileChooser.setTitle("Open Resource File");
      fileChooser.getExtensionFilters().addAll(
              new ExtensionFilter("Stereolithography Files", "*.stl"),
              new ExtensionFilter("All Files", "*.*"));
      File selectedFile = fileChooser.showOpenDialog(primaryStage);

      return loadMeshView(selectedFile);
  }

  // Initial scene layout
  MeshView loadMeshView(File selectedFile) {
      if (selectedFile != null) {
            StlMeshImporter importer = new StlMeshImporter();
            importer.read(selectedFile);
            TriangleMesh mesh = (TriangleMesh)importer.getImport();
            System.out.println("Imported file: "+selectedFile+" With, "+mesh.getFaces().size()/mesh.getFaceElementSize()+" facets!");
            MeshView meshView = new MeshView(mesh);
            
            int nFiles = rootMeshRotates.getChildren().size(); 
            meshView.setTranslateX(VIEWPORT_SIZE / 4 * nFiles - VIEWPORT_SIZE / 6);
            //meshView.setTranslateY(VIEWPORT_SIZE / 2 + MODEL_Y_OFFSET);
            //meshView.setTranslateZ(VIEWPORT_SIZE / 2);
            meshView.setScaleX(MODEL_SCALE_FACTOR);
            meshView.setScaleY(MODEL_SCALE_FACTOR);
            meshView.setScaleZ(MODEL_SCALE_FACTOR);
            
            PhongMaterial sample = new PhongMaterial(objectColor);
            sample.setSpecularColor(lightColor);
            sample.setSpecularPower(16);
            meshView.setMaterial(sample);
            
            meshView.setOnMouseEntered(  e -> {controller.notifyMouseEnteredMesh( e,meshView);} );
            
            rootMeshRotates.getChildren().add( meshView );
            
            return meshView;
      }
      return null;
  }

  void removeMeshView( MeshView meshView ) {
      rootMeshRotates.getChildren().remove( meshView );
  }

  private Group buildScene() {
    rootMeshAll     = new Group(rootMeshRotates);
    rootAll         = new Group(rootMeshAll, path3D);
    rootMeshAll.setTranslateX(   VIEWPORT_SIZE  /2 + MODEL_X_OFFSET);
    rootMeshAll.setTranslateY(   VIEWPORT_SIZE  /2 + MODEL_Y_OFFSET);
    rootMeshAll.setTranslateZ(   VIEWPORT_SIZE  /2);

    pointLight1 = new PointLight(lightColor);
    pointLight1.setTranslateX(   VIEWPORT_SIZE*3/4);
    pointLight1.setTranslateY(   VIEWPORT_SIZE  /2);
    pointLight1.setTranslateZ(   VIEWPORT_SIZE  /2);
    pointLight2 = new PointLight(lightColor);
    pointLight2.setTranslateX(   VIEWPORT_SIZE*1/4);
    pointLight2.setTranslateY(   VIEWPORT_SIZE*3/4);
    pointLight2.setTranslateZ(   VIEWPORT_SIZE*3/4);
    pointLight3 = new PointLight(lightColor);
    pointLight3.setTranslateX(   VIEWPORT_SIZE*5/8);
    pointLight3.setTranslateY(   VIEWPORT_SIZE  /2);
    pointLight3.setTranslateZ(  -VIEWPORT_SIZE    );

    ambientLight = new AmbientLight(ambientColor);

    rootAll.getChildren().add(  pointLight1);
    rootAll.getChildren().add(  pointLight2);
    rootAll.getChildren().add(  pointLight3);
    rootAll.getChildren().add(ambientLight );

    rootAll.setOnMouseEntered(  e -> {controller.notifyMouseEntered( e,rootAll);});
    rootAll.setOnMouseExited(   e -> {controller.notifyMouseExited(  e,rootAll);});

    return rootAll;
  }

  private PerspectiveCamera addCamera(SubScene scene) {
    PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
    System.out.println("Near Clip: " + perspectiveCamera.getNearClip());
    System.out.println("Far Clip:  " + perspectiveCamera.getFarClip());
    System.out.println("FOV:       " + perspectiveCamera.getFieldOfView());

    scene.setCamera(perspectiveCamera);
    return perspectiveCamera;
  }

  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    AnchorPane pane = new AnchorPane();
    
    HBox hBox = new HBox(5); // 5 adds that space between nodes
    ToggleButton    wireFrameButton = new ToggleButton("Wireframe");
    wireFrameButton   .setOnAction( e -> {controller.notifyWireFrameButton(  e );});
    Button         crossSectnButton = new Button("Cross-section");
    crossSectnButton  .setOnAction( e -> {controller.notifyCrossSectnButton( e , true );});
    ToggleButton   renderBackButton = new ToggleButton("Render Backs");
    renderBackButton  .setOnAction( e -> {controller.notifyRenderBackButton( e );});
    ToggleButton   checkCllsnButton = new ToggleButton("Check Collision");
    checkCllsnButton  .setOnAction( e -> {controller.notifyCheckCllsnButton( e );});
    Button         addSTLFileButton = new Button("Add File");
    addSTLFileButton  .setOnAction( e -> {controller.notifyAddSTLFileButton( e );});
    Button         removeFileButton = new Button("Remove");
    removeFileButton  .setOnAction( e -> {controller.notifyRemoveFileButton( e );});
    hBox.getChildren().addAll(
            wireFrameButton,crossSectnButton,renderBackButton,
            checkCllsnButton,addSTLFileButton,removeFileButton);
    AnchorPane.setLeftAnchor(hBox, 10.0);
    AnchorPane.setTopAnchor( hBox, 10.0);

    Group meshDisplay = buildScene();
    meshDisplay.setScaleX(2);
    meshDisplay.setScaleY(2);
    meshDisplay.setScaleZ(2);
    meshDisplay.setTranslateX(50);
    meshDisplay.setTranslateY(50);

    scene3D           = new Scene(          pane, VIEWPORT_SIZE, VIEWPORT_SIZE);//, true);//
    SubScene subScene = new SubScene(meshDisplay, VIEWPORT_SIZE, VIEWPORT_SIZE, true, SceneAntialiasing.BALANCED);//, true, SceneAntialiasing.DISABLED);//);//
    subScene.setFill(sceneColor);
    addCamera(subScene);
    pane.getChildren().add(subScene);
    pane.getChildren().add(  hBox      );
    scene3D.setOnKeyPressed(  (KeyEvent e) -> {controller.notifyKeyPressed( e );} );
    scene3D.setOnMousePressed(  e -> {controller.notifyMousePressed( e,rootMeshRotates);});
    scene3D.setOnMouseReleased( e -> {controller.notifyMouseReleased(e,rootMeshRotates);});
    scene3D.setOnMouseDragged(  e -> {controller.notifyMouseDragged( e,rootMeshRotates);});
    scene3D.setOnScroll(        e -> {controller.notifyScrollMove(   e                );});

    // 2D scene definition. This for cross-section
    final VBox vbox  = new VBox();
    final HBox hbox  = new HBox();
    scene2D          = new Scene(vbox);
    path2D           = new Group();
    Button         crossSectnButton2 = new Button("Cross-section");
    crossSectnButton2 .setOnAction( e -> {controller.notifyCrossSectnButton( e , false );});
    hbox.setSpacing(10);
    //hbox.getChildren().addAll(crossSectnButton2);
    vbox.getChildren().addAll(path2D, hbox);
    
    primaryStage.setTitle("STL Object Viewer");
    primaryStage.setScene(scene3D);
    primaryStage.show();
  }

  // Transformations applied later in the execution
  void rotate(double angle, Point3D rotationAxis, boolean isCrossSection) {
      Rotate rotate = new Rotate(angle,rotationAxis);
      rootMeshRotates.getTransforms().addAll( rotate );
      path3D.getChildren().clear();
      if (isCrossSection) crossSection();
  }
  
  void translateAllMeshes(double mult) {
    rootMeshAll.getTransforms().add(new Translate(0,0,mult * VIEWPORT_SIZE));
  }
  
  void setRender( boolean isWireframe ) {
    for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
        if (isWireframe) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).drawModeProperty().set(DrawMode.LINE);
        } else {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).drawModeProperty().set(DrawMode.FILL);
        }
    }
  }
  
  void setBackFace( boolean isDisplayBack ) {
    for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
        if (isDisplayBack) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).cullFaceProperty().set(CullFace.NONE);//
        } else {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).cullFaceProperty().set(CullFace.BACK);//
        }
    }
  }
  
  void setColorsNormal(  ) {
      path3D.getChildren().clear();
      PhongMaterial sample = new PhongMaterial(objectColor);
      sample.setSpecularColor(lightColor);
      sample.setSpecularPower(16);
      for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).setMaterial(sample);;//
      }
    }
        
  void setColorsDarker(  ) {
      PhongMaterial sample = new PhongMaterial(objectDColor);
      sample.setSpecularColor(lightDColor);
      sample.setSpecularPower(16);
      for (int i = 0; i < rootMeshRotates.getChildren().size(); i++) {
          ((Shape3D)rootMeshRotates.getChildren().get(i)).setMaterial(sample);;//
      }
    }
        
  boolean isCollision() {
      setColorsNormal();
      boolean returnValue = false;
      for     (int iA =    0; iA < rootMeshRotates.getChildren().size(); iA++) {
          for (int iB = iA+1; iB < rootMeshRotates.getChildren().size(); iB++) {
              if ( isCollision(iA,iB) ) returnValue = true;
          }
      }
      return returnValue;
  }
  
  
  
  boolean isCollision(int iA, int iB) {
      MeshView shapeA = (MeshView)rootMeshRotates.getChildren().get(iA);
      MeshView shapeB = (MeshView)rootMeshRotates.getChildren().get(iB);
      
      TriangleMesh meshA = (TriangleMesh)shapeA.getMesh();
      TriangleMesh meshB = (TriangleMesh)shapeB.getMesh();
      
      // Get an array of all of the x,y,z points from the triangles that make
      // up our two meshes.
      ObservableFloatArray aPoints = meshA.getPoints();
      ObservableFloatArray bPoints = meshB.getPoints();
      
      float minX = Float.POSITIVE_INFINITY;
      float maxX = Float.NEGATIVE_INFINITY;
      float minY = Float.POSITIVE_INFINITY;
      float maxY = Float.NEGATIVE_INFINITY;
      float minZ = Float.POSITIVE_INFINITY;
      float maxZ = Float.NEGATIVE_INFINITY;
      
      float X;
      float Y;
      float Z;
      // Get the min and max x,y,z out of all the vertices of shape A.
      //System.out.println("Number of points in the mesh A: " + Integer.toString(aPoints.size()));
      for(int i=0; i < aPoints.size()/3; i++) {
          Point3D vertex = shapeA.localToScene( aPoints.get((i*3)+0), 
                  aPoints.get((i*3)+1), aPoints.get((i*3)+2));
          X = (float) vertex.getX();
          Y = (float) vertex.getY();
          Z = (float) vertex.getZ();
          if (X < minX) {
              minX = X;
          }
          else if (X > maxX) {
              maxX = X;
          }
          if (Y < minY) {
              minY = Y;
          }
          else if (Y > maxY) {
              maxY = Y;
          }if (Z < minZ) {
              minZ = Z;
          }
          else if (Z > maxZ) {
              maxZ = Z;
          }
      }
      // Include mesh B in our min/max calculations.
      // We have to do this one separately, because the global
      // coordinates we calculate differ based on what local axis is
      // being used, i.e. what mesh the coordinates are from.
      //System.out.println("Number of points in the mesh B: " + Integer.toString(bPoints.size()));
      for(int i=0; i < bPoints.size()/3; i++) {
          Point3D vertex = shapeB.localToScene( bPoints.get((i*3)+0), 
                  bPoints.get((i*3)+1), bPoints.get((i*3)+2));
          X = (float) vertex.getX();
          Y = (float) vertex.getY();
          Z = (float) vertex.getZ();
          if (X < minX) {
              minX = X;
          }
          else if (X > maxX) {
              maxX = X;
          }
          if (Y < minY) {
              minY = Y;
          }
          else if (Y > maxY) {
              maxY = Y;
          }if (Z < minZ) {
              minZ = Z;
          }
          else if (Z > maxZ) {
              maxZ = Z;
          }
      }
      float xLowBound  = minX;
      float xHighBound = maxX;
      float yLowBound  = minY;
      float yHighBound = maxY;
      float zLowBound  = minZ;
      float zHighBound = maxZ;
      
      // Create an arraylist of all the triangles in mesh A,
      // and mesh B.
      ArrayList<Triangle3D> aTriangles = getTriangles(shapeA);
      ArrayList<Triangle3D> bTriangles = getTriangles(shapeB);
      
      // Start the recursive call, and return that, passing it
      // initially our big bounding box, and all the triangles
      // that we have.
      boolean collision = recursiveBoxingCollisionCheck(aTriangles, bTriangles,
              xLowBound,xHighBound,yLowBound,yHighBound,zLowBound,zHighBound, 0);
      
      if (collision) {
          System.out.println("COLLISION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
          PhongMaterial sample = new PhongMaterial(collsnColor);
          sample.setSpecularColor(lightColor);
          sample.setSpecularPower(16);
          shapeA.setMaterial(sample);
          shapeB.setMaterial(sample);
          return true;
      }
      else {
          System.out.println("NO COLLISION!");
          return false;
      }

  }
  
  private boolean recursiveBoxingCollisionCheck(ArrayList<Triangle3D> A,
          ArrayList<Triangle3D> B, float xMin, float xMax, float yMin,
          float yMax, float zMin, float zMax, int recursionDepth) {
      
      if (A.size() == 0 || B.size() == 0) return false;
      if ((recursionDepth >= 5) || ((A.size() * B.size()) <= 100)) {
          return bruteForceCheck(A,B);
      }
      else { // Break up our box into 8 more boxes, and call the
             // recursion function on those.
          float xMid = xMin + ((xMax - xMin)/2);
          float yMid = yMin + ((yMax - yMin)/2);
          float zMid = zMin + ((zMax - zMin)/2);
          
          
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMin,xMid,yMin,yMid,zMin,zMid),
                  getTrianglesInBox(B,xMin,xMid,yMin,yMid,zMin,zMid),
                                      xMin,xMid,yMin,yMid,zMin,zMid,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMin,xMid,yMin,yMid,zMid,zMax),
                  getTrianglesInBox(B,xMin,xMid,yMin,yMid,zMid,zMax),
                                      xMin,xMid,yMin,yMid,zMid,zMax,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMin,xMid,yMid,yMax,zMin,zMid),
                  getTrianglesInBox(B,xMin,xMid,yMid,yMax,zMin,zMid),
                                      xMin,xMid,yMid,yMax,zMin,zMid,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMin,xMid,yMid,yMax,zMid,zMax),
                  getTrianglesInBox(B,xMin,xMid,yMid,yMax,zMid,zMax),
                                      xMin,xMid,yMid,yMax,zMid,zMax,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMid,xMax,yMin,yMid,zMin,zMid),
                  getTrianglesInBox(B,xMid,xMax,yMin,yMid,zMin,zMid),
                                      xMid,xMax,yMin,yMid,zMin,zMid,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMid,xMax,yMin,yMid,zMid,zMax),
                  getTrianglesInBox(B,xMid,xMax,yMin,yMid,zMid,zMax),
                                      xMid,xMax,yMin,yMid,zMid,zMax,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMid,xMax,yMid,yMax,zMin,zMid),
                  getTrianglesInBox(B,xMid,xMax,yMid,yMax,zMin,zMid),
                                      xMid,xMax,yMid,yMax,zMin,zMid,recursionDepth+1) == true) return true;
          if (recursiveBoxingCollisionCheck(
                  getTrianglesInBox(A,xMid,xMax,yMid,yMax,zMid,zMax),
                  getTrianglesInBox(B,xMid,xMax,yMid,yMax,zMid,zMax),
                                      xMid,xMax,yMid,yMax,zMid,zMax,recursionDepth+1) == true) return true;
          
          return false; // else we're good, send false on up the stack.
      }
      
  }
  
  private ArrayList<Triangle3D> getTrianglesInBox(ArrayList<Triangle3D> triangles,
          float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
      ArrayList<Triangle3D> trianglesInBox = new ArrayList<Triangle3D>();
      for (Triangle3D t : triangles) {
          
          // First we do simple checks to see if these triangles are obviously not
          // in the box.
          if      (t.a[0] < xMin && t.b[0] < xMin && t.c[0] < xMin) continue; //skip the rest of this loop, check the next triangle
          else if (t.a[1] < yMin && t.b[1] < yMin && t.c[1] < yMin) continue; //skip the rest of this loop, check the next triangle
          else if (t.a[2] < zMin && t.b[2] < zMin && t.c[2] < zMin) continue; //skip the rest of this loop, check the next triangle
          else if (t.a[0] > xMax && t.b[0] > xMax && t.c[0] > xMax) continue; //skip the rest of this loop, check the next triangle
          else if (t.a[1] > yMax && t.b[1] > yMax && t.c[1] > yMax) continue; //skip the rest of this loop, check the next triangle
          else if (t.a[2] > zMax && t.b[2] > zMax && t.c[2] > zMax) continue; //skip the rest of this loop, check the next triangle
          
          // Now let's check if the triangle is obviously IN the box.
          
          //if any of the three vertices are inside the box, then this triangle
          // is in the box.
//          if (t.a[0] >= xMin && t.a[0] <= xMax && t.a[1] >= yMin && t.a[1] <= yMax
//                  && t.a[2] >= zMin && t.a[2] <= zMax) {
//              trianglesInBox.add(t);
//              continue;
//          }
//          else if (t.b[0] >= xMin && t.b[0] <= xMax && t.b[1] >= yMin && t.b[1] <= yMax
//                  && t.b[2] >= zMin && t.b[2] <= zMax) {
//              trianglesInBox.add(t);
//              continue;
//          }
//          else if (t.c[0] >= xMin && t.c[0] <= xMax && t.c[1] >= yMin && t.c[1] <= yMax
//                  && t.c[2] >= zMin && t.c[2] <= zMax) {
//              trianglesInBox.add(t);
//              continue;
//          }
          
          // For now, just add the triangle if it is obviously not out
          trianglesInBox.add(t);

          // Now the only possibility that would still leave the triangle in the box,
          // would be if an edge of the triangle is passing through the box.
          // So we do an exhaustive check for that case.
//          float[] minBoxValues = new float[3];
//          float[] maxBoxValues = new float[3];
//          minBoxValues[0] = xMin;
//          minBoxValues[1] = yMin;
//          minBoxValues[2] = zMin;
//          maxBoxValues[0] = xMax;
//          maxBoxValues[1] = yMax;
//          maxBoxValues[2] = zMax;
//          if (t.isCollisionBoundingBoxExhaustive(minBoxValues, maxBoxValues) ) {
//              trianglesInBox.add(t);
//          }
      }
      
      return trianglesInBox;
  }
 
  private boolean bruteForceCheck(ArrayList<Triangle3D> A,
          ArrayList<Triangle3D> B) {
      for (Triangle3D a: A) {
          for (Triangle3D b: B) {
              if (a.isCollision(b)) return true;
          }
      }
      //System.out.println(""+A.size()+" "+B.size());
      return false;
      
  }
  private ArrayList<Triangle3D> getTriangles(MeshView shape) {
      // The array of triangles which will be filled up with the
      // triangles corresponding to this MeshView.
      ArrayList<Triangle3D> triangles = new ArrayList<Triangle3D>();

      // Get the TriangleMesh from the MeshView.
      TriangleMesh mesh = (TriangleMesh)shape.getMesh();
      
      /* Here we loop through the mesh.getFaces() array,
         pulling out all the point indices info for each
         of the three vertices for each triangle. From the
         points array, we can get the x,y,z coords info for
         each vertex.
         The getFaces() array comes in the form of
         [p0,t0,p1,t1,p2,t2], repeating for each face.
         The t's are texture indices that we don't need,
         and the p's are indices of the points array.
         The points array comes in the form of x,y,z coords
         for each point, repeating. We get these coords, and
         construct Triangle3D objects from them, to represent
         our triangles.
       */
      
      for (int iFace = 0; iFace < mesh.getFaces().size()/mesh.getFaceElementSize();
              iFace++ ) {
          int iFace0 = mesh.getFaces().get(iFace*6+0); // i*6+
          int iFace1 = mesh.getFaces().get(iFace*6+2);
          int iFace2 = mesh.getFaces().get(iFace*6+4);

          Point3D vertex1 = shape.localToScene( mesh.getPoints().get(iFace0*3+0), 
                                                mesh.getPoints().get(iFace0*3+1), 
                                                mesh.getPoints().get(iFace0*3+2) );
          Point3D vertex2 = shape.localToScene( mesh.getPoints().get(iFace1*3+0), 
                                                mesh.getPoints().get(iFace1*3+1), 
                                                mesh.getPoints().get(iFace1*3+2) );
          Point3D vertex3 = shape.localToScene( mesh.getPoints().get(iFace2*3+0), 
                                                mesh.getPoints().get(iFace2*3+1), 
                                                mesh.getPoints().get(iFace2*3+2) );
          
          triangles.add(new Triangle3D(vertex1,vertex2,vertex3));
          
      }
      
      return triangles;   
      
  }
  
  
  boolean isCollisionBruteForce() {
      setColorsNormal();
      boolean returnValue = false;
      for     (int iA =    0; iA < rootMeshRotates.getChildren().size(); iA++) {
          for (int iB = iA+1; iB < rootMeshRotates.getChildren().size(); iB++) {
              if ( isCollisionBruteForce(iA,iB) ) returnValue = true;
          }
      }
      return returnValue;
  }
  
  boolean isCollisionBruteForce(int iA, int iB) {
      MeshView shapeA = (MeshView)rootMeshRotates.getChildren().get(iA);
      MeshView shapeB = (MeshView)rootMeshRotates.getChildren().get(iB);
      
      TriangleMesh meshA = (TriangleMesh)shapeA.getMesh();
      TriangleMesh meshB = (TriangleMesh)shapeB.getMesh();

      for (     int iFaceA = 0; iFaceA < meshA.getFaces().size()/meshA.getFaceElementSize(); iFaceA++ ) {
          for ( int iFaceB = 0; iFaceB < meshB.getFaces().size()/meshB.getFaceElementSize(); iFaceB++ ) {
              int iFaceA0 = meshA.getFaces().get(iFaceA*6+0); // i*6+
              int iFaceA1 = meshA.getFaces().get(iFaceA*6+2);
              int iFaceA2 = meshA.getFaces().get(iFaceA*6+4);
              int iFaceB0 = meshB.getFaces().get(iFaceB*6+0);
              int iFaceB1 = meshB.getFaces().get(iFaceB*6+2);
              int iFaceB2 = meshB.getFaces().get(iFaceB*6+4);

              Point3D a0 = shapeA.localToScene( meshA.getPoints().get(iFaceA0*3+0), 
                                                meshA.getPoints().get(iFaceA0*3+1), 
                                                meshA.getPoints().get(iFaceA0*3+2) );
              Point3D a1 = shapeA.localToScene( meshA.getPoints().get(iFaceA1*3+0), 
                                                meshA.getPoints().get(iFaceA1*3+1), 
                                                meshA.getPoints().get(iFaceA1*3+2) );
              Point3D a2 = shapeA.localToScene( meshA.getPoints().get(iFaceA2*3+0), 
                                                meshA.getPoints().get(iFaceA2*3+1), 
                                                meshA.getPoints().get(iFaceA2*3+2) );
              Triangle3D a = new Triangle3D( a0, a1, a2 );

              Point3D b0 = shapeB.localToScene( meshB.getPoints().get(iFaceB0*3+0), 
                                                meshB.getPoints().get(iFaceB0*3+1), 
                                                meshB.getPoints().get(iFaceB0*3+2) );
              Point3D b1 = shapeB.localToScene( meshB.getPoints().get(iFaceB1*3+0), 
                                                meshB.getPoints().get(iFaceB1*3+1), 
                                                meshB.getPoints().get(iFaceB1*3+2) );
              Point3D b2 = shapeB.localToScene( meshB.getPoints().get(iFaceB2*3+0), 
                                                meshB.getPoints().get(iFaceB2*3+1), 
                                                meshB.getPoints().get(iFaceB2*3+2) );
              Triangle3D b = new Triangle3D( b0, b1, b2 );

              //System.out.println("triangle: "+a.ax+" "+a.ay+" "+a.az+" "+b.ax+" "+b.ay+" "+b.az+" ");

              // TODO Remove this before giving to students (or ask them if it is needed)
              {
                  float minAx, minAy, minAz, minBx, minBy, minBz;
                  float maxAx, maxAy, maxAz, maxBx, maxBy, maxBz;

                  if (a0.getX()>a1.getX()) { minAx = (float) a1.getX(); maxAx = (float) a0.getX(); } else { minAx = (float) a0.getX(); maxAx = (float) a1.getX(); } if (a2.getX()<minAx) { minAx = (float) a2.getX(); } if (a2.getX()>maxAx) { maxAx = (float) a2.getX(); } 
                  if (b0.getX()>b1.getX()) { minBx = (float) b1.getX(); maxBx = (float) b0.getX(); } else { minBx = (float) b0.getX(); maxBx = (float) b1.getX(); } if (b2.getX()<minBx) { minBx = (float) b2.getX(); } if (b2.getX()>maxBx) { maxBx = (float) b2.getX(); } if (minAx>maxBx || maxAx<minBx) continue;
                  if (a0.getY()>a1.getY()) { minAy = (float) a1.getY(); maxAy = (float) a0.getY(); } else { minAy = (float) a0.getY(); maxAy = (float) a1.getY(); } if (a2.getY()<minAy) { minAy = (float) a2.getY(); } if (a2.getY()>maxAy) { maxAy = (float) a2.getY(); } 
                  if (b0.getY()>b1.getY()) { minBy = (float) b1.getY(); maxBy = (float) b0.getY(); } else { minBy = (float) b0.getY(); maxBy = (float) b1.getY(); } if (b2.getY()<minBy) { minBy = (float) b2.getY(); } if (b2.getY()>maxBy) { maxBy = (float) b2.getY(); } if (minAy>maxBy || maxAy<minBy) continue; 
                  if (a0.getZ()>a1.getZ()) { minAz = (float) a1.getZ(); maxAz = (float) a0.getZ(); } else { minAz = (float) a0.getZ(); maxAz = (float) a1.getZ(); } if (a2.getZ()<minAz) { minAz = (float) a2.getZ(); } if (a2.getZ()>maxAz) { maxAz = (float) a2.getZ(); } 
                  if (b0.getZ()>b1.getZ()) { minBz = (float) b1.getZ(); maxBz = (float) b0.getZ(); } else { minBz = (float) b0.getZ(); maxBz = (float) b1.getZ(); } if (b2.getZ()<minBz) { minBz = (float) b2.getZ(); } if (b2.getZ()>maxBz) { maxBz = (float) b2.getZ(); } if (minAz>maxBz || maxAz<minBz) continue; 
              }

              if ( a.isCollision(b) ) {
                  System.out.println("COLLISION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                  PhongMaterial sample = new PhongMaterial(collsnColor);
                  sample.setSpecularColor(lightColor);
                  sample.setSpecularPower(16);
                  shapeA.setMaterial(sample);
                  shapeB.setMaterial(sample);
                  return true;
              }
          }
      }

      return false;
  }

  void crossSection(  ) {
      path3D.getChildren().clear();
      double zHeight = (
              rootMeshRotates.getBoundsInLocal().getMaxZ()-
              rootMeshRotates.getBoundsInLocal().getMinZ()  );
      int nSections = 10;
      for ( int iSection = 0; iSection < nSections; iSection++ ) {
          crossSection(rootMeshRotates.getBoundsInLocal().getMinZ() + zHeight*((iSection+0.5)/nSections));
      }
  }

  void crossSection( double zLevel ) {
      for (int iMesh = 0; iMesh < rootMeshRotates.getChildren().size(); iMesh++) {
          MeshView     shape = (MeshView)rootMeshRotates.getChildren().get(iMesh);
          TriangleMesh  mesh = (TriangleMesh)shape.getMesh();
          for (int iFace = 0; iFace < mesh.getFaces().size()/mesh.getFaceElementSize(); iFace++ ) {
              //System.out.println("iFace:" + iFace);
              int iVertex0 = mesh.getFaces().get(iFace*6+0); // i*6+
              int iVertex1 = mesh.getFaces().get(iFace*6+2);
              int iVertex2 = mesh.getFaces().get(iFace*6+4);

              Point3D a0 = shape.localToParent( mesh.getPoints().get(iVertex0*3+0), 
                                                mesh.getPoints().get(iVertex0*3+1), 
                                                mesh.getPoints().get(iVertex0*3+2) );
              Point3D a1 = shape.localToParent( mesh.getPoints().get(iVertex1*3+0), 
                                                mesh.getPoints().get(iVertex1*3+1), 
                                                mesh.getPoints().get(iVertex1*3+2) );
              Point3D a2 = shape.localToParent( mesh.getPoints().get(iVertex2*3+0), 
                                                mesh.getPoints().get(iVertex2*3+1), 
                                                mesh.getPoints().get(iVertex2*3+2) );
              Triangle3D a = new Triangle3D( a0, a1, a2 );

              Pair3D pair = a.intersectPlaneZ( (float) zLevel );

              if (pair != null) {
                  Point3D point1 = rootMeshAll.localToParent(rootMeshRotates.localToParent(pair.a.x,pair.a.y,zLevel));
                  Point3D point2 = rootMeshAll.localToParent(rootMeshRotates.localToParent(pair.b.x,pair.b.y,zLevel));
    
                  List<org.fxyz.geometry.Point3D> pointPath = new ArrayList<org.fxyz.geometry.Point3D>();
                  pointPath.add(new org.fxyz.geometry.Point3D((float)point1.getX(),(float)point1.getY(),(float)point1.getZ()));
                  pointPath.add(new org.fxyz.geometry.Point3D((float)point2.getX(),(float)point2.getY(),(float)point2.getZ()));
                  PolyLine3D polyLine = new PolyLine3D(pointPath,5,Color.WHITE);
                  path3D.getChildren().add(polyLine);
              }

              //Point3D a00 = shape.localToParent( mesh.getPoints().get(0), 
              //          mesh.getPoints().get(1), 
              //          mesh.getPoints().get(2) );
              //System.out.println("Shape A at 0 "+a00.getX()+" "+a00.getY()+" "+a00.getZ());
          }
      }
  }

  SegmentedPaths getOffsetPaths(SegmentedPaths crossSection, float offset) {

      // Start the total timing
      long startTime = System.nanoTime();

      // Offset the cross-section
      SegmentedPaths offsetPaths = crossSection.offsetStage1(path2D, offset); // Create offsets

      // Find and remove intersections within the path
      long startTimeStage2 = System.nanoTime(); // Start  the timing for stage 2
      offsetPaths.offsetStage2(); 
      long   endTimeStage2 = System.nanoTime(); // Finish the timing for stage 2

      // Find and remove segments within offset of path
      crossSection.offsetStage3(offsetPaths, offset); 

      // Skipping stage 4. Not important for this exercise

      offsetPaths.removeMinisculeSegments();
      offsetPaths.combineSegmentPaths();
      offsetPaths.removeMinisculeSegments(); // Repeat for some extra cleanup 
      offsetPaths.combineSegmentPaths();

      // Finish the total timing
      long endTime  = System.nanoTime();

      // Record timings
      long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
      sumOfDurations += duration;
      long durationStage2 = (endTimeStage2 - startTimeStage2);  //divide by 1000000 to get milliseconds.
      sumOfDurationsStage2 += durationStage2;

      return offsetPaths;
  }

  void generateOffsetPathsDemo(  ) {
      // Get the cross-section
      SegmentedPaths paths = crossSection2D();
      paths.removeMinisculeSegments();
      paths.combineSegmentPaths();
      paths.removeMinisculeSegments(); // Repeat for some extra cleanup 
      paths.combineSegmentPaths();
      System.out.println("# of paths: "+paths.size());

      // Offset the cross-section
      SegmentedPaths offsetPaths01 = getOffsetPaths(paths, 10/2.f);
      SegmentedPaths offsetPaths02 = getOffsetPaths(paths, 20/2.f);
      SegmentedPaths offsetPaths03 = getOffsetPaths(paths, 30/2.f);
      SegmentedPaths offsetPaths04 = getOffsetPaths(paths, 40/2.f);
      SegmentedPaths offsetPaths05 = getOffsetPaths(paths, 50/2.f);
      SegmentedPaths offsetPaths06 = getOffsetPaths(paths, 60/2.f);
      SegmentedPaths offsetPaths07 = getOffsetPaths(paths, 70/2.f);
      SegmentedPaths offsetPaths08 = getOffsetPaths(paths, 80/2.f);
      SegmentedPaths offsetPaths09 = getOffsetPaths(paths, 90/2.f);
      SegmentedPaths offsetPaths10 = getOffsetPaths(paths,100/2.f);
      SegmentedPaths offsetPaths11 = getOffsetPaths(paths,110/2.f);
      SegmentedPaths offsetPaths12 = getOffsetPaths(paths,120/2.f);
      SegmentedPaths offsetPaths13 = getOffsetPaths(paths,130/2.f);
      SegmentedPaths offsetPaths14 = getOffsetPaths(paths,140/2.f);
      SegmentedPaths offsetPaths15 = getOffsetPaths(paths,150/2.f);
      SegmentedPaths offsetPaths16 = getOffsetPaths(paths,160/2.f);
      SegmentedPaths offsetPaths17 = getOffsetPaths(paths,170/2.f);
      SegmentedPaths offsetPaths18 = getOffsetPaths(paths,180/2.f);
      SegmentedPaths offsetPaths19 = getOffsetPaths(paths,190/2.f);

      paths        .displayPaths(path2D, Color.BLACK );
      offsetPaths01.displayPaths(path2D, Color.BLUE  );
      offsetPaths02.displayPaths(path2D, Color.BLUE  );
      offsetPaths03.displayPaths(path2D, Color.BLUE  );
      offsetPaths04.displayPaths(path2D, Color.BLUE  );
      offsetPaths05.displayPaths(path2D, Color.BLUE  );
      offsetPaths06.displayPaths(path2D, Color.BLUE  );
      offsetPaths07.displayPaths(path2D, Color.BLUE  );
      offsetPaths08.displayPaths(path2D, Color.BLUE  );
      offsetPaths09.displayPaths(path2D, Color.BLUE  );
      offsetPaths10.displayPaths(path2D, Color.BLUE  );
      offsetPaths11.displayPaths(path2D, Color.BLUE  );
      offsetPaths12.displayPaths(path2D, Color.BLUE  );
      offsetPaths13.displayPaths(path2D, Color.BLUE  );
      offsetPaths14.displayPaths(path2D, Color.BLUE  );
      offsetPaths15.displayPaths(path2D, Color.BLUE  );
      offsetPaths16.displayPaths(path2D, Color.BLUE  );
      offsetPaths17.displayPaths(path2D, Color.BLUE  );
      offsetPaths18.displayPaths(path2D, Color.BLUE  );
      offsetPaths19.displayPaths(path2D, Color.BLUE  );

      System.out.println("Calculations stage 2: " + ((double)(sumOfDurationsStage2)/1000000.0) + " milliseconds");
      System.out.println("Calculations total:   " + ((double)(sumOfDurations      )/1000000.0) + " milliseconds");
      sumOfDurationsStage2 = sumOfDurations = 0;
  }

  void newStage2D(  ) {
      Stage newStage = new Stage();
      
      newStage.setScene(scene2D);
      //scene2D.getStylesheets().add("chart.css"); //ToDo figure out path settings in Java  
      newStage.show();
  }

  SegmentedPaths crossSection2D(  ) {
      path2D.getChildren().clear();
      double zMin = Double.MAX_VALUE;
      double zMax = Double.MIN_VALUE;
      for (    int iMesh = 0;    iMesh < rootMeshRotates.getChildren().size(); iMesh++) {
          MeshView     shape = (MeshView)rootMeshRotates.getChildren().get(    iMesh  );
          TriangleMesh  mesh = (TriangleMesh)shape.getMesh();
          for (int iFace = 0; iFace < mesh.getFaces().size()/mesh.getFaceElementSize(); iFace++ ) {
              //System.out.println("iFace:" + iFace);
              int iVertex0 = mesh.getFaces().get(iFace*6+0); // i*6+
              int iVertex1 = mesh.getFaces().get(iFace*6+2);
              int iVertex2 = mesh.getFaces().get(iFace*6+4);

              Point3D a0 = shape.localToScene(  mesh.getPoints().get(iVertex0*3+0), 
                                                mesh.getPoints().get(iVertex0*3+1), 
                                                mesh.getPoints().get(iVertex0*3+2) );
              Point3D a1 = shape.localToScene(  mesh.getPoints().get(iVertex1*3+0), 
                                                mesh.getPoints().get(iVertex1*3+1), 
                                                mesh.getPoints().get(iVertex1*3+2) );
              Point3D a2 = shape.localToScene(  mesh.getPoints().get(iVertex2*3+0), 
                                                mesh.getPoints().get(iVertex2*3+1), 
                                                mesh.getPoints().get(iVertex2*3+2) );

              if ( a0.getZ() < zMin ) zMin = a0.getZ();
              if ( a1.getZ() < zMin ) zMin = a1.getZ();
              if ( a2.getZ() < zMin ) zMin = a2.getZ();
              if ( a0.getZ() > zMax ) zMax = a0.getZ();
              if ( a1.getZ() > zMax ) zMax = a1.getZ();
              if ( a2.getZ() > zMax ) zMax = a2.getZ();
          }
      }

      double zHeight = zMax - zMin;
      double ratioZ = 0.5;
      
      return crossSection2D(zMin + zHeight*ratioZ);
  }

  SegmentedPaths crossSection2D( double zLevel ) {
      SegmentedPaths paths = new SegmentedPaths();
      for (    int iMesh = 0;    iMesh < rootMeshRotates.getChildren().size(); iMesh++) {
          MeshView     shape = (MeshView)rootMeshRotates.getChildren().get(    iMesh  );
          TriangleMesh  mesh = (TriangleMesh)shape.getMesh();
          for (int iFace = 0; iFace < mesh.getFaces().size()/mesh.getFaceElementSize(); iFace++ ) {
              //System.out.println("iFace:" + iFace);
              int iVertex0 = mesh.getFaces().get(iFace*6+0); // i*6+
              int iVertex1 = mesh.getFaces().get(iFace*6+2);
              int iVertex2 = mesh.getFaces().get(iFace*6+4);

              Point3D a0 = shape.localToScene(  mesh.getPoints().get(iVertex0*3+0), 
                                                mesh.getPoints().get(iVertex0*3+1), 
                                                mesh.getPoints().get(iVertex0*3+2) );
              Point3D a1 = shape.localToScene(  mesh.getPoints().get(iVertex1*3+0), 
                                                mesh.getPoints().get(iVertex1*3+1), 
                                                mesh.getPoints().get(iVertex1*3+2) );
              Point3D a2 = shape.localToScene(  mesh.getPoints().get(iVertex2*3+0), 
                                                mesh.getPoints().get(iVertex2*3+1), 
                                                mesh.getPoints().get(iVertex2*3+2) );
              Triangle3D a = new Triangle3D( a0, a1, a2 );

              Pair3D pair = a.intersectPlaneZ( (float) zLevel );

              if (pair != null) { // No intersection => null
                  SegmentedPath segment = new SegmentedPath(pair.a);
                  segment.addNextPoint(pair.b);
                  paths.addPath( segment );
              }
          }
      }
      return paths;
  }

}
