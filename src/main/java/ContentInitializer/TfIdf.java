package ContentInitializer;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

class TfIdf {

	private static Map<Integer, ArrayList<Integer>> map = new HashMap<>();
	private static Map<Integer, Integer> o = new HashMap<>();
	private static Map<Integer, Float> idfo = new HashMap<>();
	private static Map<Integer, HashMap<Integer, Float>> tfidf_map = new HashMap<>();
	private static int maxTimes = 0;

	static Map<Integer, HashMap<Integer, Float>> getMap(Map<Integer, ArrayList<Integer>> map) {
		TfIdf.map = map;
		calcO();
		idfizer();
		tfidfizer();
		return tfidf_map;
	}

	private static void idfizer() {

		Float totalD = (float) map.size();
		for (Entry<Integer, Integer> entry : o.entrySet()) {
			Float idf = (float) (Math.log(totalD / entry.getValue()));
			idfo.put(entry.getKey(), idf);
		}
	}

	private static void tfidfizer() {
		for (Entry<Integer, ArrayList<Integer>> entry : map.entrySet()) {
			int item = entry.getKey();
			HashSet<Integer> featuresSet = new HashSet<>(entry.getValue());
			Float normDem = 0f;
			for (int feature : featuresSet) {
				normDem = normDem + (float) Math.pow(1 * idfo.get(feature), 2);
			}
			normDem = (float) Math.sqrt(normDem);
			HashMap<Integer, Float> itemTfIdfFeatures = tfidf_map.get(item);
			if (itemTfIdfFeatures == null)
				itemTfIdfFeatures = new HashMap<>();
			for (int feature : featuresSet) {
				Float tfidf = 1 * idfo.get(feature);
				Float wkj = tfidf / normDem;
				Entry<Integer, Float> pair=new SimpleEntry<>(feature,wkj);
				itemTfIdfFeatures.put(feature,wkj);
			}
			tfidf_map.put(item, itemTfIdfFeatures);
		}
	}

	private static void calcO() {
		for (Entry<Integer, ArrayList<Integer>> entry : map.entrySet()) {
			ArrayList<Integer> itemObjects = entry.getValue();
			Integer item = entry.getKey();
			if (itemObjects == null) {
				System.out.println(item);
				System.out.println("Cannot retrieve features of a not found movie");
			} else {
				HashSet<Integer> bag = new HashSet<>();
				for (int i = 0; i < itemObjects.size(); i++) {
					Integer featureNumber = itemObjects.get(i);
					if (!bag.contains(featureNumber)) {
						Integer numberOfItemsThatContainFeature = o.get(featureNumber);
						if (numberOfItemsThatContainFeature == null) {
							numberOfItemsThatContainFeature = 1;
						} else {
							numberOfItemsThatContainFeature = numberOfItemsThatContainFeature + 1;
						}
						if (numberOfItemsThatContainFeature > maxTimes)
							maxTimes = numberOfItemsThatContainFeature;
						o.put(featureNumber, numberOfItemsThatContainFeature);
						bag.add(featureNumber);
					}
				}
			}
		}
	}
}
