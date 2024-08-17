package org.eflerrr.client.model.uploadbuffer;

import com.vaadin.flow.component.upload.MultiFileReceiver;
import com.vaadin.flow.component.upload.receivers.FileData;

import java.io.InputStream;

public interface UploadBuffer extends MultiFileReceiver {

    InputStream getInputStream(String fileName);

    FileData getFileData(String fileName);

}
