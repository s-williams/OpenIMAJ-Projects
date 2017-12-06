package uk.ac.soton.ecs.saw1g15.coursework3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class Run1 {

	public final static int K = 16;
	
	static float[] createVector(FImage image) {
		int counter = 0;
		float[] imageVector = new float[image.getRows() * image.getCols()];
		for (int i = 0; i < image.getRows(); i++) {
			for (int j = 0; j < image.getCols(); j++) {
				imageVector[counter] = image.getPixel(i, j);
				counter++;
			}
		}
		return imageVector;
	}

	public static void main(String[] args) {
		try {
			// Create a grouped dataset for the training data
    		VFSGroupDataset<FImage> training = 
    				new VFSGroupDataset<FImage>("zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
    		
    		// Plotted Vectors
    		ArrayList<VectorLabelPair> graph = new ArrayList<VectorLabelPair>();
    		
    		// Gets list of classifications in the dataset
    		Set<String> groups = training.getGroups();
    		
    		// For every classification image group of an image..
    		for (String s : groups) {
	    		// For every image in the image group...
    			// Ignore duplicates
    			if (!s.equals("training")) {
		    		for (FImage image : training.getInstances(s)) {
		    			// TODO Crop image to a square
		    			
		    			// Resize the image to 16x16
		    			FImage resized = image.process(new ResizeProcessor(16, 16, false));
		    			
		    			// Create vector by concatenating each image row
		    			float[] imageVector = createVector(resized);
		    			
		    			VectorLabelPair imageVectorLabelPair = new VectorLabelPair(s, imageVector);
		    			
		    			// Plot the vector
		    			graph.add(imageVectorLabelPair);	    			
		    		}
    			}
    		}
    		
    		System.out.println("Training done! Testing now!");
    		
    		// Create dataset for testing data
    		VFSListDataset<FImage> testing = 
    				new VFSListDataset<FImage>("zip:http://comp3204.ecs.soton.ac.uk/cw/testing.zip", ImageUtilities.FIMAGE_READER);
    		
    		// For every image in the testing set...
    		int count = 0;
    		for (FImage image : testing) {
    			if (count < 5) {
	    			count++;
	    			// TODO Crop image to a square
	    			
	    			// Resize the image to 16x16
	    			FImage resized = image.process(new ResizeProcessor(16, 16, false));
	    			
	    			// Create vector by concatenating each image row
	    			float[] imageVector = createVector(resized);
	    			
	    			// Calculate distance to all vectors
//	    			ArrayList<VectorLabelPair> graphy = (ArrayList<VectorLabelPair>) graph.clone();
	    			for (VectorLabelPair v : graph) {
	    				v.setDistanceFrom(imageVector);
	    			}
	    			
	    			// Find closest neighbour K times and store in topK
	    			Collections.sort(graph);
	    			ArrayList<VectorLabelPair> topK = new ArrayList<VectorLabelPair>(graph.subList(0,K));
//	    			for (int i = 0; i < K; i++) {
//	    				VectorLabelPair closest = new VectorLabelPair("Awful", new float[0]);
//	    				closest.distance = Float.MAX_VALUE;
//	    				
//	    				// Loop through testing data
//	    				Iterator<VectorLabelPair> iter = graphy.iterator();
//	    				while (iter.hasNext()) {
//	    					VectorLabelPair v = iter.next();
//	        				if (v.distance < closest.distance) {
//	        					closest = v;
//	        				}
//	        			}
//
//	    				// Remove closest neighbour found
//    					graphy.remove(graphy.indexOf(closest));
//    					topK.add(closest);
//	    			}
	    			
//	    			//**** DEBUG *****
	    			System.out.println("Content of topK:");
	    			for (VectorLabelPair v : topK) {
	    				System.out.println(v.label + " at distance " + v.distance);
	    			}
//	    			//****************
	    			
	    			// Find most common label in the K nearest
	    			Map<String, Integer> labelsCount = new HashMap<>();
	    			for (VectorLabelPair v : topK) {
	    				  Integer c = labelsCount.get(v.label);
	    				  if(c == null) c = new Integer(0);
	    				  c++;
	    				  labelsCount.put(v.label, c);
	    			}			
	    			Map.Entry<String,Integer> mostRepeated = null;
	    			for(Map.Entry<String, Integer> e: labelsCount.entrySet()) {
	    			    if(mostRepeated == null || mostRepeated.getValue()<e.getValue())
	    			        mostRepeated = e;
	    			}
	    			// Classification is most common label
	    			String classification = mostRepeated.getKey();
	    			
	    			// Output classification
	    			System.out.println("Classified as: " + classification);
	    			DisplayUtilities.display(image, classification);
    			}
    		}
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// Stores a float vector and a label
class VectorLabelPair implements Comparable<VectorLabelPair> {
	String label;
	float[] vector;
	float distance;
	
	public VectorLabelPair(String label, float[] vector) {
		this.label = label;
		this.vector = vector;
	}
	
	public void setDistanceFrom(float[] v) {
		float sum = 0;
		for (int i = 0; i < vector.length; i++) {
			sum += (vector[i] - v[i]) * (vector[i] - v[i]);
		}
		distance = (float)Math.sqrt((double)sum);
	}

	@Override
	public int compareTo(VectorLabelPair o) {
		if (distance == o.distance) return 0;
		if (distance >  o.distance) return 1;
		return -1;
	}
	
}