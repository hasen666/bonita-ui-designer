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
package org.bonitasoft.web.designer.rendering;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

/**
 * @author Benjamin Parisel
 */
@Named
public class DirectivesCollector {

    public static String ASSETS_FOLDER = "assets";
    private WorkspacePathResolver pathResolver;
    private DirectiveFileGenerator directiveFileGenerator;

    @Inject
    public DirectivesCollector(WorkspacePathResolver pathResolver, DirectiveFileGenerator directiveFileGenerator) {
        this.pathResolver = pathResolver;
        this.directiveFileGenerator = directiveFileGenerator;
    }

    public List<String> buildUniqueDirectivesFiles(Previewable previewable, String pageId) {
        Path assetFolder = pathResolver.getPagesRepositoryPath().resolve(pageId).resolve(ASSETS_FOLDER);
        String filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable,assetFolder);
        return Arrays.asList(ASSETS_FOLDER +"/" + filename.toString());
    }
}