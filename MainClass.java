//package b_object3D_collision;
package c;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainClass extends Application {

  Controller controller = new Controller();
  ViewSTL    viewer     = new ViewSTL(controller);

  public static void main(String[] args) {
    //System.setProperty("prism.dirtyopts", "false");
    launch(args);
  }

  @Override
  public void start(Stage primaryStage)
  {
      viewer.start(primaryStage);
  }
}
