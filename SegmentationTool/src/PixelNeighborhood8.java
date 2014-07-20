/**
 * @author Aleksandar Cirkovic
 *
 */
public class PixelNeighborhood8 implements PixelNeighborhood {

	/**
	 * 
	 */
	public PixelNeighborhood8() {
	}
	@Override
	public int numNeighbors() {
		return 8;
	}

	/* (non-Javadoc)
	 * @see PixelNeighborhood#getNeighbor(PixelInfo, int, int, int)
	 */
	@Override
	//for simplicity, the getNeighbor method is the same for 4 and 8 pixels, works
	//for both values (and even larger ones, for experiments)
	
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
		case 0:
			x=x+n;
			break;
		case 1:
			y=y+n;
			break;
		case 2:
			x=x-n;
			break;
		case 3:
			y=y-n;
			break;
		case 4:
			x=x+n;
			y=y-n;
			break;
		case 5:
			x=x+n;
			y=y+n;
			break;
		case 6:
			x=x-n;
			y=y+n;
			break;
		case 7:
			x=x-n;
			y=y-n;
			break;
		}
		//now we can calculate the new neighbor-pixel
		neighbor=PixelInfo.createFromXY(x,y,width,height);
		return neighbor;
	}

}
