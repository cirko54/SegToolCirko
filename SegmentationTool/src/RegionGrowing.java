import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

// algorithm that starts from a given pixel and 
// finds all connected pixels that match some inclusion criteria
public class RegionGrowing {

	// inclusion criterium 1: threshold range
	public boolean useThresholdRange = true;
	public int min_threshold, max_threshold;

	// inclusion criterium 2: IGNORE, NOT USED
	public boolean useGradient = false;
	public int gradientThreshold;

	// inclusion criterium 3: already belonging to other segmented region?
	public boolean stopAtOtherSegmentedRegions;

	// all required info about the image to be processed
	private short[] pixels;
	private byte[] labels;
	int width, height;

	// constructor requires image dimensions as well as arrays for pixel values
	// and labels.
	// gradients are currently not used.
	RegionGrowing(int width, int height, short[] pixels, byte[] labels,
			short[] gradients) {

		this.width = width;
		this.height = height;

		this.pixels = pixels;
		this.labels = labels;

		this.min_threshold = 0;
		this.max_threshold = 255 * 255;

		this.useThresholdRange = true;
		this.stopAtOtherSegmentedRegions = true;

		// NOT USED
		this.gradientThreshold = 40;
		this.useGradient = false;

	}

	// start growing algorithm at specified pixel location.
	// all pixels belonging to the connected region will be set to "true" in the
	// selection
	// returns number of pixels in the identified region
	public int grow(PixelInfo startPixel, boolean[] selection) {

		// how many pixels are contained in the image?
		final int N = this.width * this.height;

		// queue to represent "active front" of pixels to be processed
		Queue<PixelInfo> activePixels = new LinkedList<PixelInfo>(); // active
																		// front

		// binary image to mark which pixels have already been visited
		boolean[] visited = new boolean[N];
		for (int i = 0; i < N; i++)
			visited[i] = false;

		// start the algorithm with just the start pixel in the active front
		activePixels.offer(startPixel);

		// count how many have been selected
		int countSelected = 0;

		// TODO
		// dummy implementation - replace it by region growing algorithm!
//		for (int jj = -10; jj <= 10; jj++) {
//			for (int ii = -10; ii <= 10; ii++) {
//				PixelInfo pixel = startPixel.relativePos(ii, jj, width, height);
//				// check whether pixel should belong to region
//				if (shouldPixelBeIncluded(pixel)) {
//					// ok,so add pixel to S and add neighbors to A
//					selection[pixel.idx] = true;
//					countSelected++;
//				}
//			}
//		}
		
		PixelNeighborhood neighborhood = new PixelNeighborhood4();
		PixelInfo pixel = startPixel;
		for (int i=0;i<neighborhood.numNeighbors();i++) {
			PixelInfo n = neighborhood.getNeighbor(pixel, i, width, height);
			if ((n.isValidIndex(width,height) && (shouldPixelBeIncluded(n))) {
				selection[n.idx] = true;
				countSelected++;
				activePixels.offer(n);
				grow(n, selection);
			}
		}
		// how many have been selected?
		return countSelected;

	}

	// decide whether a pixel should be included or not
	public boolean shouldPixelBeIncluded(PixelInfo pixel) {
		int color = pixels[pixel.idx];

		//check for criteria: ThresholdRange activated, color within range; return
		//value as inclusion decision
		boolean colorWithinThresholdRange = (color >= min_threshold && color <= max_threshold);
		return (this.useThresholdRange && colorWithinThresholdRange);
	}

}
