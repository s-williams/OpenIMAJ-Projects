package uk.ac.soton.ecs.saw1g15.ch9;

import java.io.File;
import java.net.URL;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioWaveform;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		// Playing audio
//    		XuggleAudio xa = new XuggleAudio("https://incompetech.com/music/royalty-free/mp3-royaltyfree/Robobozo.mp3");
//    		AudioPlayer.createAudioPlayer( xa ).run();
    		
    		// Showing waveform
    		final AudioWaveform vis = new AudioWaveform( 400, 400 );
    		vis.showWindow( "Waveform" );

    		final XuggleAudio xa = new XuggleAudio( 
    		    new URL( "http://www.audiocheck.net/download.php?" +
    		        "filename=Audio/audiocheck.net_sweep20-20klin.wav" ) );

    		SampleChunk sc = null;
    		while( (sc = xa.nextSampleChunk()) != null )
    		    vis.setData( sc.getSampleBuffer() );

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
