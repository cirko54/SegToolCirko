
// container storing pixel integer coordinates (x,y) 
// as well as the pixel's index (idx) in the pixel array 
public class PixelInfo {

	// pixel position within image
	public int x,y;
	
	// index in pixel buffer
	public int idx;
	
	// construct directly with x,y,idx
	PixelInfo(int x, int y, int idx) {
		this.x = x; 
		this.y = y;
		this.idx = idx;	
	}
	
	// calculate idx from relative coordinates
	PixelInfo relativePos(int relX, int relY, int width, int height) {
		return createFromXY(this.x+relX,this.y+relY, width, height);
	}
	
	// construct from x,y; calculate idx
	static PixelInfo createFromXY(int x, int y, int width, int height) {
		return new PixelInfo(x, y, x+y*width);
	}
	
	// construct from idx; calculate x and y
	static PixelInfo createFromIdx(int idx, int width, int height) {
		return new PixelInfo(idx % width, idx/width, idx);
	}
	
	// query whether this is a valid pixel index
	public boolean isValidIndex(int width, int height) {
		return x>=0 && x<width && y>=0 && y<height; 
	}	
}
//	// query number of neighbors in currently selected neighborhood
//	public int numNeighbors() { 
//		return 0; 
//	} 
//
//	// find the i'th neighbor (i must be < numNeighbors())
//	// result may be an invalid pixel index!
//	public PixelInfo getNeighbor(int i, int width, int height) {
//		return null; 
//	}
//
//
//}
