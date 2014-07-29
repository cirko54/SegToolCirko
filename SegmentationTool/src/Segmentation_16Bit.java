import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.ImagePlus;

// a filter for interactive segmentation of 16 bit grayscale images
public class Segmentation_16Bit implements PlugInFilter {

	// --- public attributes for the parameter view/controller ---

	// min and max pixel value occurring in input image
	public int min_value = 0, max_value = 255 * 255;

	// current window center + width used for 8 bit display
	public int window_center, window_width;

	// region growing component for identifying connected components
	public RegionGrowing regionGrowing;

	// names and colors for the different labels assigned to pixels
	public final String[] labelNames = { "Label 1", "Label 2", "Label 3",
			"Label 4", "Label 5" };
	public final int[] labelColors = new int[] { 0x0000ff, 0x2020ff, 0x4040ff,
			0x6060ff, 0x8080ff };

	// values for neighborhood size in growing algorithm
	public final String[] neighSizeMenu = new String[] { "4 Pix.", "8 Pix." };
	public final static byte[] neighSizeValues = new byte[] { 4, 8 };

	// --- end of public attributes ---

	// a dialog provides the combined view + controller
	private SegmentationToolDialog dialog;

	// input and output image
	private ImagePlus inputImage;
	private ImagePlus outputImage;

	// current selection (which pixels are seletced, bit mask)
	private boolean[] selection;

	// per-pixel labels (up to 255 different labels per pixel)
	private byte[] labels;

	// for visualization: which color/alpha to use for selection and pixels
	// within threshold
	private final int thresholdColor = 0xff0000;
	private final double thresholdAlpha = 0.25;
	private final int selectionColor = 0x00ff00;
	private final double selectionAlpha = 0.50;

	// remember whether info dialog has already been shown
	private boolean messageAlreadyShown = false;

	// this method is called to check which types of images this plugin accepts
	@Override
	public int setup(String arg, ImagePlus imp) {

		// remember the input image
		inputImage = imp;

		// this filter only does 16 bit images, and does not change the original
		// image
		return DOES_16 + NO_CHANGES;
	}

	// this method is called when the plugin is applied through the ImageJ UI
	@Override
	public void run(ImageProcessor ip) {

		// number of pixels
		final int N = ip.getWidth() * ip.getHeight();

		// analyze the input image, find min_value and max_value, set data
		// window
		findMinMaxPixelValue();
		calculateAutoWindow();

		// initially empty selection
		selection = new boolean[N];
		for (int i = 0; i < N; i++)
			selection[i] = false;

		// segmented regions are stored in a label field
		this.labels = new byte[N];

		// init region grower
		regionGrowing = new RegionGrowing(ip.getWidth(), ip.getHeight(),
				(short[]) ip.getPixels(), labels, null);

		// init thresholds
		regionGrowing.min_threshold = this.min_value;
		regionGrowing.max_threshold = this.max_value;

		// create a new RGB output image
		calculateOutputImage();

		// show it before dialog opens (so dialog can take over mouse in that
		// window)
		outputImage.show();
		outputImage.updateAndDraw();

		// create and show the dialg window, which acts as parameter
		// controller/view
		dialog = new SegmentationToolDialog(this);
		dialog.setVisible(true);

	}

	// getters for essential image properties
	public int getImageWidth() {
		return inputImage.getWidth();
	}

	public int getImageHeight() {
		return inputImage.getHeight();
	}

	public int getNumPixels() {
		return getImageHeight() * getImageWidth();
	}

	// given min and max, automatically determine a default display window
	public void calculateAutoWindow() {
		window_width = max_value - min_value;
		window_center = min_value + window_width / 2;
	}

	// calculate the pixels of the output image from those of the input image
	private void calculateOutputImage() {

		if (inputImage == null || inputImage.getProcessor() == null) {
			if (!messageAlreadyShown) {
				new MessageDialog(null, "Segmentation Error",
						"The input image is no longer available.");
				this.messageAlreadyShown = true;
			}
			return;
		}

		if (outputImage == null || outputImage.getProcessor() == null)
			outputImage = NewImage.createRGBImage("Output Image",
					inputImage.getWidth(), inputImage.getHeight(), 1, 0);

		short[] inPixels = (short[]) inputImage.getProcessor().getPixels();
		int[] outPixels = (int[]) outputImage.getProcessor().getPixels();

		// get properties of pixel value window to be displayed
		int wmin = window_center - (window_width / 2);
		int wmax = window_center + (window_width / 2);

		// go over all pixels
		int max = inputImage.getWidth() * inputImage.getHeight();
		for (int idx = 0; idx < max; idx++) {

			// input pixel value (16 bit), mask out the sign bit
			int v = inPixels[idx] & 0xffff;

			// within thresholding range?
			boolean inRange = (v >= regionGrowing.min_threshold && v <= regionGrowing.max_threshold);

			// mapping to range [0:255]
			v = (int) ((v - wmin) / (double) (wmax - wmin) * 255.0);

			// clipping to 0...255
			if (v < 0) {
				v = 0;
			} else if (v > 255) {
				v = 255;
			}

			// convert single scalar intensity value to RGB gray value
			v = v + (v << 8) + (v << 16);

			// if threshold checkbox is activated, replace pixels within
			// threshold values with semi-transparent red
			if (regionGrowing.useThresholdRange && inRange) {
				v = interpolRGB(v, 0xff0000, 0.5);
			}

			// part of an already segmented region?
			int label = labels[idx] & 0xff;
			if (label != 0) {
				v = labelColors[label - 1]; // label starts at 0, arrays start
											// at 0
			}

			// part of current selection?
			if (selection[idx] == true) {
				v = interpolRGB(v, selectionColor, selectionAlpha);
			}

			// write result
			outPixels[idx] = v;

		}
	}

	// go through pixels of the input image and find min/max etc.
	private void findMinMaxPixelValue() {

		int total = inputImage.getWidth() * inputImage.getHeight();
		short[] pixels = (short[]) inputImage.getProcessor().getPixels();
		min_value = 255 * 255;
		max_value = 0;
		for (int i = 0; i < total; i++) {
			int value = (int) (pixels[i] & 0xffff);
			if (value < min_value)
				min_value = value;
			if (value > max_value)
				max_value = value;
		}
	}

	// this is called from the controller if anything has changed
	public void update() {
		calculateOutputImage();
		outputImage.show();
		outputImage.updateAndDraw();
	}

	// add "true" pixels in newSelection to current selection
	public void addToSelection(boolean[] newSelection) {
		final int N = getNumPixels();
		for (int i = 0; i < N; i++) {
			if ((!this.selection[i]) && (newSelection[i])) {
				this.selection[i] = newSelection[i];
			}
		}
	}

	// remove "true" pixels in newSelection from current selection
	public void removeFromSelection(boolean[] newSelection) {
		final int N = getNumPixels();
		for (int i = 0; i < N; i++) {
			if ((this.selection[i]) && (newSelection[i])) {
				this.selection[i] = false;
			}
		}
	}

	// replace current selection by newSelection
	public void replaceSelection(boolean[] newSelection) {
		this.selection = newSelection;
	}

	// clear current selection (all false)
	public void clearSelection() {
		final int N = getNumPixels();
		for (int i = 0; i < N; i++) {
			this.selection[i] = false;
		}
	}

	// erode current selection
	public void erode_selection(int kernel_size) {
		ErosionDilatation erosion1 = new ErosionDilatation(this.selection,
				kernel_size, getImageWidth(), getImageHeight(), false);
//		removeFromSelection(erosion1.getChangedSelection());
		replaceSelection(erosion1.getFinalSelection());
	}

	// dilate current selection
	public void dilate_selection(int kernel_size) {
		final boolean transferred_selection[] = this.selection;
		ErosionDilatation dilatation1 = new ErosionDilatation(transferred_selection,
				kernel_size, getImageWidth(), getImageHeight(), true);
		
//		addToSelection(dilatation1.getChangedSelection());
		replaceSelection(dilatation1.getFinalSelection());
	}

	// clear the currently selected material
	public void clearLabel(byte labelIndex) {
		final int N = getNumPixels();
		for (int i = 0; i < N; i++) {
			if ((labels[i] & 0xff) == labelIndex)
				labels[i] = 0;
		}
	}

	// save the current selection as a material
	public void saveSelectionAsLabel(byte labelIndex) {
		final int N = getNumPixels();
		clearLabel(labelIndex);
		for (int i = 0; i < N; i++) {
			if (selection[i])
				labels[i] = labelIndex;
		}
	}

	// select a material and make it the current selection
	public void selectLabel(byte labelIndex) {
		final int N = getNumPixels();
		clearSelection();
		for (int i = 0; i < N; i++) {
			if (labels[i] == labelIndex)
				selection[i] = true;
		}
	}

	/*
	 * interpolate between two RGB colors rgb1 and rgb2 interpol is a weight
	 * between 0.0 and 1.0
	 */
	static int interpolRGB(int rgb1, int rgb2, double interpol) {
		byte red = (byte) (((rgb1 & 0xff0000) >> 16) * (1.0 - interpol) + ((rgb2 & 0xff0000) >> 16)
				* interpol);
		byte green = (byte) (((rgb1 & 0x00ff00) >> 8) * (1.0 - interpol) + ((rgb2 & 0x00ff00) >> 8)
				* interpol);
		byte blue = (byte) (((rgb1 & 0x0000ff)) * (1.0 - interpol) + ((rgb2 & 0x0000ff))
				* interpol);
		return ((red << 16) & 0xff0000) + ((green << 8) & 0x00ff00)
				+ (blue & 0x0000ff);
	}

	// getter for the output image's window
	ImageWindow getOutputWindow() {
		return outputImage.getWindow();
	}

}
