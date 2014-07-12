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
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see PixelNeighborhood#getNeighbor(PixelInfo, int, int, int)
	 */
	@Override
	public PixelInfo[] getNeighbor(PixelInfo pixel, int i, int width, int height) {
		PixelInfo[] neighbors;
		//calculate the X and Y coordinates from the given index
		int x = i%width;
		int y = (i-x)/width;
		int index=0;
		for (int ii=-1;ii<=1;ii=ii+2) {
			for (int jj=-1; jj<=1; jj=jj+2) {
				neighbors[index] = jj*width+ii;
				index++;
			}
		}
		return neighbors;
	}

}
