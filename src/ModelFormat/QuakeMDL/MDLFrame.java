/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.QuakeMDL;

/**
 *
 * @author cjones
 */
public class MDLFrame
{
	public static final int SINGLE_FRAME = 0;
	public static final int GROUPED_FRAME = 1;
	//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
	private int type; // 0 = simple, !0 = group
	private MDLFrameElement[] frameElements;


	public MDLFrame(int _type, MDLFrameElement _frame)
	{
		if (type != SINGLE_FRAME)
			throw new IllegalArgumentException("Can't use this constructor for frame groups.");

		type = _type;
		frameElements = new MDLFrameElement[1]; //single frame
		frameElements[0] = _frame;
	}

	public MDLFrameElement[] getFrameElements()
	{
		return frameElements;
	}

	public int getType()
	{
		return type;
	}

	private String listFrames()
	{
		String str = "";

		for (int i = 0; i < frameElements.length; i++)
		{
			str = str + "internal index: " + i + ", " + frameElements[i] + "\n";
		}
		return str;
	}

	@Override
	public String toString()
	{
		return String.format("[Frame Object]\ntype: %d, %s", type, listFrames());
	}
}
