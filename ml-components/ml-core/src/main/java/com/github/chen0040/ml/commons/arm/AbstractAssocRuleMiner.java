package com.github.chen0040.ml.commons.arm;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.MLModuleOutputType;
import com.github.chen0040.ml.commons.AbstractMLModule;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public abstract class AbstractAssocRuleMiner extends AbstractMLModule implements AssocRuleMiner {
    protected int minSupportLevel;
    protected int maxFrequentItemSetSize;
    protected List<String> uniqueItems;
    protected ItemSetWarehouse warehouse;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        AbstractAssocRuleMiner rhs2 = (AbstractAssocRuleMiner)rhs;
        minSupportLevel = rhs2.minSupportLevel;
        maxFrequentItemSetSize = rhs2.maxFrequentItemSetSize;
        uniqueItems = new ArrayList<>();
        for(String item : rhs2.uniqueItems){
            uniqueItems.add(item);
        }
        warehouse = (ItemSetWarehouse)rhs2.warehouse.clone();
    }

    public AbstractAssocRuleMiner(){
        minSupportLevel = 4;
        maxFrequentItemSetSize = -1;
        warehouse = new ItemSetWarehouse();
        setOutputType(MLModuleOutputType.MinedAssociationRules);
    }

    public int getMinSupportLevel() {
        return minSupportLevel;
    }

    public void setMinSupportLevel(int minSupportLevel) {
        this.minSupportLevel = minSupportLevel;
    }

    public int getMaxFrequentItemSetSize() {
        return maxFrequentItemSetSize;
    }

    public void setMaxFrequentItemSetSize(int maxFrequentItemSetSize) {
        this.maxFrequentItemSetSize = maxFrequentItemSetSize;
    }

    protected void scan4UniqueItems(IntelliContext batch){
        int m = batch.tupleCount();
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < m; ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            int n = tuple.tupleLength();
            for(int j=0; j < n; ++j){
                String item = batch.getAttributeValueAsString(tuple, j);
                if(item != null && !item.equals("")) {
                    set.add(item);
                }
            }
        }
        uniqueItems = new ArrayList<>();
        for(String item : set){
            uniqueItems.add(item);
        }

        Collections.sort(uniqueItems);
    }

    public List<String> getUniqueItems(){
        return uniqueItems;
    }


    protected void updateItemSupport(IntelliContext batch, List<ItemSet> C){
        for(int j=0; j < C.size(); ++j){
            C.get(j).support = 0;
        }

        int m = batch.tupleCount();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            for(int j=0; j < C.size(); ++j){
                ItemSet itemset = C.get(j);
                if(itemset.inTuple(batch, tuple)){
                    itemset.support++;
                }
            }
        }
    }

    protected ItemSetCluster getFrequentItemSets(List<ItemSet> C, int k){
        ItemSetCluster frequent_itemset_cluster = new ItemSetCluster(k);
        for(ItemSet itemset : C){
            if(itemset.support >= minSupportLevel){
                frequent_itemset_cluster.add(itemset);
            }
        }
        return frequent_itemset_cluster;
    }

    protected ItemSetCluster scan4OneItemFrequentItemSets(IntelliContext batch){
        int m = batch.tupleCount();

        HashMap<String, ItemSet> one_item_frequent_itemset_candidates = new HashMap<String, ItemSet>();
        for(int i=0; i < uniqueItems.size(); ++i){
            one_item_frequent_itemset_candidates.put(uniqueItems.get(i), new ItemSet(uniqueItems.get(i)));
        }

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            int n = tuple.tupleLength();
            for(int j=0; j < n; ++j){
                String item = batch.getAttributeValueAsString(tuple, j);
                if(item != null && !item.equals("")){
                    one_item_frequent_itemset_candidates.get(item).support++;
                }
            }
        }

        int k = 1;
        ItemSetCluster one_item_frequent_itemset_cluster = new ItemSetCluster(k);
        for(ItemSet itemset : one_item_frequent_itemset_candidates.values()){
            if(itemset.support >= minSupportLevel){
                one_item_frequent_itemset_cluster.add(itemset);
            }
        }

        return one_item_frequent_itemset_cluster;
    }

    public ItemSetWarehouse getFrequentItemSets(){
        return this.warehouse;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context){
        throw new NotImplementedException();
    }
}
