#!/bin/sh

source /broad/software/scripts/useuse
use Python-2.7 

ID=$1;shift
MAF=$1;shift
TBAM1=$1;shift
NBAM1=$1; shift
OUT=$1; shift
REF=$1; shift
WINH=$1; shift
WIND=$1; shift
IGVMEM=$1; shift

echo "ID:               	${ID}"
echo "MAF:              	${MAF}" 
echo "Tumor bam capture:   	${TBAM1}" 
echo "Normal bam capture:	${NBAM1}" 
echo "output area:      	${OUT}" 
echo "reference genome:		${REF}" 
echo "window height (pixels):${WINH}" 
echo "window width (bp):	${WIND}" 
echo "IGV memory:        	${IGVMEM}" 


Dir=`dirname $0`

echo ""

igvcommands=${OUT}/${ID}.IGV.cmd
echo "igv commands: " $igvcommands

echo ""
echo "maf2igv gatk command line: "

java -jar $GATK -T VariantToIgvScript -V $MAF --igv_script_file ${igvcommands} -bam:tumor $TBAM1 -bam:tumor  $NBAM1 --igv_reference_genome $REF --window_width $WIND -out $OUT -R ~/cga_home/reference/human_g1k_v37_decoy.fasta

if [[ $? -ne 0 ]] ; then
   exit 1
fi


echo ""
wc -l $igvcommands

last_line=$( tail -1 "${igvcommands}" )
if [[ " $last_line " =~ "\s+exit\s+" ]] 
    then
        echo "$igvcommands complete "
    else
        echo "error $igvcommands missing 'exit' in last line  "
        exit 1
fi


jobId=$LSB_JOBID
if [[ -z $jobId ]]; then jobId=$$; fi
XID=$[($jobId%10000)+10000]
echo "XID: " $XID
#Xvnc :$display -depth 16
#DISPLAY=$localhost:$display
#echo $DISPLAY


echo ""
Xvnc :${XID} -SecurityTypes None -depth 16 -geometry 1024x768  &
echo "Xvnc :${XID} -SecurityTypes None -depth 16 -geometry 1024x768 -rfbport ${PORT}" 

echo ""
export DISPLAY=localhost:$XID
echo $DISPLAY 

echo ""
echo "output area annotation file"
echo $PWD/${OUT} > ${OUT}/${ID}.snapshots.txt
find ${PWD}/${OUT}/${ID}.snapshots.txt

echo ""
echo "igv command:"
echo "java -Dapple.laf.useScreenMenuBar=true -Xmx${IGVMEM}m -jar ${Dir}/igv.jar -b ${igvcommands} -o ${Dir}/igv.prefs.properties" 

#java -Dapple.laf.useScreenMenuBar=true -Xmx${IGVMEM}m -jar $Dir/igv.jar -p 60151 -b ${igvcommands} 

java -Dapple.laf.useScreenMenuBar=true -Xmx${IGVMEM}m -jar $Dir/igv.jar -b ${igvcommands} -o ${Dir}/igv.prefs.properties

echo ""
echo "stop Xvnc"

ps -ef |grep $USER | grep $XID | grep Xvnc| grep -v grep 
ps -ef |grep $USER | grep $XID | grep Xvnc| grep -v grep | awk '{ print $2 }' 
ps -ef |grep $USER | grep $XID | grep Xvnc| grep -v grep | awk '{ print $2 }' | xargs kill

rm -fv ${OUT}/*.bam
rm -fv ${OUT}/*.bai


echo "done"
