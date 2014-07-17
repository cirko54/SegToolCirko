/**
 * 
 */

/**
 * @author Aleksandar Cirkovic
 *
 */
public class PixelNeighborhood4 implements PixelNeighborhood {

	/**
	 * 
	 */
	public PixelNeighborhood4() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see PixelNeighborhood#numNeighbors()
	 */
	@Override
	public int numNeighbors() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see PixelNeighborhood#getNeighbor(PixelInfo, int, int, int)
	 */
	@Override
	public PixelInfo getNeighbor(PixelInfo pixel, int i, int width, int height) {
		PixelInfo neighbor = new PixelInfo(0,0,0);
		//in case that i>8:
		//1 cycle = 8 pixels around the given pixel
		//i'th neighbor = number of cycles (n) + rest
		//still works best for i<=8 :-)
		int rest = i%8;
		int n=(i/8)+1;
		int x = pixel.x;
		int y = pixel.y;
		switch (rest) {
		case 1:
			x=x+n;
			break;
		case 2:
			y=y+n;
			break;
		case 3:
			x=x-n;
			break;
		case 4:
			y=y-n;
			break;
		case 5:
			x=x+n;
			y=y-n;
			break;
		case 6:
			x=x+n;
			y=y+n;
			break;
		case 7:
			x=x-n;
			y=y+n;
			break;
		case 8:
			x=x-n;
			y=y-n;
			break;
		}
		//now we can calculate the new neighbor-pixel
		neighbor=PixelInfo.createFromXY(x,y,width,height);
		return neighbor;
	}

}
