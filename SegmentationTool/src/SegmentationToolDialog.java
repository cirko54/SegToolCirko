import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import ij.*;
import ij.gui.*;

// dialog that acts as a view + controller for the parameters of Segmentation_16Bit 
public class SegmentationToolDialog extends JDialog implements ChangeListener,
		MouseListener, ActionListener, MouseMotionListener {

	private static final long serialVersionUID = 1555993295227439414L;

	// the model for this controller
	final Segmentation_16Bit model;

	// dialog UI elements
	private JLabel min_label, max_label;
	private JSlider window_center_slider, window_width_slider;
	private JTextField window_center_txt, window_width_txt;
	private JSlider min_threshold_slider, max_threshold_slider;
	private JTextField min_threshold_txt, max_threshold_txt;
	private JCheckBox chk_useThresholds, chk_excludeSegmentedRegions;
	private JButton btn_erode3, btn_dilate3, btn_erode7, btn_dilate7,
			btn_clear;
	private JButton btn_saveLabel, btn_selectLabel, btn_clearLabel;
	private JComboBox combo_label;
	private JComboBox neighborhood_size;

	// the mouse listener needs to know the canvas within the output window
	ImagePlus img;
	ImageCanvas canvas;

	// constructor puts together the elements of the dialog
	public SegmentationToolDialog(final Segmentation_16Bit model) {

		// init dialog window
		super(IJ.getInstance(), "Segmentation Tool");

		// remember the model
		this.model = model;

		// dialog containing UI elements to control plugin
		setSize(400, 800);
		Container panel = getContentPane();

		// layout: multiple horizontal boxes nexted in one vertical box
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// add groups of UI elements
		panel.add(makeGroup_DisplayProperties());
		panel.add(makeGroup_Thresholding());
		panel.add(makeGroup_neighSizeSel());
		panel.add(makeGroup_Selection());
		panel.add(makeGroup_Labels());

		// move all components up, keep free space at bottom (if at all)
		panel.add(Box.createVerticalGlue());
		pack();

		// track mouse action
		IJ.register(SegmentationToolDialog.class);
		canvas = model.getOutputWindow().getCanvas();
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);

	}

	// group: display properties
	// ----------------------------------------------------
	private JPanel makeGroup_DisplayProperties() {

		// labels to show histogram statistics
		min_label = new JLabel("" + model.min_value);
		max_label = new JLabel("" + model.max_value);

		// slider with text to define the center of the pixel value image
		window_center_slider = new JSlider(JSlider.HORIZONTAL, model.min_value,
				model.max_value, model.window_center);
		window_center_slider
				.setMajorTickSpacing((model.max_value - model.min_value) / 10);
		window_center_slider.setPaintTicks(true);
		window_center_slider.setPaintLabels(false);
		window_center_slider.addChangeListener(this);
		window_center_txt = new JTextField(5);

		// slider with text to define the width of the pixel value image
		window_width_slider = new JSlider(JSlider.HORIZONTAL, 0,
				(model.max_value - model.min_value) * 2, model.window_width);
		window_width_slider
				.setMajorTickSpacing((model.max_value - model.min_value) * 2 / 10);
		window_width_slider.setPaintTicks(true);
		window_width_slider.setPaintLabels(false);
		window_width_txt = new JTextField(5);

		// layout in multiple rows
		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		group.setBorder(BorderFactory.createTitledBorder("Display"));

		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel("Fenstermitte:"));
		addSliderWithTextField(row, window_center_slider, window_center_txt,
				this);
		group.add(row);

		row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel("Fensterbreite:"));
		addSliderWithTextField(row, window_width_slider, window_width_txt, this);
		group.add(row);

		row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		JButton autoWindow = new JButton("Auto-Window");
		row.add(autoWindow);
		autoWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				model.calculateAutoWindow();
				model.update();
				updateViewController();
			}
		});
		row.add(autoWindow);
		row.add(Box.createHorizontalGlue());
		group.add(row);
		return group;
	}

	// group: thresholding ----------------------------------------------------
	private JPanel makeGroup_Thresholding() {

		// slider with text to define lower threshold
		min_threshold_slider = new JSlider(JSlider.HORIZONTAL, model.min_value,
				model.max_value, model.regionGrowing.min_threshold);
		min_threshold_slider
				.setMajorTickSpacing((model.max_value - model.min_value) / 10);
		min_threshold_slider.setPaintTicks(true);
		min_threshold_slider.setPaintLabels(false);
		min_threshold_slider.addChangeListener(this);
		min_threshold_txt = new JTextField(5);

		// slider with text to define upper threshold
		max_threshold_slider = new JSlider(JSlider.HORIZONTAL, model.min_value,
				model.max_value, model.regionGrowing.max_threshold);
		max_threshold_slider
				.setMajorTickSpacing((model.max_value - model.min_value) / 10);
		max_threshold_slider.setPaintTicks(true);
		max_threshold_slider.setPaintLabels(false);
		max_threshold_slider.addChangeListener(this);
		max_threshold_txt = new JTextField(5);
		chk_useThresholds = new JCheckBox("pixel value thresholds:",
				model.regionGrowing.useThresholdRange);
		chk_useThresholds.addChangeListener(this);

		// layout in multiple rows
		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		group.setBorder(BorderFactory.createTitledBorder("Thresholding"));

		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel("Pixel values: min=" + model.min_value + " max="
				+ model.max_value));
		row.add(Box.createHorizontalGlue());
		group.add(row);

		row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(this.chk_useThresholds);
		row.add(Box.createHorizontalGlue());
		group.add(row);

		row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel("Min: "));
		addSliderWithTextField(row, min_threshold_slider, min_threshold_txt,
				this);
		group.add(row);

		row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel("Max: "));
		addSliderWithTextField(row, max_threshold_slider, max_threshold_txt,
				this);
		group.add(row);

		return group;
	}

	// group: manipulate selection
	// ----------------------------------------------------
	private JPanel makeGroup_Selection() {

		// buttons changing the selection
		btn_clear = new JButton("Clear");
		btn_clear.addActionListener(this);
		btn_erode3 = new JButton("Erode 3x3");
		btn_erode3.addActionListener(this);
		btn_erode7 = new JButton("Erode 7x7");
		btn_erode7.addActionListener(this);
		btn_dilate3 = new JButton("Dilate 3x3");
		btn_dilate3.addActionListener(this);
		btn_dilate7 = new JButton("Dilate 7x7");
		btn_dilate7.addActionListener(this);

		// layout in one row for now
		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		group.setBorder(BorderFactory.createTitledBorder("Selected Pixels"));
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(btn_erode3);
		row.add(btn_erode7);
		row.add(btn_dilate3);
		row.add(btn_dilate7);
		row.add(btn_clear);
		row.add(Box.createHorizontalGlue());
		group.add(row);

		return group;
	}

	// group: neighborhood size
	// select the neighborhood growing algorithm
	private JPanel makeGroup_neighSizeSel() {
		neighborhood_size = new JComboBox(model.neighSizeMenu);
		neighborhood_size.addActionListener(this);

		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		group.setBorder(BorderFactory.createTitledBorder("Neighborhood Size"));

		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(neighborhood_size);
		row.add(Box.createHorizontalGlue());
		group.add(row);
		
		return group;
	}

	// group: labels ----------------------------------------------------
	private JPanel makeGroup_Labels() {

		// buttons to save/select labels
		btn_saveLabel = new JButton("save");
		btn_saveLabel.addActionListener(this);
		btn_selectLabel = new JButton("select");
		btn_selectLabel.addActionListener(this);
		btn_clearLabel = new JButton("clear");
		btn_clearLabel.addActionListener(this);
		combo_label = new JComboBox(model.labelNames);
		combo_label.addActionListener(this);
		chk_excludeSegmentedRegions = new JCheckBox(
				"exclude already labeled regions");

		// layout in multiple rows
		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		group.setBorder(BorderFactory.createTitledBorder("Labels"));

		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel("Label: "));
		row.add(combo_label);
		row.add(btn_saveLabel);
		row.add(btn_selectLabel);
		row.add(btn_clearLabel);
		row.add(Box.createHorizontalGlue());
		group.add(row);

		row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(chk_excludeSegmentedRegions);
		row.add(Box.createHorizontalGlue());
		group.add(row);

		return group;
	}

	// update values in UI components according to model state
	public void updateViewController() {

		min_label.setText("" + model.min_value);
		max_label.setText("" + model.max_value);

		window_center_slider.setValue(model.window_center);
		window_center_slider.setMinimum(model.min_value);
		window_center_slider.setMaximum(model.max_value);
		window_center_slider
				.setMajorTickSpacing((model.max_value - model.min_value) / 10);

		window_width_slider.setValue(model.window_width);
		window_width_slider.setMinimum(0);
		window_width_slider.setMaximum((model.max_value - model.min_value) * 2);
		window_width_slider
				.setMajorTickSpacing((model.max_value - model.min_value) * 2 / 10);

		chk_useThresholds.setSelected(model.regionGrowing.useThresholdRange);
		chk_excludeSegmentedRegions
				.setSelected(model.regionGrowing.stopAtOtherSegmentedRegions);

	}

	// called whenever one of the UI elements changes...
	@Override
	public void stateChanged(ChangeEvent e) {
		updateModel();
	}

	// update values in model, and refresh display of the model
	public void updateModel() {

		model.window_center = window_center_slider.getValue();
		model.window_width = window_width_slider.getValue();

		model.regionGrowing.useThresholdRange = this.chk_useThresholds
				.isSelected();
		model.regionGrowing.min_threshold = min_threshold_slider.getValue();
		model.regionGrowing.max_threshold = max_threshold_slider.getValue();

		model.regionGrowing.stopAtOtherSegmentedRegions = this.chk_excludeSegmentedRegions
				.isSelected();

		model.update();

	}

	// react to button presses
	@Override
	public void actionPerformed(ActionEvent ev) {

		// which label is currently selected?
		byte labelIndex = (byte) (combo_label.getSelectedIndex() + 1);

		if (ev.getSource() == this.btn_clear) {
			model.clearSelection();
		} else if (ev.getSource() == this.btn_erode3) {
			model.erode_selection(3);
		} else if (ev.getSource() == this.btn_erode7) {
			model.erode_selection(7);
		} else if (ev.getSource() == this.btn_dilate3) {
			model.dilate_selection(3);
		} else if (ev.getSource() == this.btn_dilate7) {
			model.dilate_selection(7);
		} else if (ev.getSource() == this.btn_clearLabel) {
			model.clearLabel(labelIndex);
		} else if (ev.getSource() == this.btn_saveLabel) {
			model.saveSelectionAsLabel(labelIndex);
			model.clearSelection();
		} else if (ev.getSource() == this.btn_selectLabel) {
			model.selectLabel(labelIndex);
		}

		// get the selected neighborhood size for the model
		byte neighSizeIndex = (byte) (neighborhood_size.getSelectedIndex());
		model.regionGrowing.neighSize = Segmentation_16Bit.neighSizeValues[neighSizeIndex];

		updateModel();
	}

	// react to mouse actions
	public void mouseClicked(MouseEvent e) {

		// get pixel index
		int x = canvas.offScreenX(e.getX());
		int y = canvas.offScreenY(e.getY());
		PixelInfo startPixel = PixelInfo.createFromXY(x, y,
				model.getImageWidth(), model.getImageHeight());

		// create an empty temprary selection
		boolean[] tmp_select = new boolean[model.getNumPixels()];

		// start region growing
		int q = this.model.regionGrowing.grow(startPixel, tmp_select);
		System.out.println("found a connected region of " + q + " pixels");

		// add, replace, subtract?
		if ((e.getModifiers() & Event.SHIFT_MASK) != 0) {
			model.addToSelection(tmp_select);
		} else if ((e.getModifiers() & Event.ALT_MASK) != 0) {
			model.removeFromSelection(tmp_select);
		} else {
			// no modifier key: replace model's selection by this one
			model.replaceSelection(tmp_select);
		}

		updateModel();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	// add the provided slider, add a text field, and sync the two. also
	// make sure that the text field is connected to the change listener
	private void addSliderWithTextField(JPanel panel, final JSlider slider,
			final JTextField text, ChangeListener listener) {
		// add slider and text field to this panel
		panel.add(slider);
		panel.add(text);

		// set text field content to that of the slider
		text.setText("" + slider.getValue());

		// make sure the listener is registered
		slider.addChangeListener(listener);

		// update text if slider was changed
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				text.setText("" + slider.getValue());
			}
		});

		// update slider if text was changed
		// http://stackoverflow.com/questions/1548606/java-link-jslider-and-jtextfield-for-float-value
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent ke) {
				String typed = text.getText();
				if (!typed.matches("\\d+")) {
					text.setText("" + slider.getValue());
				}
				int value = Integer.parseInt(typed);
				if (value > slider.getMaximum())
					value = slider.getMaximum();
				if (value < slider.getMinimum())
					value = slider.getMinimum();
				slider.setValue(value);
			}
		});
	}

}
