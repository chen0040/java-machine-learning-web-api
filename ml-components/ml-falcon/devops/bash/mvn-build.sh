mvn --file=../.. clean package

version="0.0.1"

cp  ../../target/ml-falcon-1.0.1-jar-with-dependencies.jar tdfalcon-$version.jar
chmod 777 tdfalcon-$version.jar
