$mineFieldSize=32
$numAgents=1
$targetMove=1
$falconMode="sarsa" #other values: "qlearnlambda", "qlearn", "sarsalambda", "r"
$rl=1
$bounded=1
$simCount=6
$folderName="tdfalcon/td-sarsa"


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
     java -jar $HOME/tdfalcon/tdfalcon-0.0.1.jar -j TD -s $one -c $two -t $three -m $four -f $five -b $six -h $seven -g $eight -n $nine > $HOME/tdfalcon/console/console.out-$ten 2> $HOME/tdfalcon/console/console.error-$eleven
    } -ArgumentList $mineFieldSize,$numAgents,$targetMove,$falconMode,$rl,$bounded,$simIndex,$simCount,$folderName,$simIndex,$simIndex


}