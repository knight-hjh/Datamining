package com.pku.yangliu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**KNN�㷨��ʵ���࣬�������������н����Ҽ������ƶ�
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */

public class KNNClassifier {
	
	/**��KNN�㷨�Բ����ĵ�������,��ȡ����������ѵ��������
	 * @param trainFiles ѵ�������������������ɵ��ļ�
	 * @param testFiles ���������������������ɵ��ļ�
	 * @param kNNResultFile KNN�������ļ�·��
	 * @return double ����׼ȷ��
	 * @throws IOException 
	 */
	private double doProcess(String trainFiles, String testFiles,
			String kNNResultFile) throws IOException {
		// TODO Auto-generated method stub
		//���ȶ�ȡѵ�������Ͳ�����������map<String,map<word,TF>>������Լ���ѵ������ע��ѵ����������Ŀ��ϢҲ�ñ��棬
		//Ȼ�������������������ÿһ����������ȥ������������ѵ�����������ƶȣ����ƶȱ�����map<String,double>��
		//��map��ȥ��Ȼ��ȡǰK�������������k����������������������Ŀ����Ȩ�ص÷֣�������ͬһ����Ŀ��Ȩ����ͽ����õ�
		//���÷ֵ���Ŀ���Ϳ����жϲ����������ڸ���Ŀ�£�Kֵ���Է������ԣ��ҵ�����׼ȷ����ߵ��Ǹ�ֵ
		//��ע��Ҫ��"��Ŀ_�ļ���"��Ϊÿ���ļ���key�����ܱ���ͬ����ͬ���ݵ��ļ�����
		//��ע������JM��������������JAVA heap�������
		//���������������н����Ҽ������ƶ�
		File trainSamples = new File(trainFiles);
		BufferedReader trainSamplesBR = new BufferedReader(new FileReader(trainSamples));
		String line;
		String [] lineSplitBlock;
		Map<String,TreeMap<String,Double>> trainFileNameWordTFMap = new TreeMap<String,TreeMap<String,Double>> ();
		TreeMap<String,Double> trainWordTFMap = new TreeMap<String,Double>();
		while((line = trainSamplesBR.readLine()) != null){
			lineSplitBlock = line.split(" ");
			trainWordTFMap.clear();
			for(int i = 2; i < lineSplitBlock.length; i = i + 2){
				trainWordTFMap.put(lineSplitBlock[i], Double.valueOf(lineSplitBlock[i+1]));
			}
			TreeMap<String,Double> tempMap = new TreeMap<String,Double>();
			tempMap.putAll(trainWordTFMap);
			trainFileNameWordTFMap.put(lineSplitBlock[0]+"_"+lineSplitBlock[1], tempMap);
		}
		trainSamplesBR.close();
		
		File testSamples = new File(testFiles);
		BufferedReader testSamplesBR = new BufferedReader(new FileReader(testSamples));
		Map<String,Map<String,Double>> testFileNameWordTFMap = new TreeMap<String,Map<String,Double>> ();
		Map<String,String> testClassifyCateMap = new TreeMap<String, String>();//�����γɵ�<�ļ�������Ŀ>��
		Map<String,Double> testWordTFMap = new TreeMap<String,Double>();
		while((line = testSamplesBR.readLine()) != null){
			lineSplitBlock = line.split(" ");
			testWordTFMap.clear();
			for(int i = 2; i < lineSplitBlock.length; i = i + 2){
				testWordTFMap.put(lineSplitBlock[i], Double.valueOf(lineSplitBlock[i+1]));
			}
			TreeMap<String,Double> tempMap = new TreeMap<String,Double>();
			tempMap.putAll(testWordTFMap);
			testFileNameWordTFMap.put(lineSplitBlock[0]+"_"+lineSplitBlock[1], tempMap);
		}
		testSamplesBR.close();
		//�������ÿһ��������������������ѵ�������ľ��룬������
		String classifyResult;
		FileWriter testYangliuWriter = new FileWriter(new File("F:/DataMiningSample/docVector/yangliuTest"));
		FileWriter KNNClassifyResWriter = new FileWriter(kNNResultFile);
		Set<Map.Entry<String,Map<String,Double>>> testFileNameWordTFMapSet = testFileNameWordTFMap.entrySet();
		for(Iterator<Map.Entry<String,Map<String,Double>>> it = testFileNameWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Map<String,Double>> me = it.next();
			classifyResult = KNNComputeCate(me.getKey(), me.getValue(), trainFileNameWordTFMap, testYangliuWriter);
			KNNClassifyResWriter.append(me.getKey()+" "+classifyResult+"\n");
			KNNClassifyResWriter.flush();
			testClassifyCateMap.put(me.getKey(), classifyResult);
		}
		KNNClassifyResWriter.close();
		//��������׼ȷ��
		double righteCount = 0;
		Set<Map.Entry<String, String>> testClassifyCateMapSet = testClassifyCateMap.entrySet();
		for(Iterator <Map.Entry<String, String>> it = testClassifyCateMapSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			String rightCate = me.getKey().split("_")[0];
			if(me.getValue().equals(rightCate)){
				righteCount++;
			}
		}	
		testYangliuWriter.close();
		return righteCount / testClassifyCateMap.size();
	}
	
	/**����ÿһ����������ȥ������������ѵ�������������н��������ƶ�
	 * ���ƶȱ�����map<String,double>����map��ȥ��Ȼ��ȡǰK��������
	 * �����k����������������������Ŀ����Ȩ�ص÷֣�������ͬһ����
	 * Ŀ��Ȩ����ͽ����õ����÷ֵ���Ŀ���Ϳ����жϲ����������ڸ�
	 * ��Ŀ�¡�Kֵ���Է������ԣ��ҵ�����׼ȷ����ߵ��Ǹ�ֵ
	 * @param testWordTFMap ��ǰ�����ļ���<����,��Ƶ>����
	 * @param trainFileNameWordTFMap ѵ������<��Ŀ_�ļ���,����>Map
	 * @param testYangliuWriter 
	 * @return String K���ھ�Ȩ�ص÷�������Ŀ
	 * @throws IOException 
	 */
	private String KNNComputeCate(
			String testFileName,
			Map<String, Double> testWordTFMap,
			Map<String, TreeMap<String, Double>> trainFileNameWordTFMap, FileWriter testYangliuWriter) throws IOException {
		// TODO Auto-generated method stub
		HashMap<String,Double> simMap = new HashMap<String,Double>();//<��Ŀ_�ļ���,����> ������Ҫ����HashMap����value����
		double similarity;
		Set<Map.Entry<String,TreeMap<String,Double>>> trainFileNameWordTFMapSet = trainFileNameWordTFMap.entrySet();
		for(Iterator<Map.Entry<String,TreeMap<String,Double>>> it = trainFileNameWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, TreeMap<String,Double>> me = it.next();
			similarity = computeSim(testWordTFMap, me.getValue());
			simMap.put(me.getKey(),similarity);
		}
		//�����simMap����value����
		ByValueComparator bvc = new ByValueComparator(simMap);
		TreeMap<String,Double> sortedSimMap = new TreeMap<String,Double>(bvc);
		sortedSimMap.putAll(simMap);
		
		//��disMap��ȡǰK�������ѵ�������������������֮�ͣ�K��ֵͨ�������������
		Map<String,Double> cateSimMap = new TreeMap<String,Double>();//K�����ѵ������������Ŀ�ľ���֮��
		double K = 20;
		double count = 0;
		double tempSim;
		
		Set<Map.Entry<String, Double>> simMapSet = sortedSimMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = simMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			count++;
			String categoryName = me.getKey().split("_")[0];
			if(cateSimMap.containsKey(categoryName)){
				tempSim = cateSimMap.get(categoryName);
				cateSimMap.put(categoryName, tempSim + me.getValue());
			}
			else cateSimMap.put(categoryName, me.getValue());
			if (count > K) break;
		}
		//���浽cateSimMap�����sim�����Ǹ���Ŀ�����ҳ���
		//testYangliuWriter.flush();
		//testYangliuWriter.close();
		double maxSim = 0;
		String bestCate = null;
		Set<Map.Entry<String, Double>> cateSimMapSet = cateSimMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = cateSimMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(me.getValue()> maxSim){
				bestCate = me.getKey();
				maxSim = me.getValue();
			}
		}
		return bestCate;
	}

	/**�����������������ѵ���������������ƶ�
	 * @param testWordTFMap ��ǰ�����ļ���<����,��Ƶ>����
	 * @param trainWordTFMap ��ǰѵ������<����,��Ƶ>����
	 * @return Double ����֮������ƶ� �������н����Ҽ���
	 * @throws IOException 
	 */
	private double computeSim(Map<String, Double> testWordTFMap,
			Map<String, Double> trainWordTFMap) {
		// TODO Auto-generated method stub
		double mul = 0, testAbs = 0, trainAbs = 0;
		Set<Map.Entry<String, Double>> testWordTFMapSet = testWordTFMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = testWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(trainWordTFMap.containsKey(me.getKey())){
				mul += me.getValue()*trainWordTFMap.get(me.getKey());
			}
			testAbs += me.getValue() * me.getValue();
		}
		testAbs = Math.sqrt(testAbs);
		
		Set<Map.Entry<String, Double>> trainWordTFMapSet = trainWordTFMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> it = trainWordTFMapSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			trainAbs += me.getValue()*me.getValue();
		}
		trainAbs = Math.sqrt(trainAbs);
		return mul / (testAbs * trainAbs);
	}

	/**����KNN�㷨�������ļ�������ȷ��Ŀ�ļ�������ȷ�ʺͻ�������ļ�����Ը��ñ�Ҷ˹�㷨���еķ���
	 * @param kNNRightFile ������ȷ��Ŀ�ļ�
	 * @param kNNResultFile �������ļ�
	 * @throws IOException 
	 */
	private void createRightFile(String kNNResultFile, String kNNRightFile) throws IOException {
		// TODO Auto-generated method stub
		String rightCate;
		FileReader fileR = new FileReader(kNNResultFile);
		FileWriter KNNRrightResult = new FileWriter(new File(kNNRightFile));
		BufferedReader fileBR = new BufferedReader(fileR);
		String line;
		String lineBlock[];
		while((line = fileBR.readLine()) != null){
			lineBlock = line.split(" ");
			rightCate = lineBlock[0].split("_")[0];
			KNNRrightResult.append(lineBlock[0]+" "+rightCate+"\n");
		}
		KNNRrightResult.flush();
		KNNRrightResult.close();
	}
		
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public void KNNClassifierMain(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//wordMap���������ԴʵĴʵ�<���ʣ��������ĵ��г��ֵĴ���>
		double[] accuracyOfEveryExp = new double[10];
		double accuracyAvg,sum = 0;
		KNNClassifier knnClassifier = new KNNClassifier();
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		Map<String,Double> wordMap = new TreeMap<String,Double>();
		Map<String,Double> IDFPerWordMap = new TreeMap<String,Double>();	
		ComputeWordsVector computeWV = new ComputeWordsVector();
		wordMap = computeWV.countWords("F:/DataMiningSample/processedSample_includeNotSpecial", wordMap);
		IDFPerWordMap = computeWV.computeIDF("F:/DataMiningSample/processedSampleOnlySpecial",wordMap);
		computeWV.printWordMap(wordMap);
		//��������KNN�㷨10��������Ҫ���ĵ�TF�����ļ�
		for(int i = 0; i < 10; i++){
			computeWV.computeTFMultiIDF("F:/DataMiningSample/processedSampleOnlySpecial",0.9, i, IDFPerWordMap,wordMap);
			String trainFiles = "F:/DataMiningSample/docVector/wordTFIDFMapTrainSample"+i;
			String testFiles = "F:/DataMiningSample/docVector/wordTFIDFMapTestSample"+i;
			String kNNResultFile = "F:/DataMiningSample/docVector/KNNClassifyResult"+i;
			String kNNRightFile = "F:/DataMiningSample/docVector/KNNClassifyRight"+i;
			accuracyOfEveryExp[i] = knnClassifier.doProcess(trainFiles, testFiles, kNNResultFile);
			knnClassifier.createRightFile(kNNResultFile,kNNRightFile);
			accuracyOfEveryExp[i] = nbClassifier.computeAccuracy(kNNResultFile, kNNRightFile);//����׼ȷ�ʸ��ñ�Ҷ˹�㷨�еķ���
			sum += accuracyOfEveryExp[i];
			System.out.println("The accuracy for KNN Classifier in "+i+"th Exp is :" + accuracyOfEveryExp[i]);
		}
		accuracyAvg = sum / 10;
		System.out.println("The average accuracy for KNN Classifier in all Exps is :" + accuracyAvg);
	}
	
	//��HashMap����value������
	static class ByValueComparator implements Comparator<Object> {
		HashMap<String, Double> base_map;

		public ByValueComparator(HashMap<String, Double> disMap) {
			this.base_map = disMap;
		}
		
		@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			String arg0 = o1.toString();
			String arg1 = o2.toString();
			if (!base_map.containsKey(arg0) || !base_map.containsKey(arg1)) {
				return 0;
			}
			if (base_map.get(arg0) < base_map.get(arg1)) {
				return 1;
			} else if (base_map.get(arg0) == base_map.get(arg1)) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
