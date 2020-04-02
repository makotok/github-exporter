package com.github.makotok.github.exporter;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import lombok.Data;

@Data
public class ExportOptions {

    public static enum OutputFormat {
        /** csv形式 */
        CSV,

        /** xlsx形式 */
        XLSX;
    }

    public static class OutputFormatOptionHandler extends OptionHandler<OutputFormat> {

        public OutputFormatOptionHandler(CmdLineParser parser, OptionDef option,
                Setter<? super OutputFormat> setter) {
            super(parser, option, setter);
        }

        @Override
        public int parseArguments(Parameters params) throws CmdLineException {
            setter.addValue(OutputFormat.valueOf(params.getParameter(0)));
            return 1;
        }

        @Override
        public String getDefaultMetaVariable() {
            return Messages.DEFAULT_META_STRING_OPTION_HANDLER.format();
        }
    }

    /** リポジトリ名 */
    @Option(name = "-repo", usage = "GitHub Repository (owner/repo)", required = true)
    private String repositoryPath;

    /** アクセストークン */
    @Option(name = "-oauth", usage = "GitHub Access Token", required = true)
    private String oauthToken;

    /** 出力ディレクトリ */
    @Option(name = "-output.dir", usage = "Output Directory")
    private Path outputDirectory = Paths.get("./output");

    /** 出力形式 */
    @Option(name = "-output.format", usage = "Output Format", handler = OutputFormatOptionHandler.class)
    private OutputFormat outputFormat = OutputFormat.XLSX;

    private String issueState;

    public ExportOptions() {
    }

    /**
     * 拡張子を除く出力ファイル名を返します。
     *
     * @return 拡張子を除く出力ファイル名
     */
    public String getOutputBaseName() {
        return repositoryPath.replaceAll("/", "_");
    }

}
