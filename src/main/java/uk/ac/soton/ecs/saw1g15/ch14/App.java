package uk.ac.soton.ecs.saw1g15.ch14;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.time.Timer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
    			public void perform(Integer i) {
    			    System.out.println(i);
    			}
    		});
    		
    		VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);

    		GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);
    		
    		List<MBFImage> output = new ArrayList<MBFImage>();
    		ResizeProcessor resize = new ResizeProcessor(200);
    		
    		// Non Parallel Code
    		
    		Timer t1 = Timer.timer();
    		
    		for (ListDataset<MBFImage> clzImages : images.values()) {
    		    MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

    		    for (MBFImage i : clzImages) {
    		        MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
    		        tmp.fill(RGBColour.WHITE);

    		        MBFImage small = i.process(resize).normalise();
    		        int x = (200 - small.getWidth()) / 2;
    		        int y = (200 - small.getHeight()) / 2;
    		        tmp.drawImage(small, x, y);

    		        current.addInplace(tmp);
    		    }
    		    current.divideInplace((float) clzImages.size());
    		    output.add(current);
    		}
    		
    		System.out.println("Time: " + t1.duration() + "ms");

    		DisplayUtilities.display("Images", output);
    		
    		t1 = Timer.timer();
    		
    		// Parallel Code
//    		for (ListDataset<MBFImage> clzImages : images.values()) {
//    			final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);
//
//    		    Parallel.forEach(clzImages, new Operation<MBFImage>() {
//    		        public void perform(MBFImage i) {
//    		            final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
//    		            tmp.fill(RGBColour.WHITE);
//
//    		            final MBFImage small = i.process(resize).normalise();
//    		            final int x = (200 - small.getWidth()) / 2;
//    		            final int y = (200 - small.getHeight()) / 2;
//    		            tmp.drawImage(small, x, y);
//
//    		            synchronized (current) {
//    		                current.addInplace(tmp);
//    		            }
//    		        }
//    		    });
//
//    		    current.divideInplace((float) clzImages.size());
//    		    output.add(current);
//    		}
    		
    		System.out.println("Time: " + t1.duration() + "ms");


    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
