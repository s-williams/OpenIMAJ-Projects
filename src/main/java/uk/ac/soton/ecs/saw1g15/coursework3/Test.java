package uk.ac.soton.ecs.saw1g15.coursework3;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

public class Test {
	public static void main(String[] args) {
		try {
			FImage image = ImageUtilities.readF(new File("data/kim.png"));
			
			DisplayUtilities.display(image, "before");
			image = crop(image);
			DisplayUtilities.display(image, "after");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static FImage crop(FImage image) {
		int y = 0;
		int x = 0;
		int index = Math.min(image.getHeight()-1, image.getWidth()-1);
		int y_max = index;
		int x_max = index;
		if (image.getHeight() > image.getWidth()) {
			y = (int) (0.5 * ((image.getHeight()-1) - (image.getWidth()-1)));
			y_max = y_max + y;
		} else  {
			x = (int) (0.5 * (image.getWidth() - image.getHeight()));
			x_max = x_max + x;
		}
		
		float[][] pixels = new float[index][index];
		int newx = 0;
		int newy = 0;
		for (int i = y; i < y_max; i++) {
			for (int j = x; j < x_max; j++) {
				pixels[newy][newx] = image.getPixelNative(j, i);
				newx++;
			}
			newx = 0;
			newy++;
		}
		FImage cropped = new FImage(pixels);
		return cropped;
	}
}
