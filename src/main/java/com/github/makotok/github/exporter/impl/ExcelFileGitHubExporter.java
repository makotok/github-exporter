package com.github.makotok.github.exporter.impl;

import static java.time.format.DateTimeFormatter.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.github.makotok.github.exporter.ExportOptions;
import com.github.makotok.github.exporter.GitHubExporter;

public class ExcelFileGitHubExporter implements GitHubExporter {

    public ExcelFileGitHubExporter() {
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

        // ワークブックを生成
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(outputPath))) {
            exportWorkbook(githubRepo, options, os);
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
    protected void exportWorkbook(GHRepository githubRepo, ExportOptions options, OutputStream os) throws IOException {
        // ワークブックを生成
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {

            workbook.setCompressTempFiles(true);

            // Issuesを出力
            writeIssuesSheet(workbook, githubRepo, options);

            // ワークブックを保存
            workbook.write(os);
        }
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
        var filename = options.getOutputBaseName() + "_" + LocalDate.now().format(ISO_LOCAL_DATE) + ".xlsx";
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
    protected void writeIssuesSheet(Workbook workbook, GHRepository githubRepo, ExportOptions options)
            throws IOException {

        // issues取得
        var issues = githubRepo.getIssues(GHIssueState.ALL);

        // シート生成
        var sheet = (SXSSFSheet) workbook.createSheet("Issues");
        sheet.setRandomAccessWindowSize(100);

        // ヘッダ出力
        var headerCol = 0;
        var rowIndex = 0;
        var headerRow = sheet.createRow(rowIndex++);
        headerRow.createCell(headerCol++).setCellValue("Number");
        headerRow.createCell(headerCol++).setCellValue("Title");
        headerRow.createCell(headerCol++).setCellValue("State");
        headerRow.createCell(headerCol++).setCellValue("Body");
        headerRow.createCell(headerCol++).setCellValue("Comments");
        headerRow.createCell(headerCol++).setCellValue("URL");

        // データ出力
        for (var issue : issues) {
            var row = sheet.createRow(rowIndex++);
            var col = 0;
            row.createCell(col++).setCellValue(issue.getNumber());
            row.createCell(col++).setCellValue(issue.getTitle());
            row.createCell(col++).setCellValue(issue.getState().name());
            row.createCell(col++).setCellValue(getTextWithinMaxLength(issue.getBody()));
            row.createCell(col++).setCellValue(formatComments(issue.getComments()));
            row.createCell(col++).setCellValue(issue.getHtmlUrl().toString());
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
        return getTextWithinMaxLength(buf.toString());
    }

    protected String getTextWithinMaxLength(String text) {
        if (text != null && text.length() > SpreadsheetVersion.EXCEL2007.getMaxTextLength()) {
            return text.substring(0, SpreadsheetVersion.EXCEL2007.getMaxTextLength());
        }
        return text;
    }

}
