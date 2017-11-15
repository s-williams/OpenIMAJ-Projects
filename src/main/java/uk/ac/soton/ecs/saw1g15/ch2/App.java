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
    		JFrame jframe = new JFrame("Chapter 2");
    		
			MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));
			
			System.out.println(image.colourSpace);
			
			DisplayUtilities.display(image, jframe);
			
			DisplayUtilities.display(image.getBand(0), jframe);
			
			MBFImage clone = image.clone();
			for (int y=0; y<image.getHeight(); y++) {
			    for(int x=0; x<image.getWidth(); x++) {
			        clone.getBand(1).pixels[y][x] = 0;
			        clone.getBand(2).pixels[y][x] = 0;
			    }
			}
			DisplayUtilities.display(clone, jframe);
			
			image.processInplace(new CannyEdgeDetector());

			DisplayUtilities.display(image, jframe);
			
			image.drawShapeFilled(new Ellipse(700f, 450f, 22f, 12f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
			
			image.drawShapeFilled(new Ellipse(650f, 425f, 27f, 14f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
			
			image.drawShapeFilled(new Ellipse(600f, 380f, 32f, 17f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
			
			image.drawShapeFilled(new Ellipse(500f, 300f, 102f, 72f, 0f), RGBColour.RED);
			image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
			
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

//    	//Create an image
//        MBFImage image = new MBFImage(500,140, ColourSpace.RGB);
//
//        //Fill the image with white
//        image.fill(RGBColour.WHITE);
//        		        
//        //Render some test into the image
//        image.drawText("Timeo Danaos et", 10, 60, HersheyFont.TIMES_MEDIUM, 50, RGBColour.DARK_GRAY);
//        image.drawText("dona ferentes", 10, 120, HersheyFont.TIMES_MEDIUM, 50, RGBColour.DARK_GRAY);
//
//
//        //Apply a Gaussian blur
//        image.processInplace(new FGaussianConvolve(2f));
//        
//        //Display the image
//        DisplayUtilities.display(image);
    }
}
