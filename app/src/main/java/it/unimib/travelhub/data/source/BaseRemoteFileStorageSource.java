package it.unimib.travelhub.data.source;

import android.net.Uri;

import java.io.File;

public abstract class BaseRemoteFileStorageSource {
    public abstract void upload(String remotePath, Uri imageUri, RemoteFileStorageSource.uploadCallback uploadCallback);
    public abstract void download(String downloadUrl, File file, RemoteFileStorageSource.downloadCallback downloadCallback);
}
