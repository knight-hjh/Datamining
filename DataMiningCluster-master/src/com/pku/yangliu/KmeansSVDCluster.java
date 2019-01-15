package com.pku.yangliu;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.lang.Integer;


/**Kmeans�����㷨��ʵ���࣬��newsgroups�ĵ����۳�10�ࡢ20�ࡢ30��,����SVD�ֽ�
 * �㷨��������:��ÿ��������ľ������ĵ�����������ľ������ĵ�ʱ���㷨����
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */

public class KmeansSVDCluster {
	
	/**Kmeans�㷨������
	 * @param Map<String, Map<String, Double>> allTestSampleMap ���в���������<�ļ���������>���ɵ�map
	 * @param double [][] docSimilarityMatrix �ĵ����ĵ��������Ծ��� [i,j]Ϊ�ĵ�i���ĵ�j�������Զ���
	 * @param int K ���������
	 * @return Map<String,Integer> ����Ľ��  ��<�ļ�����������ɺ������������>
	 * @throws IOException 
	 */
	private Map<String, Integer> doProcess(
			Map<String, Map<String, Double>> allTestSampleMap, double[][] docSimilarityMatrix, int K) {
		// TODO Auto-generated method stub
		//0�����Ȼ�ȡallTestSampleMap�����ļ���˳����ɵ�����
		String[] testSampleNames = new String[allTestSampleMap.size()];
		int count = 0, tsLength = allTestSampleMap.size();
		Set<Map.Entry<String, Map<String, Double>>> allTestSampeleMapSet = allTestSampleMap.entrySet();
		for(Iterator<Map.Entry<String, Map<String, Double>>> it = allTestSampeleMapSet.iterator(); it.hasNext(); ){
			Map.Entry<String, Map<String, Double>> me = it.next();
			testSampleNames[count++] = me.getKey();
		}
		//1����ʼ���ѡ���㷨�����ѡ������Ǿ��ȷֿ�ѡ��������ú���
		Map<Integer, double[]> meansMap = getInitPoint(testSampleNames, docSimilarityMatrix, K);//����K�����ĵ�
		//2����ʼ��K������
		int [] assignMeans = new int[tsLength];//��¼���е����ڵľ�����ţ���ʼ��ȫ��Ϊ0
		Map<Integer, Vector<Integer>> clusterMember = new TreeMap<Integer,Vector<Integer>>();//��¼ÿ������ĳ�Ա�����
		Vector<Integer> mem = new Vector<Integer>();
		int iterNum = 0;//��������
		while(true){
			System.out.println("Iteration No." + (iterNum++) + "----------------------");
			//3���ҳ�ÿ��������ľ�������
			int[] nearestMeans = new int[tsLength];
			for(int i = 0; i < tsLength; i++){
				nearestMeans[i] = findNearestMeans(meansMap, i);
			}
			//4���жϵ�ǰ���е����ڵľ�������Ƿ��Ѿ�ȫ�������������ľ��࣬����ǻ��ߴﵽ���ĵ�����������ô�����㷨
			int okCount = 0;
			for(int i = 0; i <tsLength; i++){
				if(nearestMeans[i] == assignMeans[i]) okCount++;
			}
			System.out.println("okCount = " + okCount);
			if(okCount == tsLength || iterNum >= 25) break;//����������1000��
			//5�����ǰ�����������㣬��ô��Ҫ���¾����ٽ���һ�ε�������Ҫ�޸�ÿ������ĳ�Ա��ÿ�������ڵľ�����Ϣ
			clusterMember.clear();
			for(int i = 0; i < tsLength; i++){
				assignMeans[i] = nearestMeans[i];
				if(clusterMember.containsKey(nearestMeans[i])){
					clusterMember.get(nearestMeans[i]).add(i);	
				}
				else {
					mem.clear();
					mem.add(i);
					Vector<Integer> tempMem = new Vector<Integer>();
					tempMem.addAll(mem);
					clusterMember.put(nearestMeans[i], tempMem);
				}
			}
			//6�����¼���ÿ����������ĵ�
			for(int i = 0; i < K; i++){
				if(!clusterMember.containsKey(i)){//ע��kmeans���ܲ����վ���
					continue;
				}
				double[] newMean = computeNewMean(clusterMember.get(i), docSimilarityMatrix);
				meansMap.put(i, newMean);
			}
		}
		
		//7���γɾ��������ҷ���
		Map<String, Integer> resMap = new TreeMap<String, Integer>();
		for(int i = 0; i < tsLength; i++){
			resMap.put(testSampleNames[i], assignMeans[i]);
		}
		return resMap;
	}

	/**�����µľ���������ÿ���ĵ������ƶ�
	 * @param clusterM �þ�������������ĵ������
	 * @param double [][] docSimilarityMatrix �ĵ�֮������ƶȾ���
	 * @return double[] �µľ���������ÿ���ĵ������ƶ�
	 * @throws IOException 
	 */
	private double[] computeNewMean(Vector<Integer> clusterM,
			double [][] docSimilarityMatrix) {
		// TODO Auto-generated method stub
		double sim;
		double [] newMean = new double[docSimilarityMatrix.length];
		double memberNum = (double)clusterM.size();
		for(int i = 0; i < docSimilarityMatrix.length; i++){
			sim = 0;
			for(Iterator<Integer> it = clusterM.iterator(); it.hasNext();){
				sim += docSimilarityMatrix[it.next()][i];
			}
			newMean[i] = sim / memberNum;
		}
		return newMean;
	}

	/**�ҳ����뵱ǰ������ľ�������
	 * @param Map<Integer, double[]> meansMap ���ĵ�Map valueΪ���ĵ��ÿ���ĵ������ƶ�
	 * @param int m
	 * @return i ����ľ������ĵ��� ��
	 * @throws IOException 
	 */
	private int findNearestMeans(Map<Integer, double[]> meansMap ,int m) {
		// TODO Auto-generated method stub
		double maxSim = 0;
		int j = -1;
		double[] simArray;
		Set<Map.Entry<Integer, double[]>> meansMapSet = meansMap.entrySet();
		for(Iterator<Map.Entry<Integer, double[]>> it = meansMapSet.iterator(); it.hasNext();){
			Map.Entry<Integer, double[]> me = it.next();
			simArray = me.getValue();
			if(maxSim < simArray[m]){
				maxSim = simArray[m];
				j = me.getKey();
			}
		}
		return j;
	}

	/**��ȡkmeans�㷨�����ĳ�ʼ��
	 * @param k ���������
	 * @param String[] testSampleNames ���������ļ�������
	 * @param double[][] docSimilarityMatrix �ĵ������Ծ���
	 * @return Map<Integer, double[]> ��ʼ���ĵ����� key�����ţ�valueΪ�����������ĵ������ƶ�����
	 * @throws IOException 
	 */
	private Map<Integer, double[]> getInitPoint(String[] testSampleNames, double[][] docSimilarityMatrix, int K) {
		// TODO Auto-generated method stub
		int i = 0;
		Map<Integer, double[]> meansMap = new TreeMap<Integer, double[]>();//����K���������ĵ�����
		System.out.println("���ξ���ĳ�ʼ���Ӧ���ļ�Ϊ��");
		for(int count = 0; count < testSampleNames.length; count++){
			if(count == i * testSampleNames.length / K){
				meansMap.put(i, docSimilarityMatrix[count]);
				System.out.println(testSampleNames[count]);
				i++;
			}
		}
		return meansMap;
	}

	/**������������ļ���
	 * @param kmeansClusterResultFile ����ļ�Ŀ¼
	 * @param kmeansClusterResult ������
	 * @throws IOException 
	 */
	private void printClusterResult(Map<String, Integer> kmeansClusterResult, String kmeansClusterResultFile) throws IOException {
		// TODO Auto-generated method stub
		FileWriter resWriter = new FileWriter(kmeansClusterResultFile);
		Set<Map.Entry<String,Integer>> kmeansClusterResultSet = kmeansClusterResult.entrySet();
		for(Iterator<Map.Entry<String,Integer>> it = kmeansClusterResultSet.iterator(); it.hasNext(); ){
			Map.Entry<String, Integer> me = it.next();
			resWriter.append(me.getKey() + " " + me.getValue() + "\n");
		}
		resWriter.flush();
		resWriter.close();
	}
	
	/**Kmeans�㷨
	 * @param String testSampleDir ��������Ŀ¼
	 * @param String[] term ����������
	 * @throws IOException 
	 */
	public void KmeansClusterMain(String testSampleDir, String[] terms) throws IOException {
		//���ȼ����ĵ�TF-IDF����������ΪMap<String,Map<String,Double>> ��ΪMap<�ļ�����Map<�����ʣ�TF-IDFֵ>>
		ComputeWordsVector computeV = new ComputeWordsVector();
		DimensionReduction dimReduce = new DimensionReduction();
		int[] K = {10, 20, 30};
		Map<String,Map<String,Double>> allTestSampleMap = computeV.computeTFMultiIDF(testSampleDir);
		//����allTestSampleMap����һ��doc*term����Ȼ����SVD�ֽ�
		double[][] docSimilarityMatrix = dimReduce.getSimilarityMatrix(allTestSampleMap, terms);
		for(int i = 0; i < K.length; i++){
			System.out.println("��ʼ���࣬�۳�" + K[i] + "��");
			String KmeansClusterResultFile = "F:/DataMiningSample/KmeansClusterResult/";
			Map<String,Integer> KmeansClusterResult = new TreeMap<String, Integer>();
			KmeansClusterResult = doProcess(allTestSampleMap, docSimilarityMatrix, K[i]);
			KmeansClusterResultFile += K[i];
			printClusterResult(KmeansClusterResult,KmeansClusterResultFile);
			System.out.println("The Entropy for this Cluster is " + computeV.evaluateClusterRes(KmeansClusterResultFile, K[i]));
		}
	}
}

