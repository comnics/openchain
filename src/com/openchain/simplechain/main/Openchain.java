package com.openchain.simplechain.main;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;
import com.openchain.simplechain.core.Block;
import com.openchain.simplechain.core.Transaction;
import com.openchain.simplechain.core.TransactionOutput;
import com.openchain.simplechain.core.Wallet;
import com.openchain.simplechain.util.StringUtil;

/**
 * 블럭체인의 간단한 구현으로 이해를 돕기 위해 목적으로 구성하고 있습니다.
 * 
 * @author comnic
 *
 */
public class Openchain {

	//blockchain ArrayList
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); //list of all unspent transactions.
	
	//difficulty - 숫자가 클수록 어렵다. target생성 규칙상.
	//3이상은 좀 오래걸리는 듯 합니다. 초기에 3으로 하시고 숫자를 조절해 보시면 이해가 되실듯 합니다.^^
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	
	public static Wallet walletA;
	public static Wallet walletB;
	
	/**
	 * main
	 * @param arg
	 */
	public static void main(String[] arg){

		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		//Create the new wallets
		walletA = new Wallet();
		walletB = new Wallet();

		walletA.generateKeyPair();
		walletB.generateKeyPair();
		
		//Test public and private keys
//		System.out.println("Private and public keys:");
//		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
//		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		//테스트를 위한 Transaction생성(WalletA -> walletB : 100)  
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 100, null);
		//생성된 Transaction에 서명합니다.
		transaction.generateSignature(walletA.privateKey);
		
		//서명한 Transaction을 검증합니다.
		System.out.println("Is this Transaction Verify? " + transaction.verifiySignature());
		
		/*
		//초기 블럭을 만듭니다.
		blockchain.add(new Block("Genesis block", "0"));
		System.out.println("\nTrying to Mine Genesis block!");
		blockchain.get(0).mineBlock(difficulty);
		
		//이후 블럭을 생성합니다.
		for(int i = 1 ; i <= 10 ; i++){
			blockchain.add(new Block("block " + i, blockchain.get(blockchain.size()-1).hash));
			System.out.printf("\nTrying to Mine block #%d", i+1 );
			blockchain.get(i).mineBlock(difficulty);
		}
		
		//전체 blockchain이 정상인지 체크합니다.
		System.out.println("\nBlockchain is Valid : " + isChainValid());
		
		//전체 블럭을 출력합니다.
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nOpenchain Block list : ");
		System.out.println(blockchainJson);
		*/
	}
	
	/**
	 * blockchain이 유효한지 체크합니다.
	 *  - 현재 블럭의 hash가 유효한 값인지 체크한다.
	 *  - 이전 블럭의 hash값과 동일한지 체크한다.
	 * @return
	 */
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;

		//전체 블럭을 체크합니다.
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			
			//현재 블럭의 hash가 맞는지 체크합니다.
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}
			
			//이전 블럭의 hash값과 동일한지 체크합니다.
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
		}
		return true;
	}	
}
