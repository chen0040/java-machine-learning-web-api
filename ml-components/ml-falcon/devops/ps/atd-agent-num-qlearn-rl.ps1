$mineFieldSize=16
$targetMove=1
$falconMode="qlearn" #other values: "qlearnlambda", "sarsa", "sarsalambda", "r"
$rl=1
$bounded=0
$simCount=1
$folderName="tdfalcon/num-agents-rl-q"



If (!(Test-Path $HOME/tdfalcon)) {
   New-Item -Path $HOME/tdfalcon -ItemType Directory
}

If (!(Test-Path $HOME/tdfalcon/console)) {
   New-Item -Path $HOME/tdfalcon/console -ItemType Directory
}

Copy-Item tdfalcon-0.0.1.jar $HOME/tdfalcon

echo "start running"
for($numAgents=4; $numAgents -le 40; $numAgents += 2) {
    $simIndex = $numAgents
    java -jar $HOME/tdfalcon/tdfalcon-0.0.1.jar -j ATD -s $mineFieldSize -c $numAgents -t $targetMove -m $falconMode -f $rl -b $bounded -h $simIndex -g $simCount -n $folderName
}
echo "done"
