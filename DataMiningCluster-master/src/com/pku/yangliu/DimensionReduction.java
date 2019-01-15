package com.pku.yangliu;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**����LSI���ĵ���������������ά��SVD�������JAMA���������ʵ��
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class DimensionReduction {

	/**�Ѳ���������mapת�����ĵ������Ծ���
	 * @param Map<String, Map<String, Double>> allTestSampleMap ���в���������<�ļ���������>���ɵ�map
	 * @param String[] terms �����ʼ���
	 * @return double[][] doc-doc�����Ծ���
	 * @throws IOException 
	 */
	public double[][] getSimilarityMatrix(
			Map<String, Map<String, Double>> allTestSampleMap, String[] terms) {
		// TODO Auto-generated method stub
		System.out.println("Begin compute docTermMatrix!");
		int i = 0;
		double [][] docTermMatrix = new double[allTestSampleMap.size()][terms.length];
		Set<Map.Entry<String, Map<String,Double>>> allTestSampleMapSet = allTestSampleMap.entrySet();
		for(Iterator<Map.Entry<String, Map<String,Double>>> it = allTestSampleMapSet.iterator();it.hasNext();){
			Map.Entry<String, Map<String,Double>> me = it.next();	
			for(int j = 0; j < terms.length; j++){
				if(me.getValue().containsKey(terms[j])){
					docTermMatrix[i][j] = me.getValue().get(terms[j]);
				}
				else {
					docTermMatrix[i][j] =0;
				}
			}
			i++;	
		}
	    double[][] similarityMatrix = couputeSimilarityMatrix(docTermMatrix);
		return similarityMatrix;
	}

	/**����docTermMatrix���������Ծ���
	 * @param double[][] docTermMatrix doc-term����
	 * @return double[][] doc-doc�����Ծ���
	 * @throws IOException 
	 */
	private double[][] couputeSimilarityMatrix(double[][] docTermMatrix) {
		// TODO Auto-generated method stub
		System.out.println("Compute docTermMatrix done! begin compute SVD");
		Matrix docTermM = new Matrix(docTermMatrix);
		SingularValueDecomposition s = docTermM.transpose().svd();
		System.out.println(" Compute SVD done!");
		//A*A' = D*S*S'*D'   �����doc-term����
		//A'*A = D*S'*S*D'   �����term-doc����
		//ע��svd����ֻ�ʺ��������������ľ����������С���������ɶ���ת�þ�����SVD�ֽ�
		Matrix D = s.getU();
		Matrix S = s.getS();
		for(int i = 100; i < S.getRowDimension(); i++){//����100ά
			S.set(i, i, 0);
		}
		System.out.println("Compute SimilarityMatrix done!");
		return D.times(S.transpose().times(S.times(D.transpose()))).getArray();
	}
}
