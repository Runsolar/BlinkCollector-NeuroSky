package com.example.blinkcollector_neurosky.repository;

import androidx.annotation.NonNull;
import com.example.blinkcollector_neurosky.data.FilesListData;
import javax.inject.Inject;
import dagger.Reusable;

@Reusable
public class DeviseStorage {

    @Inject
    DeviseStorage() {

    }

    public void saveFile(@NonNull FilesListData data) {
        //todo @Cynnabarflower
    }

    public void removeFile(@NonNull FilesListData data) {
        //todo @Cynnabarflower
    }

    @NonNull
    public FilesListData[] loadFiles() {
        //todo @Cynnabarflower
        return new FilesListData[] {};
    }
}
