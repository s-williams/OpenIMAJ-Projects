package uk.ac.soton.ecs.saw1g15.ch13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.EigenImages;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * Face recognition 101: Eigenfaces!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		// Dataset of approximately aligned faces
    		VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
			
    		// Split dataset into training and testing
    		int nTraining = 5;
    		int nTesting = 5;
    		GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
    		GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
    		GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

    		List<FImage> basisImages = DatasetAdaptors.asList(training);
    		int nEigenvectors = 100;
    		EigenImages eigen = new EigenImages(nEigenvectors);
    		eigen.train(basisImages);

    		List<FImage> eigenFaces = new ArrayList<FImage>();
    		for (int i = 0; i < 12; i++) {
    		    eigenFaces.add(eigen.visualisePC(i));
    		}
    		DisplayUtilities.display("EigenFaces", eigenFaces);

    		Map<String, DoubleFV[]> features = new HashMap<String, DoubleFV[]>();
    		for (final String person : training.getGroups()) {
    		    final DoubleFV[] fvs = new DoubleFV[nTraining];

    		    for (int i = 0; i < nTraining; i++) {
    		        final FImage face = training.get(person).get(i);
    		        fvs[i] = eigen.extractFeature(face);
    		    }
    		    features.put(person, fvs);
    		}

    		double correct = 0, incorrect = 0;
    		for (String truePerson : testing.getGroups()) {
    		    for (FImage face : testing.get(truePerson)) {
    		        DoubleFV testFeature = eigen.extractFeature(face);

    		        String bestPerson = null;
    		        double minDistance = Double.MAX_VALUE;
    		        for (final String person : features.keySet()) {
    		            for (final DoubleFV fv : features.get(person)) {
    		                double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);

    		                if (distance < minDistance) {
    		                    minDistance = distance;
    		                    bestPerson = person;
    		                }
    		            }
    		        }
    		        // 13.1.3 Applying a threshold
    		        // The best threshold would achieve similar accuracy as if there was no threshold (0.94) and would only reject
    		        // images that the guess is unknown for.
    		        // Experimental evidence suggests this threshold to be around 14.0.
    		        double threshold = 14.0;
	                if (minDistance > threshold) {
	                	minDistance = Double.MAX_VALUE;
	                	bestPerson = "unknown";
	                }

    		        System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

    		        if (truePerson.equals(bestPerson))
    		            correct++;
    		        else
    		            incorrect++;
    		    }
    		}

    		System.out.println("Accuracy: " + (correct / (correct + incorrect)));
    		
    		// 13.1.1 Reconstructing a face from the weights
    		// Get a random face
    		FImage randomFace = dataset.getRandomInstance();
    		
    		// Get its features
    		final DoubleFV[] randomFeatures = new DoubleFV[nTraining];

    	    for (int i = 0; i < nTraining; i++) {
    	    	randomFeatures[i] = eigen.extractFeature(randomFace);
    	    }
    		
    		// Reconstruct it
    		DisplayUtilities.display(eigen.reconstruct(randomFeatures[0]).normalise());
    		
    		// 13.1.2 Exploring the effect of training set size
    		// Increasing the number of training images increases the accuracy. With just 5 training images, the performance
    		// is around 93%. With 6, its 96%. With 7, 97%. With 8, 99%. I expect performance gains to be logarithmic with larger
    		// numbers of training images approaching but not quite reaching 100%.

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
