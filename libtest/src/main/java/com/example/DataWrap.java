package com.example;

public class DataWrap implements Comparable<DataWrap>{
	int data;
	String flag;
	public DataWrap(int data,String flag){
		this.data = data;
		this.flag = flag;
	}
	public String toString(){
		return data + flag;
	}
	//����dataʵ����������������dataWrap�Ĵ�С
	@Override
	public int compareTo(DataWrap dw) {
		return this.data > dw.data? 1 : (this.data == dw.data? 0 : -1);
	}
	
}
