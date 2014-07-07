/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Undo;

import ModelFormat.ModelFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author cjones
 */
public class UndoUVChange extends UndoAction
{
	private ModelFormat model;
	private int x, y;
	private int zoom;
	private ArrayList<Integer> affectedVertices;

	public UndoUVChange(int x, int y, int zoom, Collection<Integer> verts, ModelFormat model)
	{
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.affectedVertices = new ArrayList(verts); //make sure to COPY the arraylist
		this.model = model;
	}

	@Override
	public void undo()
	{
		System.out.println("Doing undo; delta x: " + x + " y: " + y + ", affected verts: " + affectedVertices);
		for (int i = 0; i < affectedVertices.size(); i++)
		{
			model.moveUVCoordBy(affectedVertices.get(i), -x, -y, zoom);
		}
	}

	public void completeUndoSettings(int x, int y)
	{
		this.x = x - this.x;
		this.y = y - this.y;
	}

}
