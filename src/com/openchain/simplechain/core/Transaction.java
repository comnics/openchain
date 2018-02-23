package com.openchain.simplechain.core;

import java.security.*;
import java.util.ArrayList;

import com.openchain.simplechain.main.Openchain;
import com.openchain.simplechain.util.StringUtil;

/**
 * 
 * @author comnic
 *
 */
public class Transaction {

	public String transactionId; // this is also the hash of the transaction.
	public PublicKey sender; // senders address/public key.
	public PublicKey reciepient; // Recipients address/public key.
	public float value;
	public byte[] signature; // this is to prevent anybody else from spending
								// funds in our wallet.

	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

	private static int sequence = 0; // a rough count of how many transactions
										// have been generated.

	/**
	 * 생성자.
	 * 
	 * @param from
	 * @param to
	 * @param value
	 * @param inputs
	 */
	public Transaction(PublicKey from, PublicKey to, float value,
			ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}

	/**
	 * 해시를 계산한다.
	 * 
	 * @return
	 */
	private String calulateHash() {
		sequence++; // increase the sequence to avoid 2 identical transactions
					// having the same hash
		return StringUtil.applySha256(StringUtil.getStringFromKey(sender)
				+ StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value) + sequence);
	}

	/**
	 * 비밀키로 서한다.
	 * 
	 * @param privateKey
	 */
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender)
				+ StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}

	/**
	 * 서을 확인 한다.
	 * 
	 * @return
	 */
	public boolean verifiySignature() {
		String data = StringUtil.getStringFromKey(sender)
				+ StringUtil.getStringFromKey(reciepient)
				+ Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}

	/**
	 * 거래를 처리한다.
	 * 
	 * @return
	 */
	public boolean processTransaction() {

		if (verifiySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}

		// gather transaction inputs (Make sure they are unspent):
		for (TransactionInput i : inputs) {
			i.UTXO = Openchain.UTXOs.get(i.transactionOutputId);
		}

		// check if transaction is valid:
		if (getInputsValue() < Openchain.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: "
					+ getInputsValue());
			return false;
		}

		// generate transaction outputs:
		float leftOver = getInputsValue() - value;
		transactionId = calulateHash();
		outputs.add(new TransactionOutput(this.reciepient, value, transactionId)); 
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

		// add outputs to Unspent list
		for (TransactionOutput o : outputs) {
			Openchain.UTXOs.put(o.id, o);
		}

		// remove transaction inputs from UTXO lists as spent:
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue; // if Transaction can't be found skip it
			Openchain.UTXOs.remove(i.UTXO.id);
		}

		return true;
	}

	/**
	 * input의 합을 구한다.
	 * 
	 * @return
	 */
	public float getInputsValue() {
		float total = 0;
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue; // if Transaction can't be found skip it
			total += i.UTXO.value;
		}
		return total;
	}

	/**
	 * output의 합을 구한다.
	 * 
	 * @return
	 */
	public float getOutputsValue() {
		float total = 0;
		for (TransactionOutput o : outputs) {
			total += o.value;
		}

		return total;
	}

}