package uk.ac.soton.ecs.saw1g15.ch8;

import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		VideoCapture vc = new VideoCapture( 320, 240 );
    		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( vc );
    		vd.addVideoListener( 
    		  new VideoDisplayListener<MBFImage>() {
    		    public void beforeUpdate( MBFImage frame ) {
    		    	// Face Detector
//    		    	FaceDetector<DetectedFace,FImage> fd = new HaarCascadeDetector(40);
//    		    	List<DetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity(frame));

    		    	// Keypoint Detector
    		    	FaceDetector<KEDetectedFace,FImage> fd = new FKEFaceDetector();
    		    	List<KEDetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity( frame ) );
    		    	
    		    	for( KEDetectedFace face : faces ) {
    		    	    frame.drawShape(face.getBounds(), RGBColour.RED);
    		    	    
    		    	    // 8.1.1 Drawing the facial keypoints
    		    	    for (FacialKeypoint f : face.getKeypoints()) {
    		    	    	f.position.translate((float)face.getBounds().minX(), (float)face.getBounds().minY());
    		    	    	frame.drawPoint(f.position, RGBColour.GREEN, 5);
    		    	    }
    		    	    
    		    	    // 8.1.2 Speech bubble
    		    	    double x = face.getKeypoint(FacialKeypointType.MOUTH_LEFT).position.x;
    		    	    double y = face.getKeypoint(FacialKeypointType.MOUTH_LEFT).position.y;
    		    	    frame.drawShapeFilled(new Ellipse(x - 2, y - 2, 20f, 10f, 0f), RGBColour.WHITE);
    		    	    frame.drawShapeFilled(new Ellipse(x - 65, y - 15, 30f, 15f, 0f), RGBColour.WHITE);
    		    	    frame.drawShapeFilled(new Ellipse(x - 200, y - 100, 100f, 70f, 0f), RGBColour.WHITE);
    		    	    frame.drawText("IMAJ is Fun", (int)x - 275, (int)y - 100, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
    		    	    
    		    	}
    		    	
    		    	
    		    }

    		    public void afterUpdate( VideoDisplay<MBFImage> display ) {
    		    }
    		  });

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
