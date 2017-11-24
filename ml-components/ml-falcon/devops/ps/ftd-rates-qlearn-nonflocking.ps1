$mineFieldSize=32
$numAgents=20
$targetMove=1
$falconMode="qlearn" #other values: "qlearnlambda", "sarsa", "sarsalambda", "r"
$flocking=0
$bounded=0
$simCount=6
$folderName="tdfalcon/nonflocking-q"

# echo "start running"
# java -jar tdfalcon-0.0.1.jar -s $mineFieldSize -c $numAgents -t $targetMove -m $falconMode -f $flocking -b $bounded -h $simStartIndex -g $simCount -n $folderName
# echo "done"

If (!(Test-Path $HOME/tdfalcon)) {
   New-Item -Path $HOME/tdfalcon -ItemType Directory
}

If (!(Test-Path $HOME/tdfalcon/console)) {
   New-Item -Path $HOME/tdfalcon/console -ItemType Directory
}

Copy-Item tdfalcon-0.0.1.jar $HOME/tdfalcon

$simStartIndexQueue = [System.Collections.Queue]::Synchronized( (New-Object System.Collections.Queue) )
for($i=0; $i -lt 30; $i+=$simCount){
    $simStartIndexQueue.Enqueue($i)
}

for($i=0; $i -lt 30; $i+=$simCount){
    $simIndex = $simStartIndexQueue.Dequeue()
    Start-Job -ScriptBlock { Param($one,$two,$three,$four,$five,$six,$seven,$eight,$nine, $ten, $eleven)
     java -jar $HOME/tdfalcon/tdfalcon-0.0.1.jar -s $one -c $two -t $three -m $four -f $five -b $six -h $seven -g $eight -n $nine > $HOME/tdfalcon/console/console.out-$ten 2> $HOME/tdfalcon/console/console.error-$eleven
    } -ArgumentList $mineFieldSize,$numAgents,$targetMove,$falconMode,$flocking,$bounded,$simIndex,$simCount,$folderName,$simIndex,$simIndex


}
