package uk.ac.soton.ecs.saw1g15.ch7;

import java.io.File;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.effects.DioramaEffect;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.threshold.AbstractLocalThreshold;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdContrast;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * Processing video!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		// Import a cat video
    		Video<MBFImage> video = new XuggleVideo(new URL("http://static.openimaj.org/media/tutorial/keyboardcat.flv"));
    		
    		// From webcam
//    		video = new VideoCapture(320, 240);
    		
    		// Display the video
    		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
    		
    		// Iterate through every frame
    		for (MBFImage mbfImage : video) {
    		    DisplayUtilities.displayName(mbfImage.process(new CannyEdgeDetector()), "Canny");
    		    
    		    // 7.1.1 - this kills the frames per second
    		    DisplayUtilities.displayName(mbfImage.process(new AdaptiveLocalThresholdContrast(10)), "Threshold");
    		    DisplayUtilities.displayName(mbfImage.process(new DioramaEffect(new Line2d(0, 0, 1, 1))), "Diorama");
    		    DisplayUtilities.displayName(mbfImage.process(new ResizeProcessor(60)), "Resize");

    		}



    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
