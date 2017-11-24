$targetMove=1
$falconMode="qlearn" #other values: "qlearnlambda", "sarsa", "sarsalambda", "r"
$rl=1
$bounded=0
$simCount=1
$folderName="tdfalcon/uav-degrees-rl-q"



If (!(Test-Path $HOME/tdfalcon)) {
   New-Item -Path $HOME/tdfalcon -ItemType Directory
}

If (!(Test-Path $HOME/tdfalcon/console)) {
   New-Item -Path $HOME/tdfalcon/console -ItemType Directory
}

Copy-Item tdfalcon-0.0.1.jar $HOME/tdfalcon

$numAgents=40
$degrees=0.1
echo "start running"
for($degrees=0.1; $degrees -le 1.0; $degrees += 0.1) {
    $simIndex = $degrees * 10
    java -jar $HOME/tdfalcon/tdfalcon-0.0.1.jar -j URAV -s $degrees -c $numAgents -t $targetMove -m $falconMode -f $rl -b $bounded -h $simIndex -g $simCount -n $folderName
}
echo "done"