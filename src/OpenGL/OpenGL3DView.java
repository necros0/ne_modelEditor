/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package OpenGL;

import Geometry.Vec3;
import GuiComponents.MainGui;
import GuiComponents.UVMapPanel;
import java.awt.Canvas;
import ModelFormat.ModelFormat;
import javax.swing.SwingUtilities;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 *
 * @author cjones
 */
public class OpenGL3DView extends Thread
{
	private UVMapPanel uvPanel;
	private MainGui mainGui;
	private Canvas canvas;
	private ModelFormat model;
	//Current frame is set from outside this object.  This disconnects most of the 3d stuff from the rest of the program
	private int firstFrame, lastFrame, currentFrame;
	//Just used for animation (time > frameTime) then next frame dealio
	private int frameTime;
	//generated for opengl so it knows the texture we are talking about
	private int texID;
	//These are for displaying the model and keeping track of increments from mouse inputs.
	private Vec3 pos = new Vec3(0.0f, 0.0f, 0.0f), angles = new Vec3(-15.0f, 0.0f, 0.0f);
	private Vec3 org = new Vec3(), v_angle = new Vec3();
	private float distance = 96f;
	private boolean isTextureUploadRequired = true;
	public static final float MAX_ZOOM = 256f;
	//private ArrayList<Integer> model.selectedVertices = new ArrayList();
	//////
	private boolean LMBDown = false;
	//
	private static final float DISPLAY_FOV = 60.0f;
	private static final float NEAR_CLIP_PLANE = 0.1f;
	private static final float FAR_CLIP_PLANE = 512.0f;
	private Vec3 mouseOrigin, mouseDir;

	private boolean isFacesDisplayed, isWireframeDisplayed, isVertexDisplayed;

	public OpenGL3DView(MainGui mainGui, Canvas _parent, ModelFormat _model)
	{
		super("3DViewThread");
		this.mainGui = mainGui;
		this.canvas = _parent;
		this.model = _model;
	}

	@Override
	public void run()
	{
		try
		{
			Display.setDisplayMode(new DisplayMode(canvas.getWidth(), canvas.getHeight()));
			Display.setTitle("3D View");
			Display.setParent(canvas);
			Display.create();
			Mouse.create();
		}
		catch (LWJGLException e)
		{
			throw new RuntimeException(e.getMessage());
			//e.printStackTrace();
			//System.exit(0);
		}
		System.out.println("Run");

		// init OpenGL
		initGL();

		while (!Display.isCloseRequested())
		{
			long lastTime, delta;
			lastTime = System.nanoTime();
			renderGL();
			delta = 15 - (System.nanoTime() - lastTime) / 1000000;//maintain 100fps
			if (delta > 0)
			{
				try
				{
					sleep(delta); //so it doesn't use up 100% core usage.
				}
				catch (InterruptedException e)
				{
				}
			}

			Display.update();
		}

		Display.destroy();
	}

	private void initGL()
	{
		/* OpenGL */
		int width = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();

		GL11.glViewport(0, 0, width, height); // Reset The Current Viewport
		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix
		GLU.gluPerspective(DISPLAY_FOV, ((float) width / (float) height), NEAR_CLIP_PLANE, FAR_CLIP_PLANE); // Calculate The Aspect Ratio Of The Window
		GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix
		GL11.glLoadIdentity(); // Reset The Modelview Matrix

		GL11.glEnable(GL11.GL_TEXTURE_2D); // enables texturing
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH); // Enables Smooth Shading
		GL11.glClearColor(0.6f, 0.6f, 0.6f, 0.5f); // Background Colour
		GL11.glClearDepth(1.0f); // Depth Buffer Setup

		GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Test To Do
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // Really Nice Perspective Calculations

	}

	private void renderGL()
	{
		int time;
		int mouseDWheel;

		//*
		time = (int) (System.nanoTime() / 1000000);
		if (frameTime == 0 || time > frameTime)
		{
			frameTime = time + 100;
			currentFrame++;
			if (currentFrame > lastFrame)
				currentFrame = firstFrame;
		}
		//*/



		if (Mouse.isButtonDown(1))
		{
			float moveScale = distance / 768 + 0.25f;
			angles.y = angles.y + Mouse.getDX() * 1.0f * moveScale;
			if (angles.y >= 360)
				angles.y = angles.y - 360; //keep these values clamped.
			else if (angles.y <= 0)
				angles.y = angles.y + 360;

			angles.x = angles.x + Mouse.getDY() * 1.0f * moveScale;
			if (angles.x > 89)
				angles.x = 89;
			else if (angles.x < -89)
				angles.x = -89;
		}

		if (Mouse.isButtonDown(2))
		{
			float moveScale = distance / 768 + 0.25f;
			pos = pos.add(angles.getVRight().scale(Mouse.getDX() * 0.4f * moveScale));
			pos = pos.add(angles.getVUp().scale(Mouse.getDY() * 0.4f * moveScale));
		}

		mouseDWheel = Mouse.getDWheel();
		if (mouseDWheel != 0)
		{
			distance = distance + (-mouseDWheel * 0.002f * distance);
			if (distance > MAX_ZOOM)
				distance = MAX_ZOOM;
			else if (distance < 10.0f)
				distance = 10.0f;
		}

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
		GL11.glLoadIdentity(); // Reset The View

		updateMouseVectors(Mouse.getX(), Mouse.getY());
		if (Mouse.isInsideWindow()) //so we don't check needlessly (the check is pretty expensive)
		{
			if (model != null && model.isVertexCloseToLine(mouseOrigin, mouseDir, 1, this.currentFrame))
			{
				//this is NECESSARY to prevent the program crashing from running AWT stuff on the opengl thread (Swing/AWT is NOT threadsafe)
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						canvas.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
					}
				});
			}
			else
			{
				//this is NECESSARY to prevent the program crashing from running AWT stuff on the opengl thread (Swing/AWT is NOT threadsafe)
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						canvas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
					}
				});
			}
		}

		if (Mouse.isButtonDown(0))
		{
			if (!LMBDown)
			{
				if (model != null)
				{
					if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
						model.selectedVertices.clear();
					Integer clickedVertex = new Integer(model.getVertexClosestToLine(mouseOrigin, mouseDir, getVertexSizeFromDistance(distance), this.currentFrame));
					if (clickedVertex.intValue() != -1) //-1 = nothing
						model.selectedVertices.add(clickedVertex);
					if (uvPanel != null)
						uvPanel.repaint();
				}
			}
			LMBDown = true;
		}
		else
			LMBDown = false;

		org = pos.add((angles.getVForward()).scale(distance));
		v_angle = org.subtract(pos).scale(1);
		v_angle.y = -v_angle.y;
		v_angle = v_angle.toAngles();

		GL11.glRotatef(v_angle.x - 90f, 1.0f, 0.0f, 0.0f);
		GL11.glRotatef(v_angle.y - 90f, 0.0f, 0.0f, 1.0f);
		GL11.glTranslatef(-org.x, -org.y, -org.z);

		if (model != null)
			drawModel();

		drawGrid(); //draw a nice grid!


		/*
		if (vec0 != null)
		{
		//Primitive.drawCube(vec0, 0.5f);
		Primitive.drawCube(vec1, 0.5f);
		}
		//*/

		int glError = GL11.glGetError();
		if (glError != 0)
			System.out.println("OpenGL Error: " + glError);
	}

	private void drawModel()
	{
		if (isTextureUploadRequired)
		{
			texID = model.UploadTextureToOpenGL(0); //skin index 0 for now.
			System.out.println("texID: " + texID);
			isTextureUploadRequired = false;
		}

		if (firstFrame == -1) //no selection
			return;

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL); //turn on fill mode
		GL11.glColor3f(1.0f, 1.0f, 1.0f); //full colour

		GL11.glCullFace(GL11.GL_FRONT); //this stuff culls backfaces.
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		if (this.isFacesDisplayed)
			model.drawToOpenGL(currentFrame, mainGui.getEditingMode());
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);


		if (isWireframeDisplayed)
			model.drawEdgesToOpenGL(currentFrame, mainGui.getEditingMode());


		if (isVertexDisplayed)
			model.drawVerticesToOpenGL(currentFrame, getVertexSizeFromDistance(distance));


	}

	private static float getVertexSizeFromDistance(float distance)
	{
		return Math.abs(1.0f + (distance / MAX_ZOOM) * 0.25f) * 4.0f;
	}

	private void drawGrid()
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D); //so lines can be coloured

		GL11.glLineWidth(1.5f);
		GL11.glColor3f(0.4f, 0.4f, 0.4f);

		//Start drawing
		GL11.glBegin(GL11.GL_LINES);

		GL11.glVertex3f(-128.0f, 0.0f, 0.0f);
		GL11.glVertex3f(128, 0.0f, 0.0f);

		GL11.glVertex3f(0.0f, -128.0f, 0.0f);
		GL11.glVertex3f(0.0f, 128, 0.0f);

		GL11.glEnd();
		//Stop drawing
		//Axial grid lines

		//grid lines
		GL11.glLineWidth(1.0f);
		GL11.glColor3f(0.5f, 0.5f, 0.5f);
		//Start drawing
		GL11.glBegin(GL11.GL_LINES);

		for (float i = -128; i <= 128; i += 16)
		{
			GL11.glVertex3f(-128.0f, i, 0.0f);
			GL11.glVertex3f(128, i, 0.0f);

			GL11.glVertex3f(i, -128.0f, 0.0f);
			GL11.glVertex3f(i, 128, 0.0f);
		}

		GL11.glEnd();
		//Stop drawing

		/*
		if (vec0 != null)
		{
		GL11.glLineWidth(1.5f);
		GL11.glColor3f(0.4f, 0.4f, 0.4f);

		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(vec0.x, vec0.y, vec0.z);
		GL11.glVertex3f(vec1.x, vec1.y, vec1.z);
		GL11.glEnd();
		}
		//*/

		GL11.glEnable(GL11.GL_TEXTURE_2D); //reenable now that we're done.
	}

	public void updateDisplaySize()
	{
		//FIXME: Fix window resizing breaking OpenGL.
	}

	/*public void setDisplayWireframe(boolean val)
	{
		isWireframeDisplayed = val;
	}

	public void setDisplayVertices(boolean val)
	{
		isVertexDisplayed = val;
	}*/

	public void setFrames(int _firstFrame, int _lastFrame)
	{
		firstFrame = _firstFrame;
		lastFrame = _lastFrame;
		currentFrame = firstFrame;
	}

	public void setModel(ModelFormat newModel)
	{
		isTextureUploadRequired = true;
		model = newModel;
	}

	public static void setDefaultVertexDisplayColour()
	{
		Vec3 c = new Vec3(ModelFormat.VERTEX_COLOUR);
		GL11.glColor3f(c.x, c.y, c.z);
	}

	public static void setDefaultVertexOnseamDisplayColour()
	{
		Vec3 c = new Vec3(ModelFormat.VERTEX_ONSEAM_COLOUR);
		GL11.glColor3f(c.x, c.y, c.z);
	}

	public static void setSelectedVertexDisplayColour()
	{
		Vec3 c = new Vec3(ModelFormat.SELECTED_VERTEX_COLOUR);
		GL11.glColor3f(c.x, c.y, c.z);
	}

	public void toggleFaceVisibility()
	{
		this.isFacesDisplayed = !this.isFacesDisplayed;
	}

	public void toggleEdgeVisibility()
	{
		this.isWireframeDisplayed = !this.isWireframeDisplayed;
	}

	public void toggleVertexVisibility()
	{
		this.isVertexDisplayed = !this.isVertexDisplayed;
	}

	public Vec3 getPos()
	{
		return this.pos;
	}

	public Vec3 getAngles()
	{
		return this.angles;
	}

	//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private void updateMouseVectors(int x, int y)
	{
		// look direction
		Vec3 view = (pos.subtract(org)).normalize();

		// screenX
		Vec3 screenHorizontally = view.cross(angles.getVUp().scale(-1)).normalize();

		// screenY
		Vec3 screenVertically = (screenHorizontally.cross(view)).normalize();

		final float radians = (float) (DISPLAY_FOV * Math.PI / 180f);
		float halfHeight = (float) (Math.tan(radians / 2) * NEAR_CLIP_PLANE);
		float halfScaledAspectRatio = halfHeight * ((float) Display.getWidth() / (float) Display.getHeight());

		screenVertically = screenVertically.scale(halfHeight);
		screenHorizontally = screenHorizontally.scale(halfScaledAspectRatio);

		float screenX = x;
		float screenY = y;

		mouseOrigin = new Vec3(org);
		mouseOrigin = mouseOrigin.add(view.scale(NEAR_CLIP_PLANE));

		screenX -= (float) Display.getWidth() / 2f;
		screenY -= (float) Display.getHeight() / 2f;

		// normalize to 1
		screenX /= ((float) Display.getWidth() / 2f);
		screenY /= ((float) Display.getHeight() / 2f);

		mouseOrigin = mouseOrigin.add(new Vec3(screenHorizontally.x * screenX + screenVertically.x * screenY,
				screenHorizontally.y * screenX + screenVertically.y * screenY,
				screenHorizontally.z * screenX + screenVertically.z * screenY));
		mouseDir = mouseOrigin.subtract(org);

		mouseDir = mouseDir.normalize();
	}

	public void connectToUVPanel(UVMapPanel uvPanel)
	{
		this.uvPanel = uvPanel;
	}
}
