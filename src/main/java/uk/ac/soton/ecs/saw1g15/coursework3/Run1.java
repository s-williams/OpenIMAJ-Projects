package uk.ac.soton.ecs.saw1g15.coursework3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class Run1 {

	public final static int K = 16;

	public static void main(String[] args) {
		Run1 run = new Run1();
		try {
			// Create training, testing and validating datasets
			VFSGroupDataset<FImage> data = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
			GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(data, 70, 20, 10);

			ArrayList<VectorLabelPair> graph = run.trainData(splits.getTrainingDataset());
			System.out.println("Training done! Testing now!");
			
			// Classify all the images in the validation dataset and test if they are correct
			int totalCorrect = 0;
			for (String s : splits.getValidationDataset().getGroups()) {
				System.out.println(splits.getValidationDataset().getInstances(s).size());
				System.out.println(s);
				// For every string in the group check if the classifier correctly classifies the images
				if (!s.equals("training")) {
					for (FImage image : splits.getValidationDataset().getInstances(s)) {
						if(run.classifyImage(image, graph) == s) {
							totalCorrect++;
						}
					}
				}
			}

			// Calc average precision of algorithm
			System.out.println(splits.getValidationDataset().size());
			System.out.println(splits.getValidationDataset().getGroups().size());
			System.out.println("Average: " + totalCorrect/splits.getValidationDataset().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * K means Algorithm used to classify each item in a dataset
	 */
	private String classifyImage(FImage image, ArrayList<VectorLabelPair> graph) {
		// TODO Crop image to a square

		// Resize the image to 16x16
		FImage resized = image.process(new ResizeProcessor(16, 16, false));

		// Create vector by concatenating each image row
		float[] imageVector = createVector(resized);

		// Calculate distance to all vectors
		// ArrayList<VectorLabelPair> graphy = (ArrayList<VectorLabelPair>)
		// graph.clone();
		for (VectorLabelPair v : graph) {
			v.setDistanceFrom(imageVector);
		}

		// Find closest neighbour K times and store in topK
		Collections.sort(graph);
		ArrayList<VectorLabelPair> topK = new ArrayList<VectorLabelPair>(graph.subList(0, K));
		// for (int i = 0; i < K; i++) {
		// VectorLabelPair closest = new VectorLabelPair("Awful", new float[0]);
		// closest.distance = Float.MAX_VALUE;
		//
		// // Loop through testing data
		// Iterator<VectorLabelPair> iter = graphy.iterator();
		// while (iter.hasNext()) {
		// VectorLabelPair v = iter.next();
		// if (v.distance < closest.distance) {
		// closest = v;
		// }
		// }
		//
		// // Remove closest neighbour found
		// graphy.remove(graphy.indexOf(closest));
		// topK.add(closest);
		// }

		// Find most common label in the K nearest
		Map<String, Integer> labelsCount = new HashMap<>();
		for (VectorLabelPair v : topK) {
			Integer c = labelsCount.get(v.label);
			if (c == null)
				c = new Integer(0);
			c++;
			labelsCount.put(v.label, c);
		}
		Map.Entry<String, Integer> mostRepeated = null;
		for (Map.Entry<String, Integer> e : labelsCount.entrySet()) {
			if (mostRepeated == null || mostRepeated.getValue() < e.getValue())
				mostRepeated = e;
		}
		// Classification is most common label
		return mostRepeated.getKey();
	}

	/**
	 * Train the algorithm with the training data
	 */
	private ArrayList<VectorLabelPair> trainData(
			GroupedDataset<String, ListDataset<FImage>, FImage> groupedDataset) {
		// Plotted Vectors
		ArrayList<VectorLabelPair> graph = new ArrayList<VectorLabelPair>();

		// Gets list of classifications in the dataset
		Set<String> groups = groupedDataset.getGroups();

		// For every classification image group of an image..
		for (String s : groups) {
			// For every image in the image group...
			// Ignore duplicates
			if (!s.equals("training")) {
				for (FImage image : groupedDataset.getInstances(s)) {
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

		return graph;
	}

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
		distance = (float) Math.sqrt((double) sum);
	}

	@Override
	public int compareTo(VectorLabelPair o) {
		if (distance == o.distance)
			return 0;
		if (distance > o.distance)
			return 1;
		return -1;
	}

}