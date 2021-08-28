package com.hyf.jraft.counter.server;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author baB_hyf
 * @date 2021/08/07
 */
public class CounterSnapshotFile {

    private static final Logger log = LoggerFactory.getLogger(CounterSnapshotFile.class);

    private String path;

    public CounterSnapshotFile(String path) {
        this.path = path;
    }

    public boolean save(long count) {
        try {
            FileUtils.writeStringToFile(new File(path), String.valueOf(count));
            return true;
        } catch (IOException e) {
            log.error("Fail to save snapshot", e);
            return false;
        }
    }

    public long load() throws IOException {
        String content = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(content)) {
            return Long.parseLong(content);
        }

        throw new IOException("Fail to load snapshot from path " + path);
    }

    public String getPath() {
        return path;
    }
}
