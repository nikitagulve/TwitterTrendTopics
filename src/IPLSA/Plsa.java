package IPLSA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * 
 * This class implements IPLSA
 * 
 * @author yogi,niki
 * 
 */
public class Plsa {

	private int topicNum;

	private int docSize;

	private int vocabularySize;

	private ArrayList<List<Integer>> docTermMatrix;

	// p(z|d)
	private List<List<Double>> docTopicPros;

	// p(w|z)
	private List<List<Double>> topicTermPros;

	// p(z|d,w)
	private List<List<List<Double>>> docTermTopicPros;

	private List<String> allWords;

	public int[] givenLabel;

	Utility utility;

	// Graph structure
	Map<Integer, List<Integer>> clusterTimeCount;

	public Plsa(int numOfTopic) {
		topicNum = numOfTopic;
		docSize = 0;
		utility = new Utility();
		clusterTimeCount = new HashMap<Integer, List<Integer>>();
	}

	/**
	 * 
	 * train plsa
	 * 
	 * @param docs
	 *            all documents
	 */
	public void train(List<Tweet> docs, int maxIter) {
		if (docs == null) {
			throw new IllegalArgumentException(
					"The documents set must be not null!");
		}

		// statistics vocabularies
		allWords = statisticsVocabularies(docs);

		// element represent times the word appear in this document
		docTermMatrix = new ArrayList<List<Integer>>();
		// init docTermMatrix
		for (int docIndex = 0; docIndex < docSize; docIndex++) {
			Tweet doc = docs.get(docIndex);
			Integer[] l = new Integer[vocabularySize];
			for (String word : doc.getWords()) {
				if (allWords.contains(word)) {
					int wordIndex = allWords.indexOf(word);
					l[wordIndex] = new Integer((l[wordIndex] == null ? 0
							: l[wordIndex].intValue()) + 1);
				}
			}
			docTermMatrix.add(docIndex, Arrays.asList(l));

			// free memory
			l = null;
		}
		docTopicPros = new ArrayList<List<Double>>();
		topicTermPros = new ArrayList<List<Double>>();
		docTermTopicPros = new ArrayList<List<List<Double>>>();

		// init p(z|d),for each document the constraint is sum(p(z|d))=1.0
		for (int i = 0; i < docSize; i++) {
			double[] pros = randomProbilities(topicNum);
			ArrayList<Double> prob = new ArrayList<Double>();
			for (int j = 0; j < topicNum; j++) {
				prob.add(pros[j]);
			}
			docTopicPros.add(i, prob);
		}
		// init p(w|z),for each topic the constraint is sum(p(w|z))=1.0
		for (int i = 0; i < topicNum; i++) {
			double[] pros = randomProbilities(vocabularySize);
			ArrayList<Double> prob = new ArrayList<Double>();
			for (int j = 0; j < vocabularySize; j++) {
				prob.add(pros[j]);
			}
			topicTermPros.add(i, prob);
		}

		// use em to estimate params
		for (int i = 0; i < maxIter; i++) {
			em();
		}
	}

	/**
	 * 
	 * EM algorithm
	 * 
	 */
	private void em() {
		/*
		 * E-step,calculate posterior probability p(z|d,w,&),& is model
		 * params(p(z|d),p(w|z))
		 * 
		 * p(z|d,w,&)=p(z|d)*p(w|z)/sum(p(z'|d)*p(w|z')) z' represent all
		 * posible topic
		 */
		List<List<Double>> term;
		List<Double> topic;
		for (int docIndex = 0; docIndex < docSize; docIndex++) {
			term = new ArrayList<List<Double>>();
			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				double total = 0.0;
				double[] perTopicPro = new double[topicNum];
				for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
					double numerator = docTopicPros.get(docIndex).get(
							topicIndex)
							* topicTermPros.get(topicIndex).get(wordIndex);
					total += numerator;
					perTopicPro[topicIndex] = numerator;
				}

				if (total == 0.0) {
					total = avoidZero(total);
				}
				topic = new ArrayList<Double>();
				for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
					topic.add(perTopicPro[topicIndex] / total);
				}
				term.add(wordIndex, topic);
			}
			if (docTermTopicPros.size() > docIndex) {
				docTermTopicPros.set(docIndex, term);
			} else {
				docTermTopicPros.add(docIndex, term);
			}
			term = null;
			topic = null;
		}

		// M-step
		/*
		 * update
		 * p(w|z),p(w|z)=sum(n(d',w)*p(z|d',w,&))/sum(sum(n(d',w')*p(z|d',w',&)))
		 * 
		 * d' represent all documents w' represent all vocabularies
		 */
		for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
			List<Double> termPros = topicTermPros.get(topicIndex);
			double totalDenominator = 0.0;
			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				double numerator = 0.0;
				for (int docIndex = 0; docIndex < docSize; docIndex++) {
					int num = docTermMatrix.get(docIndex).get(wordIndex) == null ? 0
							: docTermMatrix.get(docIndex).get(wordIndex);
					numerator += num
							* docTermTopicPros.get(docIndex).get(wordIndex)
									.get(topicIndex);
				}
				if (termPros.size() > wordIndex)
					termPros.set(wordIndex, numerator);
				else
					termPros.add(wordIndex, numerator);

				totalDenominator += numerator;
			}

			if (totalDenominator == 0.0) {
				totalDenominator = avoidZero(totalDenominator);
			}

			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				termPros.set(wordIndex, termPros.get(wordIndex)
						/ totalDenominator);
			}
			if (topicTermPros.size() > topicIndex) {
				topicTermPros.set(topicIndex, termPros);
			} else {
				topicTermPros.add(topicIndex, termPros);
			}
			termPros = null;
		}
		/*
		 * update
		 * p(z|d),p(z|d)=sum(n(d,w')*p(z|d,w'&))/sum(sum(n(d,w')*p(z'|d,w',&)))
		 * 
		 * w' represent all vocabularies z' represent all topics
		 */
		for (int docIndex = 0; docIndex < docSize; docIndex++) {
			// actually equal sum(w) of this doc
			List<Double> topicPros = docTopicPros.get(docIndex);
			double totalDenominator = 0.0;
			for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
				double numerator = 0.0;
				for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
					int num = docTermMatrix.get(docIndex).get(wordIndex) == null ? 0
							: docTermMatrix.get(docIndex).get(wordIndex);
					numerator += num
							* docTermTopicPros.get(docIndex).get(wordIndex)
									.get(topicIndex);
				}
				topicPros.set(topicIndex, numerator);
				totalDenominator += numerator;
			}

			if (totalDenominator == 0.0) {
				totalDenominator = avoidZero(totalDenominator);
			}

			for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
				topicPros.set(topicIndex, topicPros.get(topicIndex)
						/ totalDenominator);
			}
			topicPros = null;
		}
	}

	/**
	 * 
	 * Add New tweets via incremental plsa
	 * 
	 * @param docs
	 *            all documents
	 */
	public void addNewTweets(List<Tweet> docs, int maxIter) {
		if (docs == null) {
			throw new IllegalArgumentException(
					"The documents set must be not null!");
		}

		// Get the list of all current words
		List<String> currWords = utility.getAllWords(docs);

		// Merge current words with the previous words
		allWords = utility.mergeLists(allWords, currWords);

		// element represent times the word appear in this document
		// init docTermMatrix for new tweets
		for (int docIndex = 0; docIndex < docs.size(); docIndex++) {
			Tweet doc = docs.get(docIndex);
			Integer[] l = new Integer[allWords.size()];
			for (String word : doc.getWords()) {
				if (allWords.contains(word)) {
					int wordIndex = allWords.indexOf(word);
					l[wordIndex] = new Integer((l[wordIndex] == null ? 0
							: l[wordIndex].intValue()) + 1);
				}
			}
			docTermMatrix.add(Arrays.asList(l));

			// free memory
			l = null;
		}

		List<List<Double>> currDocTopicPros = new ArrayList<List<Double>>();
		// init p(z|d),for each tweet the constraint is sum(p(z|d))=1.0
		for (int i = 0; i < docs.size(); i++) {
			double[] pros = randomProbilities(topicNum);
			ArrayList<Double> prob = new ArrayList<Double>();
			for (int j = 0; j < topicNum; j++) {
				prob.add(pros[j]);
			}
			currDocTopicPros.add(i, prob);
		}
		docTopicPros.addAll(currDocTopicPros);
		// free memory
		currDocTopicPros = null;
		// randomize p(w|z) for new words,for each topic the constraint is
		// sum(p(w|z))=1.0
		for (int i = 0; i < topicNum; i++) {
			List<Double> pros = topicTermPros.get(i);
			int newWords = allWords.size() - vocabularySize;
			utility.updateTopicTermProb(pros, newWords);
			topicTermPros.set(i, pros);
			pros = null;
		}
		vocabularySize = allWords.size();

		// run incremental em to estimate params
		for (int i = 0; i < maxIter; i++) {
			emWithNewTweets(docs, docs.size(), currWords);
		}
		docSize += docs.size();
		System.out.println("done");
	}

	private void emWithNewTweets(List<Tweet> docs, int size,
			List<String> currWords) {
		/*
		 * E-step,
		 * 
		 * calculate posterior probability p(z|q,w), for all new tweets q & is
		 * model params(p(z|d),p(w|z))
		 * 
		 * p(z|q,w)=p(z|q)*p(w|z)/sum(p(z'|d)*p(w|z')) z' represent all posible
		 * topic
		 */
		List<List<Double>> term;
		List<Double> topic;

		for (int docIndex = docSize; docIndex < docSize + size; docIndex++) {
			term = new ArrayList<List<Double>>();
			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				double total = 0.0;
				double[] perTopicPro = new double[topicNum];
				for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
					double numerator = docTopicPros.get(docIndex).get(
							topicIndex)
							* topicTermPros.get(topicIndex).get(wordIndex);
					total += numerator;
					perTopicPro[topicIndex] = numerator;
				}

				if (total == 0.0) {
					total = avoidZero(total);
				}
				topic = new ArrayList<Double>();
				for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
					topic.add(perTopicPro[topicIndex] / total);
				}
				term.add(wordIndex, topic);
			}
			if (docTermTopicPros.size() > docIndex) {
				docTermTopicPros.set(docIndex, term);
			} else {
				docTermTopicPros.add(docIndex, term);
			}
			term = null;
			topic = null;
		}

		// M-step
		/*
		 * update
		 * p(w|z),p(w|z)=sum(n(q,w)*p(z|q,w,&))/sum(sum(n(q,w')*p(z|q,w')))
		 * 
		 * q represent all new tweets w' represent all vocabularies
		 */
		double alpha = 0.8;
		for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
			List<Double> termPros = topicTermPros.get(topicIndex);
			double totalDenominator = 0.0;
			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				double numerator = 0.0;
				for (int docIndex = docSize; docIndex < docSize + size; docIndex++) {
					if (!docs.get(docIndex - docSize).containsWord(
							allWords.get(wordIndex))) {
						continue;
					}
					int num = docTermMatrix.get(docIndex).get(wordIndex) == null ? 0
							: docTermMatrix.get(docIndex).get(wordIndex);
					numerator += num
							* docTermTopicPros.get(docIndex).get(wordIndex)
									.get(topicIndex);
				}
				double prev = alpha
						* (topicTermPros.get(topicIndex).get(wordIndex) == null ? 0
								: topicTermPros.get(topicIndex).get(wordIndex));
				if (termPros.size() > wordIndex)
					termPros.set(wordIndex, (1 - alpha) * numerator + prev);
				else
					termPros.add(wordIndex, (1 - alpha) * numerator + prev);

				totalDenominator += ((1 - alpha) * numerator + prev);
			}

			if (totalDenominator == 0.0) {
				totalDenominator = avoidZero(totalDenominator);
			}

			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				termPros.set(wordIndex, termPros.get(wordIndex)
						/ totalDenominator);
			}
			if (topicTermPros.size() > topicIndex) {
				topicTermPros.set(topicIndex, termPros);
			} else {
				topicTermPros.add(topicIndex, termPros);
			}
			termPros = null;
		}
		/*
		 * update
		 * p(z|q),p(z|q)=sum(n(q,w')*p(z|q,w'))/sum(sum(n(q,w')*p(z'|q,w')))
		 * 
		 * w' represent all vocabularies z' represent all topics, q represents
		 * new tweets
		 */
		for (int docIndex = docSize; docIndex < docSize + size; docIndex++) {
			// actually equal sum(w) of this doc
			List<Double> topicPros = docTopicPros.get(docIndex);
			double totalDenominator = 0.0;
			for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
				double numerator = 0.0;
				for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
					int num = docTermMatrix.get(docIndex).get(wordIndex) == null ? 0
							: docTermMatrix.get(docIndex).get(wordIndex);
					numerator += num
							* docTermTopicPros.get(docIndex).get(wordIndex)
									.get(topicIndex);
				}
				topicPros.set(topicIndex, numerator);
				totalDenominator += numerator;
			}

			if (totalDenominator == 0.0) {
				totalDenominator = avoidZero(totalDenominator);
			}

			for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
				topicPros.set(topicIndex, topicPros.get(topicIndex)
						/ totalDenominator);
			}
			topicPros = null;
		}
	}

	/**
	 * Prints top 10 words with highest beta value for each topic
	 * 
	 * @param beta
	 */

	public void getTopKBeta(List<List<Double>> beta) {
		Map<Integer, Double> map;
		StringBuilder sb = new StringBuilder();
		int TOPK = 10;
		// Get top 10 words according to their topic wise distribution
		for (int topicIndex = 0; topicIndex < topicNum; topicIndex++) {
			map = new HashMap<Integer, Double>();
			for (int wordIndex = 0; wordIndex < vocabularySize; wordIndex++) {
				map.put(wordIndex, beta.get(topicIndex).get(wordIndex));
			}

			map = sortByValue(map);

			sb.append("Topic index: ");
			sb.append(topicIndex + 1);
			sb.append("\n");
			int count = 1;
			for (int i : map.keySet()) {
				if (count > TOPK)
					break;
				sb.append(allWords.get(i));
				sb.append("\n");
				count++;
			}
			sb.append("\n");
			map = null;
		}
		System.out.print(sb.toString());
	}

	/**
	 * Gets topic for each document based on max theta value
	 * 
	 * @param theta
	 * @return
	 */
	public int[] getTopicPerDocument(List<List<Double>> theta) {
		int[] output = new int[docSize];
		for (int docIndex = 0; docIndex < docSize; docIndex++) {
			double max = theta.get(docIndex).get(0);
			int maxIndex = 0;
			for (int topicIndex = 1; topicIndex < topicNum; topicIndex++) {
				if (Double.compare(max, theta.get(docIndex).get(topicIndex)) < 0) {
					max = theta.get(docIndex).get(topicIndex);
					maxIndex = topicIndex;
				}
			}
			output[docIndex] = maxIndex;
		}

		return output;
	}

	/**
	 * Gets Number of Documents for each Topic based on Max theta value
	 * 
	 * @param theta
	 * @return
	 */
	public String getDocumentsPerTopic(List<List<Double>> theta) {
		int[] output = new int[topicNum];
		for (int docIndex = 0; docIndex < docSize; docIndex++) {
			double max = theta.get(docIndex).get(0);
			int maxIndex = 0;
			for (int topicIndex = 1; topicIndex < topicNum; topicIndex++) {
				if (Double.compare(max, theta.get(docIndex).get(topicIndex)) < 0) {
					max = theta.get(docIndex).get(topicIndex);
					maxIndex = topicIndex;
				}
			}
			output[maxIndex] += 1;
		}

		String popular = getTopKPopular(output);

		updateClusterHourCount(output);
		output = null;
		return popular;
	}

	/**
	 * Get top 3 popular clusters from the list based on value in output
	 * 
	 * @param output
	 * @return
	 */
	private String getTopKPopular(int[] output) {
		PriorityQueue<TopicValue> pq = new PriorityQueue<TopicValue>();
		for (int topic = 0; topic < topicNum; topic++) {
			if (clusterTimeCount.size() > 0) {
				int lastIndex = clusterTimeCount.get(1).size();
				int maxIndexCount = clusterTimeCount.get(topic + 1).get(
						lastIndex - 1);
				int diff = (output[topic] - maxIndexCount);
				if (pq.size() < 3) {
					pq.add(new TopicValue(topic + 1, diff));
				} else {
					TopicValue tv = pq.peek();
					if (tv.count < diff) {
						pq.poll();
						pq.add(new TopicValue(topic + 1, diff));
					}
				}
			} else {
				int diff = output[topic];
				if (pq.size() < 3) {
					pq.add(new TopicValue(topic + 1, diff));
				} else {
					TopicValue tv = pq.peek();
					if (tv.count < diff) {
						pq.poll();
						pq.add(new TopicValue(topic + 1, diff));
					}
				}
			}
		}
		int topic3 = pq.poll().topic;
		int topic2 = pq.poll().topic;
		int topic1 = pq.poll().topic;

		return topic1 + "," + topic2 + "," + topic3;
	}

	/**
	 * Updates the document count for each cluster at each time window
	 * 
	 * @param output
	 */
	private void updateClusterHourCount(int[] output) {
		for (int i = 0; i < output.length; i++) {
			if (clusterTimeCount.containsKey(i + 1)) {
				List<Integer> counts = clusterTimeCount.get(i + 1);
				counts.add(output[i]);
			} else {
				List<Integer> counts = new ArrayList<Integer>();
				counts.add(output[i]);
				clusterTimeCount.put(i + 1, counts);
			}
		}
	}

	/**
	 * Read all the words in tweets to create vocabulary
	 * 
	 * @param docs
	 * @return
	 */
	private List<String> statisticsVocabularies(List<Tweet> docs) {
		Set<String> uniqWords = new HashSet<String>();
		for (Tweet doc : docs) {
			for (String word : doc.getWords()) {
				if (!uniqWords.contains(word)) {
					uniqWords.add(word);
				}
			}
			docSize++;
		}
		vocabularySize = uniqWords.size();

		return new LinkedList<String>(uniqWords);
	}

	/**
	 * 
	 * 
	 * Get a normalize array
	 * 
	 * @param size
	 * @return
	 */
	public double[] randomProbilities(int size) {
		if (size < 1) {
			throw new IllegalArgumentException(
					"The size param must be greate than zero");
		}
		double[] pros = new double[size];

		int total = 0;
		Random r = new Random();
		for (int i = 0; i < pros.length; i++) {
			// avoid zero
			pros[i] = r.nextInt(size) + 1;

			total += pros[i];
		}

		// normalize
		for (int i = 0; i < pros.length; i++) {
			pros[i] = pros[i] / total;
		}

		return pros;
	}

	/**
	 * 
	 * @return
	 */
	public List<List<Double>> getDocTopics() {
		return docTopicPros;
	}

	/**
	 * 
	 * @return
	 */
	public List<List<Double>> getTopicWordPros() {
		return topicTermPros;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getAllWords() {
		return allWords;
	}

	/**
	 * 
	 * Get topic number
	 * 
	 * 
	 * @return
	 */
	public Integer getTopicNum() {
		return topicNum;
	}

	/**
	 * 
	 * Get count to tweets in cluster at each time
	 * 
	 * 
	 * @return
	 */
	public Map<Integer, List<Integer>> getClusterTimeCount() {
		return clusterTimeCount;
	}

	/**
	 * 
	 * Get p(w|z)
	 * 
	 * @param word
	 * @return
	 */
	public double[] getTopicWordPros(String word) {
		int index = allWords.indexOf(word);
		if (index != -1) {
			double[] topicWordPros = new double[topicNum];
			for (int i = 0; i < topicNum; i++) {
				topicWordPros[i] = topicTermPros.get(i).get(index);
			}
			return topicWordPros;
		}

		return null;
	}

	/**
	 * Function to sort a map by value
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 
	 * avoid zero number.if input number is zero, we will return a magic number.
	 * 
	 * 
	 */
	private final static double MAGICNUM = 0.0000000000000001;

	public double avoidZero(double num) {
		if (num == 0.0) {
			return MAGICNUM;
		}

		return num;
	}

	/**
	 * Read the original labels for tweets to calculate NMI
	 * 
	 * @param confLabelFile
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void readConfLabel(String confLabelFile)
			throws NumberFormatException, IOException {
		String line;
		BufferedReader br = new BufferedReader(new FileReader(confLabelFile));

		givenLabel = new int[docSize];
		int docIndex = 0;
		while ((line = br.readLine()) != null) {
			givenLabel[docIndex] = Integer.parseInt(line.split("\t")[1]) - 1;
			docIndex++;
		}

		br.close();
	}

	/**
	 * Evaluate the quality of the clusters for Tweets
	 * 
	 * @param theta
	 */
	public void evaluation(List<List<Double>> theta) {

		int[] algoOutput = getTopicPerDocument(theta);
		double[] wk = new double[topicNum];
		double[] cj = new double[topicNum];
		double[][] wkcj = new double[topicNum][topicNum];

		for (int docIndex = 0; docIndex < docSize; docIndex++) {
			wk[givenLabel[docIndex]] += 1;
			cj[algoOutput[docIndex]] += 1;
			wkcj[givenLabel[docIndex]][algoOutput[docIndex]] += 1;
		}

		System.out.println("Purity is : "
				+ utility.calculatePurity(wkcj, docSize));
		System.out.println("NMI is : "
				+ utility.calculateNMI(wkcj, docSize, topicNum, wk, cj));
	}
}