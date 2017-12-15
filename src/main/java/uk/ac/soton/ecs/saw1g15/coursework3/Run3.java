package uk.ac.soton.ecs.saw1g15.coursework3;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

public class Run3 {
	public static void main(String[] args) {
		try {
			// Create training, testing and validating datasets
			VFSGroupDataset<FImage> data = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
			GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(data, 70, 20, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
