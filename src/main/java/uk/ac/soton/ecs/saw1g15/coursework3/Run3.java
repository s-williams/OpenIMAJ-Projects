package uk.ac.soton.ecs.saw1g15.coursework3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.util.pair.IntFloatPair;

public class Run3 {
	public static void main(String[] args) {
		try {
			// Create training, testing and validating datasets
			VFSGroupDataset<FImage> data = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
			GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(data, 15, 15, 20);
			System.out.println("Downloaded training data");
			
			// Use  SIFT for feature extraction
			HardAssigner<byte[], float[], IntFloatPair> assigner;
			DoGSIFTEngine engine = new DoGSIFTEngine();

			// Check for a hard assigner in the cache to speed up process
			File cacheFile = new File("r3assigner");
			if(IOUtils.readFromFile(cacheFile) != null) {
				assigner = IOUtils.readFromFile(cacheFile);
			} else {
				assigner = trainData(splits.getTrainingDataset(), engine);
				// Cache the assigner
				IOUtils.writeToFile(assigner, cacheFile);
			}
			System.out.println("Got Hard Assigner");
			FeatureExtractor<DoubleFV, FImage> extractor = new RunTwoExtractor(engine, assigner);
			System.out.println("Created Feature extractor");

			// Map each patch to a visual word
			NaiveBayesAnnotator<FImage, String> ann = new NaiveBayesAnnotator<FImage, String>(extractor, Mode.MAXIMUM_LIKELIHOOD);
			ann.train(splits.getTrainingDataset());
			System.out.println("Created Naive bayes classifier");
			
			// Test the accuracy of the classifier
			ClassificationEvaluator<CMResult<String>, String, FImage> eval = new ClassificationEvaluator<CMResult<String>, String, FImage>(
					ann, splits.getTestDataset(), new CMAnalyser<FImage, String>(CMAnalyser.Strategy.SINGLE));
			Map<FImage, ClassificationResult<String>> guesses = eval.evaluate();
			CMResult<String> result = eval.analyse(guesses);
			System.out.println(result);

			// Classify the testing data and print them to run3.txt
			VFSGroupDataset<FImage> testData = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/testing.zip", ImageUtilities.FIMAGE_READER);
			System.out.println("Classifying test data");
			ClassificationEvaluator<CMResult<String>, String, FImage> testingDataClass = new ClassificationEvaluator<CMResult<String>, String, FImage>(
					ann, testData, new CMAnalyser<FImage, String>(CMAnalyser.Strategy.SINGLE));
			Map<FImage, ClassificationResult<String>> guessesTestingData = testingDataClass.evaluate();
			System.out.println("Writing to file");
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("run3.txt"), "utf-8"))) {
				int nameCount = 0;
				for (FImage i : guessesTestingData.keySet()) {
					writer.write(nameCount + ".jpg " + guessesTestingData.get(i).getPredictedClasses().toArray()[0] + "\n");
					nameCount++;
				}
			}
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Make 15 lib linear classifiers for each type of image
	 */
	private static HardAssigner<byte[], float[], IntFloatPair> trainData(
			GroupedDataset<String, ListDataset<FImage>, FImage> groupedDataset,
			DoGSIFTEngine engine) {
		List<LocalFeatureList<Keypoint>> features = new ArrayList<LocalFeatureList<Keypoint>>();

		System.out.println("Analyse each image and classify them");
		for (String s : groupedDataset.getGroups()) {
			// For every string in the group check if the classifier correctly classifies the images
			if (!s.equals("training")) {
				for (FImage image : groupedDataset.getInstances(s)) {
					// Find features in the image
					features.add(engine.findFeatures(image));
				}
			}
		}
		
		// Only cluster with 300 image keys
		if (features.size() > 300)
			features = features.subList(0, 300);
		
		System.out.println("Cluster using K-Means to learn vocabulary");
		ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
		DataSource<byte[]> datasource = new LocalFeatureListDataSource<Keypoint, byte[]>(features);
		ByteCentroidsResult result = km.cluster(datasource);
		return result.defaultHardAssigner();
	}

	static class RunTwoExtractor implements FeatureExtractor<DoubleFV, FImage> {
		HardAssigner<byte[], float[], IntFloatPair> assigner;
		DoGSIFTEngine engine;

		public RunTwoExtractor(DoGSIFTEngine engine,
				HardAssigner<byte[], float[], IntFloatPair> assigner2) {
			this.assigner = assigner2;
			this.engine = engine;
		}

		@Override
		public DoubleFV extractFeature(FImage image) {
			// Break image into patches and associate each patch with a visual word based on
			// histogram of image
			BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
			BlockSpatialAggregator<byte[], SparseIntFV> spatialHist = new BlockSpatialAggregator<byte[], SparseIntFV>(
					bovw, 2, 2);
			return spatialHist.aggregate(engine.findFeatures(image), image.getBounds()).normaliseFV();
		}
	}
}