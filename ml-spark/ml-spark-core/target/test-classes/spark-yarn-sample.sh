# cat leftOuterJoinSparkYARN.sh
export SPARK_HOME=/usr/local/spark-1.0.0
export SPARK_JAR=spark-assembly-1.0.0-hadoop2.3.0.jar
export SPARK_ASSEMBLY_JAR=$SPARK_HOME/assemly/target/scala-2.10/$SPARK_JAR
export JAVA_HOME=/usr/java/jdk6
export HADOOP_HOME=/usr/local/hadoop-2.3.0
export HADOOP_CONF_DIR=$HADOOP_HOME/conf
export YARN_CONF_DIR=$HADOOP_HOME/conf
export APP_HOME=$HOME/Documents/SimuKit.J/ML-Spark
export APP_JAR=$APP_HOME/target/ML-Spark.jar
# Submit Spark' ApplicationMaster to YARN's ResourceManager,
# and instruct Spark to run the LeftOUterJoin example
prog=com.simukit.meme.ml.sparks.tests.LeftOuterJoin
SPARK_JAR=$SPARK_ASSEMBLY_JAR \
    $SPARK_HOME/bin/spark-class org.apache.spark.deploy.yarn.Client \
    --jar $APP_JAR \
    --class $prog \
    --args yarn-standalone \
    --args /left/join.users.txt \
    --args /left/join.transactions.txt \
    --num-workers 3 \
    --master-memory 4g \
    --worker-memory 2g \
    --worker-cores 1
