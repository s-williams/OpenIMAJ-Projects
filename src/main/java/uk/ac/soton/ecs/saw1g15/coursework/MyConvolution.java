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
	
	@Override
	public void processImage(final FImage image) {		
		// get image dimensions
		int iRows = image.getRows();
		int iCols = image.getCols();
		
		// get kernel dimensions
		int tRows = kernel.length;
		int tCols = kernel[0].length;
		
		// set a temporary image to black
		FImage temp = image.clone().fill(0);
		
		int trhalf = (int) Math.floor(tRows / 2);
		int tchalf = (int) Math.floor(tCols / 2);

		// loop through all pixels of the original image
		for (int x = 1; x < iCols - 1; x++) {
			for (int y = 1; y < iRows - 1; y++) {
				
				// reset sum to zero
				float sum = 0;
				
				// loop through all points within the kernel
				for (int iWin = 1; iWin < tRows; iWin++) {
					for (int jWin = 1; jWin < tCols; jWin++) {
						try {
							// convolve the pixels
							sum += image.pixels[y + jWin - tchalf - 1][x + iWin - trhalf - 1] * kernel[jWin][iWin]; 
						} catch(ArrayIndexOutOfBoundsException e) { /* ignore */ }
					}
				}
				
				// set the new pixel in the temp image with the sum
				temp.setPixel(x, y, sum);
			}
		}
		
		// normalise temp image
		FImage convolved = temp.normalise();
		
		// return convolved image
		image.internalAssign(convolved);
	}
	
	// returns hybrid of two images
	public static MBFImage hybrid(MBFImage image1, MBFImage image2, float sigma) {		
		int size = (int) (8.0f * sigma + 1.0f); // (this implies the window is +/- 4 sigmas from the centre of the Gaussian)
		if (size % 2 == 0) size++; // size must be odd
		float[][] filter = Gaussian2D.createKernelImage(size, sigma).pixels;
		
		// low pass both images
		MyConvolution mc = new MyConvolution(filter);
		MBFImage processed1 = image1.process(mc);
		MBFImage processed2 = image2.process(mc);

		// high pass the second image
		processed2 = image2.subtract(processed2);
		
		// add the two images
		MBFImage hybrid = processed1.add(processed2);
		
		// return the image
		return hybrid;
	}
	
	// progressively down-samples an image to ease visualisation of a hybrid image
	public static MBFImage downsample(MBFImage image) {
		MBFImage newImage = new MBFImage(image.getWidth() * 2, image.getHeight());
		newImage.drawImage(image, 0, 0);
		newImage.drawImage(image.process(new ResizeProcessor(0.5f)), image.getWidth(), 0);
		newImage.drawImage(image.process(new ResizeProcessor(0.25f)), (int)(image.getWidth() * 1.5), 0);
		newImage.drawImage(image.process(new ResizeProcessor(0.125f)), (int)(image.getWidth() * 1.75), 0);
		return newImage;
	}
	
	// Displays a variety of hybrid images
	public static void main(String[] args) {
		try {
			MBFImage bicycle = ImageUtilities.readMBF(new File("data/bicycle.bmp"));
			MBFImage motorcycle = ImageUtilities.readMBF(new File("data/motorcycle.bmp"));
			DisplayUtilities.display(downsample(hybrid(bicycle, motorcycle, 6.0f)), "A bicycle or a motorcycle?");
			
			MBFImage cat = ImageUtilities.readMBF(new File("data/cat.bmp"));
			MBFImage dog = ImageUtilities.readMBF(new File("data/dog.bmp"));
			DisplayUtilities.display(downsample(hybrid(cat, dog, 8.5f)), "" + "A cat or a dog?");
			
			MBFImage fish = ImageUtilities.readMBF(new File("data/fish.bmp"));
			MBFImage submarine = ImageUtilities.readMBF(new File("data/submarine.bmp"));
			DisplayUtilities.display(downsample(hybrid(fish, submarine, 5.5f)), "A fish or a submarine?");
			
			MBFImage marilyn = ImageUtilities.readMBF(new File("data/marilyn.bmp"));
			MBFImage einstein = ImageUtilities.readMBF(new File("data/einstein.bmp"));
			DisplayUtilities.display(downsample(hybrid(marilyn, einstein, 3.5f)), "Einstein or Marilyn?");
			
			MBFImage plane = ImageUtilities.readMBF(new File("data/plane.bmp"));
			MBFImage bird = ImageUtilities.readMBF(new File("data/bird.bmp"));
			DisplayUtilities.display(downsample(hybrid(plane, bird, 5.5f)), "A bird or a plane?");
			
			MBFImage trump = ImageUtilities.readMBF(new File("data/trump.png"));
			MBFImage kim = ImageUtilities.readMBF(new File("data/kim.png"));
			for (float sigma = 0.5f; sigma < 10; sigma = sigma + 0.5f)
				DisplayUtilities.display(downsample(hybrid(trump, bird, 5.5f)), sigma + "A mad man with nuclear weapons or Kim Jong Il?");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}