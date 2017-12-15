package uk.ac.soton.ecs.saw1g15.coursework3;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.AbstractDataSource;
import org.openimaj.data.ArrayBackedDataSource;
import org.openimaj.data.ByteArrayBackedDataSource;
import org.openimaj.data.DataSource;
import org.openimaj.data.FloatArrayBackedDataSource;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.Location;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;

public class Run2 {
	public static void main(String[] args) {
		try {
			// Create training, testing and validating datasets
			VFSGroupDataset<FImage> data = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
			GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(data, 10, 5, 5);

			// Make 15 lib-linear classifiers for each image
			HardAssigner<float[], float[], IntFloatPair> assigner = trainData(splits.getTrainingDataset());
			FeatureExtractor<DoubleFV, FImage> extractor = new RunTwoExtractor(assigner);

			// Map each patch to a visual word
			LiblinearAnnotator<FImage, String> ann = new LiblinearAnnotator<FImage, String>(extractor, Mode.MULTILABEL,
					SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
			ann.train(splits.getTrainingDataset());

			// Test the accuracy of the classifier
			double totalCorrect = 0;
			double totalImages = 0;
			for (String s : splits.getValidationDataset().getGroups()) {
				// For every string in the group check if the classifier correctly classifies
				// the images
				if (!s.equals("training")) {
					for (FImage image : splits.getValidationDataset().getInstances(s)) {
						if (ann.classify(image).getPredictedClasses().contains(s)) {
							totalCorrect++;
						}
						totalImages++;
					}
				}
			}

			// Calculate average precision of algorithm
			double average = (totalCorrect / totalImages) * 100;
			System.out.println("Average precision: " + average + "%");

			// Calculate values for the testing data and print them to run1.txt
			VFSGroupDataset<FImage> testData = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/testing.zip", ImageUtilities.FIMAGE_READER);
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("run2.txt"), "utf-8"))) {
				int nameCount = 0;
				for (FImage i : testData) {
					writer.write(nameCount + ".jpeg " + ann.classify(i).getPredictedClasses() + "\n");
					nameCount++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Make 15 lib linear classifiers for each type of image
	 */
	private static HardAssigner<float[], float[], IntFloatPair> trainData(
			GroupedDataset<String, ListDataset<FImage>, FImage> groupedDataset) {

		// Loop through each image type and create array list of all the patches
		ArrayList<float[][]> imagePatches = new ArrayList<float[][]>();
		for (FImage image : groupedDataset) {
			// get 8x8 patches of image
			imagePatches.addAll(getPatches(image));
		}
		// Cluster using K-Means to learn vocabulary
		FloatKMeans km = FloatKMeans.createKDTreeEnsemble(500);
		DataSource<float[]> datasource = new FloatArrayBackedDataSource(imagePatches.toArray(new float[][]{})); // HANNAH
																												// FFS
																												// ARRAYSTOREEXCEPTION
																												// GET
																												// ON IT
		FloatCentroidsResult result = km.cluster(datasource);
		return result.defaultHardAssigner();
	}

	/**
	 * Get 8x8 patches of image
	 */
	private static ArrayList<float[][]> getPatches(FImage image) {
		ArrayList<float[][]> imagePatches = new ArrayList<float[][]>();
		int noOfPatches = 0;
		if (image.getHeight() > image.getWidth()) {
			noOfPatches = Math.floorDiv(image.getWidth(), 4) - 1;
		} else {
			noOfPatches = Math.floorDiv(image.getHeight(), 4) - 1;
		}

		// Loop through each patch in the x and y direction
		for (int i = 0; i != noOfPatches; i++) {
			for (int j = 0; j != noOfPatches; j++) {

				// Loop through each pixel in the patch and make the patch float[][]
				float[][] currentPatch = new float[8][8];
				for (int x = 0; x < 8; x++) {
					for (int y = 0; y < 8; y++) {
						currentPatch[x][y] = image.getPixelNative(x + i, y + j);
					}
				}
				// TODO mean-centring and normalising each patch before clustering/quantisation
				
				imagePatches.add(currentPatch);
			}
		}
		return imagePatches;
	}

	static class RunTwoExtractor implements FeatureExtractor<DoubleFV, FImage> {
		HardAssigner<float[], float[], IntFloatPair> assigner;

		public RunTwoExtractor(HardAssigner<float[], float[], IntFloatPair> assigner) {
			this.assigner = assigner;
		}

		@Override
		public DoubleFV extractFeature(FImage image) {
			// Break image into patches and associate each patch with a visual word based on
			// histogram of image
			BagOfVisualWords<float[]> bovw = new BagOfVisualWords<float[]>(assigner);
			BlockSpatialAggregator<float[], SparseIntFV> spatialHist = new BlockSpatialAggregator<float[], SparseIntFV>(
					bovw, 2, 2);
			return spatialHist.aggregate(getPatches(image), image.getBounds()).normaliseFV();
		}
	}
}