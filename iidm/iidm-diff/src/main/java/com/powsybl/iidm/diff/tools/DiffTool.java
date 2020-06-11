/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff.tools;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.diff.DiffConfig;
import com.powsybl.iidm.diff.NetworkDiff;
import com.powsybl.iidm.diff.NetworkDiffResults;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
@AutoService(Tool.class)
public class DiffTool implements Tool {

    private static final String INPUT_FILE1 = "input-file1";
    private static final String INPUT_FILE2 = "input-file2";
    private static final String OUTPUT_FILE = "output-file";
    private static final String IDS = "ids";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "compare-network";
            }

            @Override
            public String getDescription() {
                return "Compare two networks";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(INPUT_FILE1)
                        .desc("the input file1")
                        .hasArg()
                        .argName("INPUT_FILE1")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(INPUT_FILE2)
                        .desc("the input file2")
                        .hasArg()
                        .argName("INPUT_FILE2")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("the output file")
                        .hasArg()
                        .argName("OUTPUT_FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("ids")
                        .desc("ids to consider")
                        .hasArg()
                        .argName("IDS")
                        .numberOfArgs(Option.UNLIMITED_VALUES)
                        .valueSeparator(',')
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        String inputFile1 = line.getOptionValue(INPUT_FILE1);
        String inputFile2 = line.getOptionValue(INPUT_FILE2);
        String outputFile = line.getOptionValue(OUTPUT_FILE);

        String[] ids = line.getOptionValues(IDS);

        DiffConfig config = DiffConfig.load();

        ImportConfig importConfig = new ImportConfig();

        Network network1 = Importers.loadNetwork(context.getFileSystem().getPath(inputFile1), context.getShortTimeExecutionComputationManager(), importConfig, null);
        Network network2 = Importers.loadNetwork(context.getFileSystem().getPath(inputFile2), context.getShortTimeExecutionComputationManager(), importConfig, null);
        NetworkDiffResults ndifr = new NetworkDiff(config).diff(network1, network2, ids);
        NetworkDiff.writeJson(context.getFileSystem().getPath(outputFile), ndifr);
    }
}
