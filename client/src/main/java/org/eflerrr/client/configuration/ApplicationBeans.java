package org.eflerrr.client.configuration;

import lombok.RequiredArgsConstructor;
import org.eflerrr.client.model.uploadbuffer.UploadBuffer;
import org.eflerrr.client.model.uploadbuffer.impl.FileUploadBuffer;
import org.eflerrr.client.model.uploadbuffer.impl.InMemoryUploadBuffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ApplicationBeans {

    private final ApplicationConfig config;

    @Bean(name = "configured-upload-buffer")
    public UploadBuffer configuredUploadBuffer() {
        return config.fileUpload().inMemory()
                ? new InMemoryUploadBuffer()
                : new FileUploadBuffer();
    }

}
