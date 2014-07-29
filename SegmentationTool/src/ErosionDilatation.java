/**
 * @author Aleksandar Cirkovic
 * 
 */
public class ErosionDilatation {
	private boolean[] new_selection;
	private boolean[] differential_selection;
	private boolean[] current_selection;

	ErosionDilatation(final boolean current_selection[], int kernel_size,
			int width, int height, boolean dilate) {

		// two different selection outputs will be generated, see below at
		// methods: one "final" and one with just the changed pixels
		this.new_selection = new boolean[current_selection.length];
		for (int i = 0; i < current_selection.length; i++) {
			new_selection[i] = current_selection[i];
		}
		this.current_selection = current_selection;
		this.differential_selection = new boolean[new_selection.length]; // all false
																	// by
																	// default
		final int relXY = kernel_size / 2;

		for (int i = 0; i < current_selection.length; i++) {
			if (this.current_selection[i]) {
				PixelInfo original = PixelInfo.createFromIdx(i, width, height);
				for (int jj = -relXY; jj < relXY + 1; jj++) {
					for (int kk = -relXY; kk < relXY + 1; kk++) {
						PixelInfo criterium = original.relativePos(jj, kk,
								width, height);

						// can't combine the two conditions because of possible
						// array index exceptions => first check validity
						if (criterium.isValidIndex(width, height)) {
							if (!dilate) {
								// erosion
								if (!this.current_selection[criterium.idx]) {
									this.new_selection[i] = false;
									this.differential_selection[i] = true;
								}
							} else {
								// dilatation
								if (!this.current_selection[criterium.idx]) {
									this.new_selection[criterium.idx] = true;
									this.differential_selection[criterium.idx] = true;
								}
							}
						}
					}
				}
			}
		}
	}

	// for testing purposes, one selection that is already the "final" one, and
	// one that
	// marks the pixels to be added or deleted, can be returned
	public boolean[] getFinalSelection() {
		return new_selection;
	}

	public boolean[] getChangedSelection() {
		return differential_selection;
	}
}
