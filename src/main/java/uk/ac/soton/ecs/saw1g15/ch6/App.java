package uk.ac.soton.ecs.saw1g15.ch6;

import java.util.Map.Entry;

import org.openimaj.audio.reader.OneSecondClipReader;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;
import org.openimaj.util.api.auth.common.FlickrAPIToken;

/**
 * Image Datasets!
 *
 */
public class App {
    public static void main( String[] args ) {
    	try {
    		// Use MBFIMAGE_READER for colour
    		VFSListDataset<FImage> images = 
    				new VFSListDataset<FImage>("C:\\Users\\Admin\\OpenIMAJ-Tutorial01\\data", ImageUtilities.FIMAGE_READER);
    		System.out.println(images.size());
    		
    		// A random image from the dataset
//    		DisplayUtilities.display(images.getRandomInstance(), "A random image from the dataset");

    		// All images displayed
    		DisplayUtilities.display("My images", images);
    		
    		// Dataset from zip online
    		VFSListDataset<FImage> faces = 
    				new VFSListDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
    		DisplayUtilities.display("ATT faces", faces);
    		
    		// Grouped dataset maintains associations
    		VFSGroupDataset<FImage> groupedFaces = 
    				new VFSGroupDataset<FImage>( "zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

    		// Iterates through keys (directories) of grouped dataset
    		// Uncomment at your risk
//    		for (final Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
//    			DisplayUtilities.display(entry.getKey(), entry.getValue());
//    		}
    		
    		// 6.1.1 displays a randomly selected image of each person
    		// Uncomment at your risk
//    		for (final Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
//    			DisplayUtilities.display(entry.getValue().getRandomInstance(), "A random image of " + entry.getKey());
//    		}    		
    		
    		// Dataset constructed from Flickr search
    		// REQUIRES FLICKR CREDENTIALS
    		FlickrAPIToken flickrToken = DefaultTokenFactory.get(FlickrAPIToken.class);
    		FlickrImageDataset<FImage> cats = FlickrImageDataset.create(ImageUtilities.FIMAGE_READER, flickrToken, "cat", 10);
    		DisplayUtilities.display("Cats", cats);
    		
    		// 6.1.2 Commons VFS supports the sources listed here. It includes Tar, Jar, and gzip file types and HTTP/FTP 
    		// communication protocols among many other things
    		// http://commons.apache.org/proper/commons-vfs/filesystems.html
    		
    		// 6.1.3 Dataset constructed from Bing search
    		// REQUIRES BING CREDENTIALS
    		BingAPIToken bingToken=DefaultTokenFactory.get(BingAPIToken.class);
    		BingImageDataset<FImage> dogs = BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "dog", 10);
			DisplayUtilities.display("Dogs", dogs);
    		
    		// 6.1.4 Creating a grouped dataset of famous people
    		MapBackedDataset<String,BingImageDataset<FImage>,FImage> celebs = 
    				new MapBackedDataset<String,BingImageDataset<FImage>,FImage>();
    		celebs.put("Tanenbaum", BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "Andrew Tanenbaum", 10));
    		celebs.put("Linus", BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "Linus Torvalds", 10));

    		DisplayUtilities.display("Tanenbaum", celebs.getInstances("Tanenbaum"));
    		DisplayUtilities.display("Linus", celebs.getInstances("Linus"));




    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
