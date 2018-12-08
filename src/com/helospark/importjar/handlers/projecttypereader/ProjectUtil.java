package com.helospark.importjar.handlers.projecttypereader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectUtil {

    public static void addNature(IJavaProject jarProject, String natureToAdd) throws CoreException {
        IProjectDescription desc = jarProject.getProject().getDescription();

        String[] natures = desc.getNatureIds();
        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = natureToAdd;

        desc.setNatureIds(newNatures);

        jarProject.getProject().setDescription(desc, null);

    }

    public static IFile createUninitializedFiled(IJavaProject jarProject, String name) throws CoreException {
        String[] parts = name.split("/");
        IFolder folder = jarProject.getProject().getFolder(parts[0]);
        if (!folder.exists()) {
            folder.create(true, true, null);
        }
        for (int i = 1; i < parts.length - 1; ++i) {
            folder = folder.getFolder(parts[i]);
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
        }
        return folder.getFile(parts[parts.length - 1]);
    }

    public static byte[] readAllBytes(File file) throws FileNotFoundException, IOException {
        return readAllBytes(new FileInputStream(file));
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static IFolder createOrGetFolder(IJavaProject javaProject, String folderName) throws CoreException {
        String[] parts = folderName.split("/");

        if (parts.length == 0) {
            throw new IllegalArgumentException("No folder is returned");
        }

        IProject project = javaProject.getProject();

        IFolder folder = project.getFolder(parts[0]);
        createIfNotExists(folder);

        for (int i = 1; i < parts.length; ++i) {
            folder = folder.getFolder(parts[i]);
            createIfNotExists(folder);
        }
        return folder;
    }

    private static void createIfNotExists(IFolder folder) throws CoreException {
        if (!folder.exists()) {
            folder.create(false, true, null);
        }
    }

    public static void appendToClasspath(IJavaProject javaProject, List<IFolder> folders) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + folders.size()];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        int i = oldEntries.length;
        for (IFolder folder : folders) {
            IPackageFragmentRoot rootFolder = javaProject.getPackageFragmentRoot(folder);
            newEntries[i] = JavaCore.newSourceEntry(rootFolder.getPath());
            ++i;
        }
        javaProject.setRawClasspath(newEntries, null);
    }

    public static void appendToClasspath(IJavaProject javaProject, IClasspathEntry entry) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = entry;
        javaProject.setRawClasspath(newEntries, null);
    }

}