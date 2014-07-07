/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Undo;

import java.util.Stack;

/**
 *
 * @author cjones
 */
public class UndoStack
{
	private Stack<UndoAction> undoActions = new Stack();

	public UndoStack()
	{
	}
	//TODO: Look into using Undomanager instead of this homemade stuff.

	public void registerUndoAction(UndoAction undoAction)
	{
		undoActions.add(undoAction);
	}

	public UndoAction getLastUndoAction()
	{
		if (undoActions.empty())
			return null;
		return undoActions.lastElement();
	}

	public void undo()
	{
		if (this.undoActions.empty())
			return;

		System.out.println("Popping next undo action.");

		this.undoActions.pop().undo(); //pop the last element in the stack and run the undo function...
	}
}
