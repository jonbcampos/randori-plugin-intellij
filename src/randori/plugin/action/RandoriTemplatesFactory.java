/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import icons.RandoriIcons;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 * @author Frédéric THOMAS
 */
public class RandoriTemplatesFactory implements
        FileTemplateGroupDescriptorFactory
{

    @NonNls
    public static final String[] TEMPLATES = { RandoriTemplates.RANDORI_CLASS,
            RandoriTemplates.RANDORI_INTERFACE,
            RandoriTemplates.RANDORI_BEHAVIOUR,
            RandoriTemplates.RANDORI_CONTEXT, RandoriTemplates.RANDORI_MEDIATOR };
    private final ArrayList<String> customTemplates = new ArrayList<String>();

    public static RandoriTemplatesFactory getInstance()
    {
        return RandoriTemplatesFactoryHolder.INSTANCE;
    }

    public static PsiFile createFromTemplate(@NotNull PsiDirectory directory,
            @NotNull String name, @NotNull String fileName,
            @NotNull String templateName, @NonNls String... parameters)
            throws IncorrectOperationException
    {
        if (directory == null)
            throw new IllegalArgumentException(
                    "Argument 0 for @NotNull parameter of randori/plugin/action/RandoriTemplatesFactory.createFromTemplate must not be null");
        if (name == null)
            throw new IllegalArgumentException(
                    "Argument 1 for @NotNull parameter of randori/plugin/action/RandoriTemplatesFactory.createFromTemplate must not be null");
        if (fileName == null)
            throw new IllegalArgumentException(
                    "Argument 2 for @NotNull parameter of randori/plugin/action/RandoriTemplatesFactory.createFromTemplate must not be null");
        if (templateName == null)
            throw new IllegalArgumentException(
                    "Argument 3 for @NotNull parameter of randori/plugin/action/RandoriTemplatesFactory.createFromTemplate must not be null");

        FileTemplateManager templateManager = FileTemplateManager.getInstance();

        FileTemplate template = templateManager.getJ2eeTemplate(templateName);

        Properties properties = new Properties(
                templateManager.getDefaultProperties(directory.getProject()));
        JavaTemplateUtil.setPackageNameAttribute(properties, directory);

        properties.setProperty("NAME", name);
        properties.setProperty("lowCaseName", name.substring(0, 1)
                .toLowerCase() + name.substring(1));

        for (int i = 0; i < parameters.length; i += 2)
            properties.setProperty(parameters[i], parameters[(i + 1)]);

        String text;
        try
        {
            text = template.getText(properties);
        } catch (Exception e)
        {
            throw new RuntimeException("Unable to load template for "
                    + templateManager.internalTemplateToSubject(templateName),
                    e);
        }

        PsiFileFactory factory = PsiFileFactory.getInstance(directory
                .getProject());
        PsiFile file = factory.createFileFromText(fileName, text);

        return (PsiFile) directory.add(file);
    }

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor()
    {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(
                "Randori", RandoriIcons.Randori16);

        final FileTypeManager fileTypeManager = FileTypeManager.getInstance();

        for (String template : TEMPLATES)
        {
            group.addTemplate(new FileTemplateDescriptor(template,
                    fileTypeManager.getFileTypeByFileName(template).getIcon()));
        }
        // register custom templates
        for (String template : getInstance().getCustomTemplates())
        {
            group.addTemplate(new FileTemplateDescriptor(template,
                    fileTypeManager.getFileTypeByFileName(template).getIcon()));
        }
        return group;
    }

    private void addTemplate(FileTemplateGroupDescriptor group,
            String templateFileName)
    {
        group.addTemplate(new FileTemplateDescriptor(templateFileName,
                RandoriIcons.Randori16));
    }

    public void registerCustomTemplates(String[] templates)
    {
        Collections.addAll(this.customTemplates, templates);
    }

    public String[] getCustomTemplates()
    {
        return ArrayUtil.toStringArray(this.customTemplates);
    }

    private static class RandoriTemplatesFactoryHolder
    {
        private static final RandoriTemplatesFactory INSTANCE = new RandoriTemplatesFactory();
    }
}
