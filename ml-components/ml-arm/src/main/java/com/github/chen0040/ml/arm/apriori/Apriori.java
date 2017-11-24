package com.github.chen0040.ml.arm.apriori;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.arm.ItemSet;
import com.github.chen0040.ml.commons.arm.ItemSetCluster;
import com.github.chen0040.ml.commons.arm.AbstractAssocRuleMiner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public class Apriori extends AbstractAssocRuleMiner{

    @Override
    public Object clone(){
        Apriori clone = new Apriori();
        clone.copy(this);

        return clone;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        int m = batch.tupleCount();

        if(uniqueItems==null) {
            scan4UniqueItems(batch);
        }

        ItemSetCluster frequent_itemset_cluster = scan4OneItemFrequentItemSets(batch);
        warehouse.add(frequent_itemset_cluster);

        int k = 1;

        while(frequent_itemset_cluster.size() > 0){

            List<ItemSet> C = new ArrayList<ItemSet>();
            for(int i=0; i < frequent_itemset_cluster.size(); ++i){
                ItemSet frequent_itemset_i = frequent_itemset_cluster.get(i);
                for(int j =0; j < frequent_itemset_cluster.size(); ++j){
                    if(i==j) continue;

                    ItemSet frequent_itemset_j = frequent_itemset_cluster.get(j);

                    boolean canJoin = true;
                    for(int l=0; l < k-1; ++l){
                        if(!frequent_itemset_i.get(l).equals(frequent_itemset_j.get(l))){
                            canJoin = false;
                            break;
                        }
                    }

                    if(canJoin){
                        if(frequent_itemset_i.get(k-1).compareTo(frequent_itemset_j.get(k-1)) > 0){
                            canJoin = false;
                        }
                    }

                    if(canJoin){
                        ItemSet candidate = (ItemSet)frequent_itemset_i.clone();
                        candidate.parentSupport = frequent_itemset_i.support;
                        candidate.add(frequent_itemset_j.getLastItem());
                        C.add(candidate);
                    }
                }
            }

            updateItemSupport(batch, C);

            frequent_itemset_cluster = getFrequentItemSets(C, k+1);

            if(frequent_itemset_cluster.size() > 0) {
                warehouse.add(frequent_itemset_cluster);
            }
            k++;
        }



        return new BatchUpdateResult();
    }




}
