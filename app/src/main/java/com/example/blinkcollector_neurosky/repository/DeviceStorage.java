package com.example.blinkcollector_neurosky.repository;

import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.blinkcollector_neurosky.data.FilesListData;
import com.example.blinkcollector_neurosky.data.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public class DeviceStorage {

    private static final String root = Environment.getExternalStorageDirectory().toString();
    private static final String appFolderName = "BlinkCollector";
    private static final File rootDir = new File(
            new Uri.Builder()
                    .appendPath(root)
                    .appendPath(appFolderName)
                    .build()
                    .getPath()
    );
    private static final File tempDir = new File(
            new Uri.Builder()
                    .appendPath(root)
                    .appendPath(appFolderName)
                    .appendPath("temp")
                    .build()
                    .getPath()
    );


    @Inject
    DeviceStorage() {

    }

    public void saveFile(@NonNull FilesListData data) {
        File file = associatedFile(data);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Cant create file");
            }
        }
        try {
            FileOutputStream fout = new FileOutputStream(file);
            for (Point p : data.getData()) {
                fout.write(String.format("%f;%f\n", p.getX(), p.getY()).getBytes());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeFile(@NonNull FilesListData data) {
        File file = associatedFile(data);
        if (file.exists()) {
            file.delete();
            if (file.getParentFile().listFiles() == null) {
                deleteDirectory(file.getParentFile());
            }
        }
    }

    @NonNull
    public FilesListData[] loadFiles() {
        return loadFiles(null);
    }

    @NonNull
    public FilesListData[] loadFiles(File path) {
        path = path == null ? rootDir : path;
        List<FilesListData> filesListData = new ArrayList<>();
        for (File file : listFiles(path)) {
            FilesListData data = readFile(file);
            if (data != null) {
                filesListData.add(data);
            }
        }
        FilesListData[] arr = new FilesListData[filesListData.size()];
        for (int i = 0; i < filesListData.size(); i++) {
            arr[i] = filesListData.get(i);
        }
        return arr;
    }

    public Collection<File> listFiles(File dir) {
        Set<File> fileTree = new HashSet<File>();
        if (dir == null || dir.listFiles() == null) {
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(listFiles(entry));
        }
        return fileTree;
    }

    @Nullable
    private FilesListData readFile(File file) {
        String blink = file.getParentFile().getName();
        String operator = file.getParentFile().getParentFile().getName();
        String base = file.getParentFile().getParentFile().getParentFile().getName();
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                points.add(
                        new Point(
                                Double.parseDouble(line.split(";")[0]),
                                Double.parseDouble(line.split(";")[1])
                        )
                );
            }
            return new FilesListData(
                    file.getName(),
                    blink,
                    operator,
                    base,
                    points
            );
        } catch (IOException | NumberFormatException e) {
//            e.printStackTrace();
        }
        return null;
    }

    public File associatedFile(FilesListData filesListData) {
        return new File(
                new Uri.Builder()
                        .appendPath(rootDir.getPath())
                        .appendPath(filesListData.getBase())
                        .appendPath(filesListData.getOperator())
                        .appendPath(filesListData.getBlink())
                        .appendPath(filesListData.getName())
                        .build()
                        .getPath()
        );
    }

    public File zipFiles(String path) {
        File f;
        if (path.startsWith(root)) {
            f = new File(path);
        } else {
            f = new File(root, path);
        }
        return zipFiles(f);
    }

    public File zipFiles(File file) {
        return zipFiles(Arrays.asList(loadFiles(file)));
    }

    public File zipFiles(List<FilesListData> filesListData) {
        if (filesListData.isEmpty()) {
            return null;
        }
        try {
            String operator = filesListData.get(0).getOperator();
            String blinks = filesListData.get(0).getBase();
            File zipFolder = new File(tempDir, "zipFolder");
            zipFolder.mkdirs();
            File zipFile = new File(tempDir, "zipfile");
            zipFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (FilesListData data : filesListData) {
                File f = associatedFile(data);
                if (f.exists()) {
                    if (!operator.equals(data.getOperator())) {
                        operator = null;
                    }
                    if (!blinks.equals(data.getBase())) {
                        blinks = null;
                    }
                    String path = f.getPath().replace(rootDir.getPath(), "");

                    FileInputStream fis = new FileInputStream(f);
                    ZipEntry zipEntry = new ZipEntry(path);
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    fis.close();
                }
            }
            zipOut.close();
            fos.close();
            String zipname = Calendar.getInstance().getTime().toString() + ".zip";
            if (blinks != null) {
                zipname = blinks + "_" + zipname;
            }
            if (operator != null) {
                zipname = operator + "_" + zipname;
            }
            File userZipFile = new File(rootDir.getParent(), zipname);
            copyFile(zipFile, userZipFile);
            zipFile.delete();
            deleteDirectory(tempDir);
            return userZipFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        if (!src.exists())
            return;
        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);

        byte[] buffer = new byte[1024];

        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }

    void deleteDirectory(String path) {
        File f;
        if (path.startsWith(root)) {
            f = new File(path);
        } else {
            f = new File(root, path);
        }
        if (f.exists()) {
            deleteDirectory(f);
        }
    }

    void deleteDirectory(File dir) {
        if (dir == null)
            return;
        if (dir.listFiles() == null) {
            dir.delete();
            return;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                entry.delete();
            } else {
                deleteDirectory(entry);
            }
        }
        dir.delete();
    }

    String generateFileName(String opertator, String directory) {
        File dir = new File(
                new Uri.Builder()
                        .appendPath(rootDir.getPath())
                        .appendPath(opertator)
                        .appendPath(directory)
                        .build()
                        .getPath()
        );
        File[] files;
        if (!dir.exists() || (files = dir.listFiles()) == null) {
            return new File(dir, "data1.txt").getPath();
        }
        int offset = 1;
        File file = new File(dir, "data" + (files.length + offset) + ".txt");
        while (file.exists()) {
            file = new File(dir, "data" + (files.length + ++offset) + ".txt");
        }
        return file.getName();
    }

    void normalizeBlinks(String opertator, String directory) {
        File dir = new File(
                new Uri.Builder()
                        .appendPath(rootDir.getPath())
                        .appendPath(opertator)
                        .appendPath(directory)
                        .build()
                        .getPath()
        );
        normalizeBlinks(dir);
    }

    void normalizeBase(String path) {
        File f;
        if (path.startsWith(root)) {
            f = new File(path);
        } else {
            f = new File(root, path);
        }
        File[] operators = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (operators != null) {
            for (File op : operators) {
                File[] blinks = op.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });
                if (blinks != null) {
                    for (File blink : blinks) {
                        normalizeBlinks(blink);
                    }
                }
            }
        }

    }

    private void normalizeBlinks(File dir) {
        File[] files;
        if (!dir.exists() || (files = dir.listFiles()) == null) {
            return;
        }
        ArrayList<Integer> numbers = new ArrayList<>();
        for (File f : files) {
            try {
                int num = Integer.parseInt(f.getName().replace("data", "").replace(".txt", ""));
                numbers.add(num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(numbers);
        int currentNumber = 0;
        for (Integer number : numbers) {
            File f = new File(dir, "data" + number + ".txt");
            if (f.exists()) {
                currentNumber++;
                if (number != currentNumber) {
                    f.renameTo(new File(dir, "data" + currentNumber + ".txt"));
                }
            }
        }

    }

}
