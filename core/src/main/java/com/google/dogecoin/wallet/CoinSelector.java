package com.google.dogecoin.wallet;

import com.google.dogecoin.core.Coin;
import com.google.dogecoin.core.TransactionOutput;

import java.util.LinkedList;

/**
 * A CoinSelector is responsible for picking some outputs to spend, from the list of all spendable outputs. It
 * allows you to customize the policies for creation of transactions to suit your needs. The select operation
 * may return a {@link CoinSelection} that has a valueGathered lower than the requested target, if there's not
 * enough money in the wallet.
 */
public interface CoinSelector {
    /**
     * Creates a CoinSelection that tries to meet the target amount of value. The candidates list is given to
     * this call and can be edited freely. See the docs for CoinSelection to learn more, or look a the implementation
     * of {@link com.google.dogecoin.wallet.DefaultCoinSelector}.
     */
    public CoinSelection select(Coin target, LinkedList<TransactionOutput> candidates);
}
