package uk.ac.soton.ecs.saw1g15.coursework3;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

	public final static int K = 5;

	public static void main(String[] args) {
		Run1 run = new Run1();
		try {
			// Create training, testing and validating datasets
			VFSGroupDataset<FImage> data = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
			GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(data, 70, 20, 10);

			ArrayList<VectorLabelPair> graph = run.trainData(splits.getTrainingDataset());
			System.out.println("Training done! Testing now!");

			// Classify all the images in the validation dataset and test if they are
			// correct
			double totalCorrect = 0;
			double totalImages = 0;
			for (String s : splits.getValidationDataset().getGroups()) {
				// For every string in the group check if the classifier correctly classifies
				// the images
				if (!s.equals("training")) {
					for (FImage image : splits.getValidationDataset().getInstances(s)) {
						if (run.classifyImage(image, graph) == s) {
							totalCorrect++;
						}
						totalImages++;
					}
				}
			}

			// Calc average precision of algorithm
			double average = (totalCorrect / totalImages)*100;
			System.out.println("Average precision: " + average + "%");
			
			// Calculate values for the testing data and print them to run1.txt
			VFSGroupDataset<FImage> testData = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/testing.zip", ImageUtilities.FIMAGE_READER);
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			              new FileOutputStream("run1.txt"), "utf-8"))) {
				int nameCount = 0;
				for(FImage i : testData) {
					writer.write(nameCount + ".jpeg " + run.classifyImage(i, graph) + "\n");
					nameCount++;
				}
			}
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
	private ArrayList<VectorLabelPair> trainData(GroupedDataset<String, ListDataset<FImage>, FImage> groupedDataset) {
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
					// TODO Scotts mess of a function needs fixing
					// Crop image to a square
//					DisplayUtilities.display(image);
//					image = crop(image);
//					DisplayUtilities.display(image);
//					
//					break;

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
	
	/**
	 * Crop image to square around the centre
	 */
	public static FImage crop(FImage image) {
		int index = Math.min(image.getCols(), image.getRows());
		float[][] pixels = new float[index][index];
		for (int y = 0; y < index; y++) {
			for (int x = 0; x < index; x++) {
				pixels[x][y] = image.pixels[x][y];
			}
		}
		FImage cropped = new FImage(pixels);
		return cropped;
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