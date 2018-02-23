package com.openchain.simplechain.core;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.openchain.simplechain.main.Openchain;

/**
 * 
 * @author comnic
 *
 */
public class Wallet {
	public PrivateKey privateKey;
	public PublicKey publicKey;

	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	/**
	 * 생성자 : 키를 생성한다.
	 */
	public Wallet(){
		generateKeyPair();	
	}
	
	/**
	 * key pair를 생성한다.
	 */
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA",
					"BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable security level

			KeyPair keyPair = keyGen.generateKeyPair();

			// Set the public and private keys from the keyPair
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 잔고를 구한다.
	 * 
	 * @return
	 */
	public float getBalance() {
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item : Openchain.UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			if (UTXO.isMine(publicKey)) {
				UTXOs.put(UTXO.id, UTXO);
				total += UTXO.value;
			}
		}

		return total;
	}

	/**
	 * 새로운 transaction을 만들고 리턴한다.(현재 wallet에서)
	 * @param _recipient
	 * @param value
	 * @return
	 */
	public Transaction sendFunds(PublicKey _recipient, float value) {
		if (getBalance() < value) { // gather balance and check funds.
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		
		// create array list of inputs
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

		float total = 0;
		for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if (total > value)
				break;
		}

		Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
		newTransaction.generateSignature(privateKey);

		for (TransactionInput input : inputs) {
			UTXOs.remove(input.transactionOutputId);
		}
		return newTransaction;
	}	
}
