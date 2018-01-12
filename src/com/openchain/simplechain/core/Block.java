package com.openchain.simplechain.core;

import java.util.Date;

import com.openchain.simplechain.util.StringUtil;

public class Block {

	public String hash;			/* 해시값 */
	public String previousHash;	/* 이전 블럭의 해시값 */
	private String data; 		/* 블럭의 data */
	private long timestamp; 	/* timestamp */

	private int nonce;
	
	/**
	 * 새로운 블럭을 생성합니다.
	 * 
	 * @param data
	 * @param previousHash
	 */
	public Block(String data, String previousHash ) {
		this.data = data;
		this.previousHash = previousHash;
		this.timestamp = new Date().getTime();
		this.hash = calculateHash();	//생성시 먼저 hash 값을 하나 만들어 넣어둡니다.
	}
	
	/**
	 * 새로운 해시값을 생성합니다.
	 * @return
	 */
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timestamp) +
				Integer.toString(nonce) + 
				data 
				);
		return calculatedhash;
	}
	
	/**
	 * 채굴합니다.
	 * 
	 * @param difficulty
	 */
	public void mineBlock(int difficulty) {
		//간단한 테스트와 이해를 돕기위해 target을 difficulty 숫자 만큼 앞에 0으로 채웁니다.
		String target = new String(new char[difficulty]).replace('\0', '0');
		
		//생성된 hash가 target과 동일하면 채굴 성공입니다.
		//ex) difficulty가 3이면 target은 000이 되고, 생성된 hash가 000으로 시작하는 값이면 채굴 성공입니다.
		//  채굴된 모든 hash가 000으로 시작하겠죠...ㅋㅋ
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
			
			System.out.printf("\nGEN Hash #%d : %s", nonce, hash);
		}
		System.out.println("\n채굴 성공!!! : " + hash);
	}
}