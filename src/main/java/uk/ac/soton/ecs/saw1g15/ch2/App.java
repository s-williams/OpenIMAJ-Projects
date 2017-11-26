package uk.ac.soton.ecs.saw1g15.ch2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * Processing my first image!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		// 2.1.1 Using a named display to ensure only one window is opened throughout the program
    		JFrame jframe = new JFrame("Chapter 2");
    		
    		// Reading an image from a URL
			MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));
			
			System.out.println(image.colourSpace);
			
			// Displaying the image and the red channel of the image alone
			DisplayUtilities.display(image, jframe);
			DisplayUtilities.display(image.getBand(0), jframe);
			
			// Setting all the blue and green channels of pixels to black, thus making a red version of the image
			MBFImage clone = image.clone();
			for (int y=0; y<image.getHeight(); y++) {
			    for(int x=0; x<image.getWidth(); x++) {
			        clone.getBand(1).pixels[y][x] = 0;
			        clone.getBand(2).pixels[y][x] = 0;
			    }
			}
			DisplayUtilities.display(clone, jframe);
			
			// Above can also just be like so:
			// clone.getBand(1).fill(0f);
			// clone.getBand(2).fill(0f);

			// Running Canny Edge Detector alogorithm
			image.processInplace(new CannyEdgeDetector());

			DisplayUtilities.display(image, jframe);
			
			// Drawing speech bubbles
			// 2.1.2 Speech bubble border is done by drawing a slightly larger red ellipse before drawing the white ellipse in 
			// the same location. This creates the perception of a red border.
			image.drawShapeFilled(new Ellipse(700f, 450f, 22f, 12f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
			
			image.drawShapeFilled(new Ellipse(650f, 425f, 27f, 14f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
			
			image.drawShapeFilled(new Ellipse(600f, 380f, 32f, 17f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
			
			image.drawShapeFilled(new Ellipse(500f, 300f, 102f, 72f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
			
			// Filling speech bubble with text
			image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
			image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
			
			DisplayUtilities.display(image, jframe);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
