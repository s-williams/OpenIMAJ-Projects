package uk.ac.soton.ecs.saw1g15.ch4;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

/**
 * Global image features!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		URL[] imageURLs = new URL[] {
			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist1.jpg" ),
			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist2.jpg" ), 
			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist3.jpg" ) 
			};
    		
    		List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
    		HistogramModel model = new HistogramModel(4, 4, 4);

    		for( URL u : imageURLs ) {
  			    model.estimateModel(ImageUtilities.readMBF(u));
   			    histograms.add( model.histogram.clone() );
   			}
    		
    		// 4.1.1 Finding and displaying similar images
    		double lowest = Double.MAX_VALUE;
    		int ii = 0;
    		int jj = 0;
    		
    		for( int i = 0; i < histograms.size(); i++ ) {
    		    for( int j = i; j < histograms.size(); j++ ) {
    		        double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );
    		        
    		        System.out.println("Distance: " + distance + " with images " + i + " and " + j);
    		        
    		        // Check if the distance is the lowest so far and that i and j are not the same
    		        if (distance < lowest && i != j) {
    		        	lowest = distance;
    		        	// Store the i and j values of the highest distance so far
    		        	ii = i;
    		        	jj = j;
    		        }
    		    }
    		}
    		
    		// Display the two most similar images
    		DisplayUtilities.display(ImageUtilities.readMBF(imageURLs[ii]));
    		DisplayUtilities.display(ImageUtilities.readMBF(imageURLs[jj]));
			
    		// The result is that the two sunset images are displayed. This is the expected result since the two sunset images
    		// look similar and have a similar use of colours with lots of reds visible - unlike the moon photo image which
    		// has lots of grays that aren't featured in the other two images.
    		
    		// 4.1.2 Exploring the intersection comparison operator    		
    		for( int i = 0; i < histograms.size(); i++ ) {
    		    for( int j = i; j < histograms.size(); j++ ) {
    		        double intersection = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.INTERSECTION );
    		        
    		        System.out.println("Intersection: " + intersection + " with images " + i + " and " + j);
    		    }
    		}

    		// INTERSECTION is similar to EUCLIDEAN but different. With intersection, the most similar images receive a score
    		// of 1 whilst the most dissimilar images achieve a score of 0. Essentially, this finds the overlap between two
    		// images. Again, the two sunset images are found to be most similar.

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
