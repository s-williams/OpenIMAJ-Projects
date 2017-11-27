package uk.ac.soton.ecs.saw1g15.ch3;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang3.ArrayUtils;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

/**
 * Clustering, segmentation and connected components!
 *
 */
public class App {
	
	public static HardAssigner<float[],?,?> assigner;
	public static float[][] centroids;
	
    public static void main( String[] args ) {
    	try {
    		// Selection of different inputs
//    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.redd.it/1bvygg20ghsz.jpg"));
//    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.redd.it/knbs8h5s43gz.jpg"));
    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.redd.it/uwvjph19mltz.jpg"));
//    		MBFImage input = ImageUtilities.readMBF(new URL("https://i.imgur.com/4nlgwoc.jpg"));
    		MBFImage cloned = input.clone();
    		System.out.println("Image input");
    		
    		// Apply a colour space transform to the image RGB - LAB
    		input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
    		System.out.println("Colour space converted");
    		
    		// Construct K Means algorithm
    		// Parameter (2) is number of clusters or classes we want to generate
    		FloatKMeans cluster = FloatKMeans.createExact(2);
    		System.out.println("K means algorithm generated");
    		
    		// Flatten pixels
    		float[][] imageData = input.getPixelVectorNative(new float[input.getWidth() * input.getHeight()][3]);
    		System.out.println("Pixels flatterned");
    		
    		FloatCentroidsResult result = cluster.cluster(imageData);
    		System.out.println("Pixels grouped");
    		
    		centroids = result.centroids;
    		for (float[] fs : centroids) {
    		    System.out.println(Arrays.toString(fs));
    		}
    		
    		assigner = result.defaultHardAssigner();
    		
    		// 3.1.1 Loop over image pixels with pixel processor
    		input.processInplace(new PixelProcessor<Float[]>() {
    		    public Float[] processPixel(Float[] pixel) {
    		    	// Uses ArrayUtils to convert the Float[] pixel into a float[] which can then be used by the centroid
    		    	float[] lower = ArrayUtils.toPrimitive(pixel);
    		    	int centroid = assigner.assign(lower);
    		    	lower = centroids[centroid];
    		    	pixel = ArrayUtils.toObject(lower);
    		    	return pixel;
    		    }
    		});
    		
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
			
			// 3.1.2 Running Felzenswalb Huttenlocher Segmenter
			// This segmenter takes a much longer time but the results are much better
			// It is very evident that the algorithm is different from what we have implemented since there are components that overlap
			FelzenszwalbHuttenlocherSegmenter felz = new FelzenszwalbHuttenlocherSegmenter();
			DisplayUtilities.display(SegmentationUtilities.renderSegments(cloned, felz.segment(cloned)));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
