package uk.ac.soton.ecs.saw1g15.coursework;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {
	private float[][] kernel;

	public MyConvolution(float[][] kernel) {
		this.kernel = kernel;
	}

	@Override
	public void processImage(FImage image) {
		// convolve image with kernel and store result back in image
		//
		// hint: use FImage#internalAssign(FImage) to set the contents
		// of your temporary buffer image to the image.
	}
}