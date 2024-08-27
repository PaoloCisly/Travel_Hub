package it.unimib.travelhub.data.source;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class RemoteFileStorageSource extends BaseRemoteFileStorageSource {
    private final FirebaseStorage storage;

    public RemoteFileStorageSource() {
        this.storage = FirebaseStorage.getInstance();
    }

    public interface uploadCallback {
        void onSuccessUpload(String url);
        void onFailure(Exception e);
    }
    @Override
    public void upload(String remotePath, Uri imageUri, RemoteFileStorageSource.uploadCallback uploadCallback) {
        StorageReference storageReference = storage.getReference().child(remotePath);
        UploadTask uploadTask = storageReference.putFile(imageUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                uploadCallback.onFailure(task.getException());
            }

            // Continue with the task to get the download URL
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                uploadCallback.onSuccessUpload(task.getResult().toString());
            } else {
                uploadCallback.onFailure(task.getException());
            }
        });
    }

    public interface downloadCallback {
        void onSuccessDownload(String url);
        void onFailure(Exception e);
    }
    @Override
    public void download(String downloadUrl, File file, RemoteFileStorageSource.downloadCallback downloadCallback) {
        StorageReference storageReference = storage.getReferenceFromUrl(downloadUrl);
        storageReference.getFile(file).addOnSuccessListener(taskSnapshot -> downloadCallback
                .onSuccessDownload(file.getPath()))
                .addOnFailureListener(downloadCallback::onFailure);
    }
}
