
maf2igv.sh EwngSRC-SJDES018 /local/cga-fh/cga/Sigma_Pediatric/Individual_Set/AN_Sigma_EwingSarcoma_29Jan2013_TRN31/jobs/mutsig1.0_Sigma_Pediatric/AN_Sigma_EwingSarcoma_29Jan2013_TRN31.final_analysis_set.maf EwngSRC-SJDES018-Tumor EwngSRC-SJDES018-Normal   /seq/picard_aggregation/C594/SJDES018-1/v3/SJDES018-1.bam /seq/picard_aggregation/C594/SJDES018-2/v3/SJDES018-2.bam /seq/picard_aggregation/G14864/SJDES018-1/v2/SJDES018-1.bam /seq/picard_aggregation/G14864/SJDES018-2/v1/SJDES018-2.bam /xchip/cga/gdac-prod/genepattern/jobResults/4872146/EwngSRC-SJDES018-Tumor.recalibrated.bam - - snapshot hg19 400 70 12345 1234  3500

#echo ""
#XID=12345
#PORT=1234
#Xvnc :${XID} -SecurityTypes None -depth 16 -geometry 1024x768 -rfbport ${PORT} &
#echo "Xvnc :${XID} -SecurityTypes None -depth 16 -geometry 1024x768 -rfbport ${PORT}" 
#ps -ef |grep $USER | grep $XID | grep -v grep | awk '{ print $2 }' | xargs kill
