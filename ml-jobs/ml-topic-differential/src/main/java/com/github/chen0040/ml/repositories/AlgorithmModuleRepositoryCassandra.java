package com.github.chen0040.ml.repositories;

import com.github.chen0040.ml.models.AlgorithmModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 11/11/15.
 */
public class AlgorithmModuleRepositoryCassandra implements AlgorithmModuleRepository {

    private CassandraFactory csFactory;

    public AlgorithmModuleRepositoryCassandra(){
        csFactory = new CassandraFactory();
    }

    @Override
    public AlgorithmModule findById(String id) {
        final String entityName = AlgorithmModule.class.getSimpleName();
        final String cql = "SELECT sam FROM " + entityName + " sam WHERE sam.id = :id";
        Map<String, Object> params = new HashMap<String, Object>(0);
        params.put("id", id);

        AlgorithmModule model = csFactory.queryUnique(AlgorithmModule.class, cql, params);
        return model;
    }



    @Override
    public List<AlgorithmModule> findAllByCompanyId(String companyId) {
        final String entityName = AlgorithmModule.class.getSimpleName();
        final String cql = "SELECT sam FROM " + entityName + " sam WHERE sam.companyId = :companyId";
        Map<String, Object> params = new HashMap<String, Object>(0);
        params.put("companyId", companyId);


        List<AlgorithmModule> models = csFactory.queryList(AlgorithmModule.class, cql, params, -1);
        return models;
    }

    @Override
    public List<AlgorithmModule> findAll() {
        final String entityName = AlgorithmModule.class.getSimpleName();
        final String cql = "SELECT sam FROM " + entityName + " sam";
        Map<String, Object> params = new HashMap<String, Object>(0);



        List<AlgorithmModule> models = csFactory.queryList(AlgorithmModule.class, cql, params, -1);
        return models;
    }

    public static void main(String[] args){

        AlgorithmModuleRepositoryCassandra repo = new AlgorithmModuleRepositoryCassandra();
        List<AlgorithmModule> modules = repo.findAll();


        System.out.println("cs returned: "+modules.size());

        /*
        cassandra.ip=172.16.2.116,172.16.2.117,172.16.2.118
        cassandra.port=9042
        cassandra.thriftport=9160
        cassandra.keyspace=neuro01
        cassandra.schema.rule=update
        cassandra.schema.refresh=false

        <property name="persistenceXmlLocation" value="classpath:/META-INF/persistence.xml" />
        <property name="jpaProperties">
        <props>
        <prop key="kundera.nodes">${cassandra.ip}</prop>
        <prop key="kundera.port">${cassandra.thriftport}</prop>
        <prop key="kundera.keyspace">${cassandra.keyspace}</prop>
        <prop key="kundera.dialect">cassandra</prop>
        <prop key="kundera.client.lookup.class">com.impetus.client.cassandra.thrift.ThriftClientFactory
                </prop>
        </props>
        </property>*/
    }
}
