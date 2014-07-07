/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Images;

import Utility.BinaryFileReader;

/**
 *
 * @author cjones
 */
public class PCX extends Image
{
	private byte manufacturer;
	private byte version;
	private byte encoding;
	private byte bpp;
	private short xmin;
	private short ymin;
	private short xmax;
	private short ymax;
	private short hdpi;
	private short vdpi;
	private byte[] colourmap = new byte[48];
	private byte reserved;
	private byte nplanes;
	private short bytesperline;
	private short paletteinfo;
	private short hscreensize;
	private short vscreensize;
	private byte[] filler = new byte[54];
	private int pixels[];

	public PCX(String _filename)
	{
		BinaryFileReader data = new BinaryFileReader(_filename);

		//Read PCX header (128 bits)
		manufacturer = data.nextByte();
		version = data.nextByte();
		encoding = data.nextByte();
		bpp = data.nextByte();
		if (bpp != 8)
			throw new IllegalArgumentException("PCX file must be 8 bit.");
		xmin = data.nextShort();
		ymin = data.nextShort();
		xmax = data.nextShort();
		ymax = data.nextShort();
		hdpi = data.nextShort();
		vdpi = data.nextShort();
		for (int i = 0; i < colourmap.length; i++)
			colourmap[i] = data.nextByte();
		reserved = data.nextByte();
		nplanes = data.nextByte();
		bytesperline = data.nextShort();
		paletteinfo = data.nextShort();
		hscreensize = data.nextShort();
		vscreensize = data.nextShort();
		for (int i = 0; i < filler.length; i++)
			filler[i] = data.nextByte();
		//Done reading PCX header.

		/*
		This block from: http://www.koders.com/java/fid3ABE2B0300FA73FED63BC36449358CC2B17E78D4.aspx
		 */
		byte[] raw = new byte[data.getLength() - 128];
		for (int i = 0; i < raw.length; i++)
		{
			raw[i] = data.nextByte();
		}

		int imageWidth = xmax + 1;
		int imageHeight = ymax + 1;
		pixels = new int[imageWidth * imageHeight];
		int pixcount = 0;
		int rawcount = 0;
		int runLength;
		byte dataByte;
		for (int y = 0; y <= ymax; y++, pixcount += xmax + 1)
		{
			for (int x = 0; x <= xmax;)
			{
				dataByte = raw[rawcount++];

				if ((dataByte & 0xC0) == 0xC0) //this is the RLE compression
				{
					runLength = dataByte & 0x3F;
					dataByte = raw[rawcount++];
				}
				else
					runLength = 1;

				while (runLength-- > 0)
					pixels[pixcount + x++] = dataByte & 0xff;
			}

		}
		/*
		End of block from: http://www.koders.com/java/fid3ABE2B0300FA73FED63BC36449358CC2B17E78D4.aspx
		 */
	}

	public int[] getPixels()
	{
		return pixels;
	}

	public int getWidth()
	{
		return xmax + 1;
	}

	public int getHeight()
	{
		return ymax + 1;
	}
}
