package org.broadinstitute.cga.tools.gatk.walkers.cancer.visualize;

import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.ArgumentCollection;
import org.broadinstitute.sting.commandline.Input;
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
import java.util.Collections;
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

    @Argument(fullName="window_width", shortName="width", doc="width in bp of screenshot", required = false)
    int window_width = 100;

    @Argument(fullName="window_height", shortName="height", doc="height in pixels of screenshot", required = false)
    int window_height = 400;

    @Argument(fullName="igv_reference_genome", shortName="igv_ref", doc="name of reference genome to pass to igv")
    String igv_ref = "hg19";

    @Input(fullName="input_bam", shortName="bam", doc="A bam to add to the igv screenshot")
    List<File> bams = Collections.emptyList();

    @Output(fullName="igv_script_file", shortName="igv", doc="Name of igv script file to output")
    File igv_script = null;

    @Output(fullName="output_dir", shortName="out", doc="Name of directory to place snapshots in")
    File output_dir = new File("Igv_snapshot");





    @Override
    public String map(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        GenomeLoc location = context.getLocation();

        if ( ref == null){
            return "";
        }

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


        StringBuffer result = new StringBuffer();
        result.append( String.format("goto %s:%d-%d %n", location.getContig(),start,stop) );
        result.append( String.format("sort base %n"));
        result.append( String.format("snapshot %s %n",fileName) );
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
        result.append(String.format("new %n"));
        result.append(String.format("genome %s %n",igv_ref));
        result.append(String.format("maxPanelHeight %s%n",window_height));
        result.append(String.format("snapshotDirectory %s %n", output_dir.getAbsolutePath()));

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
        for( File bam: bams){
//            List<String> bamTags = id.getTags().getPositionalTags();
//            if(bamTags.size() == 2){
//
//
//
//            } else {
//                throw new UserException.BadArgumentValue("-I", "Each input bam must be tagged as either tumor or normal, " +
//                        "and be given a label, i.e. -I:tumor,whole_genome");
//            }

            result.append(String.format("load %s %n",bam.getAbsolutePath() ) );

        }

        result.append( String.format("echo loaded %n") );

        return result.toString();
    }



    @Override
    public StringBuffer reduce(String value, StringBuffer result) {
        result.append(value);
        return result;
    }

    @Override
    public void onTraversalDone(final StringBuffer result) {
        result.append(String.format("exit %n"));

        try (BufferedWriter writer = new BufferedWriter( new FileWriter(igv_script))){
            writer.write(result.toString());
        }   catch (IOException e){
            throw new UserException.CouldNotCreateOutputFile(igv_script,e);
        }


    }
}
