package com.github.makotok.github.exporter;

import java.io.IOException;

import org.kohsuke.github.GitHub;

/**
 * GitHubの情報を出力するインタフェースです。
 *
 * @author makot
 */
public interface GitHubExporter {

    void export(GitHub github, ExportOptions options) throws IOException;
}
