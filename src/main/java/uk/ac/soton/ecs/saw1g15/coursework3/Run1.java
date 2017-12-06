package uk.ac.soton.ecs.saw1g15.coursework3;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class Run1 {

	public final static int K = 5;

	public static void main(String[] args) {
		try {
			// Create a grouped dataset for the training data
    		VFSGroupDataset<FImage> training = 
    				new VFSGroupDataset<FImage>("zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
    		
    		// For every image in the training set...
    		for (FImage image : training) {
    			// Crop image to a square
//    			FImage cropped = image.proces
    			
    			// Resize the image to 16x16
    			FImage resized = image.process(new ResizeProcessor(16, 16, false));
    			
    			// Create vector by concatenating each image row
    			
    			
    			// Plot the vector
    			
    			
    		}
    		
    		// Create dataset for testing data
    		VFSListDataset<FImage> testing = 
    				new VFSListDataset<FImage>("zip:http://comp3204.ecs.soton.ac.uk/cw/testing.zip", ImageUtilities.FIMAGE_READER);
    		
    		// For every image in the testing set...
    		for (FImage image : testing) {
    			// Resize the image to 16x16
    			
    			// Create vector by concatenating each image row
    			
    			// Get k nearest neighbours to vector
    			
    			// Classify
    			
    			// Output classification
    		}
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}