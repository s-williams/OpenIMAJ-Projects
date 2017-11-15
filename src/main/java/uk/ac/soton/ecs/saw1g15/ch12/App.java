package uk.ac.soton.ecs.saw1g15.ch12;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.util.pair.IntFloatPair;

/**
 * Classification with Caltech 101!
 *
 */
public class App {
	
	public static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift) {
		
		List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<LocalFeatureList<ByteDSIFTKeypoint>>();
		
		for (Record<FImage> rec : sample) {
		    FImage img = rec.getImage();
		
		    pdsift.analyseImage(img);
		    allkeys.add(pdsift.getByteKeypoints(0.005f));
		}
		
		if (allkeys.size() > 10000)
		    allkeys = allkeys.subList(0, 10000);
		
		ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
		DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
		ByteCentroidsResult result = km.cluster(datasource);
		
		return result.defaultHardAssigner();
	}
	
	static class PHOWExtractor implements FeatureExtractor<DoubleFV, Record<FImage>> {
	    PyramidDenseSIFT<FImage> pdsift;
	    HardAssigner<byte[], float[], IntFloatPair> assigner;

	    public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner)
	    {
	        this.pdsift = pdsift;
	        this.assigner = assigner;
	    }

	    public DoubleFV extractFeature(Record<FImage> object) {
	        FImage image = object.getImage();
	        pdsift.analyseImage(image);

	        BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);

	        BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(
	                bovw, 2, 2);

	        return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
	    }
	}
	
    public static void main( String[] args ) {
    	try {
    		GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = 
    				Caltech101.getData(ImageUtilities.FIMAGE_READER);
    		
    		// work with only 5 classes of the data in order to minimise run time.
    		GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = 
    				GroupSampler.sample(allData, 5, false);
    		
    		// split data into training and testing data
    		GroupedRandomSplitter<String, Record<FImage>> splits = 
    				new GroupedRandomSplitter<String, Record<FImage>>(data, 15, 0, 15);

    		DenseSIFT dsift = new DenseSIFT(5, 7);
    		PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);
    		
    		HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);



    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
