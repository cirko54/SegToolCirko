
public interface PixelNeighborhood {

	// query number of neighbors in currently selected neighborhood
	public int numNeighbors(); 
	
	
	// find the i'th neighbor (0 <= i < numNeighbors())
	// result may be an invalid pixel index!
	public PixelInfo getNeighbor(PixelInfo pixel, int i, int width, int height);
	
}
