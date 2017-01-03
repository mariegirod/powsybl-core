/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ToolRunningContext {

    private final PrintStream outputStream;

    private final PrintStream errorStream;

    private final FileSystem fileSystem;

    public ToolRunningContext() {
        this(System.out, System.err, FileSystems.getDefault());
    }

    public ToolRunningContext(PrintStream outputStream, PrintStream errorStream, FileSystem fileSystem) {
        this.outputStream = Objects.requireNonNull(outputStream);
        this.errorStream = Objects.requireNonNull(errorStream);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public PrintStream getErrorStream() {
        return errorStream;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }
}
