package uk.ac.soton.ecs.saw1g15.ch1;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	// Create an image
        MBFImage image = new MBFImage(500,140, ColourSpace.RGB);

        // Fill the image with white
        image.fill(RGBColour.WHITE);
        		        
        // Render some text into the image
        // 1.2.1 Rendering different text in a different font and colour
        image.drawText("Timeo Danaos et", 10, 60, HersheyFont.TIMES_MEDIUM, 50, RGBColour.DARK_GRAY);
        image.drawText("dona ferentes", 10, 120, HersheyFont.TIMES_MEDIUM, 50, RGBColour.DARK_GRAY);

        // Apply a Gaussian blur
        image.processInplace(new FGaussianConvolve(2f));
        
        // Display the image
        DisplayUtilities.display(image);
    }
}
