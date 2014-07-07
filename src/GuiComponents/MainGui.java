/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainGui.java
 *
 * Created on Jul 16, 2012, 12:16:12 AM
 */
package GuiComponents;

import Images.PCX;
import OpenGL.OpenGL3DView;
import ModelFormat.ModelFormat;
import ModelFormat.QuakeMDL.MDL;
import ModelFormat.WavefrontOBJ.OBJ;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author cjones
 */
public class MainGui extends javax.swing.JFrame
{
	private OpenGL3DView OpenGLDisplay;
	private ModelFormat model;
	private UVMapEditor uvEditorWindow;
	//public static final int MODE_NONE = 0;
	public static final int MODE_VERTEX = 1;
	public static final int MODE_EDGE = 2;
	public static final int MODE_FACE = 3;
	private int editingMode = MODE_VERTEX;

	/** Creates new form MainGui */
	public MainGui()
	{
		initComponents();
		initUserComponents();
		init3DView(null);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		//just for debug...
		CameraWatcher x = new CameraWatcher(this, OpenGLDisplay);
		x.start();
	}

	public MainGui(ModelFormat _model)
	{
		//<editor-fold defaultstate="collapsed" desc="window listener (unused) for closing">
		/*this.addWindowListener(new WindowAdapter()
		{
		@Override
		public void windowClosing(WindowEvent e)
		{
		//System.exit(0); //close program
		//this occasionally causes thread errors (something to do with daemon/non daemon threads???
		MainGui.this.dispose(); //use dispose instead
		return;
		}

		@Override
		public void windowClosed(WindowEvent e)
		{
		}
		});*/
		//</editor-fold>

		model = _model;
		initComponents();
		initUserComponents();
		init3DView(_model);
		loadModel(_model);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		//just for debug...
		CameraWatcher x = new CameraWatcher(this, OpenGLDisplay);
		x.start();
	}

	public final void loadModel(ModelFormat model)
	{
		String[] frameNames = model.getFrameNames();
		framesListBox.removeAll();
		framesListBox.setListData(frameNames);
	}

	private void init3DView(ModelFormat model)
	{
		OpenGLDisplay = new OpenGL3DView(this, canvas, model);
		OpenGLDisplay.start();
	}

	public OpenGL3DView getOpenGLDisplay()
	{
		return OpenGLDisplay;
	}

	public int getEditingMode()
	{
		return this.editingMode;
	}

	public void setEditingMode(int mode)
	{
		if (mode < MODE_VERTEX || mode > MODE_FACE)
			throw new IllegalArgumentException("Invalid editing mode.");
		this.editingMode = mode;

		this.setModeButtonStates();
		if (uvEditorWindow != null)
			uvEditorWindow.setModeButtonStates();
	}

	public void setModeButtonStates()
	{
		if (this.editingMode == MODE_VERTEX)
		{
			modeButton_vertex.setEnabled(false);

			modeButton_edge.setSelected(false);
			modeButton_edge.setEnabled(true);

			modeButton_face.setSelected(false);
			modeButton_face.setEnabled(true);
		}
		else if (this.editingMode == MODE_EDGE)
		{
			modeButton_edge.setEnabled(false);

			modeButton_vertex.setSelected(false);
			modeButton_vertex.setEnabled(true);

			modeButton_face.setSelected(false);
			modeButton_face.setEnabled(true);
		}
		else
		{
			modeButton_face.setEnabled(false);

			modeButton_vertex.setSelected(false);
			modeButton_vertex.setEnabled(true);

			modeButton_edge.setSelected(false);
			modeButton_edge.setEnabled(true);
		}

	}

	public void clearUVEditor()
	{
		this.uvEditorWindow = null;
		button_uvEditor.setEnabled(true);
		button_uvEditor.setSelected(false);
	}

	//<editor-fold defaultstate="collapsed" desc="Extra user components">
	private void initUserComponents()
	{
		//<editor-fold defaultstate="collapsed" desc="Shortcut key actions">
		//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		/*Action closeAction = new AbstractAction()
		{
		@Override
		public void actionPerformed(ActionEvent e)
		{
		UVMapEditor.this.dispose();
		}
		};
		setShortcutKey("Escape", KeyEvent.VK_ESCAPE, 0, closeAction);*/
		//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		Action vertexModeAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				modeButton_vertex.setSelected(true);
				MainGui.this.setEditingMode(MainGui.MODE_VERTEX);
			}
		};
		setShortcutKey("1", KeyEvent.VK_1, 0, vertexModeAction);
		//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		Action edgeModeAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				modeButton_edge.setSelected(true);
				MainGui.this.setEditingMode(MainGui.MODE_EDGE);
			}
		};
		setShortcutKey("2", KeyEvent.VK_2, 0, edgeModeAction);
		//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		Action faceModeAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				modeButton_face.setSelected(true);
				MainGui.this.setEditingMode(MainGui.MODE_FACE);
			}
		};
		setShortcutKey("3", KeyEvent.VK_3, 0, faceModeAction);
		//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		Action centerToSelectionAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Center to selection.");
			}
		};
		setShortcutKey("Ctrl+Space", KeyEvent.VK_SPACE, InputEvent.SHIFT_DOWN_MASK, centerToSelectionAction);
		//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		//</editor-fold>

		this.editingMode = MODE_VERTEX;
		this.setModeButtonStates();
	}
	//</editor-fold>

	private void setShortcutKey(String keyStrokeName, int keyEvent, int modifierMask, Action action)
	{
		KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent, modifierMask, false);
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStrokeName);
		this.getRootPane().getActionMap().put(keyStrokeName, action);
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jScrollPane1 = new javax.swing.JScrollPane();
        framesListBox = new javax.swing.JList();
        canvas = new java.awt.Canvas();
        jToolBar1 = new javax.swing.JToolBar();
        modeButton_vertex = new javax.swing.JToggleButton();
        modeButton_edge = new javax.swing.JToggleButton();
        modeButton_face = new javax.swing.JToggleButton();
        button_uvEditor = new javax.swing.JButton();
        button_drawFaces = new javax.swing.JButton();
        button_drawEdges = new javax.swing.JButton();
        button_drawVertices = new javax.swing.JButton();
        posTextBox = new javax.swing.JTextField();
        anglesTextBox = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        framesListBox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        framesListBox.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                framesListBoxMouseClicked(evt);
            }
        });
        framesListBox.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                framesListBoxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(framesListBox);

        canvas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        canvas.setName(""); // NOI18N
        canvas.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentResized(java.awt.event.ComponentEvent evt)
            {
                canvasComponentResized(evt);
            }
        });

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        modeButton_vertex.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GuiComponents/Resources/vertexIcon_up.png"))); // NOI18N
        modeButton_vertex.setAlignmentX(0.5F);
        modeButton_vertex.setFocusable(false);
        modeButton_vertex.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        modeButton_vertex.setMargin(new java.awt.Insets(1, 1, 1, 1));
        modeButton_vertex.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GuiComponents/Resources/vertexIcon_dn.png"))); // NOI18N
        modeButton_vertex.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        modeButton_vertex.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                modeButton_vertexActionPerformed(evt);
            }
        });
        jToolBar1.add(modeButton_vertex);

        modeButton_edge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GuiComponents/Resources/edgeIcon_up.png"))); // NOI18N
        modeButton_edge.setFocusable(false);
        modeButton_edge.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        modeButton_edge.setMargin(new java.awt.Insets(1, 1, 1, 1));
        modeButton_edge.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GuiComponents/Resources/edgeIcon_dn.png"))); // NOI18N
        modeButton_edge.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        modeButton_edge.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                modeButton_edgeActionPerformed(evt);
            }
        });
        jToolBar1.add(modeButton_edge);

        modeButton_face.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GuiComponents/Resources/faceIcon_up.png"))); // NOI18N
        modeButton_face.setFocusable(false);
        modeButton_face.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        modeButton_face.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GuiComponents/Resources/faceIcon_dn.png"))); // NOI18N
        modeButton_face.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        modeButton_face.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                modeButton_faceActionPerformed(evt);
            }
        });
        jToolBar1.add(modeButton_face);

        button_uvEditor.setText("Show UV Mapping");
        button_uvEditor.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        button_uvEditor.setFocusable(false);
        button_uvEditor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_uvEditor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_uvEditor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                button_uvEditorActionPerformed(evt);
            }
        });
        jToolBar1.add(button_uvEditor);

        button_drawFaces.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        button_drawFaces.setFocusable(false);
        button_drawFaces.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_drawFaces.setLabel("Draw Faces");
        button_drawFaces.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_drawFaces.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                button_drawFacesActionPerformed(evt);
            }
        });
        jToolBar1.add(button_drawFaces);

        button_drawEdges.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        button_drawEdges.setFocusable(false);
        button_drawEdges.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_drawEdges.setLabel("Draw Edges");
        button_drawEdges.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_drawEdges.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                button_drawEdgesActionPerformed(evt);
            }
        });
        jToolBar1.add(button_drawEdges);

        button_drawVertices.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        button_drawVertices.setFocusable(false);
        button_drawVertices.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_drawVertices.setLabel("Draw Vertices");
        button_drawVertices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_drawVertices.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                button_drawVerticesActionPerformed(evt);
            }
        });
        jToolBar1.add(button_drawVertices);

        posTextBox.setText("jTextField1");

        anglesTextBox.setText("jTextField1");

        jButton4.setText("set texture");
        jButton4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton4ActionPerformed(evt);
            }
        });

        jButton1.setText("Rename Frames");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Open");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Save");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 991, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(posTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(anglesTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 333, Short.MAX_VALUE))
                            .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE))))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anglesTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void framesListBoxValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_framesListBoxValueChanged
	{//GEN-HEADEREND:event_framesListBoxValueChanged
		OpenGLDisplay.setFrames(model.getFrameIndexFromNameIndex(framesListBox.getMinSelectionIndex()), model.getFrameIndexFromNameIndex(framesListBox.getMaxSelectionIndex()));
	}//GEN-LAST:event_framesListBoxValueChanged

	private void framesListBoxMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_framesListBoxMouseClicked
	{//GEN-HEADEREND:event_framesListBoxMouseClicked
		/*if (evt.getClickCount() == 2)
		{
		String selectedFrame = (String) framesListBox.getSelectedValue();
		selectedFrame.replaceAll("[0-9]", "");
		String[] frameNames = framesListBox.getModel().
		}*/
	}//GEN-LAST:event_framesListBoxMouseClicked

	private void canvasComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_canvasComponentResized
	{//GEN-HEADEREND:event_canvasComponentResized
		if (evt.getID() == java.awt.event.ComponentEvent.COMPONENT_RESIZED)
		{
			if (OpenGLDisplay != null)
				OpenGLDisplay.updateDisplaySize();
		}
	}//GEN-LAST:event_canvasComponentResized

	private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
	{//GEN-HEADEREND:event_jMenuItem1ActionPerformed
		JFileChooser openDialog = new JFileChooser();
		FileNameExtensionFilter mdlFilter = new FileNameExtensionFilter("Quake Model", "mdl");
		FileNameExtensionFilter objFilter = new FileNameExtensionFilter("Wavefront OBJ", "obj");

		openDialog.addChoosableFileFilter(objFilter);
		openDialog.addChoosableFileFilter(mdlFilter);
		openDialog.setCurrentDirectory(new File("."));

		int returnVal = openDialog.showOpenDialog(MainGui.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File modelFile = openDialog.getSelectedFile();
			if (mdlFilter.accept(modelFile))
				model = new MDL(modelFile.getAbsolutePath());
			else if (objFilter.accept(modelFile))
				model = new OBJ(modelFile);
			else //tried to open some wierdo file :(
				JOptionPane.showMessageDialog(this, "Can't open that type of file.");
			loadModel(model);
			OpenGLDisplay.setModel(model);
			if (uvEditorWindow != null)
			{
				uvEditorWindow.dispose();
				uvEditorWindow = null;
			}
			framesListBox.setSelectedIndex(0);
		}
	}//GEN-LAST:event_jMenuItem1ActionPerformed

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton4ActionPerformed
	{//GEN-HEADEREND:event_jButton4ActionPerformed
		if (model == null)
			return;

		JFileChooser openDialog = new JFileChooser();
		FileNameExtensionFilter pcxFilter = new FileNameExtensionFilter("ZSoft PCX", "pcx");

		openDialog.addChoosableFileFilter(pcxFilter);
		openDialog.setCurrentDirectory(new File("."));

		int returnVal = openDialog.showOpenDialog(MainGui.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File imageFile = openDialog.getSelectedFile();
			PCX pcx = new PCX(imageFile.getAbsolutePath());
			model.setTexture(pcx);
			OpenGLDisplay.setModel(model);
		}
	}//GEN-LAST:event_jButton4ActionPerformed

	private void modeButton_vertexActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_modeButton_vertexActionPerformed
	{//GEN-HEADEREND:event_modeButton_vertexActionPerformed
		this.setEditingMode(MODE_VERTEX);
	}//GEN-LAST:event_modeButton_vertexActionPerformed

	private void modeButton_edgeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_modeButton_edgeActionPerformed
	{//GEN-HEADEREND:event_modeButton_edgeActionPerformed
		this.setEditingMode(MODE_EDGE);
	}//GEN-LAST:event_modeButton_edgeActionPerformed

	private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem2ActionPerformed
	{//GEN-HEADEREND:event_jMenuItem2ActionPerformed
		if (model == null)
			return;

		JFileChooser saveDialog = new JFileChooser();
		FileNameExtensionFilter mdlFilter = new FileNameExtensionFilter("Quake MDL", "mdl");

		saveDialog.addChoosableFileFilter(mdlFilter);
		saveDialog.setCurrentDirectory(new File("."));

		int returnVal = saveDialog.showSaveDialog(MainGui.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File saveFile = saveDialog.getSelectedFile();
			if (!saveFile.exists() || saveFile.canWrite() || saveFile.exists() && JOptionPane.showConfirmDialog(this,
					"File exists, overwrite? (I wouldn't recommend it!)",
					"Confirm Overwrite",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				String filename = saveFile.getAbsolutePath();
				if (!filename.endsWith(".mdl"))
					filename = filename + ".mdl";

				model.exportMDL(filename);
			}

		}
	}//GEN-LAST:event_jMenuItem2ActionPerformed

	private void modeButton_faceActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_modeButton_faceActionPerformed
	{//GEN-HEADEREND:event_modeButton_faceActionPerformed
		this.setEditingMode(MODE_FACE);
	}//GEN-LAST:event_modeButton_faceActionPerformed

	private void button_uvEditorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_button_uvEditorActionPerformed
	{//GEN-HEADEREND:event_button_uvEditorActionPerformed
		if (model != null)
		{
			uvEditorWindow = new UVMapEditor(model, this);
			button_uvEditor.setEnabled(false);
		}
	}//GEN-LAST:event_button_uvEditorActionPerformed

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
	{//GEN-HEADEREND:event_jButton1ActionPerformed
		if (model == null)
			return;

		JFileChooser openDialog = new JFileChooser();
		FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text file TXT", "txt");

		openDialog.addChoosableFileFilter(txtFilter);
		openDialog.setCurrentDirectory(new File("."));

		//FIXME: This whole section is a HACK
		int returnVal = openDialog.showOpenDialog(MainGui.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				File frameListFile = openDialog.getSelectedFile();

				Scanner scn = new Scanner(frameListFile);
				int numLines = 0;
				while (scn.hasNextLine())
				{
					scn.nextLine();
					numLines++;
				}
				scn.close();

				String[] frameName = new String[numLines];

				scn = new Scanner(frameListFile);

				for (int i = 0; i < frameName.length; i++)
				{
					frameName[i] = scn.nextLine();
				}

				scn.close();

				if (numLines != model.getFrameNames().length)
					System.out.println("Number of lines in text file don't match number of frames in model.");

				MDL MDLModel = (MDL)model;
				for (int i = 0; i < frameName.length; i++)
				{
					MDLModel.getFrames()[i].getFrameElements()[0].HACK_setName(frameName[i]);
				}
			}
			catch (Exception e)
			{
				//System.out.println(e.getMessage());
				throw new RuntimeException(e.getMessage());
			}

		}
	}//GEN-LAST:event_jButton1ActionPerformed

    private void button_drawFacesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_button_drawFacesActionPerformed
    {//GEN-HEADEREND:event_button_drawFacesActionPerformed
        OpenGLDisplay.toggleFaceVisibility();
    }//GEN-LAST:event_button_drawFacesActionPerformed

    private void button_drawEdgesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_button_drawEdgesActionPerformed
    {//GEN-HEADEREND:event_button_drawEdgesActionPerformed
        OpenGLDisplay.toggleEdgeVisibility();
    }//GEN-LAST:event_button_drawEdgesActionPerformed

    private void button_drawVerticesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_button_drawVerticesActionPerformed
    {//GEN-HEADEREND:event_button_drawVerticesActionPerformed
        OpenGLDisplay.toggleVertexVisibility();
    }//GEN-LAST:event_button_drawVerticesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField anglesTextBox;
    private javax.swing.JButton button_drawEdges;
    private javax.swing.JButton button_drawFaces;
    private javax.swing.JButton button_drawVertices;
    private javax.swing.JButton button_uvEditor;
    private java.awt.Canvas canvas;
    private javax.swing.JList framesListBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton modeButton_edge;
    private javax.swing.JToggleButton modeButton_face;
    private javax.swing.JToggleButton modeButton_vertex;
    private javax.swing.JTextField posTextBox;
    // End of variables declaration//GEN-END:variables

	private class CameraWatcher extends Thread
	{
		private OpenGL3DView camera;
		private MainGui owner;

		public CameraWatcher(MainGui owner, OpenGL3DView camera)
		{
			this.owner = owner;
			this.camera = camera;
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					sleep(100);
				}
				catch (InterruptedException ex)
				{
					Logger.getLogger(MainGui.class.getName()).log(Level.SEVERE, null, ex);
					throw new RuntimeException(ex.getMessage());
				}

				owner.posTextBox.setText(camera.getPos().toString());
				owner.anglesTextBox.setText(camera.getAngles().toString());
			}
		}
	}
}
