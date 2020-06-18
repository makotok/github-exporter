/**
 *
 */
package com.github.makotok.github.exporter;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.github.GitHubBuilder;

import com.github.makotok.github.exporter.ExportOptions.OutputFormat;
import com.github.makotok.github.exporter.impl.CsvFileGitHubExporter;
import com.github.makotok.github.exporter.impl.ExcelFileGitHubExporter;

/**
 * @author makot
 *
 */
public class Main {

    private Main() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new Main().execute(args);
    }

    /**
     * コマンドライン引数を指定してメインクラスを実行します。
     *
     * @param args コマンドライン引数
     */
    protected void execute(String[] args) {
        try {
            // コマンドライン引数を解析
            var options = new ExportOptions();
            var parser = new CmdLineParser(options);
            parser.parseArgument(args);

            // GitHubと認証
            var github = new GitHubBuilder()
                    .withOAuthToken(options.getOauthToken())
                    .build();

            // GitHubの情報を出力
            var exporter = createExporter(options);
            exporter.export(github, options);
        } catch (CmdLineException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private GitHubExporter createExporter(ExportOptions options) {
        if (options.getOutputFormat() == OutputFormat.XLSX) {
            return new ExcelFileGitHubExporter();
        }
        return new CsvFileGitHubExporter();
    }

}
