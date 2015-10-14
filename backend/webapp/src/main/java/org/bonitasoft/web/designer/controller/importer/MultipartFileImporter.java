/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller.importer;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Named;

import org.bonitasoft.web.designer.controller.importer.ImportException.Type;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.MimeType;
import org.springframework.web.multipart.MultipartFile;

@Named
public class MultipartFileImporter {

    public ImportReport importFile(MultipartFile file, ArtifactImporter importer) {
        try {
            return doImport(file, importer);
        } catch (IOException | IllegalArgumentException e) {
            throw new ImportException(Type.SERVER_ERROR, e.getMessage(), e);
        }
    }

    private ImportReport doImport(MultipartFile file, ArtifactImporter importer) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Part named [file] is needed to successfully import a component");
        }

        if (isNotZipFile(file)) {
            throw new IllegalArgumentException("Only zip files are allowed when importing a component");
        }

        try (InputStream is = file.getInputStream()) {
            return importer.execute(is);
        }
    }

    // some browsers send application/octet-stream for zip files
    // so we check that mimeType is application/zip or application/octet-stream and filename ends with .zip
    private boolean isNotZipFile(MultipartFile file) {
        return !MimeType.APPLICATION_ZIP.matches(file.getContentType())
                && !(MimeType.APPLICATION_OCTETSTREAM.matches(file.getContentType()) && file.getOriginalFilename().endsWith(".zip"));
    }
}
