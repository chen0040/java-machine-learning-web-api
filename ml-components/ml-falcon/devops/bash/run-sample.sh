mineFieldSize=32
numAgents=20
targetMove=1
falconMode="qlearn" #other values: "qlearnlambda", "sarsa", "sarsalambda", "r"
flocking=1
bounded=0
simStartIndex=0
simCount=1
folderName="flocking-q"

echo "start running"
java -jar tdfalcon-0.0.1.jar -s $mineFieldSize -c $numAgents -t $targetMove -m $falconMode -f $flocking -b $bounded -h $simStartIndex -g $simCount -n $folderName

echo "done"

# nohup java -jar tdfalcon-0.0.1.jar -s $mineFieldSize -c $numAgents -t $targetMove -m $falconMode -f $flocking -b $bounded -h $simStartIndex -g $simCount -n $folderName > /dev/null 2> $HOME/$folderName/console.error &
