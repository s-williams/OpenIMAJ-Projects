package uk.ac.soton.ecs.saw1g15.ch5;

import java.io.IOException;
import java.net.URL;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;

/**
 * SIFT and feature matching!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		// Load images
        	MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
			MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));
			
			// Feature extraction
			DoGSIFTEngine engine = new DoGSIFTEngine();	
			LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
			LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());
			
			LocalFeatureMatcher<Keypoint> matcher;
			
			// Using Basic Matcher
			matcher = new BasicMatcher<Keypoint>(80);
			
			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);

			MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
			DisplayUtilities.display(basicMatches);
			
			// 5.1.1 Using BasicTwoWayMatcher - slightly more refined than Basic Matcher in that it identifies less points
			matcher = new BasicTwoWayMatcher<Keypoint>();
			
			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);

			basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
			DisplayUtilities.display(basicMatches);
			
			// RANSAC Model
			RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
					  new RANSAC.PercentageInliersStoppingCondition(0.5));
			matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);
			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);

			MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
			DisplayUtilities.display(consistentMatches);
			
			target.drawShape(query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
			DisplayUtilities.display(target); 
			
			// 5.1.2 Homography Model
			RobustHomographyEstimator newModelFitter = new RobustHomographyEstimator(5.0, HomographyRefinement.SINGLE_IMAGE_TRANSFER);
			matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), newModelFitter);
			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);

			consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
			DisplayUtilities.display(consistentMatches);
			
			target.drawShape(query.getBounds().transform(newModelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
			DisplayUtilities.display(target); 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
