package uk.ac.soton.ecs.saw1g15.ch3;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
//    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.redd.it/1bvygg20ghsz.jpg"));
//    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.redd.it/knbs8h5s43gz.jpg"));
//    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.redd.it/uwvjph19mltz.jpg"));
    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.imgur.com/4nlgwoc.jpg"));
    		System.out.println("Image input");
    		
    		// Apply a colour space transform to the image RGB - LAB
    		input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
    		System.out.println("Colour space converted");
    		
    		// Construct K Means algorithm
    		// Parameter (2) is number of clusters or classes we want to generate
    		FloatKMeans cluster = FloatKMeans.createExact(2);
    		System.out.println("K means algorithm generated");
    		
    		//Flatten pixels
    		float[][] imageData = input.getPixelVectorNative(new float[input.getWidth() * input.getHeight()][3]);
    		System.out.println("Pixels flatterned");
    		
    		FloatCentroidsResult result = cluster.cluster(imageData);
    		System.out.println("Pixels grouped");
    		
    		float[][] centroids = result.centroids;
    		for (float[] fs : centroids) {
    		    System.out.println(Arrays.toString(fs));
    		}
    		
    		HardAssigner<float[],?,?> assigner = result.defaultHardAssigner();
    		for (int y=0; y<input.getHeight(); y++) {
    		    for (int x=0; x<input.getWidth(); x++) {
    		        float[] pixel = input.getPixelNative(x, y);
    		        int centroid = assigner.assign(pixel);
    		        input.setPixelNative(x, y, centroids[centroid]);
    		    }
    		}
    		
    		input = ColourSpace.convert(input, ColourSpace.RGB);
    		DisplayUtilities.display(input);
			System.out.println("Image displayed");
			
			GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();
			List<ConnectedComponent> components = labeler.findComponents(input.flatten());
			
			int i = 0;
			for (ConnectedComponent comp : components) {
			    if (comp.calculateArea() < 50) 
			        continue;
			    input.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
			}

			DisplayUtilities.display(input);



		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
