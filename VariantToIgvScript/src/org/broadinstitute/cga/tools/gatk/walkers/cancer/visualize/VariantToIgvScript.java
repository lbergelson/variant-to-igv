package org.broadinstitute.cga.tools.gatk.walkers.cancer.simulation;

import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.ArgumentCollection;
import org.broadinstitute.sting.commandline.Output;
import org.broadinstitute.sting.gatk.arguments.StandardVariantContextInputArgumentCollection;
import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.datasources.reads.SAMReaderID;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.RodWalker;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.exceptions.UserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: louisb
 * Date: 8/12/13
 * Time: 9:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class VariantToIgvScript extends RodWalker<String, StringBuffer>{
    @ArgumentCollection
    private StandardVariantContextInputArgumentCollection variantCollection = new StandardVariantContextInputArgumentCollection();

    @Argument(fullName="window_width", shortName="window", doc="width in bp of screenshot", required = false)
    int window_width = 100;

    @Argument(fullName="igv_reference_genome", shortName="igv_ref", doc="name of reference genome to pass to igv")
    String igv_ref = "hg19";

    @Output(fullName="igv_script_file", shortName="igv", doc="Name of igv script file to output")
    File igv_script = null;

    @Output(fullName="output_dir", shortName="out", doc="Name of directory to place snapshots in")
    File output_dir = new File("Igv_snapshot");





    @Override
    public String map(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        GenomeLoc location = context.getLocation();

        //TODO make the file names the same as the python version
        String filename = location.toString()+".png";

        return generatePrintStatement(location, filename);
    }


    private String generatePrintStatement(GenomeLoc location, String fileName){
//        def writeSnapshotCommand(outputFile, Chromosome, start, stop):
//        filename = id + Gene + '.' + Chromosome + '_' + str(Start_position) + Vclass + FromTo + '.png'
//        print(("snapshot:\t" + filename))
//        outputFile.write('goto {chromosome}:{start}-{stop}\n'.format(chromosome=Chromosome, start=start, stop=stop))
//        outputFile.write('sort base\n')
//        outputFile.write('snapshot ' + filename + "\n")
        int start = location.getStart()-window_width/2;
        int stop = location.getStop()+window_width/2;

        StringBuilder result = new StringBuilder();
        result.append( "goto %s:%d-%d\n".format(location.getContig(),start,stop) );
        result.append( " sort base\n");
        result.append( "snapshot %s\n".format(fileName));
        return result.toString();
    }
    /*
          how to get name tags...

          for(SAMReaderID id : getToolkit().getReadsDataSource().getReaderIDs()) {
            if (id.getTags().getPositionalTags().size() == 0) {
                throw new RuntimeException("BAMs must be tagged as either 'tumor' or 'normal'");
            }

            for(String tag : id.getTags().getPositionalTags()) {
     */

    @Override
    public void initialize() {
        try{
            Files.createDirectories(output_dir.toPath()) ;
        } catch (IOException e){
            throw new UserException.CouldNotCreateOutputFile(output_dir, e);
        }
    }

    @Override
    public StringBuffer reduceInit() {
        StringBuffer result = new StringBuffer();
        result.append("new \n");
        result.append("genome %s\n".format(igv_ref));
        result.append("snapshotDirectory %s\n".format(output_dir.getAbsolutePath()));

        result.append( createLinksAndGenerateLoadStatements());

        return result;

    }

    private String createLinksAndGenerateLoadStatements() {
        /*
        for(SAMReaderID id : getToolkit().getReadsDataSource().getReaderIDs()) {
            if (id.getTags().getPositionalTags().size() == 0) {
                throw new RuntimeException("BAMs must be tagged as either 'tumor' or 'normal'");
            }
         */
        StringBuffer result = new StringBuffer();

        Collection<SAMReaderID> bamReaders = getToolkit().getReadsDataSource().getReaderIDs();
        for( SAMReaderID id : bamReaders){
//            List<String> bamTags = id.getTags().getPositionalTags();
//            if(bamTags.size() == 2){
//
//
//
//            } else {
//                throw new UserException.BadArgumentValue("-I", "Each input bam must be tagged as either tumor or normal, " +
//                        "and be given a label, i.e. -I:tumor,whole_genome");
//            }

            result.append("load %s\n".format(id.getSamFilePath()));

        }

        result.append("echo loaded \n");

        return result.toString();
    }



    @Override
    public StringBuffer reduce(String value, StringBuffer sum) {
        sum.append(value);
        return sum;
    }

    @Override
    public void onTraversalDone(final StringBuffer result) {
        try (BufferedWriter writer = new BufferedWriter( new FileWriter(igv_script))){
            writer.write(result.toString());
        }   catch (IOException e){
            throw new UserException.CouldNotCreateOutputFile(igv_script,e);
        }
    }
}
