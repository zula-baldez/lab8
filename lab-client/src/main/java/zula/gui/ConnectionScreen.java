package zula.gui;

import zula.gui.views.ConnectionAndLoginScreenFabric;
import zula.util.Constants;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Dimension;

public class ConnectionScreen {

   private final ConnectionAndLoginScreenFabric connectionAndLoginScreenFabric = new ConnectionAndLoginScreenFabric(Constants.RU_BUNDLE);
   public void printScreen() {
      JFrame mainFrame = new JFrame();
      mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

      mainFrame.setMinimumSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
      mainFrame.setSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
      mainFrame.setLocationRelativeTo(null);
      JPanel mainPanel = connectionAndLoginScreenFabric.createConnectionFrame(mainFrame);
      mainFrame.add(mainPanel);
      mainFrame.setVisible(true);

   }






}



