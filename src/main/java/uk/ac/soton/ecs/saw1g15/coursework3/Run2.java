package uk.ac.soton.ecs.saw1g15.coursework3;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.AbstractDataSource;
import org.openimaj.data.ByteArrayBackedDataSource;
import org.openimaj.data.DataSource;
import org.openimaj.data.FloatArrayBackedDataSource;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
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
			// Create a grouped dataset for the training data
			VFSGroupDataset<FImage> training = new VFSGroupDataset<FImage>(
					"zip:http://comp3204.ecs.soton.ac.uk/cw/training.zip", ImageUtilities.FIMAGE_READER);
			List<ConnectedComponent> allPatches = new ArrayList<ConnectedComponent>();

			for (FImage image : training) {
				
				// Loop through 8x8 patches in images
				
				// Take pixels from patches and flatten them
				float[] imageData = image.getPixelVectorNative(new float[image.getWidth() * image.getHeight()]);
				// Mean centre and normalise patch before clustering
				ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
				DataSource<byte[]> datasource = new LocalFeatureListDataSource<Keypoint, byte[]>();
				ByteCentroidsResult result = km.cluster(datasource);

				// Cluster each sample with k mean vocabulary
				HardAssigner<byte[], float[], IntFloatPair> assigner = result.defaultHardAssigner();
				BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
				BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(
		                bovw, 2, 2);
				DoubleFV extractor = spatial.aggregate(image.getBounds()).normaliseFV();

				LiblinearAnnotator<FImage, String> ann = new LiblinearAnnotator<FImage, String>(extractor,
						Mode.MULTILABEL, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
			}
			
			ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = 
					new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(
						ann, splits.getTestDataset(), new CMAnalyser<Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));
						
			Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
