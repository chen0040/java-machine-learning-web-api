$version="1.0.1"
$current_dir=$pwd

cd $PSScriptRoot/../..
mvn -f pom.xml clean package
cd $current_dir

$projs=@("ml-webapi-jersey", "ml-webapi-spring")
foreach ($proj in $projs){
    $source_folder=$PSScriptRoot + "/../../ml-webapps/" + $proj
    $source=$source_folder + "/target/" + $proj + ".jar"
    $dest=$PSScriptRoot + "/../bin/" + $proj + ".jar"
    copy $source $dest
}

