package com.github.makotok.github.exporter.impl;

import static java.time.format.DateTimeFormatter.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.github.makotok.github.exporter.ExportOptions;
import com.github.makotok.github.exporter.GitHubExporter;

public class CsvFileGitHubExporter implements GitHubExporter {

    public CsvFileGitHubExporter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(GitHub github, ExportOptions options) throws IOException {
        // GitHub リポジトリを取得
        var githubRepo = github.getRepository(options.getRepositoryPath());

        // 出力先のファイルパスを生成
        var outputPath = getOutputPath(options);

        // ファイルを出力
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(outputPath));
                PrintWriter writer = new PrintWriter(os)) {
            exportWorkbook(githubRepo, options, writer);
        }
    }

    /**
     * ワークブックを出力します。
     *
     * @param githubRepo
     * @param options
     * @param os
     * @throws IOException
     */
    protected void exportWorkbook(GHRepository githubRepo, ExportOptions options, PrintWriter writer)
            throws IOException {
        // csvを生成
        // Issuesを出力
        writeIssuesCsv(writer, githubRepo, options);
    }

    /**
     * 出力先のファイルパスを取得します。
     *
     * @param options 出力オプション
     * @return 出力先のファイルパス
     */
    protected Path getOutputPath(ExportOptions options) {
        // 出力先のフォルダを作成
        options.getOutputDirectory().toFile().mkdirs();

        // 出力先のファイルパスを生成
        var filename = options.getOutputBaseName() + "_" + LocalDate.now().format(ISO_LOCAL_DATE) + ".csv";
        return Paths.get(options.getOutputDirectory().toString(), filename);
    }

    /**
     * Issuesをシートに出力します。
     *
     * @param workbook ワークブック
     * @param githubRepo GitHubリポジトリ
     * @param options 出力オプション
     * @throws IOException
     */
    protected void writeIssuesCsv(PrintWriter writer, GHRepository githubRepo, ExportOptions options)
            throws IOException {

        // issues取得
        var issues = githubRepo.getIssues(GHIssueState.ALL);

        // ヘッダ出力
        writer.println("\"Number\",\"Title\",\"State\",\"Body\",\"Comments\",\"URL\"");

        // データ出力
        for (var issue : issues) {
            writer.append(escapeColumn(String.valueOf(issue.getNumber()))).append(",");
            writer.append(escapeColumn(issue.getTitle())).append(",");
            writer.append(escapeColumn(issue.getState().name())).append(",");
            writer.append(escapeColumn(issue.getBody())).append(",");
            writer.append(escapeColumn(formatComments(issue.getComments()))).append(",");
            writer.append(escapeColumn(issue.getHtmlUrl().toString()));
            writer.println();
        }
    }

    /**
     * コメントをフォーマットした文字列を返します。
     *
     * @param comments
     * @return
     * @throws IOException
     */
    protected String formatComments(List<GHIssueComment> comments) throws IOException {
        var buf = new StringBuilder();
        for (var comment : comments) {
            // ユーザ名
            buf.append("[").append(comment.getUser().getName()).append("]");
            // 作成日
            buf.append("[").append(comment.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("]").append(System.lineSeparator());
            // コメント
            buf.append(comment.getBody()).append(System.lineSeparator());
        }
        return buf.toString();
    }

    protected String escapeColumn(String text) {
        text = text != null ? text : "";
        return "\"" + text.replaceAll("\"", "\"\"") + "\"";
    }

}
