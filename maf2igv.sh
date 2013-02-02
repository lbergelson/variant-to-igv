#!/bin/sh

source /broad/software/scripts/useuse
use Python-2.7 

ID=$1;shift
MAF=$1;shift
TSAM=$1;shift
NSAM=$1;shift
TBAM1=$1;shift
NBAM1=$1; shift
TBAM2=$1; shift
NBAM2=$1; shift
TBAM3=$1; shift
XBAM=$1; shift
OUT=$1; shift
REF=$1; shift
WINH=$1; shift
WIND=$1; shift
XID=$1; shift
PORT=$1; shift
IGVMEM=$1; shift

echo "ID: ${ID}"
echo "MAF: ${MAF}" 
echo "Tumor Sample ${TSAM}" 
echo "Normal Sample ${NSAM}" 
echo "Tumor bam capture ${TBAM1}" 
echo "Normal bam capture ${NBAM1}" 
echo "Tumor bam wgs ${TBAM2}" 
echo "Normal bam wgs ${NBAM2}"
echo "Tumor bam RNA ${TBAM3}" 
echo "Normal bam RNA ${NBAM3}" 
echo "Xtra bam ${XBAM}" 
echo "output area ${OUT}" 
echo "reference genome ${REF}" 
echo "window height (pixels) ${WINH}" 
echo "window width (bp) ${WIND}" 
echo "X-display ID ${XID}" 
echo "IGV port ${PORT}" 
echo "IGV memory ${IGVMEM}" 


Dir=`dirname $0`
python $Dir/maf2igv.py -i $ID -m $MAF -t $TSAM -n $NSAM -C $TBAM1 -W $TBAM2 -R $TBAM3 -c $NBAM1 -w $NBAM2 -r $NBAM3 -X $XBAM -g $REF -x $WINH -b $WIND -o $OUT

$igvcommands = ${OUT}/${ID}.IGV.bat
dos2unix $igvcommands

Xvnc :$XID -SecurityTypes None -depth 16 -geometry 1024x768 -rfbport $PORT &

DISPLAY=localhost:$XID

java -Dapple.laf.useScreenMenuBar=true -Xmx${IGVMEM}m -jar $Dir/igv.jar -p 60151 -b $igvCommands 

ps -ef |grep $user | grep $xdisp | grep -v grep | awk '{ print \$2 }' | xargs kill
