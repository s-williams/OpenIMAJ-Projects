package uk.ac.soton.ecs.saw1g15.coursework3;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
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

			// Create a hard assigner to features to identifiers
			HardAssigner<float[], float[], IntFloatPair> assigner = trainData(splits.getTrainingDataset());
			FeatureExtractor<DoubleFV, FImage> extractor = new RunTwoExtractor(assigner);

			// Map each patch to a visual word
			// TODO Make 15 of these?!
			LiblinearAnnotator<FImage, String> ann = new LiblinearAnnotator<FImage, String>(extractor, Mode.MULTILABEL,
					SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
			ann.train(splits.getTrainingDataset());

			// Test the accuracy of the classifier
			ClassificationEvaluator<CMResult<String>, String, FImage> eval = new ClassificationEvaluator<CMResult<String>, String, FImage>(
					ann, splits.getTestDataset(), new CMAnalyser<FImage, String>(CMAnalyser.Strategy.SINGLE));
			Map<FImage, ClassificationResult<String>> guesses = eval.evaluate();
			CMResult<String> result = eval.analyse(guesses);
			System.out.println(result);

			// Classify the testing data and print them to run1.txt
			VFSGroupDataset<FImage> testData = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/testing.zip", ImageUtilities.FIMAGE_READER);
			ClassificationEvaluator<CMResult<String>, String, FImage> testingDataClass = new ClassificationEvaluator<CMResult<String>, String, FImage>(
					ann, testData, new CMAnalyser<FImage, String>(CMAnalyser.Strategy.SINGLE));
			Map<FImage, ClassificationResult<String>> guessesTestingData = testingDataClass.evaluate();
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("run2.txt"), "utf-8"))) {
				int nameCount = 0;
				for (FImage i : guessesTestingData.keySet()) {
					writer.write(nameCount + ".jpg " + guessesTestingData.get(i).getPredictedClasses().toArray()[0] + "\n");
					nameCount++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a hard assigner to assign features to identifiers
	 */
	private static HardAssigner<float[], float[], IntFloatPair> trainData(
			GroupedDataset<String, ListDataset<FImage>, FImage> groupedDataset) {

		// Loop through each image type and create array list of all the patches
		ArrayList<float[]> vectorPatches = new ArrayList<float[]>();
		
		for (FImage image : groupedDataset) {
			// get 8x8 patches of image sampled every fourth pixel
			for(float[] patch : getPatches(image, 8, 4)) {
				vectorPatches.add(patch);
			}
			vectorPatches.addAll(getPatches(image, 8, 4));
		}
		// Vector quantisation 
		DataSource<float[]> datasource = new FloatArrayBackedDataSource(vectorPatches.toArray(new float[][]{}));
		//DataSource<float[]> datasource = new FloatArrayBackedDataSource(imagePatches.toArray(new float[][]{})); // HANNAH
																												// FFS
																												// ARRAYSTOREEXCEPTION
																												// GET
																												// ON IT
		// Cluster using K-Means to learn vocabulary
		FloatKMeans km = FloatKMeans.createKDTreeEnsemble(500);
		FloatCentroidsResult result = km.cluster(datasource);
		return result.defaultHardAssigner();
	}

	/**
	 * Get patches of a certain size in an image sampled at a certain distance apart
	 */
	private static ArrayList<float[]> getPatches(FImage image, int sizeOfPatch, int distanceApart) {
		ArrayList<float[]> imagePatches = new ArrayList<float[]>();
		int noOfPatches = 0;
		if (image.getHeight() > image.getWidth()) {
			noOfPatches = Math.floorDiv(image.getWidth(), distanceApart) - 1;
		} else {
			noOfPatches = Math.floorDiv(image.getHeight(), distanceApart) - 1;
		}

		// Loop through each patch in the x and y direction
		for (int i = 0; i != noOfPatches; i++) {
			for (int j = 0; j != noOfPatches; j++) {

				// Loop through each pixel in the patch and make the patch float[][]
				float[][] currentPatch = new float[sizeOfPatch][sizeOfPatch];
				for (int x = 0; x < sizeOfPatch; x++) {
					for (int y = 0; y < sizeOfPatch; y++) {
						currentPatch[x][y] = image.getPixelNative(x + i, y + j);
					}
				}
				// TODO mean-centring and normalising each patch before clustering/quantisation
				// Flatten each pixel into a vector
				FImage patchImage = new FImage(currentPatch);
				float[] imageData = patchImage.getPixelVectorNative(new float[patchImage.getWidth() * patchImage.getHeight()]);
				imagePatches.add(imageData);
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
			return bovw.aggregateVectorsRaw(getPatches(image,8,4)).normaliseFV();
		}
	}
}