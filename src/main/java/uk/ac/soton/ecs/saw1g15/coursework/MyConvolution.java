package uk.ac.soton.ecs.saw1g15.coursework;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {
	private float[][] kernel;

	public MyConvolution(float[][] kernel) {
		this.kernel = kernel;
	}

	// Convolve image with kernel and store result back in image
	@Override
	public void processImage(FImage image) {
		// get image dimensions
		int iRows = image.getRows();
		int iCols = image.getCols();
		
		// get kernel dimensions
		int tRows = kernel.length;
		int tCols = kernel[0].length;
		
		// set a temporary image to black
		FImage temp = image.clone().fill(0);
		
		// half kernal rows/columns
		int trhalf = (int) Math.floor(tRows / 2);
		int tchalf = (int) Math.floor(tCols / 2);
		
		// convolve
		for (int x = trhalf + 1; x < iCols - trhalf; x++) {
			for (int y = tchalf + 1; y < iRows - tchalf; y++) {
				
				// reset sum to zero
				float sum = 0;
				
				for (int iWin = 1; iWin < tRows; iWin++) {
					for (int jWin = 1; jWin < tCols; jWin++) {
						try {
							sum = sum + image.getPixel(y + jWin - 1, x + iWin  - 1) * kernel[jWin][iWin];
						} catch (ArrayIndexOutOfBoundsException e) {}
					}
				}
				temp.setPixel(y, x, sum);
			}
		}
		
		// normalise temp image
		FImage convolved = temp.normalise();
		
		// return convolved image
		image.internalAssign(convolved);
	}
	
	public static void hybrid(MBFImage image1, MBFImage image2) {
		float sigma = 2.0f;
		
		int size = (int) (8.0f * sigma + 1.0f); // (this implies the window is +/- 4 sigmas from the centre of the Gaussian)
		if (size % 2 == 0) size++; // size must be odd
		float[][] filter = Gaussian2D.createKernelImage(size, sigma).pixels;
		
		// low pass both images
		MyConvolution mc = new MyConvolution(filter);
		MBFImage processed1 = image1.process(mc);
		MBFImage processed2 = image2.process(mc);
		
		DisplayUtilities.display(processed1);
		DisplayUtilities.display(processed2);

		// high pass
		processed2 = image2.subtract(processed2);
		
		// add the two images
		MBFImage hybrid = processed1.add(processed2);
		
		// display the image
		DisplayUtilities.display(hybrid);
	}
	
	public static void main(String[] args) {
		try {
			MBFImage bicycle = ImageUtilities.readMBF(new File("data/bicycle.bmp"));
			MBFImage motorcycle = ImageUtilities.readMBF(new File("data/motorcycle.bmp"));
			hybrid(bicycle, motorcycle);
			
			MBFImage cat = ImageUtilities.readMBF(new File("data/cat.bmp"));
			MBFImage dog = ImageUtilities.readMBF(new File("data/dog.bmp"));
			
			MBFImage fish = ImageUtilities.readMBF(new File("data/fish.bmp"));
			MBFImage submarine = ImageUtilities.readMBF(new File("data/dog.bmp"));
			
			MBFImage marilyn = ImageUtilities.readMBF(new File("data/marilyn.bmp"));
			MBFImage einstein = ImageUtilities.readMBF(new File("data/einstein.bmp"));
			
			MBFImage plane = ImageUtilities.readMBF(new File("data/plane.bmp"));
			MBFImage bird = ImageUtilities.readMBF(new File("data/bird.bmp"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}