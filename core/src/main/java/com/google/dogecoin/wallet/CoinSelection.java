package com.google.dogecoin.wallet;

import com.google.dogecoin.core.Coin;
import com.google.dogecoin.core.TransactionOutput;

import java.util.Collection;

/**
 * Represents the results of a
 * {@link com.google.dogecoin.wallet.CoinSelector#select(Coin, java.util.LinkedList)} operation. A
 * coin selection represents a list of spendable transaction outputs that sum together to give valueGathered.
 * Different coin selections could be produced by different coin selectors from the same input set, according
 * to their varying policies.
 */
public class CoinSelection {
    public Coin valueGathered;
    public Collection<TransactionOutput> gathered;

    public CoinSelection(Coin valueGathered, Collection<TransactionOutput> gathered) {
        this.valueGathered = valueGathered;
        this.gathered = gathered;
    }
}
