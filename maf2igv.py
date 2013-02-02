'''
Created on 31 Jan 2013

@author: stewart
'''
import csv
import argparse
import sys
import os
import glob

if not (sys.version_info[0] == 2  and sys.version_info[1] in [7]):
    raise "Must use Python 2.7.x"


def parseOptions():
    description = '''
    Given a maf file with headers on the first line, make an IGV screen shot for each mutation.
    '''

    epilog= '''

        Writes IGV batch script for each sample
        [outputFile].IGV_batch.txt
        [outputFile].IGV.log

        Required columns in the input file (case sensitive):
            'Chromosome'
            'Start_position'
            'Tumor_Sample_Barcode'
            'Normal_Sample_Barcode'
            '''
    parser = argparse.ArgumentParser(description=description, epilog=epilog)
    parser.add_argument('-i','--individual_id', metavar='individual_id', type=str, help ='Individual id.')
    parser.add_argument('-m','--mafFile', metavar='mafFile', type=str, help ='Maf file.')
    parser.add_argument('-n','--normal_sample_id', metavar='normal_sample_id', type=str, help ='normal sample_id.')
    parser.add_argument('-c','--n_bam_capture', metavar='n_bam_capture', type=str, help ='normal clean_bam_file_capture.',default='')
    parser.add_argument('-w','--n_bam_wgs', metavar='n_bam_wgs',type=str, help ='normal clean_bam_file_wgs.',default='')
    parser.add_argument('-r','--n_bam_rna',metavar='n_bam_rna', type=str, help='normal bam_file_rna_analysis_ready.',default='')
    parser.add_argument('-t','--tumor_sample_id', metavar='tumor_sample_id', type=str, help ='tumor sample_id.')
    parser.add_argument('-C','--t_bam_capture', metavar='t_bam_capture', type=str, help ='tumor clean_bam_file_capture.',default='')
    parser.add_argument('-W','--t_bam_wgs', metavar='t_bam_wgs',type=str, help ='tumor clean_bam_file_wgs.',default='')
    parser.add_argument('-R','--t_bam_rna',metavar='t_bam_rna', type=str, help='tumor bam_file_rna_analysis_ready.',default='')
    parser.add_argument('-X','--bam_x',metavar='bam_x', type=str, help='other bam_file.',default='')
    parser.add_argument('-o','--output', metavar='output',type=str, help='output stub',default='IGV_snapshot')
    parser.add_argument('-g','--genome_reference', metavar='genome_reference',type=str, help='genome_reference.',default='hg19')
    parser.add_argument('-x','--maxPanelHeight', metavar='maxPanelHeight',type=int, help='maxPanelHeight.',default=400)
    parser.add_argument('-b','--window_bp', metavar='window_bp',type=int, help='window_bp ',default=100)
    args = parser.parse_args()

    return args

def bamlinker(output,bamfile,sample,bamtype):

        bamlink=''
        bamL = output + '/' + sample + '.' + bamtype +'.bam'
        if not os.path.lexists(bamL):
            os.symlink(bamfile, bamL)

        stub=bamfile.replace('.bam','')
        baifile=glob.glob(stub+'*.bai')[0]
        baiL = output + '/' + sample + '.' + bamtype + '.bai'
        if not os.path.lexists(baiL):
            os.symlink(baifile, baiL)
        bamlink = os.path.abspath(bamL)
        return bamlink

if __name__ == '__main__':

    args = parseOptions()
    id = args.individual_id
    inputFile = args.mafFile
    Nsample = args.normal_sample_id
    Nbam1 = args.n_bam_capture
    Nbam2 = args.n_bam_wgs
    Nbam3 = args.n_bam_rna
    Tsample = args.tumor_sample_id
    Tbam1 = args.t_bam_capture
    Tbam2 = args.t_bam_wgs
    Tbam3 = args.t_bam_rna
    Xbam  = args.bam_x
    output = args.output
    genome_reference = args.genome_reference
    maxPanelHeight = args.maxPanelHeight
    half_window_bp = int(args.window_bp/2)

    # load the input files
    inputFileFP = file(inputFile, 'r')

    if not os.path.exists(output):
        os.makedirs(output)

    # Create the IGV batch output file
    IGV_batch_Filename = output + '/' + id + '.IGV.bat'

    outputFileFP = file(IGV_batch_Filename, 'w')

    outputFileFP.write('new ' + "\n")
    outputFileFP.write('genome ' + genome_reference + "\n")
    outputFileFP.write('maxPanelHeight ' + str(maxPanelHeight) + "\n")
    outputFileFP.write('snapshotDirectory ' + output + "\n")
    if os.path.exists(Tbam1):
        bamL=bamlinker(output,Tbam1,Tsample,'capture')
        outputFileFP.write('load ' + bamL + "\n")

    if os.path.exists(Nbam1):
        bamL=bamlinker(output,Nbam1,Nsample,'capture')
        outputFileFP.write('load ' + bamL + "\n")

    if os.path.exists(Tbam2):
        bamL=bamlinker(output,Tbam2,Tsample,'wgs')
        outputFileFP.write('load ' + bamL + "\n")

    if os.path.exists(Nbam2):
        bamL=bamlinker(output,Nbam2,Nsample,'wgs')
        outputFileFP.write('load ' + bamL + "\n")

    if os.path.exists(Tbam3):
        bamL=bamlinker(output,Tbam3,Tsample,'rna')
        outputFileFP.write('load ' + bamL + "\n")

    if os.path.exists(Nbam3):
        bamL=bamlinker(output,Nbam3,Nsample,'rna')
        outputFileFP.write('load ' + bamL + "\n")
     
    if os.path.exists(Xbam):
        bamL=bamlinker(output,Xbam,Tsample,'other')
        outputFileFP.write('load ' + bamL + "\n")

    outputFileFP.write('echo loaded ' + "\n")

  
    numComments = 0
    line = inputFileFP.readline()
    while line.startswith('#'):
        numComments = numComments + 1
        line = inputFileFP.readline()

    inputFileFP.seek(0,0)

    # Read until all comment lines have been read.  Discard comment lines
    for i in range(0,numComments):
        inputFileFP.readline()

    inputTSVReader = csv.DictReader(inputFileFP, delimiter='\t')

    Lsample = ''
    CountIn = 0
    CountOut = 0

    for line in inputTSVReader:
        FromTo = line['Reference_Allele'] + '_' + line['Tumor_Seq_Allele1']
        tum_sample_id = line['Tumor_Sample_Barcode']

        CountIn = CountIn + 1

        if not(tum_sample_id == Lsample):
            print(str(CountIn)+"\t"+Lsample)

        Lsample = tum_sample_id

        if not(tum_sample_id == Tsample):
            continue

        nor_sample_id = line['Matched_Norm_Sample_Barcode']
        Chromosome = line['Chromosome']
        Start_position = int(line['Start_position'])
        End_position = int(line['End_position'])
        Gene = line['Hugo_Symbol']
        Vtype = line['Variant_Type']
        Vclass = line['Variant_Classification']

        # band-aid broken MT reference dictionary
        if 'M' is line['Chromosome']:
                line['Chromosome']='MT'

        key = line['Chromosome'] + ":" + line['Start_position']

        p1=Start_position-half_window_bp
        p2=Start_position+half_window_bp

        outputFileFP.write('goto ' + Chromosome + ":" + str(p1) + "-" + str(p2) + "\n")
        print((line['Chromosome'] + "\t" + line['Start_position'] +"\t" + FromTo))

        outputFileFP.write('sort base' + "\n")

        png = id + '_' + Gene + '_' + Chromosome + '_' + str(Start_position) + '_'+ Vclass + '_' + FromTo + '.png'

        outputFileFP.write('snapshot ' + png + "\n")

        CountOut = CountOut + 1

    outputFileFP.write('exit ' + "\n")

    outputFileFP.close()



