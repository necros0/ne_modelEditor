/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import GuiComponents.MainGui;
import ModelFormat.QuakeMDL.MDL;
import Utility.BinaryFileReader;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author cjones
 */
public class Main
{
	//private static final String INPUT_FILE_NAME = "Testing/demon.mdl"; //temp for testing.
//
	//private static final String OBJ_FILE_NAME = "Testing/dknight.obj"; //temp for testing.
	//private static final String OBJ_FILE_NAME = "Testing/box/box.obj"; //temp for testing.
//
	//private static final String PCX_FILE_NAME = "Testing/dknight.pcx";
	private static final String COLOURMAP_FILE_NAME = "palette.lmp";
	//private static final String MDL_TEST_SAVE_FILE = "output.mdl";
	public static int[][] colourMap;

	public static void main(String[] args)
	{
		System.out.println("test");
		System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir") + "/natives"); //allows DLL files to be placed in the /natives folder

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//I HATE the default java gui look and feel.
		}
		catch (Exception e)
		{
			System.out.println("Couldn't set UI look and feel.");
		}

		MDL.colourMap = getColourMap();

		//System.out.println("demon scale: " + model.getHeader().getScale());
		//model.exportMDL(MDL_TEST_SAVE_FILE);
		//MainGui gui = new MainGui(model);

		//System.out.println("mins/maxs " + model.getFrames()[0].getFrameElements()[0].getMins() + ", " + model.getFrames()[0].getFrameElements()[0].getMaxs());

		//PCX pcxImage = new PCX(PCX_FILE_NAME);

		//OBJ objModel = new OBJ(new File(OBJ_FILE_NAME));
		//objModel.setTexture(pcxImage);
		//File dir = new File("D:/Users/cjones/Documents/NetBeansProjects/ne_qcStarter/Testing/dknight");
		//File[] allFiles = dir.listFiles();
		//objModel.addFrames(allFiles);
		//objModel.exportMDL(MDL_TEST_SAVE_FILE);

		//MDL newModel = new MDL(objModel);
		//newModel.exportMDL(MDL_TEST_SAVE_FILE);


		//MDL model = new MDL(INPUT_FILE_NAME);
		//MainGui gui = new MainGui(model);

		SwingUtilities.invokeLater(new Runnable() //this is NECESSARY to prevent the program crashing from running AWT stuff on the opengl thread (Swing/AWT is NOT threadsafe)
		{
			@Override
			public void run()
			{
				MainGui gui = new MainGui();
			}
		});

	}

	/**
	 * Create colour map from palette.lmp file.
	 *
	 * @return
	 */
	//TODO: Put this is the right spot later.
	private static int[][] getColourMap()
	{
		BinaryFileReader data = new BinaryFileReader(COLOURMAP_FILE_NAME);

		int[][] _colourMap = new int[256][3];

		for (int i = 0; i < 256; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				_colourMap[i][j] = data.nextUnsignedByte();
			}
		}

		return _colourMap;
	}
}
