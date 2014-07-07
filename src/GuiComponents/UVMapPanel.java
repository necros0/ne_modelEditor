/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GuiComponents;

import Geometry.Vec3;
import ModelFormat.ModelFormat;
import OpenGL.OpenGL3DView;
import ModelFormat.QuakeMDL.MDL;
import Undo.UndoStack;
import Undo.UndoUVChange;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JPanel;

/**
 *
 * @author cjones
 */
public class UVMapPanel extends JPanel
{
	private UndoStack undo;
	private ModelFormat model;
	private MainGui mainGui;
	private OpenGL3DView OpenGLDisplay;
	private int zoom = 1, zoomPower = 0;
	private int offset_x = 0, offset_y = 0;
	private int dragMode = 0;
	private Point selectionBoxMins, selectionBoxMaxs;
	//
	private boolean isDraggingVerts = false;

	public UVMapPanel(MainGui mainGui)
	{
		this.mainGui = mainGui;
		this.undo = new UndoStack();
	}

	public final void setModel(ModelFormat _model)
	{
		model = _model;
		zoom = 1;
		zoomPower = 0;

		if (model == null) //no model???
			return;

		if (!model.getHasTexture())
			return;

		Rectangle bounds = this.getBounds();

		offset_x = (int) (bounds.width * 0.5 - model.getTextureWidth() * 0.5);
		offset_y = (int) (bounds.height * 0.5 - model.getTextureHeight() * 0.5);
	}

	public void setOpenGL(OpenGL3DView _OpenGLDisplay)
	{
		OpenGLDisplay = _OpenGLDisplay;
		OpenGLDisplay.connectToUVPanel(this);
	}

	private int[] CreateRGBDataFromIndexedData(int[] indexedData)
	{
		int[] rgbData = new int[model.getTextureWidth() * model.getTextureHeight() * 3];

		/* Convert indexed 8 bits texture to RGB from the colourmap */
		for (int i = 0; i < indexedData.length; i++)
		{
			rgbData[(i * 3) + 0] = MDL.colourMap[indexedData[i]][0];
			rgbData[(i * 3) + 1] = MDL.colourMap[indexedData[i]][1];
			rgbData[(i * 3) + 2] = MDL.colourMap[indexedData[i]][2];
		}

		return rgbData;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (model == null) //no model???
			return;

		if (!model.getHasTexture())
			return;

		int[] pixels = CreateRGBDataFromIndexedData(model.getTextureData());
		int x = offset_x, y = offset_y;
		for (int i = 0; i < model.getTextureWidth() * model.getTextureHeight(); i++)
		{
			Color pixelColor = new Color(pixels[(i * 3) + 0], pixels[(i * 3) + 1], pixels[(i * 3) + 2]);
			paintBox(g, x - (int) (0.5 * zoom), y - (int) (0.5 * zoom), zoom, pixelColor, false);
			x += zoom;
			if (x >= (model.getTextureWidth() * zoom) + offset_x)
			{
				x = offset_x;
				y += zoom;
			}
		}

		boolean isSelected = false;
		int min_x, min_y, max_x, max_y;
		min_x = min_y = max_x = max_y = -1;
		int skinWidth = model.getTextureWidth();
		int skinHeight = model.getTextureHeight();
		float[][] uvData = model.getScalarUVCoordsArray();
		int[][][] triData = model.getUV_TriRelationArray();
		g.setColor(new Color(200, 200, 200)); //light gray...

		for (int triIndex = 0; triIndex < triData.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				int index = triData[triIndex][vIndex][1];

				g.setColor(new Color(200, 200, 200));

				int nextVIndex = vIndex + 1;
				if (nextVIndex >= 3)
					nextVIndex = 0;
				int nextIndex = triData[triIndex][nextVIndex][1];


				int s = (int) (uvData[index][0] * skinWidth * zoom) + offset_x;
				int t = (int) (uvData[index][1] * skinHeight * zoom) + offset_y;
				int nextS = (int) (uvData[nextIndex][0] * skinWidth * zoom) + offset_x;
				int nextT = (int) (uvData[nextIndex][1] * skinHeight * zoom) + offset_y;

				//boolean s1Offset = false, s2Offset = false;

				if (uvData[index][2] != 0 && triData[triIndex][vIndex][2] == 0) //onseam and on a backface tri
				{
					s = s + (int) (skinWidth * zoom * 0.5);
					//s1Offset = true;
				}
				if (uvData[nextIndex][2] != 0 && triData[triIndex][nextVIndex][2] == 0) //onseam and on a backface tri
				{
					nextS = nextS + (int) (skinWidth * zoom * 0.5);
					//s2Offset = true;
				}

				if (triData[triIndex][vIndex][1] != triData[triIndex][nextVIndex][1]) //do not draw connecting lines between a vertex and a 'fake' backface vertex.
					g.drawLine(s, t, nextS, nextT);

				if (mainGui.getEditingMode() == MainGui.MODE_VERTEX)
				{
					Integer[] selectedVertices = model.selectedVertices.toArray(new Integer[model.selectedVertices.size()]);
					for (int i = 0; i < selectedVertices.length; i++)
					{
						if (selectedVertices[i].intValue() == index)
						{
							if (min_x == -1)
							{
								min_x = max_x = s;
								min_y = max_y = t;
							}
							else
							{
								if (s < min_x)
									min_x = s;
								else if (s > max_x)
									max_x = s;
								if (t < min_y)
									min_y = t;
								else if (t > max_y)
									max_y = t;
							}
							isSelected = true;
							break;
						}
					}
				}

				if (uvData[index][2] != 0)
				{
					paintBox(g, s, t,
							 (int) (1 + 3 * (zoomPower + 1) * 0.5), isSelected ? ModelFormat.SELECTED_VERTEX_ONSEAM_COLOUR : ModelFormat.VERTEX_ONSEAM_COLOUR,
							 true);
				}
				else
				{
					paintBox(g, s, t,
							 (int) (1 + 3 * (zoomPower + 1) * 0.5), isSelected ? ModelFormat.SELECTED_VERTEX_COLOUR : ModelFormat.VERTEX_COLOUR,
							 true);
				}
				isSelected = false;
			}
		}

		if (mainGui.getEditingMode() == MainGui.MODE_FACE)
		{
			int[] xPoints = new int[3];
			int[] yPoints = new int[3];
			Integer[] selectedFaces = model.selectedFaces.toArray(new Integer[model.selectedFaces.size()]);
			for (int faceIndex = 0; faceIndex < selectedFaces.length; faceIndex++)
			{
				for (int vIndex = 0; vIndex < 3; vIndex++)
				{
					if (triData[selectedFaces[faceIndex]][vIndex][2] == 0 && uvData[triData[selectedFaces[faceIndex]][vIndex][1]][2] != 0)
						xPoints[vIndex] = scalarToAbsU(uvData[triData[selectedFaces[faceIndex]][vIndex][1]][0] + 0.5f);
					else
						xPoints[vIndex] = scalarToAbsU(uvData[triData[selectedFaces[faceIndex]][vIndex][1]][0]);
					yPoints[vIndex] = scalarToAbsV(uvData[triData[selectedFaces[faceIndex]][vIndex][1]][1]);
				}

			}

			g.setColor(ModelFormat.SELECTED_VERTEX_COLOUR); //draw selected faces as filled polygons
			g.fillPolygon(xPoints, yPoints, 3);
		}

		drawBox(g, min_x, min_y, max_x, max_y, ModelFormat.SELECTED_VERTEX_COLOUR); //selected items box

		if (selectionBoxMins != null)
		{
			min_x = (int) selectionBoxMins.x;
			min_y = (int) selectionBoxMins.y;
			max_x = (int) selectionBoxMaxs.x;
			max_y = (int) selectionBoxMaxs.y;

			drawBox(g, min_x, min_y, max_x, max_y, Color.WHITE); //selection box
		}

	}

	private void drawBox(Graphics g, int min_x, int min_y, int max_x, int max_y, Color clr)
	{
		g.setColor(clr);

		g.drawLine(min_x, min_y, min_x, max_y);
		g.drawLine(min_x, max_y, max_x, max_y);
		g.drawLine(max_x, max_y, max_x, min_y);
		g.drawLine(max_x, min_y, min_x, min_y);
	}

	/*
	Paints a box starting at x, y with width and height = size
	 */
	private void paintBox(Graphics g, int x, int y, int size, Color color, boolean isCentered)
	{
		g.setColor(color);

		Rectangle clipZone = g.getClipBounds();

		if (x + size < 0)
			return;
		if (y + size < 0)
			return;
		if (x > clipZone.width)
			return;
		if (y > clipZone.height)
			return;

		if (isCentered)
		{
			x = x - (int) Math.ceil(size * 0.5);
			y = y - (int) Math.ceil(size * 0.5);
		}

		for (int j = 0; j < size; j++)
			g.drawLine(x, y + j, x + size, y + j);
	}

	private int getClickSize()
	{
		return (int) (0 + 3 * (zoomPower + 1) * 0.5);
	}

	public void zoomIncrement(int zoomIncrement)
	{
		zoomPower = zoomPower + zoomIncrement;
		if (zoomPower < 0)
		{
			zoomPower = 0;
			zoomIncrement = 0;
		}
		else if (zoomPower > 8)
		{
			zoomPower = 8;
			zoomIncrement = 0;
		}
		zoom = (int) Math.pow(2, zoomPower); //zooming is clamped at powers of 2 because I suck. :(

		Rectangle clipZone = this.getBounds();

		int vec_x = (int) ((((clipZone.width * 0.5) - offset_x)) * -zoomIncrement * (zoomIncrement > 0 ? 1 : 0.5));
		int vec_y = (int) ((((clipZone.height * 0.5) - offset_y)) * -zoomIncrement * (zoomIncrement > 0 ? 1 : 0.5));

		offset_x = offset_x + (int) (vec_x);
		offset_y = offset_y + (int) (vec_y);

		this.repaint();
	}

	public void offsetIncrement(int x, int y)
	{
		Rectangle clipZone = this.getBounds();

		int clipMin = 2;

		offset_x += x;
		offset_y += y;

		if (offset_x > (clipZone.width - clipMin * zoom)) //don't let user scroll the image out of the frame...
			offset_x = (clipZone.width - clipMin * zoom);
		if (offset_y > (clipZone.height - clipMin * zoom))
			offset_y = (clipZone.height - clipMin * zoom);
		if (offset_x < (-model.getTextureWidth() + clipMin) * zoom)
			offset_x = (-model.getTextureWidth() + clipMin) * zoom;
		if (offset_y < (-model.getTextureHeight() + clipMin) * zoom)
			offset_y = (-model.getTextureHeight() + clipMin) * zoom;

		this.repaint();
	}

	private int scalarToAbsU(float u)
	{
		return (int) (u * model.getTextureWidth() * zoom) + offset_x;
	}

	private int scalarToAbsV(float v)
	{
		return (int) (v * model.getTextureHeight() * zoom) + offset_y;
	}

	private boolean isPointNearVertex(float[] coords, Point point)//, int facesFront)
	{
		int u = 0;
		u = scalarToAbsU(coords[0] + u);//, coords[2], facesFront);
		int v = scalarToAbsV(coords[1]);


		int clickSize = this.getClickSize();

		if (point.x > u - clickSize && point.x < u + clickSize)
		{
			if (point.y > v - clickSize && point.y < v + clickSize)
				return true;
		}
		return false;
	}

	private boolean isPointNearAnyVertex(Point point)
	{
		float[][] uvArray = model.getScalarUVCoordsArray();

		for (int UVIndex = 0; UVIndex < uvArray.length; UVIndex++)
		{
			if (isPointNearVertex(uvArray[UVIndex], point))
				return true;

			if (uvArray[UVIndex][2] != 0) //this vertex is onseam, so there is a 'fake' vertex 0.5*width to the right
			{
				Point fakePoint = new Point(point.x - (int) (model.getTextureWidth() * 0.5 * zoom), point.y);
				if (isPointNearVertex(uvArray[UVIndex], fakePoint)) //we need to check at this fake vertex' position.
					return true;
			}
		}
		return false;
	}

	//<editor-fold defaultstate="collapsed" desc="grow/shrink">
	public void growSelection()
	{
		if (model.selectedVertices.isEmpty())
			return; //nothing to grow.

		HashSet<Integer> newSelection = new HashSet(); //HashSet to avoid duplicates automatically


		int[][][] triArray = model.getUV_TriRelationArray();
		for (int triIndex = 0; triIndex < triArray.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				Integer index = triArray[triIndex][vIndex][1];

				if (model.selectedVertices.contains(index)) //this vertex is selected
				{
					for (int vIndex2 = 0; vIndex2 < 3; vIndex2++) //restart the vIndex counter to select ALL the verts on this tri
					{
						newSelection.add(new Integer(triArray[triIndex][vIndex2][1]));
					}
					break; //don't need to recheck any verts here, break and go to the next triangle
				}
			}
		}

		model.selectedVertices.clear();
		model.selectedVertices.addAll(newSelection);
		this.repaint();
	}

	public void shrinkSelection()
	{
		if (model.selectedVertices.isEmpty())
			return; //nothing to shrink.

		HashSet<Integer> newSelection = new HashSet(); //HashSet to avoid duplicates automatically


		int[][][] triArray = model.getUV_TriRelationArray();
		for (int triIndex = 0; triIndex < triArray.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				Integer index = triArray[triIndex][vIndex][1];

				if (model.selectedVertices.contains(index)) //this vertex is selected
				{
					boolean vertsCompletelySelected = true;
					for (int triIndex2 = 0; triIndex2 < triArray.length; triIndex2++)
					{
						for (int vIndex2 = 0; vIndex2 < 3; vIndex2++) //restart the vIndex counter to select ALL the verts on this tri
						{
							Integer index2 = triArray[triIndex2][vIndex2][1];
							if (index2.equals(index) && model.selectedVertices.contains(index2))
							{
								for (int vIndex3 = 0; vIndex3 < 3; vIndex3++) //last time... check each tri from the beginning of it's vertex list
								{
									Integer index3 = triArray[triIndex2][vIndex3][1];
									vertsCompletelySelected = vertsCompletelySelected & model.selectedVertices.contains(index3); //the minute one of these returns false, the whole thing fails.
								}
								break;
							}
						}
						if (!vertsCompletelySelected) //already failed
							break;
					}
					//if all verts on all tris that also have this vert are all selected, than keep this selected, otherwise don't add it into the new list.
					if (vertsCompletelySelected)
						newSelection.add(new Integer(index));
				}
			}
		}

		model.selectedVertices.clear();
		model.selectedVertices.addAll(newSelection);
		this.repaint();
	}
	//</editor-fold>

	public void processNewSelection(ArrayList<Integer> newSelection, boolean isCtrlDown)
	{
		if (newSelection.isEmpty()) //didn't select anything
		{
			if (!isCtrlDown) //not pressing control
				model.selectedVertices.clear(); //clear entire selection
		}
		else //selected something
		{
			if (!isCtrlDown) //not pressing control
			{
				boolean clearSelection = true; //initially we will clear the selection
				for (int i = 0; i < newSelection.size(); i++)
				{
					if (model.selectedVertices.contains(newSelection.get(i)))
					{
						clearSelection = false; //if one of the new selection is in the old selection, don't clear
						break; //no need to keep looping
					}
				}
				/*
				 * We use a boolean here because the check to know if we are
				 * clearing or not has to be an amalgamation of all the checks in the above loop.
				 */
				if (clearSelection)
				{
					model.selectedVertices.clear();
					model.selectedVertices.addAll(newSelection);
				}
			}
			else //If Ctrl is not pressed, than it's easy: we clear selected automatically and redo the selection from scratch.
			{
				Integer[] selectedVertices = model.selectedVertices.toArray(new Integer[model.selectedVertices.size()]);
				for (int i = 0; i < selectedVertices.length; i++)
				{
					if (newSelection.contains(selectedVertices[i]))
					{ //selected a previously selected one AND we're pressing control
						newSelection.remove(selectedVertices[i]); //remove from new selection so we don't add it again
						model.selectedVertices.remove(i);
					}
				}

				model.selectedVertices.addAll(newSelection); //add the new ones into the old selection
			}

		}

	}

	private void processBoxSelection(int min_x, int min_y, int max_x, int max_y, boolean isCtrlDown)
	{
		int skinWidth = model.getTextureWidth();
		int skinHeight = model.getTextureHeight();

		float[][] uvArray = model.getScalarUVCoordsArray();
		ArrayList<Integer> newSelection = new ArrayList();

		for (int UVIndex = 0; UVIndex < uvArray.length; UVIndex++)
		{
			int u = (int) (uvArray[UVIndex][0] * skinWidth * zoom) + offset_x;
			int v = (int) (uvArray[UVIndex][1] * skinHeight * zoom) + offset_y;

			if (u > min_x && u < max_x
				&& v > min_y && v < max_y)
				newSelection.add(new Integer(UVIndex));

			if (uvArray[UVIndex][2] != 0) //this vertex is onseam, so there is a 'fake' vertex 0.5*width to the right
			{
				u = u + (int) (model.getTextureWidth() * 0.5 * zoom);
				if (u > min_x && u < max_x
					&& v > min_y && v < max_y)
					newSelection.add(new Integer(UVIndex));
			}
		}

		if (newSelection.isEmpty())
			return; //didn't get anything

		if (isCtrlDown) //box selection is always additive...
		{
			for (int i = 0; i < newSelection.size(); i++)
			{
				if (model.selectedVertices.contains(newSelection.get(i)))
					newSelection.remove(i);
				else
					model.selectedVertices.add(newSelection.get(i));
			}
			//model.selectedVertices.addAll(newSelection);
		}
		else
		{
			model.selectedVertices.clear();
			model.selectedVertices.addAll(newSelection);
		}
		//processNewSelection(newSelection, isCtrlDown);
	}

	//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public boolean processClick_vertexMode(Point point, boolean isCtrlDown)
	{
		float[][] uvArray = model.getScalarUVCoordsArray();
		ArrayList<Integer> newSelection = new ArrayList();

		//System.out.println("clicked at " + point);
		for (int UVIndex = 0; UVIndex < uvArray.length; UVIndex++) //TODO: Implement proper drill down, not selecting EVERYTHING (which is dumb)
		{
			if (isPointNearVertex(uvArray[UVIndex], point))
				newSelection.add(new Integer(UVIndex));

			if (uvArray[UVIndex][2] != 0) //this vertex is onseam, so there is a 'fake' vertex 0.5*width to the right
			{
				Point fakePoint = new Point(point.x - (int) (model.getTextureWidth() * 0.5 * zoom), point.y);
				if (isPointNearVertex(uvArray[UVIndex], fakePoint)) //we need to check at this fake vertex' position.
					newSelection.add(new Integer(UVIndex));
			}
		}

		processNewSelection(newSelection, isCtrlDown);
		this.repaint();

		return !newSelection.isEmpty();
	}

	//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private boolean processClick_edgeMode(Point point, boolean ctrlDown)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private boolean pointInsideTriangle(int[][] triangle, Point point)
	{
		boolean result = true;
		float[][] uvData = model.getScalarUVCoordsArray();

		//int u = scalarToAbsU(coords[0]);
		//int v = scalarToAbsV(coords[1]);

		Point[] verts = new Point[3];

		for (int vIndex = 0; vIndex < 3; vIndex++)
		{
			if (triangle[vIndex][2] == 0 && uvData[triangle[vIndex][1]][2] != 0)
				verts[vIndex] = new Point(scalarToAbsU(uvData[triangle[vIndex][1]][0] + 0.5f),
										  scalarToAbsV(uvData[triangle[vIndex][1]][1]));
			else
				verts[vIndex] = new Point(scalarToAbsU(uvData[triangle[vIndex][1]][0]),
										  scalarToAbsV(uvData[triangle[vIndex][1]][1]));
		}

		for (int vIndex = 0; vIndex < 3; vIndex++)
		{
			boolean tempResult = true;

			int vIndexAdj = vIndex + 1;
			if (vIndexAdj >= 3)
				vIndexAdj = vIndexAdj - 3;

			int vIndexOpp = vIndex + 2;
			if (vIndexOpp >= 3)
				vIndexOpp = vIndexOpp - 3;

			if (((verts[vIndexAdj].x - verts[vIndex].x) * (point.y - verts[vIndex].y) - (verts[vIndexAdj].y - verts[vIndex].y) * (point.x - verts[vIndex].x)) > 0)
				tempResult = false;
			if (((verts[vIndexAdj].x - verts[vIndex].x) * (verts[vIndexOpp].y - verts[vIndex].y) - (verts[vIndexAdj].y - verts[vIndex].y) * (verts[vIndexOpp].x - verts[vIndex].x)) > 0)
				tempResult = !tempResult;

			result = result & tempResult;
			if (!result)
				return false;
		}

		return result;
	}

	private boolean processClick_faceMode(Point point, boolean ctrlDown)
	{
		int[][][] triData = model.getUV_TriRelationArray();

		model.selectedFaces.clear();
		for (int triIndex = 0; triIndex < triData.length; triIndex++)
		{
			if (pointInsideTriangle(triData[triIndex], point))
				model.selectedFaces.add(new Integer(triIndex));
		}

		this.repaint();
		return false;
	}

	//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	public boolean processClick(Point point, boolean isCtrlDown)
	{
		if (mainGui.getEditingMode() == MainGui.MODE_VERTEX)
			return processClick_vertexMode(point, isCtrlDown);
		else if (mainGui.getEditingMode() == MainGui.MODE_EDGE)
			return processClick_edgeMode(point, isCtrlDown);
		else
			return processClick_faceMode(point, isCtrlDown);
	}

	public void moveSelection(int x, int y)
	{
		if (model.selectedVertices.isEmpty()) //no selection!
			return;

		Integer[] selectedVertices = model.selectedVertices.toArray(new Integer[model.selectedVertices.size()]);
		for (int i = 0; i < model.selectedVertices.size(); i++)
		{
			model.moveUVCoordBy(selectedVertices[i].intValue(), x, y, zoom);
		}

		this.repaint();
	}

	public void processMouseMove(int x, int y)
	{
		if (isPointNearAnyVertex(new Point(x, y)))
			setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
		else
			setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
	}

	public void processLMBDrag(int x, int y, int lastMouseX, int lastMouseY, boolean isCtrlDown)
	{

		/*
		Add a check in to NOT TREAT AS A DRAG until the mouse moves more than X pixels from initial click point!
		 */

		if (dragMode == 0)
		{
			if (processClick(new Point(x, y), isCtrlDown)) //selected something, we'll be moving next.
			{
				dragMode = 1;

			}
			else //didn't select anything so we're making a selection box
				dragMode = 2;
		}
		else
		{
			if (dragMode == 1)
			{
				if (!isDraggingVerts)
				{
					undo.registerUndoAction(new UndoUVChange(x, y, zoom, model.selectedVertices, model));
					isDraggingVerts = true;
				}
				this.moveSelection(x - lastMouseX, y - lastMouseY);
			}
			else
			{
				if (selectionBoxMins == null) //no current selection box.
				{
					selectionBoxMins = new Point(x, y);
					//selectionBoxMaxs = new Point(0, 0);
				}
				selectionBoxMaxs = new Point(x, y);

				this.repaint();
			}
		}

	}

	public void stopDragging(int x, int y, boolean isCtrlDown)
	{
		dragMode = 0;
		if (isDraggingVerts)
		{
			if (undo.getLastUndoAction() instanceof UndoUVChange) //i hate using instanceof :\
			{
				UndoUVChange undoUV = (UndoUVChange) undo.getLastUndoAction();
				undoUV.completeUndoSettings(x, y);
			}
			isDraggingVerts = false;
		}
		if (selectionBoxMins != null)
		{
			int min_x = (int) (selectionBoxMins.x < selectionBoxMaxs.x ? selectionBoxMins.x : selectionBoxMaxs.x);
			int min_y = (int) (selectionBoxMins.y < selectionBoxMaxs.y ? selectionBoxMins.y : selectionBoxMaxs.y);
			int max_x = (int) (selectionBoxMins.x > selectionBoxMaxs.x ? selectionBoxMins.x : selectionBoxMaxs.x);
			int max_y = (int) (selectionBoxMins.y > selectionBoxMaxs.y ? selectionBoxMins.y : selectionBoxMaxs.y);

			processBoxSelection(min_x, min_y, max_x, max_y, isCtrlDown);

			selectionBoxMins = selectionBoxMaxs = null;
			this.repaint();
		}
	}

	public void undo()
	{
		System.out.println("Undo...");
		if (isDraggingVerts) //can't undo while dragging verts
			return;
		this.undo.undo();
	}
}
